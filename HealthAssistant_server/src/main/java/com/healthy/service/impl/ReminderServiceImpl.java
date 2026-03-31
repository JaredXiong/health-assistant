package com.healthy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.healthy.dto.SetReminderBatchDTO;
import com.healthy.dto.SetReminderFromMedicineDTO;
import com.healthy.dto.UpdateReminderDTO;
import com.healthy.entity.MedicineInfo;
import com.healthy.entity.Reminder;
import com.healthy.entity.UserMedicine;
import com.healthy.exception.UserException;
import com.healthy.mapper.MedicineInfoMapper;
import com.healthy.mapper.ReminderMapper;
import com.healthy.mapper.UserMapper;
import com.healthy.mapper.UserMedicineMapper;
import com.healthy.service.ReminderService;
import com.healthy.service.WechatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReminderServiceImpl implements ReminderService {

    private final ReminderMapper reminderMapper;
    private final UserMapper userMapper;          // 新增注入
    private final WechatService wechatService;    // 原有
    private final UserMedicineMapper userMedicineMapper;
    private final MedicineInfoMapper medicineInfoMapper;

    @Override
    @Transactional
    public void setRemindersForUserMedicine(Long userId, SetReminderBatchDTO dto) {
        // 1. 校验用户药品
        UserMedicine userMedicine = userMedicineMapper.selectById(dto.getUserMedicineId());
        if (userMedicine == null || !userMedicine.getUserId().equals(userId)) {
            throw new UserException("用户药品不存在或无权限");
        }

        // 删除该药品下的所有提醒
        LambdaQueryWrapper<Reminder> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(Reminder::getUserMedicineId, dto.getUserMedicineId());
        reminderMapper.delete(deleteWrapper);   // 现在可以调用了

        // 3. 批量插入新提醒
        LocalDate startDate = dto.getStartDate() != null ? dto.getStartDate() : LocalDate.now();
        LocalDate endDate = dto.getEndDate() != null ? dto.getEndDate() : LocalDate.now().plusMonths(1);

        for (String timeStr : dto.getReminderTimes()) {
            Reminder reminder = new Reminder();
            reminder.setUserMedicineId(dto.getUserMedicineId());
            reminder.setDosage(userMedicine.getDosagePerTime()); // 使用药品记录的每次用量
            reminder.setReminderTime(LocalTime.parse(timeStr));
            reminder.setDaysOfWeek(dto.getDaysOfWeek());
            reminder.setStartDate(startDate);
            reminder.setEndDate(endDate);
            reminder.setStatus(0); // 0-待推送
            reminderMapper.insert(reminder);
        }

        // 4. 更新药品提醒启用标志
        userMedicine.setReminderEnabled(true);
        userMedicineMapper.updateById(userMedicine);

        log.info("为药品ID {} 设置了 {} 个提醒", dto.getUserMedicineId(), dto.getReminderTimes().size());
    }

    @Override
    public List<Reminder> getRemindersByUserMedicineId(Long userMedicineId, Long userId) {
        UserMedicine userMedicine = userMedicineMapper.selectById(userMedicineId);
        if (userMedicine == null || !userMedicine.getUserId().equals(userId)) {
            throw new UserException("无权限");
        }
        LambdaQueryWrapper<Reminder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reminder::getUserMedicineId, userMedicineId)
                .orderByAsc(Reminder::getReminderTime);
        return reminderMapper.selectList(wrapper);
    }

    @Override
    public void markAsPushed(Long id) {
        reminderMapper.updateStatus(id, 1);
        log.info("提醒 {} 已标记为已推送", id);
    }

    @Override
    public List<Reminder> getTodayReminders(Long userId) {
        return reminderMapper.selectTodayRemindersByUserId(userId);
    }

    @Override
    public void cancelReminder(Long reminderId, Long userId) {
        Reminder reminder = reminderMapper.selectById(reminderId);
        if (reminder == null) {
            throw new UserException("提醒不存在");
        }
        UserMedicine userMedicine = userMedicineMapper.selectById(reminder.getUserMedicineId());
        if (userMedicine == null || !userMedicine.getUserId().equals(userId)) {
            throw new UserException("无权操作此提醒");
        }
        reminderMapper.updateStatus(reminderId, 2);
    }

    @Override
    public void updateReminder(UpdateReminderDTO dto, Long userId) {
        Reminder existing = reminderMapper.selectById(dto.getId());
        if (existing == null) {
            throw new UserException("提醒不存在");
        }
        UserMedicine userMedicine = userMedicineMapper.selectById(existing.getUserMedicineId());
        if (userMedicine == null || !userMedicine.getUserId().equals(userId)) {
            throw new UserException("无权操作此提醒");
        }

        Reminder reminder = new Reminder();
        reminder.setId(dto.getId());
        reminder.setUserMedicineId(existing.getUserMedicineId());
        reminder.setDosage(dto.getDosage());
        reminder.setReminderTime(LocalTime.parse(dto.getReminderTime()));
        reminder.setDaysOfWeek(dto.getDaysOfWeek());
        reminder.setStartDate(dto.getStartDate());
        reminder.setEndDate(dto.getEndDate());
        reminder.setStatus(0);
        reminderMapper.updateById(reminder);
    }

    @Override
    public List<Reminder> getValidReminders(Long userId) {
        return reminderMapper.selectValidRemindersByUserId(userId);
    }

    @Override
    @Transactional
    public void createReminderForUserMedicine(Long userId, SetReminderFromMedicineDTO dto) {
        // 1. 校验用户药品
        UserMedicine userMedicine = userMedicineMapper.selectById(dto.getUserMedicineId());
        if (userMedicine == null || !userMedicine.getUserId().equals(userId)) {
            throw new UserException("用户药品不存在或无权限");
        }

        // 2. 获取药品信息
        MedicineInfo medicine = medicineInfoMapper.selectById(userMedicine.getMedicineId());

        // 3. 创建提醒
        Reminder reminder = new Reminder();
        reminder.setUserMedicineId(dto.getUserMedicineId());
        reminder.setDosage(userMedicine.getDosagePerTime() != null ? userMedicine.getDosagePerTime() : medicine.getUsageDosage());
        reminder.setReminderTime(LocalTime.parse(dto.getReminderTime()));
        reminder.setDaysOfWeek(dto.getDaysOfWeek());
        reminder.setStartDate(dto.getStartDate());
        reminder.setEndDate(dto.getEndDate());
        reminder.setStatus(0);
        reminderMapper.insert(reminder);

        // 4. 更新用户药品提醒启用状态
        userMedicine.setReminderEnabled(true);
        userMedicineMapper.updateById(userMedicine);
    }
}