package com.healthy.service;

import com.healthy.dto.LoginDTO;
import com.healthy.vo.LoginResultVO;
import com.healthy.vo.UserVO;

public interface LoginService {

    /**
     * 微信小程序登录
     * @param loginDTO 登录参数
     * @return 登录结果（包含token和用户信息）
     */
    LoginResultVO wxLogin(LoginDTO loginDTO);

    /**
     * 根据用户ID获取用户信息
     * @param userId 用户ID
     * @return 用户信息
     */
    UserVO getUserInfo(Long userId);
}