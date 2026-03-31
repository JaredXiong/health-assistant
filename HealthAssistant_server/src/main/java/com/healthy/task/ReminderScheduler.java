package com.healthy.task;

import com.healthy.entity.Reminder;
import com.healthy.mapper.ReminderMapper;
import com.healthy.service.WechatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReminderScheduler {

    private final ReminderMapper reminderMapper;
    private final WechatService wechatService;

    // 每分钟的第0秒执行
    @Scheduled(cron = "0 * * * * *")
    public void pushReminders() {
        log.info("开始扫描待推送提醒...");

        LocalTime now = LocalTime.now();
        String currentTime = now.format(DateTimeFormatter.ofPattern("HH:mm"));
        LocalDate today = LocalDate.now();
        int todayWeek = today.getDayOfWeek().getValue(); // 周一=1, 周日=7

        // 查询需要推送的提醒
        List<Reminder> reminders = reminderMapper.selectToBePushed(currentTime, todayWeek);
        if (reminders.isEmpty()) {
            log.info("暂无需要推送的提醒");
            return;
        }

        // 获取 access_token
        String accessToken;
        try {
            accessToken = wechatService.getAccessToken();
        } catch (Exception e) {
            log.error("获取 access_token 失败，本次推送终止", e);
            return;
        }

        // 逐个推送
        for (Reminder reminder : reminders) {
            try {
                boolean success = wechatService.sendSubscribeMessage(reminder, accessToken);
                if (success) {
                    // 标记为已推送
                    reminderMapper.updateStatus(reminder.getId(), 1);
                    log.info("提醒 {} 推送成功", reminder.getId());
                } else {
                    // 推送失败，可根据错误码决定是否重试，这里暂时不做重试
                    log.warn("提醒 {} 推送失败", reminder.getId());
                }
            } catch (Exception e) {
                log.error("推送提醒 {} 时发生异常", reminder.getId(), e);
            }
        }
    }
}