package com.healthy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.healthy.entity.Family;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface FamilyMapper extends BaseMapper<Family> {
    @Select("SELECT f.* FROM family f " +
            "INNER JOIN family_member fm ON f.id = fm.family_id " +
            "WHERE fm.user_id = #{userId} AND fm.status = 1")
    Family selectByUserId(@Param("userId") Long userId);
    @Select("SELECT * FROM family WHERE invite_code = #{inviteCode}")
    Family selectByInviteCode(@Param("inviteCode") String inviteCode);
}
