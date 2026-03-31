package com.healthy.vo;

import lombok.Data;

@Data
public class ChatResponseVO {
    private String reply;      // AI回复内容
    private String sessionId;  // 本次会话ID
}
