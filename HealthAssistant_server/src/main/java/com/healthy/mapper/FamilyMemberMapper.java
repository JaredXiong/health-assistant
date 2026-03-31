package com.healthy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.healthy.entity.FamilyMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface FamilyMemberMapper extends BaseMapper<FamilyMember> {
    @Select("SELECT * FROM family_member WHERE family_id = #{familyId} AND status = 1")
    List<FamilyMember> selectActiveByFamilyId(@Param("familyId") Long familyId);

    @Select("SELECT * FROM family_member WHERE user_id = #{userId} AND status = 1")
    FamilyMember selectActiveByUserId(@Param("userId") Long userId);

    @Update("UPDATE family_member SET status = 0 WHERE family_id = #{familyId}")
    int removeAllByFamilyId(@Param("familyId") Long familyId);

    @Select("SELECT COUNT(*) FROM family_member WHERE family_id = #{familyId} AND status = 1")
    Long selectCountByFamilyId(@Param("familyId") Long familyId);

    @Select("SELECT * FROM family_member WHERE family_id = #{familyId} AND role = 'PARENT' AND status = 1")
    List<FamilyMember> selectParentsByFamilyId(@Param("familyId") Long familyId);
}