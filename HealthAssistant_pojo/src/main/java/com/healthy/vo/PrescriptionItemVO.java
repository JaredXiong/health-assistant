package com.healthy.vo;

import lombok.Data;

@Data
public class PrescriptionItemVO {
    private Long id;
    private String medicineName;
    private String specification;
    private String dosagePerTime;
    private String frequency;
    private String totalAmount;
    private String usage;
    private String notes;
    private Long userMedicineId;    // 关联的药箱药品ID
}