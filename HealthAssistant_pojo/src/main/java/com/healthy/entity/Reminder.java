package com.healthy.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("reminder")
public class Reminder {
    private Long id;
    private Long userMedicineId;          // 新增关联字段
    private String dosage;        // 每次用量（可保留，从user_medicine冗余）
    private LocalTime reminderTime;
    private String daysOfWeek;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer status; // 0待推送 1已推送 2已取消
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private String openid;   // 用于推送时获取，不持久化

    @TableField(exist = false)
    private String medicineName; // 新增：药品名称，用于前端展示
}