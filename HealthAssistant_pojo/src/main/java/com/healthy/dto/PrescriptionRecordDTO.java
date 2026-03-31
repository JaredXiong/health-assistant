package com.healthy.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Data
public class PrescriptionRecordDTO {
    private Long id;                 // 用于更新
    @NotNull(message = "就诊日期不能为空")
    private LocalDate visitDate;
    private String hospital;
    private String department;
    private String doctor;
    private String notes;
    private List<String> photos;
    private List<PrescriptionItemDTO> items;
}
