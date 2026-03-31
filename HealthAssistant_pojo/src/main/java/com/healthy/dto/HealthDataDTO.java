package com.healthy.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class HealthDataDTO {
    private Integer heartRate;        // 心率
    //private String bloodPressure;     // 血压字符串 "120/80"
    private Integer systolicBp;       // 收缩压（从 bloodPressure 解析）
    private Integer diastolicBp;      // 舒张压（从 bloodPressure 解析）
    private Integer bloodOxygen;      // 血氧
    private BigDecimal bloodSugar;    // 血糖
    private BigDecimal bodyTemperature;  // 体温
    private Integer respiratoryRate;  // 呼吸频率
    private LocalDateTime measurementTime;  // 测量时间
}
