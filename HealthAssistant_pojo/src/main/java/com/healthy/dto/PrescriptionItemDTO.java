package com.healthy.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PrescriptionItemDTO {
    private Long id;                 // 用于更新时传入
    private String medicineName;
    private String specification;
    private String dosagePerTime;
    private String frequency;
    private String totalAmount;
    private String usage;
    private String notes;
    private LocalDate startDate;
    private LocalDate endDate;
}
