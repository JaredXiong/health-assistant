package com.healthy.service.impl;

import com.alibaba.fastjson.JSON;
import com.healthy.entity.*;
import com.healthy.exception.BaseException;
import com.healthy.mapper.*;
import com.healthy.result.PageResult;
import com.healthy.service.FamilyHealthService;
import com.healthy.vo.EmergencyContactVO;
import com.healthy.vo.FamilyReportVO;
import com.healthy.vo.HealthHistoryVO;
import com.healthy.vo.HealthOverviewVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FamilyHealthServiceImpl implements FamilyHealthService {

    private final FamilyMemberMapper familyMemberMapper;
    private final HealthDataMapper healthDataMapper;
    private final DataAuthorizationMapper authMapper;
    private final UserMapper userMapper;
    private final FamilyMapper familyMapper;

    @Override
    public List<HealthOverviewVO> getHealthOverview(Long userId) {
        FamilyMember current = familyMemberMapper.selectActiveByUserId(userId);
        if (current == null) {
            throw new BaseException("用户未加入家庭");
        }
        List<FamilyMember> members = familyMemberMapper.selectActiveByFamilyId(current.getFamilyId());

        // 当前用户是孩子时，获取其被授权列表
        Map<Long, List<String>> authMap = new HashMap<>(); // key: grantorId, value: dataTypes
        if ("CHILD".equals(current.getRole())) {
            List<DataAuthorization> auths = authMapper.selectValidByGrantee(userId);
            for (DataAuthorization auth : auths) {
                List<String> types = JSON.parseArray(auth.getDataTypes(), String.class);
                authMap.put(auth.getGrantorId(), types);
            }
        }

        List<HealthOverviewVO> result = new ArrayList<>();
        for (FamilyMember member : members) {
            boolean canView = false;
            if ("PARENT".equals(current.getRole())) {
                canView = true; // 家长可看所有
            } else if (member.getUserId().equals(userId)) {
                canView = true; // 自己
            } else {
                // 孩子看家长：检查授权
                if (authMap.containsKey(member.getUserId())) {
                    canView = true; // 至少有一个授权项，具体字段在组装数据时过滤
                }
            }
            if (!canView) continue;

            Users user = userMapper.getById(member.getUserId());
            HealthData latest = healthDataMapper.selectLatestByUserId(member.getUserId());
            if (latest == null) continue; // 无健康数据则跳过

            HealthOverviewVO vo = new HealthOverviewVO();
            vo.setUserId(member.getUserId());
            vo.setNickname(user != null ? user.getNickname() : "未知");
            if (user != null) {
                vo.setAvatar(user.getAvatarUrl());  // 关键：设置头像
            } else {
                vo.setAvatar(null);
            }
            vo.setRelation(member.getRelation());

            HealthOverviewVO.LatestHealth latestHealth = new HealthOverviewVO.LatestHealth();
            // 根据权限填充字段
            List<String> allowedTypes = authMap.getOrDefault(member.getUserId(), Collections.emptyList());
            if ("PARENT".equals(current.getRole()) || member.getUserId().equals(userId)) {
                // 家长或自己，所有字段可见
                allowedTypes = Arrays.asList("heartRate", "bloodPressure", "bloodOxygen", "bloodSugar");
            }
            if (allowedTypes.contains("heartRate")) {
                latestHealth.setHeartRate(latest.getHeartRate());
            }
            if (allowedTypes.contains("bloodPressure") && latest.getSystolicBp() != null && latest.getDiastolicBp() != null) {
                latestHealth.setBloodPressure(latest.getSystolicBp() + "/" + latest.getDiastolicBp());
            }
            if (allowedTypes.contains("bloodOxygen")) {
                latestHealth.setBloodOxygen(latest.getBloodOxygen());
            }
            if (allowedTypes.contains("bloodSugar") && latest.getBloodSugar() != null) {
                latestHealth.setBloodSugar(latest.getBloodSugar().doubleValue());
            }
            latestHealth.setUpdateTime(latest.getMeasurementTime());
            vo.setLatest(latestHealth);
            result.add(vo);
        }
        return result;
    }

    @Override
    public PageResult getHealthHistory(Long userId, Long targetUserId, String type,
                                       LocalDate startDate, LocalDate endDate,
                                       Integer pageNum, Integer pageSize) {
        // 权限检查
        FamilyMember current = familyMemberMapper.selectActiveByUserId(userId);
        if (current == null) {
            throw new BaseException("用户未加入家庭");
        }
        FamilyMember target = familyMemberMapper.selectActiveByUserId(targetUserId);
        if (target == null || !target.getFamilyId().equals(current.getFamilyId())) {
            throw new BaseException("目标成员不在家庭中");
        }
        // 检查数据访问权限
        boolean canAccess = false;
        if ("PARENT".equals(current.getRole())) {
            canAccess = true; // 家长可看任何成员
        } else if (userId.equals(targetUserId)) {
            canAccess = true; // 自己
        } else {
            // 孩子看家长，需授权
            DataAuthorization auth = authMapper.selectByGrantorAndGrantee(targetUserId, userId);
            if (auth != null && (auth.getExpireTime() == null || auth.getExpireTime().isAfter(LocalDateTime.now()))) {
                List<String> types = JSON.parseArray(auth.getDataTypes(), String.class);
                if (types.contains(type)) {
                    canAccess = true;
                }
            }
        }
        if (!canAccess) {
            throw new BaseException("无权访问该成员的健康数据");
        }

        // 构建时间范围
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.plusDays(1).atStartOfDay().minusNanos(1) : null;

        int offset = (pageNum - 1) * pageSize;
        List<HealthData> dataList = healthDataMapper.selectByUserIdWithPage(targetUserId, startDateTime, endDateTime, offset, pageSize, null);
        long total = healthDataMapper.countByUserId(targetUserId, startDateTime, endDateTime, null);

        List<HealthHistoryVO> historyList = dataList.stream().map(hd -> {
            HealthHistoryVO vo = new HealthHistoryVO();
            vo.setTime(hd.getMeasurementTime());
            Object value = extractValueByType(hd, type);
            vo.setValue(value);
            return vo;
        }).collect(Collectors.toList());

        return new PageResult(total, historyList);
    }

    private Object extractValueByType(HealthData hd, String type) {
        return switch (type) {
            case "heartRate" -> hd.getHeartRate();
            case "bloodPressure" -> {
                if (hd.getSystolicBp() != null && hd.getDiastolicBp() != null) {
                    Map<String, Integer> bp = new HashMap<>();
                    bp.put("systolic", hd.getSystolicBp());
                    bp.put("diastolic", hd.getDiastolicBp());
                    yield bp;
                }
                yield null;
            }
            case "bloodOxygen" -> hd.getBloodOxygen();
            case "bloodSugar" -> hd.getBloodSugar();
            case "bodyTemperature" -> hd.getBodyTemperature();
            case "respiratoryRate" -> hd.getRespiratoryRate();
            default -> null;
        };
    }

    @Override
    public FamilyReportVO generateFamilyReport(Long userId, String period) {
        FamilyMember current = familyMemberMapper.selectActiveByUserId(userId);
        if (current == null) {
            throw new BaseException("用户未加入家庭");
        }
        if (!"PARENT".equals(current.getRole())) {
            throw new BaseException("只有家长可以生成家庭报告");
        }
        List<FamilyMember> members = familyMemberMapper.selectActiveByFamilyId(current.getFamilyId());

        // 确定日期范围
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate;
        if ("week".equalsIgnoreCase(period)) {
            startDate = endDate.minusWeeks(1);
        } else {
            startDate = endDate.minusMonths(1);
        }

        // 聚合数据：按天计算平均值
        Map<LocalDate, List<HealthData>> dailyData = new LinkedHashMap<>();
        for (FamilyMember member : members) {
            List<HealthData> memberData = healthDataMapper.selectByUserIdWithRange(member.getUserId(), startDate, endDate);
            for (HealthData data : memberData) {
                LocalDate date = data.getMeasurementTime().toLocalDate();
                dailyData.computeIfAbsent(date, k -> new ArrayList<>()).add(data);
            }
        }

        List<LocalDate> sortedDates = new ArrayList<>(dailyData.keySet());
        sortedDates.sort(Comparator.naturalOrder());

        // 计算趋势
        List<Integer> heartRates = new ArrayList<>();
        List<String> dateStrs = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");
        for (LocalDate date : sortedDates) {
            List<HealthData> dayData = dailyData.get(date);
            double avgHeartRate = dayData.stream()
                    .map(HealthData::getHeartRate)
                    .filter(Objects::nonNull)
                    .mapToInt(Integer::intValue)
                    .average().orElse(0);
            heartRates.add((int) Math.round(avgHeartRate));
            dateStrs.add(date.format(fmt));
        }

        // 计算整体平均值（过滤 null）
        double avgHeartRate = members.stream()
                .flatMap(m -> healthDataMapper.selectByUserIdWithRange(m.getUserId(), startDate, endDate).stream())
                .map(HealthData::getHeartRate)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average().orElse(0);

        double avgSystolic = members.stream()
                .flatMap(m -> healthDataMapper.selectByUserIdWithRange(m.getUserId(), startDate, endDate).stream())
                .map(HealthData::getSystolicBp)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average().orElse(0);

        double avgDiastolic = members.stream()
                .flatMap(m -> healthDataMapper.selectByUserIdWithRange(m.getUserId(), startDate, endDate).stream())
                .map(HealthData::getDiastolicBp)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average().orElse(0);

        double avgBloodOxygen = members.stream()
                .flatMap(m -> healthDataMapper.selectByUserIdWithRange(m.getUserId(), startDate, endDate).stream())
                .map(HealthData::getBloodOxygen)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average().orElse(0);

        double avgBloodSugar = members.stream()
                .flatMap(m -> healthDataMapper.selectByUserIdWithRange(m.getUserId(), startDate, endDate).stream())
                .map(HealthData::getBloodSugar)
                .filter(Objects::nonNull)
                .mapToDouble(BigDecimal::doubleValue)
                .average().orElse(0);

        // 生成建议（简单规则）
        List<String> insights = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();
        if (avgHeartRate > 80) {
            insights.add("家庭平均心率偏高");
            suggestions.add("建议家庭成员增加有氧运动");
        }
        if (avgSystolic > 120 || avgDiastolic > 80) {
            insights.add("家庭平均血压偏高");
            suggestions.add("注意低盐饮食，定期监测血压");
        }

        FamilyReportVO report = new FamilyReportVO();
        // 获取家庭名称
        Family family = familyMapper.selectById(current.getFamilyId());
        report.setFamilyName(family != null ? family.getName() : "未知家庭");
        report.setPeriod(startDate.toLocalDate() + " ~ " + endDate.toLocalDate());
        report.setGeneratedAt(LocalDateTime.now());

        FamilyReportVO.Overview overview = new FamilyReportVO.Overview();
        overview.setAvgHeartRate(avgHeartRate);
        overview.setAvgBloodPressure(Math.round(avgSystolic) + "/" + Math.round(avgDiastolic));
        overview.setAvgBloodOxygen(avgBloodOxygen);
        overview.setAvgBloodSugar(avgBloodSugar);
        report.setOverview(overview);

        FamilyReportVO.Trends trends = new FamilyReportVO.Trends();
        trends.setHeartRate(heartRates);
        trends.setDates(dateStrs);
        report.setTrends(trends);

        report.setInsights(insights);
        report.setSuggestions(suggestions);

        return report;
    }

    @Override
    public List<EmergencyContactVO> getEmergencyContacts(Long userId) {
        FamilyMember current = familyMemberMapper.selectActiveByUserId(userId);
        if (current == null) {
            throw new BaseException("用户未加入家庭");
        }
        // 查询所有家长
        List<FamilyMember> parents = familyMemberMapper.selectParentsByFamilyId(current.getFamilyId());
        List<EmergencyContactVO> contacts = new ArrayList<>();
        for (FamilyMember parent : parents) {
            Users user = userMapper.getById(parent.getUserId());
            if (user != null && user.getPhone() != null) {
                EmergencyContactVO vo = new EmergencyContactVO();
                vo.setUserId(user.getId());
                vo.setNickname(user.getNickname());
                // 脱敏处理
                String phone = user.getPhone();
                if (phone.length() == 11) {
                    phone = phone.substring(0, 3) + "****" + phone.substring(7);
                }
                vo.setPhone(phone);
                contacts.add(vo);
            }
        }
        return contacts;
    }
}