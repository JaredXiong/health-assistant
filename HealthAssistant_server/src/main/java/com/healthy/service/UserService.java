package com.healthy.service;

import com.healthy.dto.UpdateUserDTO;

public interface UserService {
    void updateUser(Long userId, UpdateUserDTO dto);
}
