package com.healthy.dto;

import lombok.Data;
import javax.validation.constraints.Pattern;

@Data
public class UpdateUserDTO {
    private String nickname;          // 微信昵称（可选）
    private String name;               // 真实姓名（可选）

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;              // 手机号（可选，如果传则校验格式）

    private String avatarUrl; // 头像URL
}
