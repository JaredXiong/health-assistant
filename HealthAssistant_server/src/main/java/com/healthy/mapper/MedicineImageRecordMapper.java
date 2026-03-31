package com.healthy.mapper;

import com.healthy.entity.MedicineImageRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MedicineImageRecordMapper {
    int insert(MedicineImageRecord record);
    int update(MedicineImageRecord record);
    MedicineImageRecord selectById(Long id);
}
