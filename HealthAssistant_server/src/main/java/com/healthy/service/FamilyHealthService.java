package com.healthy.service;

import com.healthy.result.PageResult;
import com.healthy.vo.EmergencyContactVO;
import com.healthy.vo.FamilyReportVO;
import com.healthy.vo.HealthOverviewVO;

import java.time.LocalDate;
import java.util.List;

public interface FamilyHealthService {
    List<HealthOverviewVO> getHealthOverview(Long userId);
    PageResult getHealthHistory(Long userId, Long targetUserId, String type,
                                LocalDate startDate, LocalDate endDate,
                                Integer pageNum, Integer pageSize);
    FamilyReportVO generateFamilyReport(Long userId, String period);
    List<EmergencyContactVO> getEmergencyContacts(Long userId);
}