package com.healthy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("health_data")
public class HealthData {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Integer heartRate;  // 心率

    private Integer systolicBp;  // 收缩压

    private Integer diastolicBp;  // 舒张压

    private Integer bloodOxygen;  // 血氧饱和度

    private BigDecimal bloodSugar;  // 血糖

    private BigDecimal bodyTemperature;  // 体温

    private Integer respiratoryRate;  // 呼吸频率

    private LocalDateTime measurementTime;  // 测量时间

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}