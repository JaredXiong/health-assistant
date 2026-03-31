package com.healthy.dto;

import lombok.Data;

@Data
public class LoginDTO {
    private String code;        // 微信小程序登录code
    private String nickname;    // 微信昵称（可选）
    private String avatarUrl;   // 头像URL（可选）
}