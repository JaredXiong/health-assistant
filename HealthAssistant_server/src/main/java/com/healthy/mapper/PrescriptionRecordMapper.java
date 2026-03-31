package com.healthy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.healthy.entity.PrescriptionRecord;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface PrescriptionRecordMapper extends BaseMapper<PrescriptionRecord> {
    @MapKey("month")
    List<Map<String, Object>> getMonthlyCount(@Param("userId") Long userId);
}