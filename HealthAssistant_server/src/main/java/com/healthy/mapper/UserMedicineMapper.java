package com.healthy.mapper;

import com.healthy.entity.UserMedicine;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface UserMedicineMapper {
    int insert(UserMedicine userMedicine);
    int update(UserMedicine userMedicine);
    UserMedicine selectById(Long id);
    UserMedicine selectByUserAndMedicine(@Param("userId") Long userId, @Param("medicineId") Long medicineId);
    List<UserMedicine> selectByUserId(@Param("userId") Long userId, @Param("status") String status);
    int deleteById(Long id);
}