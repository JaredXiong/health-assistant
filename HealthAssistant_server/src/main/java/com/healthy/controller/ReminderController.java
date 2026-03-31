package com.healthy.controller;

import com.healthy.context.BaseContext;
import com.healthy.dto.SetReminderBatchDTO;
import com.healthy.dto.SetReminderFromMedicineDTO;
import com.healthy.dto.UpdateReminderDTO;
import com.healthy.entity.Reminder;
import com.healthy.result.Result;
import com.healthy.service.ReminderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/reminder")
@Slf4j
@RequiredArgsConstructor
@Api(tags = "用药提醒")
public class ReminderController {

    private final ReminderService reminderService;

    @GetMapping("/today")
    @ApiOperation("获取今日用药提醒")
    public Result<List<Reminder>> getTodayReminders() {
        Long userId = BaseContext.getCurrentId(); // 从 JWT 获取
        if (userId == null) {
            return Result.error("用户未登录");
        }
        try {
            List<Reminder> reminders = reminderService.getTodayReminders(userId);
            return Result.success(reminders);
        } catch (Exception e) {
            log.error("获取今日提醒失败", e);
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/cancel/{id}")
    @ApiOperation("取消用药提醒")
    public Result<String> cancelReminder(@PathVariable Long id) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            return Result.error("用户未登录");
        }
        try {
            reminderService.cancelReminder(id, userId);
            return Result.success("取消成功");
        } catch (Exception e) {
            log.error("取消提醒失败", e);
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/update")
    @ApiOperation("更新用药提醒")
    public Result<String> updateReminder(@RequestBody UpdateReminderDTO dto) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            return Result.error("用户未登录");
        }
        // 基本校验
        if (dto.getId() == null) {
            return Result.error("提醒ID不能为空");
        }
        try {
            reminderService.updateReminder(dto, userId);
            return Result.success("更新成功");
        } catch (Exception e) {
            log.error("更新提醒失败", e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/valid")
    @ApiOperation("获取所有有效提醒（未过期且未取消）")
    public Result<List<Reminder>> getValidReminders() {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            return Result.error("用户未登录");
        }
        try {
            List<Reminder> reminders = reminderService.getValidReminders(userId);
            return Result.success(reminders);
        } catch (Exception e) {
            log.error("获取有效提醒失败", e);
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/set-from-medicine")
    @ApiOperation("从药品管理设置提醒")
    public Result<String> setReminderFromMedicine(@RequestBody SetReminderFromMedicineDTO dto) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            return Result.error("用户未登录");
        }
        try {
            reminderService.createReminderForUserMedicine(userId, dto);
            return Result.success("提醒设置成功");
        } catch (Exception e) {
            log.error("设置提醒失败", e);
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/set-batch")
    @ApiOperation("批量设置药品提醒（会覆盖原有提醒）")
    public Result<String> setRemindersBatch(@RequestBody @Valid SetReminderBatchDTO dto) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            return Result.error("用户未登录");
        }
        reminderService.setRemindersForUserMedicine(userId, dto);
        return Result.success("提醒设置成功");
    }

    // 可选：获取某个药品的提醒列表
    @GetMapping("/list/{userMedicineId}")
    @ApiOperation("获取药品的所有有效提醒")
    public Result<List<Reminder>> getRemindersByMedicine(@PathVariable Long userMedicineId) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            return Result.error("用户未登录");
        }
        List<Reminder> list = reminderService.getRemindersByUserMedicineId(userMedicineId, userId);
        return Result.success(list);
    }
}