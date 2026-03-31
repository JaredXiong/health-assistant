package com.healthy.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InviteCodeVO {
    private String inviteCode;
    private LocalDateTime expireTime;
}
