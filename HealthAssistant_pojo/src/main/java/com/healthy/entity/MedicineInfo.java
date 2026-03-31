package com.healthy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("medicine_info")
public class MedicineInfo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String specification;
    private String manufacturer;
    private String approvalNumber;
    private String usageDosage;
    private String ingredients;      // 新增：成份
    private String indications;      // 新增：功能主治
    // 新增字段
    private String adverseReactions;
    private String precautions;
    private String contraindications;
    private LocalDate expiryDate;  // 新增：有效期
    private LocalDateTime createTime;

}