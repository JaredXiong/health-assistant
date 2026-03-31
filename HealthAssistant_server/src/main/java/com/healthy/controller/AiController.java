package com.healthy.controller;

import com.healthy.context.BaseContext;
import com.healthy.dto.ChatRequestDTO;
import com.healthy.result.Result;
import com.healthy.service.AiChatService;
import com.healthy.vo.ChatResponseVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/ai")
@Slf4j
@RequiredArgsConstructor
@Api(tags = "AI健康助手")
public class AiController {

    private final AiChatService aiChatService;

    @PostMapping("/chat")
    @ApiOperation("对话接口")
    public Result<ChatResponseVO> chat(@RequestBody @Valid ChatRequestDTO dto) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            return Result.error("用户未登录");
        }
        ChatResponseVO response = aiChatService.chat(userId, dto.getSessionId(), dto.getMessage());
        return Result.success(response);
    }
}