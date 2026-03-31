package com.healthy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.healthy.entity.MedicineInfo;
import org.apache.ibatis.annotations.Param;

public interface MedicineInfoMapper extends BaseMapper<MedicineInfo> {
    MedicineInfo selectByNameAndApproval(@Param("name") String name, @Param("approvalNumber") String approvalNumber);
}