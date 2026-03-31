package com.healthy.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthReportVO {
    private Long id;
    private Long userId;
    private Integer overallScore;
    private String healthLevel;
    private String bloodPressureEval;
    private String bloodSugarEval;
    private String heartRateEval;
    private String bloodOxygenEval;
    private String riskFactors;
    private String recommendations;
    private LocalDateTime generateTime;
    private String bodyTemperatureEval;
    private String respiratoryRateEval;
}
