package com.healthy.vo;

import lombok.Data;

@Data
public class EmergencyContactVO {
    private Long userId;
    private String nickname;
    private String phone; // 脱敏或真实，根据策略
}