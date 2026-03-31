package com.healthy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("prescription_items")
public class PrescriptionItem {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long recordId;

    private String medicineName;

    private String specification;

    private String dosagePerTime;

    private String frequency;

    private String totalAmount;

    @TableField(value = "`usage`")
    private String usage;

    private String notes;

    private Long userMedicineId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    private LocalDate startDate;   // 建议开始日期

    private LocalDate endDate;     // 建议结束日期
}
