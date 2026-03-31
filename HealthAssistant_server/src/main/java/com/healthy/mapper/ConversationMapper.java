package com.healthy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.healthy.entity.Conversation;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface ConversationMapper extends BaseMapper<Conversation> {
    List<Conversation> selectBySession(@Param("userId") Long userId, @Param("sessionId") String sessionId);
}