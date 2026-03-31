package com.healthy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.healthy.entity.UserMedicine;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface UserMedicineMapper extends BaseMapper<UserMedicine> {
    UserMedicine selectByUserAndMedicine(@Param("userId") Long userId, @Param("medicineId") Long medicineId);
    List<UserMedicine> selectByUserId(@Param("userId") Long userId, @Param("status") String status);

    void update(UserMedicine userMedicine);
}