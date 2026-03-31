package com.healthy.service;

import com.healthy.dto.UserMedicineDTO;   // 需要创建，包含状态、剂量、频率等
import com.healthy.vo.UserMedicineVO;     // 返回给前端的视图对象

import java.time.LocalDate;
import java.util.List;

public interface UserMedicineService {
    /**
     * 用户将药品加入管理（识别后调用）
     * @param userId 当前用户ID
     * @param medicineId 药品ID（来自识别结果）
     * @param dto 用户补充信息（剂量、频率、状态等，非必填）
     * @return 用户药品记录ID
     */
    Long addUserMedicine(Long userId, Long medicineId, UserMedicineDTO dto);

    /**
     * 获取用户的所有药品，可按状态筛选
     */
    List<UserMedicineVO> getUserMedicines(Long userId, String status);

    /**
     * 更新用户药品信息（如切换状态、补充频率等）
     */
    void updateUserMedicine(Long userMedicineId, UserMedicineDTO dto);

    /**
     * 删除用户药品记录（逻辑删除或物理删除，根据需求）
     */
    void removeUserMedicine(Long userMedicineId);

    /**
     * 将放置状态的药品转为使用状态（需补充频率）
     */
    void activateMedicine(Long userMedicineId, String dosagePerTime, String frequency, LocalDate startDate, LocalDate endDate);

    /**
     * 从处方明细将药品添加到药箱
     * @param itemId 处方明细ID
     * @param userId 当前用户ID
     * @return 新创建的用户药品ID
     */
    Long addFromPrescription(Long itemId, Long userId);
}