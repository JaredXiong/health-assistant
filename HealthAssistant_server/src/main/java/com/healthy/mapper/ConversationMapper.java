package com.healthy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.healthy.entity.Conversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface ConversationMapper extends BaseMapper<Conversation> {
    @Select("SELECT * FROM conversation WHERE user_id = #{userId} AND session_id = #{sessionId} ORDER BY create_time ASC")
    List<Conversation> selectBySession(@Param("userId") Long userId, @Param("sessionId") String sessionId);
}