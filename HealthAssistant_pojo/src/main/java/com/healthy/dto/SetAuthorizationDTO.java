package com.healthy.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SetAuthorizationDTO {
    @NotNull(message = "孩子ID不能为空")
    private Long childId;

    private List<String> dataTypes; // 授权数据类型列表

    private LocalDateTime expireTime; // 过期时间，null表示永久
}