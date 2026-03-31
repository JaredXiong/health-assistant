package com.healthy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.healthy.context.BaseContext;
import com.healthy.dto.PrescriptionItemDTO;
import com.healthy.dto.PrescriptionRecordDTO;
import com.healthy.entity.PrescriptionItem;
import com.healthy.entity.PrescriptionRecord;
import com.healthy.exception.BaseException;
import com.healthy.mapper.PrescriptionItemMapper;
import com.healthy.mapper.PrescriptionRecordMapper;
import com.healthy.result.PageResult;
import com.healthy.service.PrescriptionService;
import com.healthy.vo.PrescriptionItemVO;
import com.healthy.vo.PrescriptionRecordVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrescriptionServiceImpl implements PrescriptionService {

    private final PrescriptionRecordMapper recordMapper;
    private final PrescriptionItemMapper itemMapper;

    @Override
    public PageResult getRecords(Integer page, Integer pageSize) {
        Long userId = BaseContext.getCurrentId();
        Page<PrescriptionRecord> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<PrescriptionRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PrescriptionRecord::getUserId, userId)
                .orderByDesc(PrescriptionRecord::getVisitDate);
        IPage<PrescriptionRecord> recordPage = recordMapper.selectPage(pageParam, wrapper);

        List<PrescriptionRecordVO> voList = recordPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return new PageResult(recordPage.getTotal(), voList);
    }

    @Override
    public PrescriptionRecordVO getRecordDetail(Long recordId) {
        Long userId = BaseContext.getCurrentId();
        PrescriptionRecord record = recordMapper.selectById(recordId);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BaseException("就诊记录不存在或无权限");
        }
        return convertToVO(record);
    }

    @Override
    @Transactional
    public Long addRecord(PrescriptionRecordDTO dto) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new BaseException("用户未登录");
        }
        PrescriptionRecord record = new PrescriptionRecord();
        BeanUtils.copyProperties(dto, record);
        record.setUserId(userId);
        recordMapper.insert(record);

        if (!CollectionUtils.isEmpty(dto.getItems())) {
            for (PrescriptionItemDTO itemDTO : dto.getItems()) {
                PrescriptionItem item = new PrescriptionItem();
                BeanUtils.copyProperties(itemDTO, item);
                item.setRecordId(record.getId());
                itemMapper.insert(item);
            }
        }
        return record.getId();
    }

    @Override
    @Transactional
    public void updateRecord(Long recordId, PrescriptionRecordDTO dto) {
        Long userId = BaseContext.getCurrentId();
        PrescriptionRecord record = recordMapper.selectById(recordId);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BaseException("就诊记录不存在或无权限");
        }
        // 更新主表
        BeanUtils.copyProperties(dto, record, "id", "userId", "createdAt");
        record.setId(recordId);
        recordMapper.updateById(record);

        // 更新明细：先删除原有明细，再重新插入
        LambdaQueryWrapper<PrescriptionItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PrescriptionItem::getRecordId, recordId);
        itemMapper.delete(wrapper);

        if (!CollectionUtils.isEmpty(dto.getItems())) {
            for (PrescriptionItemDTO itemDTO : dto.getItems()) {
                PrescriptionItem item = new PrescriptionItem();
                BeanUtils.copyProperties(itemDTO, item);
                item.setRecordId(recordId);
                itemMapper.insert(item);
            }
        }
    }

    @Override
    @Transactional
    public void deleteRecord(Long recordId) {
        Long userId = BaseContext.getCurrentId();
        PrescriptionRecord record = recordMapper.selectById(recordId);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BaseException("就诊记录不存在或无权限");
        }
        recordMapper.deleteById(recordId);
        LambdaQueryWrapper<PrescriptionItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PrescriptionItem::getRecordId, recordId);
        itemMapper.delete(wrapper);
    }

    private PrescriptionRecordVO convertToVO(PrescriptionRecord record) {
        PrescriptionRecordVO vo = new PrescriptionRecordVO();
        BeanUtils.copyProperties(record, vo);
        List<PrescriptionItem> items = itemMapper.selectByRecordId(record.getId());
        List<PrescriptionItemVO> itemVOs = items.stream().map(this::convertItemToVO).collect(Collectors.toList());
        vo.setItems(itemVOs);
        return vo;
    }

    private PrescriptionItemVO convertItemToVO(PrescriptionItem item) {
        PrescriptionItemVO vo = new PrescriptionItemVO();
        BeanUtils.copyProperties(item, vo);
        return vo;
    }

    @Override
    public Map<String, Object> getStatistics() {
        Long userId = BaseContext.getCurrentId();
        // 按月份统计记录数
        List<Map<String, Object>> monthlyCount = recordMapper.getMonthlyCount(userId);
        // 常见药品统计（通过处方明细）
        List<Map<String, Object>> commonMedicines = itemMapper.getCommonMedicines(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("monthlyCount", monthlyCount);
        result.put("commonMedicines", commonMedicines);
        return result;
    }
}