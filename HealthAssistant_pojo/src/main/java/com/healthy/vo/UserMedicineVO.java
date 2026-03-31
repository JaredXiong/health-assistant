package com.healthy.vo;

import com.healthy.entity.Reminder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserMedicineVO {
    private Long id;
    private Long userId;
    private Long medicineId;
    private String status;              // PLACED, USING, STANDBY, EXPIRED
    private String dosagePerTime;
    private String frequency;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean reminderEnabled;

    // 药品基本信息（从 medicine_info 关联）
    private String medicineName;
    private String specification;
    private String manufacturer;
    private String approvalNumber;
    private String usageDosage;          // 说明书用法用量
    private String ingredients;
    private String indications;
    private LocalDate expiryDate;        // 新增：有效期

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<Reminder> reminders;
}