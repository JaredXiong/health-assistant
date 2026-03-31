package com.healthy.service.impl;

import com.healthy.entity.*;
import com.healthy.mapper.*;
import com.healthy.properties.DeepSeekProperties;
import com.healthy.service.AiChatService;
import com.healthy.vo.ChatResponseVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements AiChatService {

    private final RestTemplate restTemplate;
    private final DeepSeekProperties deepSeekProperties;
    private final ConversationMapper conversationMapper;
    private final HealthDataMapper healthDataMapper;
    private final HealthReportMapper healthReportMapper;
    private final UserMedicineMapper userMedicineMapper;
    private final MedicineInfoMapper medicineInfoMapper;

    private static final int MAX_HISTORY = 10;  // 保留最近10轮对话

    @Override
    @Transactional
    public ChatResponseVO chat(Long userId, String sessionId, String userMessage) {
        // 1. 如果sessionId为空，生成新会话ID
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString();
        }

        // 2. 保存用户消息
        Conversation userConv = new Conversation();
        userConv.setUserId(userId);
        userConv.setSessionId(sessionId);
        userConv.setRole("user");
        userConv.setContent(userMessage);
        conversationMapper.insert(userConv);

        // 3. 构建上下文（健康数据 + 历史对话）
        String systemPrompt = buildSystemPrompt(userId);
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));

        // 获取最近的历史对话（按时间升序）
        List<Conversation> history = conversationMapper.selectBySession(userId, sessionId);
        // 取最近 MAX_HISTORY 条（不包括刚插入的用户消息？这里取所有，但限制条数）
        history.stream()
                .limit(MAX_HISTORY * 2)  // 用户+助手各一条算一轮，所以乘以2
                .forEach(conv -> messages.add(Map.of(
                        "role", conv.getRole(),
                        "content", conv.getContent()
                )));

        // 4. 调用 DeepSeek API
        String aiReply = callDeepSeek(messages);

        // 5. 保存 AI 回复
        Conversation aiConv = new Conversation();
        aiConv.setUserId(userId);
        aiConv.setSessionId(sessionId);
        aiConv.setRole("assistant");
        aiConv.setContent(aiReply);
        conversationMapper.insert(aiConv);

        // 6. 返回
        ChatResponseVO vo = new ChatResponseVO();
        vo.setReply(aiReply);
        vo.setSessionId(sessionId);
        return vo;
    }

    /**
     * 构建系统提示词，包含用户健康数据
     */
    private String buildSystemPrompt(Long userId) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一位专业的健康顾问，请根据用户的健康数据提供个性化建议。\n");

        // 获取最新健康数据
        HealthData latestHealth = healthDataMapper.selectLatestByUserId(userId);
        if (latestHealth != null) {
            prompt.append("用户最新健康数据：\n");
            if (latestHealth.getHeartRate() != null)
                prompt.append("- 心率：").append(latestHealth.getHeartRate()).append(" 次/分\n");
            if (latestHealth.getSystolicBp() != null && latestHealth.getDiastolicBp() != null)
                prompt.append("- 血压：").append(latestHealth.getSystolicBp()).append("/").append(latestHealth.getDiastolicBp()).append(" mmHg\n");
            if (latestHealth.getBloodOxygen() != null)
                prompt.append("- 血氧：").append(latestHealth.getBloodOxygen()).append("%\n");
            if (latestHealth.getBloodSugar() != null)
                prompt.append("- 血糖：").append(latestHealth.getBloodSugar()).append(" mmol/L\n");
            if (latestHealth.getBodyTemperature() != null)
                prompt.append("- 体温：").append(latestHealth.getBodyTemperature()).append("℃\n");
            if (latestHealth.getRespiratoryRate() != null)
                prompt.append("- 呼吸频率：").append(latestHealth.getRespiratoryRate()).append(" 次/分\n");
            prompt.append("\n");
        }

        // 获取最新健康报告中的建议（可选）
        HealthReport latestReport = healthReportMapper.selectLatestByUserId(userId);
        if (latestReport != null && latestReport.getRecommendations() != null) {
            prompt.append("近期健康建议：").append(latestReport.getRecommendations()).append("\n\n");
        }

        // 获取用户正在服用的药品
        List<UserMedicine> medicines = userMedicineMapper.selectByUserId(userId, "USING");
        if (!medicines.isEmpty()) {
            prompt.append("当前正在服用的药品：\n");
            for (UserMedicine um : medicines) {
                MedicineInfo medicine = medicineInfoMapper.selectById(um.getMedicineId());
                if (medicine != null) {
                    prompt.append("- ").append(medicine.getName());
                    if (um.getDosagePerTime() != null)
                        prompt.append("，每次").append(um.getDosagePerTime());
                    if (um.getFrequency() != null)
                        prompt.append("，").append(um.getFrequency());
                    prompt.append("\n");
                }
            }
            prompt.append("\n");
        }

        prompt.append("请根据以上信息，友好、专业地回答用户的问题。");
        return prompt.toString();
    }

    /**
     * 调用 DeepSeek API
     */
    private String callDeepSeek(List<Map<String, String>> messages) {
        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", deepSeekProperties.getModel());
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 1000);

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
                // 解析响应，获取回复内容
                // 使用 fastjson 解析
                com.alibaba.fastjson.JSONObject json = com.alibaba.fastjson.JSON.parseObject(response.getBody());
                return json.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");
            } else {
                log.error("DeepSeek API 返回异常状态码: {}", response.getStatusCode());
                return "抱歉，我暂时无法回答，请稍后再试。";
            }
        } catch (Exception e) {
            log.error("调用 DeepSeek API 失败", e);
            return "系统繁忙，请稍后再试。";
        }
    }
}
