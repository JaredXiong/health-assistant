package com.healthy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.healthy.entity.PrescriptionItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface PrescriptionItemMapper extends BaseMapper<PrescriptionItem> {
    List<PrescriptionItem> selectByRecordId(@Param("recordId") Long recordId);
    int updateUserMedicineId(@Param("itemId") Long itemId, @Param("userMedicineId") Long userMedicineId);
    List<Map<String, Object>> getCommonMedicines(@Param("userId") Long userId);
}