package com.healthy.service;

import com.healthy.vo.FamilyMemberVO;
import com.healthy.vo.MemberDetailVO;

import java.util.List;

public interface FamilyMemberService {
    List<FamilyMemberVO> getMembers(Long userId);
    MemberDetailVO getMemberDetail(Long userId, Long targetUserId);
    void updateRelation(Long userId, Long targetUserId, String relation);
    void updateRole(Long userId, Long targetUserId, String role);
    void removeMember(Long userId, Long targetUserId);
}