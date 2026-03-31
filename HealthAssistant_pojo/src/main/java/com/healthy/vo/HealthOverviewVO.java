package com.healthy.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HealthOverviewVO {
    private Long userId;
    private String nickname;
    private String avatar;
    private String relation;
    private LatestHealth latest;

    @Data
    public static class LatestHealth {
        private Integer heartRate;
        private String bloodPressure;  // "收缩压/舒张压"
        private Integer bloodOxygen;
        private Double bloodSugar;
        private LocalDateTime updateTime;
    }
}