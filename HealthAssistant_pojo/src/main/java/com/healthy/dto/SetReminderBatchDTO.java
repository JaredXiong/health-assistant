package com.healthy.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Data
public class SetReminderBatchDTO {
    @NotNull(message = "用户药品ID不能为空")
    private Long userMedicineId;

    @NotNull(message = "提醒时间列表不能为空")
    private List<String> reminderTimes;   // 时间点列表，格式 "HH:mm"

    private String daysOfWeek;            // 重复星期，如 "1,3,5"（可选，可全局或每个时间点独立，这里简化全局）
    private LocalDate startDate;          // 开始日期（可选，默认当天）
    private LocalDate endDate;            // 结束日期（可选）
}