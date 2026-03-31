package com.healthy.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ReminderSaveRequest {
    private String code;              // 微信登录 code，用于换取 openid
    private String medicineName;
    private String dosage;
    private String reminderTime;       // 格式 "HH:mm"
    private String daysOfWeek;         // 如 "1,3,5"
    private LocalDate startDate;
    private LocalDate endDate;
}