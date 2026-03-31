package com.healthy.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.healthy.entity.Family;
import com.healthy.entity.FamilyMember;
import com.healthy.exception.BaseException;
import com.healthy.mapper.FamilyMapper;
import com.healthy.mapper.FamilyMemberMapper;
import com.healthy.mapper.UserMapper;
import com.healthy.service.FamilyService;
import com.healthy.vo.FamilyInfoVO;
import com.healthy.vo.FamilyVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class FamilyServiceImpl implements FamilyService {

    private final FamilyMapper familyMapper;
    private final FamilyMemberMapper familyMemberMapper;
    private final UserMapper userMapper;

    @Override
    public FamilyVO createFamily(Long userId, String familyName) {
        // 检查用户是否已加入家庭
        FamilyMember existing = familyMemberMapper.selectActiveByUserId(userId);
        if (existing != null) {
            throw new BaseException("用户已加入家庭，不能重复创建");
        }

        // 生成唯一邀请码
        String inviteCode;
        do {
            inviteCode = generateInviteCode();
        } while (familyMapper.selectByInviteCode(inviteCode) != null);

        LocalDateTime expire = LocalDateTime.now().plusHours(24);

        Family family = Family.builder()
                .name(familyName)
                .inviteCode(inviteCode)
                .inviteCodeExpire(expire)
                .createdBy(userId)
                .build();
        familyMapper.insert(family);

        // 添加创建者为家长
        FamilyMember member = FamilyMember.builder()
                .familyId(family.getId())
                .userId(userId)
                .role("PARENT")
                .relation("创建者")
                .status(1)
                .build();
        familyMemberMapper.insert(member);

        FamilyVO vo = new FamilyVO();
        vo.setFamilyId(family.getId());
        vo.setFamilyName(family.getName());
        vo.setInviteCode(family.getInviteCode());
        vo.setCreatedAt(family.getCreatedAt());
        return vo;
    }

    @Override
    public FamilyInfoVO getFamilyInfoByUserId(Long userId) {
        FamilyMember member = familyMemberMapper.selectActiveByUserId(userId);
        if (member == null) {
            // 未加入家庭，返回null但不抛异常
            return null;
        }
        Family family = familyMapper.selectById(member.getFamilyId());
        if (family == null) {
            throw new BaseException("家庭信息不存在");
        }
        // 统计成员数量
        Long memberCount = familyMemberMapper.selectCountByFamilyId(family.getId());

        FamilyInfoVO vo = new FamilyInfoVO();
        vo.setFamilyId(family.getId());
        vo.setFamilyName(family.getName());
        vo.setMemberCount(memberCount.intValue());
        vo.setAdminId(family.getCreatedBy()); // 创建者为管理员
        vo.setInviteCode(family.getInviteCode());
        vo.setCreatedAt(family.getCreatedAt());
        return vo;
    }

    @Override
    public void updateFamilyName(Long userId, String newName) {
        // 权限校验（保持不变）
        FamilyMember member = familyMemberMapper.selectActiveByUserId(userId);
        if (member == null || !"PARENT".equals(member.getRole())) {
            throw new BaseException("无权限修改家庭名称");
        }
        Family family = familyMapper.selectById(member.getFamilyId());
        if (family == null) {
            throw new BaseException("家庭不存在");
        }

        // 使用 UpdateWrapper 仅更新 name 和 updatedAt
        LambdaUpdateWrapper<Family> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Family::getId, member.getFamilyId())
                .set(Family::getName, newName)
                .set(Family::getUpdatedAt, LocalDateTime.now());
        familyMapper.update(null, wrapper);
    }

    @Override
    public void dismissFamily(Long userId) {
        FamilyMember member = familyMemberMapper.selectActiveByUserId(userId);
        if (member == null) {
            throw new BaseException("用户未加入家庭");
        }
        // 只有创建者可以解散家庭（或者家长都可以？这里限制创建者）
        Family family = familyMapper.selectById(member.getFamilyId());
        if (!family.getCreatedBy().equals(userId)) {
            throw new BaseException("只有家庭创建者可以解散家庭");
        }
        // 软删除所有成员
        familyMemberMapper.removeAllByFamilyId(family.getId());
        // 物理删除家庭？或者保留但标记？这里选择物理删除家庭（谨慎），或者可以软删除家庭表。
        // 为了数据完整，建议保留家庭记录，但标记为解散。但当前表无状态字段，暂物理删除。
        familyMapper.deleteById(family.getId());
    }

    @Override
    public String refreshInviteCode(Long userId) {
        // 1. 权限校验（保持不变）
        FamilyMember member = familyMemberMapper.selectActiveByUserId(userId);
        if (member == null || !"PARENT".equals(member.getRole())) {
            throw new BaseException("无权限刷新邀请码");
        }

        // 2. 生成新邀请码
        String newCode = generateInviteCode();
        LocalDateTime expire = LocalDateTime.now().plusHours(24);

        // 3. 使用 UpdateWrapper 更新
        LambdaUpdateWrapper<Family> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Family::getId, member.getFamilyId())
                .set(Family::getInviteCode, newCode)
                .set(Family::getInviteCodeExpire, expire)
                .set(Family::getUpdatedAt, LocalDateTime.now());
        familyMapper.update(null, wrapper);

        return newCode;
    }

    @Override
    public FamilyVO joinFamily(Long userId, String inviteCode) {
        // 检查用户是否已有家庭
        FamilyMember existing = familyMemberMapper.selectActiveByUserId(userId);
        if (existing != null) {
            throw new BaseException("用户已加入家庭，不能重复加入");
        }
        // 查询邀请码对应的家庭
        Family family = familyMapper.selectByInviteCode(inviteCode);
        if (family == null) {
            throw new BaseException("邀请码无效");
        }
        if (family.getInviteCodeExpire() != null && family.getInviteCodeExpire().isBefore(LocalDateTime.now())) {
            throw new BaseException("邀请码已过期");
        }
        // 加入家庭，角色默认为孩子（需由家长后续修改）
        FamilyMember member = FamilyMember.builder()
                .familyId(family.getId())
                .userId(userId)
                .role("CHILD")  // 默认孩子
                .relation("成员")
                .status(1)
                .build();
        familyMemberMapper.insert(member);

        FamilyVO vo = new FamilyVO();
        vo.setFamilyId(family.getId());
        vo.setFamilyName(family.getName());
        vo.setInviteCode(family.getInviteCode());
        vo.setCreatedAt(family.getCreatedAt());
        return vo;
    }

    private String generateInviteCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}