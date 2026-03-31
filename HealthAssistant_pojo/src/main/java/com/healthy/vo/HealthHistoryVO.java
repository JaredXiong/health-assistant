package com.healthy.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HealthHistoryVO {
    private LocalDateTime time;
    private Object value; // 可以是数值或组合对象（如血压）
}