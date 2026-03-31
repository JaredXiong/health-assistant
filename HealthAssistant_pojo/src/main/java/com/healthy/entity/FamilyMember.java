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
@TableName("family_member")
public class FamilyMember {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long familyId;

    private Long userId;

    private String role; // PARENT, CHILD

    private String relation; // 关系称谓

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime joinedAt;

    private Integer status; // 1-正常 0-已移除
}