package com.healthy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.healthy.entity.Users;

public interface UserMapper extends BaseMapper<Users> {

    // 根据openid查询用户
    Users getByOpenid(String openid);

    // 根据ID查询用户
    Users getById(Long id);
}