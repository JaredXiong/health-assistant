package com.healthy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.healthy.entity.FamilyMember;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FamilyMemberMapper extends BaseMapper<FamilyMember> {
    List<FamilyMember> selectActiveByFamilyId(@Param("familyId") Long familyId);
    FamilyMember selectActiveByUserId(@Param("userId") Long userId);
    int removeAllByFamilyId(@Param("familyId") Long familyId);
    Long selectCountByFamilyId(@Param("familyId") Long familyId);
    List<FamilyMember> selectParentsByFamilyId(@Param("familyId") Long familyId);
}