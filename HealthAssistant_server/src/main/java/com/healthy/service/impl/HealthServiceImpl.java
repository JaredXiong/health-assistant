package com.healthy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.healthy.context.BaseContext;
import com.healthy.dto.HealthDataDTO;
import com.healthy.dto.ManualSyncDTO;
import com.healthy.entity.HealthData;
import com.healthy.entity.HealthReport;
import com.healthy.mapper.HealthDataMapper;
import com.healthy.mapper.HealthReportMapper;
import com.healthy.result.PageResult;
import com.healthy.service.AIGenerationService;   // 新增导入
import com.healthy.service.HealthService;
import com.healthy.vo.HealthDataVO;
import com.healthy.vo.HealthReportVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class HealthServiceImpl implements HealthService {

    private final HealthDataMapper healthDataMapper;
    private final HealthReportMapper healthReportMapper;
    private final AIGenerationService aiGenerationService;   // 注入 AI 服务

    // 健康指标参考范围（用于降级方案）
    private static final int NORMAL_HEART_RATE_MIN = 60;
    private static final int NORMAL_HEART_RATE_MAX = 100;

    private static final int NORMAL_SYSTOLIC_MIN = 90;
    private static final int NORMAL_SYSTOLIC_MAX = 120;
    private static final int NORMAL_DIASTOLIC_MIN = 60;
    private static final int NORMAL_DIASTOLIC_MAX = 80;

    private static final int NORMAL_BLOOD_OXYGEN_MIN = 95;
    private static final int NORMAL_BLOOD_OXYGEN_MAX = 100;

    private static final BigDecimal NORMAL_BLOOD_SUGAR_MIN = new BigDecimal("3.9");
    private static final BigDecimal NORMAL_BLOOD_SUGAR_MAX = new BigDecimal("6.1");

    @Override
    @Transactional
    public HealthReportVO manualSyncHealthData(ManualSyncDTO dto) {
        log.info("手动同步健康数据: {}", dto);

        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        // 不再需要解析血压字符串，直接获取拆分后的字段
        Integer systolicBp = dto.getSystolicBp();
        Integer diastolicBp = dto.getDiastolicBp();

        // 创建健康数据DTO（用于分析报告）
        HealthDataDTO healthDataDTO = new HealthDataDTO();
        healthDataDTO.setHeartRate(dto.getHeartRate());
        healthDataDTO.setSystolicBp(systolicBp);
        healthDataDTO.setDiastolicBp(diastolicBp);
        healthDataDTO.setBloodOxygen(dto.getBloodOxygen());
        healthDataDTO.setBloodSugar(dto.getBloodSugar());
        healthDataDTO.setBodyTemperature(dto.getBodyTemperature());
        healthDataDTO.setRespiratoryRate(dto.getRespiratoryRate());
        healthDataDTO.setMeasurementTime(LocalDateTime.now());

        // 分析并生成报告
        HealthReportVO report = analyzeAndGenerateReport(healthDataDTO);

        // 保存健康数据到数据库
        HealthData healthData = HealthData.builder()
                .userId(userId)
                .heartRate(dto.getHeartRate())
                .systolicBp(systolicBp)
                .diastolicBp(diastolicBp)
                .bloodOxygen(dto.getBloodOxygen())
                .bloodSugar(dto.getBloodSugar())
                .bodyTemperature(dto.getBodyTemperature())
                .respiratoryRate(dto.getRespiratoryRate())
                .measurementTime(LocalDateTime.now())
                .build();

        healthDataMapper.insert(healthData);

        // 保存健康报告到数据库
        HealthReport healthReport = HealthReport.builder()
                .userId(userId)
                .healthDataId(healthData.getId())
                .overallScore(report.getOverallScore())
                .healthLevel(report.getHealthLevel())
                .bloodPressureEval(report.getBloodPressureEval())
                .bloodSugarEval(report.getBloodSugarEval())
                .heartRateEval(report.getHeartRateEval())
                .bloodOxygenEval(report.getBloodOxygenEval())
                .bodyTemperatureEval(report.getBodyTemperatureEval())
                .respiratoryRateEval(report.getRespiratoryRateEval())
                .riskFactors(report.getRiskFactors())
                .recommendations(report.getRecommendations())
                .generateTime(LocalDateTime.now())
                .build();

        healthReportMapper.insert(healthReport);

        report.setId(healthReport.getId());
        return report;
    }

    @Override
    public Long uploadHealthData(HealthDataDTO dto) {

        Long userId = BaseContext.getCurrentId();

        // 直接使用 DTO 中的收缩压/舒张压，不再解析
        HealthData healthData = HealthData.builder()
                .userId(userId)
                .heartRate(dto.getHeartRate())
                .systolicBp(dto.getSystolicBp())   // 直接使用
                .diastolicBp(dto.getDiastolicBp()) // 直接使用
                .bloodOxygen(dto.getBloodOxygen())
                .bloodSugar(dto.getBloodSugar())
                .bodyTemperature(dto.getBodyTemperature())
                .respiratoryRate(dto.getRespiratoryRate())
                .measurementTime(dto.getMeasurementTime() != null ?
                        dto.getMeasurementTime() : LocalDateTime.now())
                .build();

        healthDataMapper.insert(healthData);
        return healthData.getId();
    }

    @Override
    public HealthReportVO getLatestHealthReport() {
        Long userId = BaseContext.getCurrentId();
        HealthReport healthReport = healthReportMapper.selectLatestByUserId(userId);

        if (healthReport == null) {
            return null;
        }

        return convertToVO(healthReport);
    }

    @Override
    public HealthReportVO analyzeAndGenerateReport(HealthDataDTO dto) {
        // 1. 先尝试 AI 生成
        HealthReportVO aiReport = aiGenerationService.generateReport(dto);
        if (aiReport != null) {
            // 混合策略：评分和健康等级仍用原有规则（可保持一致性）
            HealthReportVO ruleReport = ruleBasedAnalyze(dto);
            aiReport.setOverallScore(ruleReport.getOverallScore());
            aiReport.setHealthLevel(ruleReport.getHealthLevel());

            // 保留 AI 生成的各单项评价（可选，也可保留原有）
            // 如果希望单项评价也由 AI 提供，可注释掉以下赋值
            aiReport.setHeartRateEval(ruleReport.getHeartRateEval());
            aiReport.setBloodPressureEval(ruleReport.getBloodPressureEval());
            aiReport.setBloodSugarEval(ruleReport.getBloodSugarEval());
            aiReport.setBloodOxygenEval(ruleReport.getBloodOxygenEval());
            aiReport.setBodyTemperatureEval(ruleReport.getBodyTemperatureEval());
            aiReport.setRespiratoryRateEval(ruleReport.getRespiratoryRateEval());

            // 设置生成时间
            aiReport.setGenerateTime(LocalDateTime.now());
            return aiReport;
        }

        // 2. AI 不可用，降级到原有规则
        return ruleBasedAnalyze(dto);
    }

    /**
     * 原有的基于规则的分析方法，抽离出来供降级或混合使用
     */
    private HealthReportVO ruleBasedAnalyze(HealthDataDTO dto) {
        int totalScore = 100;
        List<String> riskFactors = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();

        StringBuilder heartRateEval = new StringBuilder();
        StringBuilder bloodPressureEval = new StringBuilder();
        StringBuilder bloodSugarEval = new StringBuilder();
        StringBuilder bloodOxygenEval = new StringBuilder();
        StringBuilder bodyTemperatureEval = new StringBuilder();
        StringBuilder respiratoryRateEval = new StringBuilder();

        // 分析心率
        if (dto.getHeartRate() != null) {
            if (dto.getHeartRate() < NORMAL_HEART_RATE_MIN) {
                heartRateEval.append("心率偏低(").append(dto.getHeartRate()).append("次/分)");
                riskFactors.add("心动过缓");
                recommendations.add("适当增加有氧运动，避免突然站立");
                totalScore -= 15;
            } else if (dto.getHeartRate() > NORMAL_HEART_RATE_MAX) {
                heartRateEval.append("心率偏高(").append(dto.getHeartRate()).append("次/分)");
                riskFactors.add("心动过速");
                recommendations.add("注意休息，避免咖啡因和刺激性饮料");
                totalScore -= 15;
            } else {
                heartRateEval.append("心率正常(").append(dto.getHeartRate()).append("次/分)");
            }
        }

        // 分析血压
        if (dto.getSystolicBp() != null && dto.getDiastolicBp() != null) {
            int systolic = dto.getSystolicBp();
            int diastolic = dto.getDiastolicBp();

            if (systolic < NORMAL_SYSTOLIC_MIN || diastolic < NORMAL_DIASTOLIC_MIN) {
                bloodPressureEval.append("血压偏低(").append(systolic).append("/").append(diastolic).append("mmHg)");
                riskFactors.add("低血压");
                recommendations.add("增加盐分摄入，保持充足水分");
                totalScore -= 20;
            } else if (systolic > NORMAL_SYSTOLIC_MAX || diastolic > NORMAL_DIASTOLIC_MAX) {
                bloodPressureEval.append("血压偏高(").append(systolic).append("/").append(diastolic).append("mmHg)");
                riskFactors.add("高血压风险");
                recommendations.add("低盐饮食，定期监测血压");
                totalScore -= 20;
            } else {
                bloodPressureEval.append("血压正常(").append(systolic).append("/").append(diastolic).append("mmHg)");
            }
        } else {
            bloodPressureEval.append("血压数据缺失，无法评价");
        }

        // 分析血氧
        if (dto.getBloodOxygen() != null) {
            if (dto.getBloodOxygen() < NORMAL_BLOOD_OXYGEN_MIN) {
                bloodOxygenEval.append("血氧偏低(").append(dto.getBloodOxygen()).append("%)");
                riskFactors.add("低血氧症");
                recommendations.add("保持室内通风，适当进行深呼吸锻炼");
                totalScore -= 15;
            } else {
                bloodOxygenEval.append("血氧正常(").append(dto.getBloodOxygen()).append("%)");
            }
        }

        // 分析血糖
        if (dto.getBloodSugar() != null) {
            if (dto.getBloodSugar().compareTo(NORMAL_BLOOD_SUGAR_MIN) < 0) {
                bloodSugarEval.append("血糖偏低(").append(dto.getBloodSugar()).append("mmol/L)");
                riskFactors.add("低血糖");
                recommendations.add("规律饮食，避免长时间空腹");
                totalScore -= 15;
            } else if (dto.getBloodSugar().compareTo(NORMAL_BLOOD_SUGAR_MAX) > 0) {
                bloodSugarEval.append("血糖偏高(").append(dto.getBloodSugar()).append("mmol/L)");
                riskFactors.add("高血糖风险");
                recommendations.add("控制碳水化合物摄入，增加膳食纤维");
                totalScore -= 15;
            } else {
                bloodSugarEval.append("血糖正常(").append(dto.getBloodSugar()).append("mmol/L)");
            }
        }

        // 分析体温
        if (dto.getBodyTemperature() != null) {
            BigDecimal temp = dto.getBodyTemperature();
            BigDecimal normalTempMin = new BigDecimal("36.0");
            BigDecimal normalTempMax = new BigDecimal("37.2");

            if (temp.compareTo(normalTempMin) < 0) {
                bodyTemperatureEval.append("体温偏低(").append(temp).append("℃)");
                riskFactors.add("体温过低");
                recommendations.add("注意保暖，适当增加衣物");
                totalScore -= 10;
            } else if (temp.compareTo(normalTempMax) > 0) {
                bodyTemperatureEval.append("体温偏高(").append(temp).append("℃)");
                riskFactors.add("发热");
                recommendations.add("多饮水，监测体温变化，必要时就医");
                totalScore -= 15;
            } else {
                bodyTemperatureEval.append("体温正常(").append(temp).append("℃)");
            }
        }

        // 分析呼吸频率
        if (dto.getRespiratoryRate() != null) {
            int rate = dto.getRespiratoryRate();
            int normalRateMin = 12;
            int normalRateMax = 20;

            if (rate < normalRateMin) {
                respiratoryRateEval.append("呼吸频率偏低(").append(rate).append("次/分)");
                riskFactors.add("呼吸过缓");
                recommendations.add("适当活动，增强心肺功能");
                totalScore -= 10;
            } else if (rate > normalRateMax) {
                respiratoryRateEval.append("呼吸频率偏高(").append(rate).append("次/分)");
                riskFactors.add("呼吸急促");
                recommendations.add("保持平静，若持续偏高建议就医检查");
                totalScore -= 10;
            } else {
                respiratoryRateEval.append("呼吸频率正常(").append(rate).append("次/分)");
            }
        }

        // 确定健康等级
        String healthLevel;
        if (totalScore >= 90) {
            healthLevel = "优秀";
        } else if (totalScore >= 75) {
            healthLevel = "良好";
        } else if (totalScore >= 60) {
            healthLevel = "一般";
        } else {
            healthLevel = "较差";
        }

        // 默认健康建议（无风险时）
        if (riskFactors.isEmpty()) {
            recommendations.add("继续保持良好的生活习惯");
            recommendations.add("定期进行健康检查");
        }

        // 构建返回值
        return HealthReportVO.builder()
                .overallScore(totalScore)
                .healthLevel(healthLevel)
                .heartRateEval(heartRateEval.toString())
                .bloodPressureEval(bloodPressureEval.toString())
                .bloodSugarEval(bloodSugarEval.toString())
                .bloodOxygenEval(bloodOxygenEval.toString())
                .bodyTemperatureEval(!bodyTemperatureEval.isEmpty() ? bodyTemperatureEval.toString() : null)
                .respiratoryRateEval(!respiratoryRateEval.isEmpty() ? respiratoryRateEval.toString() : null)
                .riskFactors(riskFactors.isEmpty() ? null : String.join("; ", riskFactors))
                .recommendations(recommendations.isEmpty() ? null : String.join("; ", recommendations))
                .generateTime(LocalDateTime.now())
                .build();
    }

    private HealthReportVO convertToVO(HealthReport healthReport) {
        HealthReportVO vo = new HealthReportVO();
        BeanUtils.copyProperties(healthReport, vo);
        return vo;
    }

    @Override
    public PageResult queryHealthDataHistory(Integer page, Integer pageSize,
                                             LocalDateTime startTime, LocalDateTime endTime,
                                             Boolean excludeLatest) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        Long excludeId = null;
        if (excludeLatest != null && excludeLatest) {
            HealthData latest = healthDataMapper.selectLatestByUserId(userId);
            if (latest != null) {
                excludeId = latest.getId();
            }
        }

        int offset = (page == null || page < 1) ? 0 : (page - 1) * pageSize;
        int limit = (pageSize == null || pageSize < 1) ? 10 : pageSize;

        List<HealthData> list = healthDataMapper.selectByUserIdWithPage(
                userId, startTime, endTime, offset, limit, excludeId);

        Long total = healthDataMapper.countByUserId(userId, startTime, endTime, excludeId);

        List<HealthDataVO> voList = list.stream().map(this::convertToVO).collect(Collectors.toList());
        return new PageResult(total, voList);
    }

    private HealthDataVO convertToVO(HealthData entity) {
        HealthDataVO vo = new HealthDataVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public PageResult getHistoryReports(Long userId, Integer page, Integer pageSize) {
        Page<HealthReport> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<HealthReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthReport::getUserId, userId)
                .orderByDesc(HealthReport::getGenerateTime);
        IPage<HealthReport> reportPage = healthReportMapper.selectPage(pageParam, wrapper);
        List<HealthReportVO> voList = reportPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return new PageResult(reportPage.getTotal(), voList);
    }
}