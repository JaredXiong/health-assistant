package com.healthy.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FamilyMemberVO {
    private Long userId;
    private String nickname;
    private String avatar;          // 如果需要，可从user表获取
    private String role;
    private String relation;
    private LocalDateTime joinTime;
}
