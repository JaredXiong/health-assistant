package com.healthy.vo;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PrescriptionRecordVO {
    private Long id;
    private LocalDate visitDate;
    private String hospital;
    private String department;
    private String doctor;
    private String notes;
    private List<String> photos;
    private List<PrescriptionItemVO> items;
    private LocalDateTime createdAt;
}