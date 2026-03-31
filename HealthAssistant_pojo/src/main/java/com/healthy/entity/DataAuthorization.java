package com.healthy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("data_authorization")
public class DataAuthorization {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long familyId;

    private Long grantorId;   // 授权者（家长）

    private Long granteeId;    // 被授权者（孩子）

    private String dataTypes;  // JSON字符串，例如 ["heartRate","bloodPressure"]

    private LocalDateTime expireTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}