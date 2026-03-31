package com.healthy.service;

import com.healthy.dto.HealthDataDTO;

public interface AsyncReportService {
    void generateReportAsync(Long userId, Long healthDataId, HealthDataDTO dto);
}
