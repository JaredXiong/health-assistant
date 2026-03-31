package com.healthy.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UserMedicine {
    private Long id;
    private Long userId;
    private Long medicineId;
    private String status;          // PLACED, USING, STANDBY, EXPIRED
    private String dosagePerTime;
    private String frequency;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean reminderEnabled;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}