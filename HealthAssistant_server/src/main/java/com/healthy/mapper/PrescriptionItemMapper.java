package com.healthy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.healthy.entity.PrescriptionItem;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface PrescriptionItemMapper extends BaseMapper<PrescriptionItem> {

    @Select("SELECT * FROM prescription_items WHERE record_id = #{recordId}")
    List<PrescriptionItem> selectByRecordId(@Param("recordId") Long recordId);

    @Update("UPDATE prescription_items SET user_medicine_id = #{userMedicineId} WHERE id = #{itemId}")
    int updateUserMedicineId(@Param("itemId") Long itemId, @Param("userMedicineId") Long userMedicineId);

    @MapKey("medicine_name")
    List<Map<String, Object>> getCommonMedicines(@Param("userId") Long userId);
}