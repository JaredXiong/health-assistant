package com.healthy.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Users {
    private Long id;            // 主键
    private String openid;      // 微信openid
    private String name;        // 姓名
    private String phone;       // 手机号
    private String nickname;    // 微信昵称
    private String avatarUrl; // 头像URL
}