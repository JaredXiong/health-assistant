package com.healthy.service;

import com.healthy.dto.PrescriptionRecordDTO;
import com.healthy.result.PageResult;
import com.healthy.vo.PrescriptionRecordVO;

import java.util.Map;

public interface PrescriptionService {
    /**
     * 获取当前用户就诊记录列表（分页）
     */
    PageResult getRecords(Integer page, Integer pageSize);

    /**
     * 获取就诊记录详情（包含明细）
     */
    PrescriptionRecordVO getRecordDetail(Long recordId);

    /**
     * 新增就诊记录
     */
    Long addRecord(PrescriptionRecordDTO dto);

    /**
     * 更新就诊记录（可同时修改明细）
     */
    void updateRecord(Long recordId, PrescriptionRecordDTO dto);

    /**
     * 删除就诊记录（连带删除明细）
     */
    void deleteRecord(Long recordId);

    Map<String, Object> getStatistics();
}