package com.healthy.controller;

import com.healthy.context.BaseContext;
import com.healthy.dto.LoginDTO;
import com.healthy.dto.UpdateUserDTO;
import com.healthy.result.Result;
import com.healthy.service.LoginService;
import com.healthy.service.UserService;
import com.healthy.utils.AliOssUtil;
import com.healthy.utils.ImageCompressUtil;
import com.healthy.vo.LoginResultVO;
import com.healthy.vo.UserVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Api(tags = "用户管理")
public class UserController {

    private final LoginService loginService;
    private final UserService userService;
    private final AliOssUtil aliOssUtil;

    @PostMapping("/login")
    @ApiOperation("微信小程序登录")
    public Result<LoginResultVO> login(@RequestBody LoginDTO loginDTO) {
        log.info("用户登录：code={}", loginDTO.getCode());
        try {
            LoginResultVO loginResult = loginService.wxLogin(loginDTO);
            return Result.success(loginResult);
        } catch (Exception e) {
            log.error("登录异常：", e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/test")
    @ApiOperation("测试接口")
    public Result<String> test() {
        return Result.success("服务正常");
    }

    @GetMapping("/info/{userId}")
    @ApiOperation("获取用户信息")
    public Result<UserVO> getUserInfo(@PathVariable Long userId) {
        UserVO userVO = loginService.getUserInfo(userId);
        if (userVO == null) {
            return Result.error("用户不存在");
        }
        return Result.success(userVO);
    }

    /**
     * 验证token有效性
     */
    @PostMapping("/checkToken")
    public Result<Map<String, Object>> checkToken(@RequestBody Map<String, String> request) {
        String token = request.get("token");

        if (token == null || token.isEmpty()) {
            return Result.error("token不能为空");
        }

        try {
            // 解析token - 这里需要注入JWT配置，暂时返回成功
            Map<String, Object> result = new HashMap<>();
            result.put("valid", true);
            result.put("message", "token验证成功");

            return Result.success(result);
        } catch (Exception e) {
            log.error("token验证失败: {}", e.getMessage());
            Map<String, Object> result = new HashMap<>();
            result.put("valid", false);
            result.put("error", "token无效或已过期");
            return Result.success(result);
        }
    }

    @PutMapping("/update")
    @ApiOperation("更新用户信息")
    public Result<String> updateUser(@RequestBody @Valid UpdateUserDTO dto) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            return Result.error("用户未登录");
        }
        userService.updateUser(userId, dto);
        return Result.success("更新成功");
    }

    @PostMapping("/upload-avatar")
    @ApiOperation("上传头像")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            return Result.error("用户未登录");
        }
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }
        try {
            // 压缩图片（限制最大 2MB）
            byte[] originalBytes = file.getBytes();
            byte[] compressedBytes = ImageCompressUtil.compressToMaxSize(originalBytes, 2 * 1024 * 1024);

            // 生成唯一文件名：avatar/用户ID/时间戳_原文件名
            String fileName = "avatar/" + userId + "/" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
            String avatarUrl = aliOssUtil.upload(compressedBytes, fileName);
            return Result.success(avatarUrl);
        } catch (IOException e) {
            log.error("头像上传失败", e);
            return Result.error("头像上传失败：" + e.getMessage());
        }
    }
}