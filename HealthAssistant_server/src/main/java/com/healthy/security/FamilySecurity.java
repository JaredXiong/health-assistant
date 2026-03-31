package com.healthy.security;

import com.alibaba.fastjson.JSON;
import com.healthy.entity.DataAuthorization;
import com.healthy.entity.FamilyMember;
import com.healthy.mapper.DataAuthorizationMapper;
import com.healthy.mapper.FamilyMemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FamilySecurity {
    private final FamilyMemberMapper familyMemberMapper;
    private final DataAuthorizationMapper authMapper;

    public boolean isFamilyAdmin(Long userId) {
        FamilyMember member = familyMemberMapper.selectActiveByUserId(userId);
        return member != null && "PARENT".equals(member.getRole());
    }

    public boolean canAccessMemberData(Long viewerId, Long targetUserId, String dataType) {
        if (viewerId.equals(targetUserId)) return true;
        FamilyMember viewer = familyMemberMapper.selectActiveByUserId(viewerId);
        FamilyMember target = familyMemberMapper.selectActiveByUserId(targetUserId);
        if (viewer == null || target == null || !viewer.getFamilyId().equals(target.getFamilyId())) {
            return false;
        }
        if ("PARENT".equals(viewer.getRole())) {
            return "CHILD".equals(target.getRole()); // 家长可看所有孩子
        } else if ("CHILD".equals(viewer.getRole())) {
            // 孩子查看家长，需检查授权
            DataAuthorization auth = authMapper.selectByGrantorAndGrantee(targetUserId, viewerId);
            if (auth != null && (auth.getExpireTime() == null || auth.getExpireTime().isAfter(LocalDateTime.now()))) {
                // 检查dataType是否在授权列表中
                List<String> types = JSON.parseArray(auth.getDataTypes(), String.class);
                return types.contains(dataType);
            }
        }
        return false;
    }
}
