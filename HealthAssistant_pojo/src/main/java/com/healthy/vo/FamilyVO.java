package com.healthy.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FamilyVO {
    private Long familyId;
    private String familyName;
    private String inviteCode;
    private LocalDateTime createdAt;
}