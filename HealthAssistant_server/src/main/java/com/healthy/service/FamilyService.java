package com.healthy.service;

import com.healthy.vo.FamilyInfoVO;
import com.healthy.vo.FamilyVO;

public interface FamilyService {
    FamilyVO createFamily(Long userId, String familyName);
    FamilyInfoVO getFamilyInfoByUserId(Long userId);
    void updateFamilyName(Long userId, String newName);
    void dismissFamily(Long userId);
    String refreshInviteCode(Long userId);
    FamilyVO joinFamily(Long userId, String inviteCode);
}