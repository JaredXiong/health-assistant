package com.healthy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("health_report")
public class HealthReport {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long healthDataId;

    private Integer overallScore;  // 总体评分

    private String healthLevel;  // 健康等级

    private String bloodPressureEval;  // 血压评价

    private String bloodSugarEval;  // 血糖评价

    private String heartRateEval;  // 心率评价

    private String bloodOxygenEval;  // 血氧评价

    private String riskFactors;  // 风险因素

    private String recommendations;  // 健康建议

    private LocalDateTime generateTime;  // 生成时间

    private String bodyTemperatureEval;  // 体温评价

    private String respiratoryRateEval;  // 呼吸频率评价

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}