package com.healthy.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UserMedicineDTO {
    private Long medicineId;           // 药品ID（从OCR结果中获得）
    private String dosagePerTime;      // 每次用量（可选）
    private String frequency;          // 服用频率（可选）
    private LocalDate startDate;       // 开始日期（可选）
    private LocalDate endDate;         // 结束日期（可选）
    private Boolean reminderEnabled;   // 是否启用提醒（可选，默认false）
    private String status;             // 状态（前端可传入，默认由后端根据频率决定）
}
