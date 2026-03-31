package com.healthy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.healthy.entity.PrescriptionRecord;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

public interface PrescriptionRecordMapper extends BaseMapper<PrescriptionRecord> {
    Map<String, Object> getMonthlyCount(@Param("userId") Long userId);
}