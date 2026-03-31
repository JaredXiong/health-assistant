package com.healthy.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateReminderDTO {
    private Long id;                // 提醒ID
    private String medicineName;     // 药品名称
    private String dosage;           // 每次用量
    private String reminderTime;     // 提醒时间，格式 HH:mm
    private String daysOfWeek;       // 星期几，如 "1,3,5"
    private LocalDate startDate;
    private LocalDate endDate;
}