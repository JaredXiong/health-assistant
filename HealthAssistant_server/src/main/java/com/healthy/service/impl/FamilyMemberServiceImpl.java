package com.healthy.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.healthy.entity.DataAuthorization;
import com.healthy.entity.FamilyMember;
import com.healthy.entity.Users;
import com.healthy.exception.BaseException;
import com.healthy.mapper.*;
import com.healthy.service.FamilyMemberService;
import com.healthy.vo.FamilyMemberVO;
import com.healthy.vo.MemberDetailVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class FamilyMemberServiceImpl implements FamilyMemberService {

    private final FamilyMemberMapper familyMemberMapper;
    private final FamilyMapper familyMapper;
    private final UserMapper userMapper;
    private final HealthDataMapper healthDataMapper;
    private final DataAuthorizationMapper authMapper;

    @Override
    public List<FamilyMemberVO> getMembers(Long userId) {
        FamilyMember current = familyMemberMapper.selectActiveByUserId(userId);
        if (current == null) {
            throw new BaseException("用户未加入家庭");
        }
        List<FamilyMember> members = familyMemberMapper.selectActiveByFamilyId(current.getFamilyId());
        return members.stream().map(member -> {
            Users user = userMapper.getById(member.getUserId());
            FamilyMemberVO vo = new FamilyMemberVO();
            vo.setUserId(member.getUserId());
            vo.setNickname(user != null ? user.getNickname() : "未知");
            if (user != null) {
                vo.setAvatar(user.getAvatarUrl());  // 关键：设置头像
            } else {
                vo.setAvatar(null);
            }
            vo.setRole(member.getRole());
            vo.setRelation(member.getRelation());
            vo.setJoinTime(member.getJoinedAt());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public MemberDetailVO getMemberDetail(Long userId, Long targetUserId) {
        FamilyMember current = familyMemberMapper.selectActiveByUserId(userId);
        if (current == null) {
            throw new BaseException("用户未加入家庭");
        }
        FamilyMember target = familyMemberMapper.selectActiveByUserId(targetUserId);
        if (target == null || !target.getFamilyId().equals(current.getFamilyId())) {
            throw new BaseException("目标成员不存在或不在同一家庭");
        }
        // 权限检查：家长可查看任意成员；孩子只能查看自己或被授权的家长（但详情页可看基本信息，健康数据单独控制）
        if ("CHILD".equals(current.getRole()) && !userId.equals(targetUserId)) {
            // 孩子查看其他成员需授权，但这里仅返回基本信息，健康数据由health接口控制，故允许查看基本信息
            // 但为了安全，孩子不能查看其他孩子的详情？根据业务决定，此处允许查看基本信息
        }
        Users user = userMapper.getById(targetUserId);
        MemberDetailVO vo = new MemberDetailVO();
        vo.setUserId(targetUserId);
        vo.setNickname(user != null ? user.getNickname() : "未知");
        if (user != null) {
            vo.setAvatar(user.getAvatarUrl());  // 关键：设置头像
        } else {
            vo.setAvatar(null);
        }
        vo.setRole(target.getRole());
        vo.setRelation(target.getRelation());
        vo.setJoinTime(target.getJoinedAt());
        // 最新健康数据由 health overview 提供，此处不返回，或简单返回最近一条
        // 可以调用healthDataMapper.selectLatestByUserId
        var latestHealth = healthDataMapper.selectLatestByUserId(targetUserId);
        if (latestHealth != null) {
            // 查询当前用户对目标用户的授权
            DataAuthorization auth = null;
            if (!userId.equals(targetUserId)) {
                auth = authMapper.selectByGrantorAndGrantee(targetUserId, userId);
            }
            // 如果有授权，解析允许的数据类型
            List<String> allowedTypes = new ArrayList<>();
            if (auth != null && (auth.getExpireTime() == null || auth.getExpireTime().isAfter(LocalDateTime.now()))) {
                String dataTypesJson = auth.getDataTypes(); // 假设返回 String
                if (dataTypesJson != null && !dataTypesJson.isEmpty()) {
                    allowedTypes = JSON.parseArray(dataTypesJson, String.class);
                }
            }
            // 组装健康数据时，仅当允许或为自己时返回
                Map<String, Object> healthMap = new HashMap<>();
                if (userId.equals(targetUserId) || allowedTypes.contains("heartRate")) {
                    healthMap.put("heartRate", latestHealth.getHeartRate());
                }
                if (userId.equals(targetUserId) || allowedTypes.contains("bloodPressure")) {
                    healthMap.put("bloodPressure", latestHealth.getSystolicBp() + "/" + latestHealth.getDiastolicBp());
                }
                if (userId.equals(targetUserId) || allowedTypes.contains("bloodOxygen")) {
                    healthMap.put("bloodOxygen", latestHealth.getBloodOxygen());
                }
                if (userId.equals(targetUserId) || allowedTypes.contains("bloodSugar")) {
                    healthMap.put("bloodSugar", latestHealth.getBloodSugar());
                }
                if (userId.equals(targetUserId) || allowedTypes.contains("bodyTemperature")) {
                    healthMap.put("bodyTemperature", latestHealth.getBodyTemperature());
                }
                if (userId.equals(targetUserId) || allowedTypes.contains("respiratoryRate")) {
                    healthMap.put("respiratoryRate", latestHealth.getRespiratoryRate());
                }
                healthMap.put("measurementTime", latestHealth.getMeasurementTime());
                vo.setLatestHealth(healthMap);
        }
        return vo;
    }

    @Override
    public void updateRelation(Long userId, Long targetUserId, String relation) {
        FamilyMember current = familyMemberMapper.selectActiveByUserId(userId);
        if (current == null) {
            throw new BaseException("用户未加入家庭");
        }
        if (!"PARENT".equals(current.getRole())) {
            throw new BaseException("只有家长可以修改成员关系");
        }
        LambdaUpdateWrapper<FamilyMember> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(FamilyMember::getUserId, targetUserId)   // 根据用户ID更新，而不是根据记录ID
                .set(FamilyMember::getRelation, relation);
        familyMemberMapper.update(null, wrapper);
    }

    @Override
    public void updateRole(Long userId, Long targetUserId, String role) {
        FamilyMember current = familyMemberMapper.selectActiveByUserId(userId);
        if (current == null) {
            throw new BaseException("用户未加入家庭");
        }
        if (!"PARENT".equals(current.getRole())) {
            throw new BaseException("只有家长可以修改成员角色");
        }
        if (userId.equals(targetUserId)) {
            throw new BaseException("不能修改自己的角色");
        }
        FamilyMember target = familyMemberMapper.selectActiveByUserId(targetUserId);
        if (target == null || !target.getFamilyId().equals(current.getFamilyId())) {
            throw new BaseException("目标成员不存在或不在同一家庭");
        }
        // 检查是否为最后一个家长？如果要将最后一个家长改为孩子，需至少保留一个家长。简单处理：不限制。
        LambdaUpdateWrapper<FamilyMember> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(FamilyMember::getId, target.getId())
                .set(FamilyMember::getRole, role);
        familyMemberMapper.update(null, wrapper);
    }

    @Override
    public void removeMember(Long userId, Long targetUserId) {
        FamilyMember current = familyMemberMapper.selectActiveByUserId(userId);
        if (current == null) {
            throw new BaseException("用户未加入家庭");
        }
        if (!"PARENT".equals(current.getRole())) {
            throw new BaseException("只有家长可以移除成员");
        }
        if (userId.equals(targetUserId)) {
            throw new BaseException("不能移除自己，请使用解散家庭");
        }
        FamilyMember target = familyMemberMapper.selectActiveByUserId(targetUserId);
        if (target == null || !target.getFamilyId().equals(current.getFamilyId())) {
            throw new BaseException("目标成员不存在或不在同一家庭");
        }
        LambdaUpdateWrapper<FamilyMember> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(FamilyMember::getId, target.getId())   // 使用主键 ID 更新
                .set(FamilyMember::getStatus, 0);
        familyMemberMapper.update(null, wrapper);
    }
}