package com.healthy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.healthy.context.BaseContext;
import com.healthy.dto.UserMedicineDTO;
import com.healthy.entity.MedicineInfo;
import com.healthy.entity.PrescriptionItem;
import com.healthy.entity.Reminder;
import com.healthy.entity.UserMedicine;
import com.healthy.exception.BaseException;
import com.healthy.exception.UserException;
import com.healthy.mapper.MedicineInfoMapper;
import com.healthy.mapper.PrescriptionItemMapper;
import com.healthy.mapper.ReminderMapper;
import com.healthy.mapper.UserMedicineMapper;
import com.healthy.service.UserMedicineService;
import com.healthy.vo.UserMedicineVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户药品管理服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserMedicineServiceImpl implements UserMedicineService {

    private final UserMedicineMapper userMedicineMapper;
    private final MedicineInfoMapper medicineInfoMapper;
    private final PrescriptionItemMapper prescriptionItemMapper;
    private final ReminderMapper reminderMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addUserMedicine(Long userId, Long medicineId, UserMedicineDTO dto) {
        log.info("用户添加药品管理：userId={}, medicineId={}, dto={}", userId, medicineId, dto);

        // 1. 校验药品是否存在
        MedicineInfo medicine = medicineInfoMapper.selectById(medicineId);
        if (medicine == null) {
            throw new BaseException("药品信息不存在，请重新识别");
        }

        // 2. 检查是否已添加过该药品（可选，根据业务决定是否允许重复添加）
        UserMedicine exist = userMedicineMapper.selectByUserAndMedicine(userId, medicineId);
        if (exist != null) {
            throw new BaseException("您已添加过该药品，请勿重复添加");
        }

        // 3. 创建用户药品记录
        UserMedicine userMedicine = new UserMedicine();
        userMedicine.setUserId(userId);
        userMedicine.setMedicineId(medicineId);

        // 根据是否有频率决定初始状态
        if (dto != null && StringUtils.hasText(dto.getFrequency())) {
            // 用户补充了服用频率，状态设为使用中
            userMedicine.setStatus("USING");
            userMedicine.setDosagePerTime(dto.getDosagePerTime());
            userMedicine.setFrequency(dto.getFrequency());
            userMedicine.setStartDate(dto.getStartDate() != null ? dto.getStartDate() : LocalDate.now());
            userMedicine.setEndDate(dto.getEndDate());
            userMedicine.setReminderEnabled(dto.getReminderEnabled() != null ? dto.getReminderEnabled() : false);
        } else {
            // 未补充频率，状态设为放置
            userMedicine.setStatus("PLACED");
            // 其他字段留空
        }

        userMedicineMapper.insert(userMedicine);
        log.info("用户药品添加成功，ID：{}", userMedicine.getId());
        return userMedicine.getId();
    }


    @Override
    public List<UserMedicineVO> getUserMedicines(Long userId, String status) {
        List<UserMedicine> list = userMedicineMapper.selectByUserId(userId, status);
        return list.stream().map(um -> {
            UserMedicineVO vo = new UserMedicineVO();
            BeanUtils.copyProperties(um, vo);

            // 查询药品信息
            MedicineInfo medicine = medicineInfoMapper.selectById(um.getMedicineId());
            if (medicine != null) {
                vo.setMedicineName(medicine.getName());
                vo.setSpecification(medicine.getSpecification());
                vo.setManufacturer(medicine.getManufacturer());
                vo.setApprovalNumber(medicine.getApprovalNumber());
                vo.setUsageDosage(medicine.getUsageDosage());
                vo.setIngredients(medicine.getIngredients());
                vo.setIndications(medicine.getIndications());
                vo.setExpiryDate(medicine.getExpiryDate());  // 设置有效期
            }

            // 查询该药品的所有有效提醒
            LambdaQueryWrapper<Reminder> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Reminder::getUserMedicineId, um.getId())
                    .orderByAsc(Reminder::getReminderTime);
            List<Reminder> reminders = reminderMapper.selectList(wrapper);
            vo.setReminders(reminders);

            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserMedicine(Long userMedicineId, UserMedicineDTO dto) {
        log.info("更新用户药品信息：userMedicineId={}, dto={}", userMedicineId, dto);

        // 1. 查询原记录并校验权限
        UserMedicine existing = userMedicineMapper.selectById(userMedicineId);
        if (existing == null) {
            throw new BaseException("用户药品记录不存在");
        }

        Long currentUserId = BaseContext.getCurrentId();
        if (!existing.getUserId().equals(currentUserId)) {
            throw new UserException("无权操作此药品");
        }

        // 2. 更新字段（只更新非空字段）
        if (dto.getDosagePerTime() != null) {
            existing.setDosagePerTime(dto.getDosagePerTime());
        }
        if (dto.getFrequency() != null) {
            existing.setFrequency(dto.getFrequency());
        }
        if (dto.getStartDate() != null) {
            existing.setStartDate(dto.getStartDate());
        }
        if (dto.getEndDate() != null) {
            existing.setEndDate(dto.getEndDate());
        }
        if (dto.getReminderEnabled() != null) {
            existing.setReminderEnabled(dto.getReminderEnabled());
        }
        if (dto.getStatus() != null) {
            existing.setStatus(dto.getStatus());
        }

        userMedicineMapper.updateById(existing);
        log.info("用户药品更新成功，ID：{}", userMedicineId);
    }

    @Override
    @Transactional
    public void removeUserMedicine(Long userMedicineId) {
        // 权限校验...
        UserMedicine existing = userMedicineMapper.selectById(userMedicineId);
        if (existing == null) {
            throw new BaseException("用户药品记录不存在");
        }
        Long currentUserId = BaseContext.getCurrentId();
        if (!existing.getUserId().equals(currentUserId)) {
            throw new UserException("无权操作此药品");
        }

        // 1. 删除关联的提醒
        LambdaQueryWrapper<Reminder> reminderWrapper = new LambdaQueryWrapper<>();
        reminderWrapper.eq(Reminder::getUserMedicineId, userMedicineId);
        reminderMapper.delete(reminderWrapper);

        // 2. 解除处方明细中的关联
        LambdaUpdateWrapper<PrescriptionItem> itemWrapper = new LambdaUpdateWrapper<>();
        itemWrapper.eq(PrescriptionItem::getUserMedicineId, userMedicineId)
                .set(PrescriptionItem::getUserMedicineId, null);
        prescriptionItemMapper.update(null, itemWrapper);

        // 3. 删除用户药品记录
        userMedicineMapper.deleteById(userMedicineId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void activateMedicine(Long userMedicineId, String dosagePerTime, String frequency,
                                 LocalDate startDate, LocalDate endDate) {
        log.info("激活药品为使用状态：userMedicineId={}, dosagePerTime={}, frequency={}",
                userMedicineId, dosagePerTime, frequency);

        // 1. 查询记录
        UserMedicine existing = userMedicineMapper.selectById(userMedicineId);
        if (existing == null) {
            throw new BaseException("用户药品记录不存在");
        }

        // 2. 权限校验
        Long currentUserId = BaseContext.getCurrentId();
        if (!existing.getUserId().equals(currentUserId)) {
            throw new UserException("无权操作此药品");
        }

        // 3. 校验必填项
        if (!StringUtils.hasText(frequency)) {
            throw new BaseException("服用频率不能为空");
        }

        // 4. 更新字段
        existing.setStatus("USING");
        existing.setDosagePerTime(dosagePerTime);
        existing.setFrequency(frequency);
        existing.setStartDate(startDate != null ? startDate : LocalDate.now());
        existing.setEndDate(endDate);
        // 提醒启用状态默认 false，由用户后续自行开启
        existing.setReminderEnabled(false);

        userMedicineMapper.updateById(existing);
        log.info("药品激活成功，ID：{}", userMedicineId);
    }

    @Override
    @Transactional
    public Long addFromPrescription(Long itemId, Long userId) {
        // 1. 查询处方明细
        PrescriptionItem item = prescriptionItemMapper.selectById(itemId);
        if (item == null) {
            throw new BaseException("处方明细不存在");
        }

        // 2. 查询或创建药品信息
        MedicineInfo medicine = medicineInfoMapper.selectByNameAndApproval(item.getMedicineName(), null);
        Long medicineId;
        if (medicine != null) {
            medicineId = medicine.getId();
        } else {
            MedicineInfo newMedicine = new MedicineInfo();
            newMedicine.setName(item.getMedicineName());
            newMedicine.setSpecification(item.getSpecification());
            medicineInfoMapper.insert(newMedicine);
            medicineId = newMedicine.getId();
        }

        // 3. 创建用户药品记录，状态为 PLACED（放置）
        UserMedicine userMedicine = new UserMedicine();
        userMedicine.setUserId(userId);
        userMedicine.setMedicineId(medicineId);
        userMedicine.setStatus("PLACED");
        userMedicine.setDosagePerTime(item.getDosagePerTime());
        userMedicine.setFrequency(item.getFrequency());
        userMedicine.setReminderEnabled(false);
        // 设置开始/结束日期（若处方明细中提供）
        if (item.getStartDate() != null) {
            userMedicine.setStartDate(item.getStartDate());
        }
        if (item.getEndDate() != null) {
            userMedicine.setEndDate(item.getEndDate());
        }
        userMedicineMapper.insert(userMedicine);

        // 4. 更新处方明细，记录关联的用户药品ID
        item.setUserMedicineId(userMedicine.getId());
        prescriptionItemMapper.updateById(item);

        return userMedicine.getId();
    }
}