package com.healthy.service;

import com.healthy.dto.SetReminderBatchDTO;
import com.healthy.dto.SetReminderFromMedicineDTO;
import com.healthy.dto.UpdateReminderDTO;
import com.healthy.entity.Reminder;
import java.util.List;

public interface ReminderService {
    void markAsPushed(Long id);
    List<Reminder> getTodayReminders(Long userId);
    void cancelReminder(Long reminderId, Long userId);
    void updateReminder(UpdateReminderDTO dto, Long userId);
    List<Reminder> getValidReminders(Long userId);
    void createReminderForUserMedicine(Long userId, SetReminderFromMedicineDTO dto);
    /**
     * 批量设置药品提醒（会覆盖原有提醒）
     */
    void setRemindersForUserMedicine(Long userId, SetReminderBatchDTO dto);

    /**
     * 获取药品的所有有效提醒（未过期、未取消）
     */
    List<Reminder> getRemindersByUserMedicineId(Long userMedicineId, Long userId);
}