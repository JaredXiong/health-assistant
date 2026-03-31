package com.healthy.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class SetReminderFromMedicineDTO {
    private Long userMedicineId;           // 用户药品ID
    private String reminderTime;           // 提醒时间 HH:mm
    private String daysOfWeek;              // 重复星期 "1,3,5"
    private LocalDate startDate;            // 提醒开始日期
    private LocalDate endDate;              // 提醒结束日期
}