package com.healthy.dto;

import lombok.Data;
import java.util.List;

@Data
public class OCRResultDTO {
    private Long medicineId;          // 新增：药品数据库主键ID
    private String medicineName;      // 药品名称
    private String specification;     // 规格
    private String manufacturer;      // 生产厂家
    private String approvalNumber;    // 批准文号
    private String usageDosage;       // 用法用量
    private String ingredients;       // 新增：成份
    private String indications;       // 新增：功能主治
    // 新增字段
    private String adverseReactions;
    private String precautions;
    private String contraindications;
    private List<String> otherInfo;   // 其他信息
    private String rawText; // 新增：原始识别文本
    private String imageUrl;           // 新增：图片访问URL
}