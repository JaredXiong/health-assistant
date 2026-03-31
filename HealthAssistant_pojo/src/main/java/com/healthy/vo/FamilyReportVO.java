package com.healthy.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class FamilyReportVO {
    private String familyName;
    private String period;          // 日期范围描述
    private LocalDateTime generatedAt;
    private Overview overview;
    private Trends trends;
    private List<String> insights;
    private List<String> suggestions;

    @Data
    public static class Overview {
        private Double avgHeartRate;
        private String avgBloodPressure; // 如 "118/75"
        private Double avgBloodOxygen;
        private Double avgBloodSugar;
    }

    @Data
    public static class Trends {
        private List<Integer> heartRate; // 按天
        private List<String> dates;       // 对应日期
        // 可扩展其他指标
    }
}