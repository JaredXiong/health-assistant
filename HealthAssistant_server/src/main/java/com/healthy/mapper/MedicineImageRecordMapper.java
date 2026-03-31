package com.healthy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.healthy.entity.MedicineImageRecord;

public interface MedicineImageRecordMapper extends BaseMapper<MedicineImageRecord> {
    void update(MedicineImageRecord record);
}