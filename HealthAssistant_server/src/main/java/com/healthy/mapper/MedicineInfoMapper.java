package com.healthy.mapper;

import com.healthy.entity.MedicineInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MedicineInfoMapper {
    int insert(MedicineInfo medicineInfo);
    MedicineInfo selectById(Long id);
    MedicineInfo selectByNameAndApproval(@Param("name") String name, @Param("approvalNumber") String approvalNumber);
}
