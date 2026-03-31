package com.healthy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.healthy.entity.Family;
import org.apache.ibatis.annotations.Param;

public interface FamilyMapper extends BaseMapper<Family> {
    Family selectByUserId(@Param("userId") Long userId);
    Family selectByInviteCode(@Param("inviteCode") String inviteCode);
}