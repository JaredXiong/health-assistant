package com.healthy.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MedicineImageRecord {
    private Long id;
    private Long userId;
    private String imageUrl;
    private String ocrRawResult;
    private String parsedInfo;
    private Integer status; // 0待处理 1成功 2失败
    private String ingredients;      // 新增：识别的成份
    private String indications;      // 新增：识别的功能主治
    // 新增字段
    private String adverseReactions;
    private String precautions;
    private String contraindications;
    private LocalDateTime createTime;
}