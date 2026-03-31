package com.healthy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("medicine_image_record")
public class MedicineImageRecord {
    @TableId(type = IdType.AUTO)
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