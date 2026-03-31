package com.healthy.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.healthy.dto.HealthDataDTO;
import com.healthy.properties.DeepSeekProperties;
import com.healthy.service.AIGenerationService;
import com.healthy.vo.HealthReportVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIGenerationServiceImpl implements AIGenerationService {

    private final RestTemplate restTemplate;
    private final DeepSeekProperties deepSeekProperties;

    @Override
    public HealthReportVO generateReport(HealthDataDTO healthData) {
        // 构建 Prompt
        String prompt = buildPrompt(healthData);

        // 准备请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", deepSeekProperties.getModel());
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", "你是一位专业的健康顾问，请根据用户的健康数据生成风险因素和健康建议。返回格式必须是 JSON，包含 riskFactors（字符串数组）和 recommendations（字符串数组）。"),
                Map.of("role", "user", "content", prompt)
        ));
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 500);
        requestBody.put("response_format", Map.of("type", "json_object")); // 强制返回 JSON

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(deepSeekProperties.getApiKey());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    deepSeekProperties.getUrl(),
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                return parseResponse(response.getBody());
            } else {
                log.error("DeepSeek API 返回异常: {}", response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            log.error("调用 DeepSeek API 失败", e);
            return null;
        }
    }

    private String buildPrompt(HealthDataDTO dto) {
        return String.format(
                "用户健康数据如下：\n心率：%d 次/分\n收缩压：%d mmHg\n舒张压：%d mmHg\n血氧：%d %%\n血糖：%.1f mmol/L\n体温：%.1f ℃\n呼吸频率：%d 次/分\n\n请分析并生成风险因素和健康建议。",
                dto.getHeartRate(),
                dto.getSystolicBp(),
                dto.getDiastolicBp(),
                dto.getBloodOxygen(),
                dto.getBloodSugar(),
                dto.getBodyTemperature(),
                dto.getRespiratoryRate()
        );
    }

    private HealthReportVO parseResponse(String responseBody) {
        JSONObject json = JSON.parseObject(responseBody);
        JSONObject message = json.getJSONArray("choices").getJSONObject(0).getJSONObject("message");
        String content = message.getString("content");

        // 解析返回的 JSON 内容
        JSONObject result = JSON.parseObject(content);
        List<String> riskFactors = result.getJSONArray("riskFactors").toJavaList(String.class);
        List<String> recommendations = result.getJSONArray("recommendations").toJavaList(String.class);

        // 构造 HealthReportVO（其他字段可保持默认或由原规则生成）
        HealthReportVO report = new HealthReportVO();
        report.setRiskFactors(String.join("; ", riskFactors));
        report.setRecommendations(String.join("; ", recommendations));
        // 可选：让 AI 也生成整体评分、健康等级等
        // report.setOverallScore(...);
        return report;
    }
}