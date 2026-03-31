package com.healthy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.healthy.entity.Reminder;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

public interface ReminderMapper extends BaseMapper<Reminder> {
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    List<Reminder> selectToBePushed(@Param("currentTime") String currentTime,
                                    @Param("todayWeek") Integer todayWeek);

    // 根据 openid 查询今日有效提醒（状态未取消，且在有效期内且星期匹配）
    List<Reminder> selectTodayReminders(@Param("openid") String openid,
                                        @Param("todayWeek") Integer todayWeek,
                                        @Param("todayDate") LocalDate todayDate);

    List<Reminder> selectValidReminders(@Param("openid") String openid,
                                        @Param("today") LocalDate today);

    List<Reminder> selectByUserMedicineId(@Param("userMedicineId") Long userMedicineId);

    // 根据 userId 查询今日提醒（通过 user_medicine 关联）
    List<Reminder> selectTodayRemindersByUserId(@Param("userId") Long userId);

    // 根据 userId 查询所有有效提醒
    List<Reminder> selectValidRemindersByUserId(@Param("userId") Long userId);
}