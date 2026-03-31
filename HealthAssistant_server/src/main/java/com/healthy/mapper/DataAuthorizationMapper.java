package com.healthy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.healthy.entity.DataAuthorization;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DataAuthorizationMapper extends BaseMapper<DataAuthorization> {
    DataAuthorization selectByGrantorAndGrantee(@Param("grantorId") Long grantorId, @Param("granteeId") Long granteeId);
    List<DataAuthorization> selectValidByGrantee(@Param("userId") Long userId);
    List<DataAuthorization> selectValidByFamilyId(@Param("familyId") Long familyId);
    List<DataAuthorization> selectByGranteeId(@Param("granteeId") Long granteeId);
}