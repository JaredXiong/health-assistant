package com.healthy.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FamilyInfoVO {
    private Long familyId;
    private String familyName;
    private Integer memberCount;
    private Long adminId;           // 管理员ID（多个管理员？可选，取创建者）
    private String inviteCode;
    private LocalDateTime createdAt;
}