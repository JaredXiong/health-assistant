package com.healthy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("conversation")
public class Conversation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String sessionId;
    private String role;   // 'user' 或 'assistant'
    private String content;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}