package com.healthy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.healthy.entity.DataAuthorization;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DataAuthorizationMapper extends BaseMapper<DataAuthorization> {
    @Select("SELECT * FROM data_authorization WHERE grantor_id = #{grantorId} AND grantee_id = #{granteeId}")
    DataAuthorization selectByGrantorAndGrantee(@Param("grantorId") Long grantorId, @Param("granteeId") Long granteeId);

    @Select("SELECT * FROM data_authorization WHERE grantee_id = #{userId} AND (expire_time IS NULL OR expire_time > NOW())")
    List<DataAuthorization> selectValidByGrantee(@Param("userId") Long userId);

    @Select("SELECT * FROM data_authorization WHERE family_id = #{familyId} AND (expire_time IS NULL OR expire_time > NOW())")
    List<DataAuthorization> selectValidByFamilyId(@Param("familyId") Long familyId);

    List<DataAuthorization> selectByGranteeId(@Param("granteeId") Long granteeId);
}