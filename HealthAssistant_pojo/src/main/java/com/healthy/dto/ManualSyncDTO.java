package com.healthy.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ManualSyncDTO {
    private Integer heartRate;        // 心率
    //private String bloodPressure;     // 血压 "120/80"
    private Integer systolicBp;      // 收缩压
    private Integer diastolicBp;     // 舒张压
    private Integer bloodOxygen;      // 血氧
    private BigDecimal bloodSugar;    // 血糖
    private BigDecimal bodyTemperature;  // 新增：体温
    private Integer respiratoryRate;      // 新增：呼吸频率
}