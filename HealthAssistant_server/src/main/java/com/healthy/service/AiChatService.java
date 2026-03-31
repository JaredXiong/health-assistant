package com.healthy.service;

import com.healthy.vo.ChatResponseVO;

public interface AiChatService {
    /**
     * 对话处理（保存历史并调用AI）
     * @param userId 当前用户ID
     * @param sessionId 会话ID（为空则新生成）
     * @param userMessage 用户消息
     * @return AI回复及会话ID
     */
    ChatResponseVO chat(Long userId, String sessionId, String userMessage);
}
