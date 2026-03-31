package com.healthy.service;

import com.healthy.dto.HealthDataDTO;
import com.healthy.vo.HealthReportVO;

public interface AIGenerationService {
    HealthReportVO generateReport(HealthDataDTO healthData);
}
