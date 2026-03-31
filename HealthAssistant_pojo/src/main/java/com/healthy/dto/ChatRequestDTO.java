package com.healthy.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class ChatRequestDTO {
    @NotBlank(message = "消息内容不能为空")
    private String message;
    private String sessionId;   // 可选，前端可传已有会话ID，不传则后端生成新会话
}
