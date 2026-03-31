package com.healthy.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class HealthDataVO {
    private Long id;
    private Long userId;
    private Integer heartRate;
    private Integer systolicBp;
    private Integer diastolicBp;
    private Integer bloodOxygen;
    private BigDecimal bloodSugar;
    private BigDecimal bodyTemperature;
    private Integer respiratoryRate;
    private LocalDateTime measurementTime;
}
