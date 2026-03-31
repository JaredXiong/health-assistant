package com.healthy.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class MemberDetailVO {
    private Long userId;
    private String nickname;
    private String avatar;
    private String role;
    private String relation;
    private LocalDateTime joinTime;
    private Map<String, Object> latestHealth; // 包含指标数据
}