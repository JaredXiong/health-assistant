package com.healthy.service.impl;

import com.healthy.dto.UpdateUserDTO;
import com.healthy.entity.Users;
import com.healthy.exception.BaseException;
import com.healthy.mapper.UserMapper;
import com.healthy.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;  // 假设有该依赖

    @Override
    @Transactional
    public void updateUser(Long userId, UpdateUserDTO dto) {
        // 1. 查询用户是否存在
        Users user = userMapper.getById(userId);
        if (user == null) {
            throw new BaseException("用户不存在");
        }

        // 2. 只更新非空字段
        if (dto.getNickname() != null) {
            user.setNickname(dto.getNickname());
        }
        if (dto.getName() != null) {
            user.setName(dto.getName());
        }
        if (dto.getPhone() != null) {
            // 可在此处添加手机号唯一性校验（如需要）
            // checkPhoneUnique(dto.getPhone(), userId);
            user.setPhone(dto.getPhone());
        }
        if (dto.getAvatarUrl() != null) {
            user.setAvatarUrl(dto.getAvatarUrl());
        }

        // 3. 执行更新
        userMapper.updateById(user);
        log.info("用户 {} 信息更新成功", userId);
    }
}