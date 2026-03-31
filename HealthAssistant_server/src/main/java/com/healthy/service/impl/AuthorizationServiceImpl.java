package com.healthy.service.impl;

import com.alibaba.fastjson.JSON;
import com.healthy.dto.SetAuthorizationDTO;
import com.healthy.entity.DataAuthorization;
import com.healthy.entity.FamilyMember;
import com.healthy.entity.Users;
import com.healthy.exception.BaseException;
import com.healthy.mapper.DataAuthorizationMapper;
import com.healthy.mapper.FamilyMemberMapper;
import com.healthy.mapper.UserMapper;
import com.healthy.service.AuthorizationService;
import com.healthy.vo.AuthorizationVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class AuthorizationServiceImpl implements AuthorizationService {

    private final DataAuthorizationMapper authMapper;
    private final FamilyMemberMapper familyMemberMapper;
    private final UserMapper userMapper;

    @Override
    public List<AuthorizationVO> getAuthorizations(Long userId, Long childId) {
        FamilyMember current = familyMemberMapper.selectActiveByUserId(userId);
        if (current == null) {
            throw new BaseException("用户未加入家庭");
        }
        List<DataAuthorization> auths;
        if ("PARENT".equals(current.getRole())) {
            // 家长可查看所有授权，可选按孩子筛选
            if (childId != null) {
                // 检查childId是否在家庭内
                FamilyMember child = familyMemberMapper.selectActiveByUserId(childId);
                if (child == null || !child.getFamilyId().equals(current.getFamilyId())) {
                    throw new BaseException("孩子不在家庭中");
                }
                auths = authMapper.selectByGranteeId(childId); // 需要增加按grantee查询的方法
            } else {
                auths = authMapper.selectValidByFamilyId(current.getFamilyId());
            }
        } else {
            // 孩子只能查看与自己相关的授权（作为被授权者）
            auths = authMapper.selectValidByGrantee(userId);
        }
        return auths.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public Long setAuthorization(Long userId, SetAuthorizationDTO dto) {
        FamilyMember current = familyMemberMapper.selectActiveByUserId(userId);
        if (current == null) {
            throw new BaseException("用户未加入家庭");
        }
        if (!"PARENT".equals(current.getRole())) {
            throw new BaseException("只有家长可以设置授权");
        }
        // 校验目标孩子是否在家庭内且角色为孩子
        FamilyMember child = familyMemberMapper.selectActiveByUserId(dto.getChildId());
        if (child == null || !child.getFamilyId().equals(current.getFamilyId())) {
            throw new BaseException("目标孩子不在家庭中");
        }
        if (!"CHILD".equals(child.getRole())) {
            throw new BaseException("只能给孩子授权");
        }
        // 检查是否已存在授权记录
        DataAuthorization existing = authMapper.selectByGrantorAndGrantee(userId, dto.getChildId());
        if (existing != null) {
            // 更新
            existing.setDataTypes(JSON.toJSONString(dto.getDataTypes()));
            existing.setExpireTime(dto.getExpireTime());
            existing.setUpdatedAt(LocalDateTime.now());
            authMapper.updateById(existing);
            return existing.getId();
        } else {
            // 新增
            DataAuthorization auth = DataAuthorization.builder()
                    .familyId(current.getFamilyId())
                    .grantorId(userId)
                    .granteeId(dto.getChildId())
                    .dataTypes(JSON.toJSONString(dto.getDataTypes()))
                    .expireTime(dto.getExpireTime())
                    .build();
            authMapper.insert(auth);
            return auth.getId();
        }
    }

    @Override
    public void revokeAuthorization(Long userId, Long authId) {
        DataAuthorization auth = authMapper.selectById(authId);
        if (auth == null) {
            throw new BaseException("授权记录不存在");
        }
        // 权限：只有授权者可以撤销
        if (!auth.getGrantorId().equals(userId)) {
            throw new BaseException("无权撤销此授权");
        }
        // 物理删除
        authMapper.deleteById(authId);
    }

    private AuthorizationVO convertToVO(DataAuthorization auth) {
        AuthorizationVO vo = new AuthorizationVO();
        vo.setAuthId(auth.getId());
        vo.setGrantorId(auth.getGrantorId());
        vo.setGranteeId(auth.getGranteeId());
        // 查询昵称
        Users grantor = userMapper.getById(auth.getGrantorId());
        Users grantee = userMapper.getById(auth.getGranteeId());
        vo.setGrantorName(grantor != null ? grantor.getNickname() : "未知");
        vo.setGranteeName(grantee != null ? grantee.getNickname() : "未知");
        vo.setDataTypes(JSON.parseArray(auth.getDataTypes(), String.class));
        vo.setExpireTime(auth.getExpireTime());
        return vo;
    }
}