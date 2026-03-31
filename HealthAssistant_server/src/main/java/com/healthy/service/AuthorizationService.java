package com.healthy.service;

import com.healthy.dto.SetAuthorizationDTO;
import com.healthy.vo.AuthorizationVO;

import java.util.List;

public interface AuthorizationService {
    List<AuthorizationVO> getAuthorizations(Long userId, Long childId);
    Long setAuthorization(Long userId, SetAuthorizationDTO dto);
    void revokeAuthorization(Long userId, Long authId);
}