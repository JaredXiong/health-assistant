package com.healthy.mapper;

import com.healthy.entity.Users;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {

    // 根据openid查询用户
    Users getByOpenid(String openid);

    // 根据ID查询用户
    Users getById(Long id);

    // 插入新用户
    void insert(Users user);

    // 更新用户信息
    void update(Users user);
}