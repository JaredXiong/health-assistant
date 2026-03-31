package com.healthy.service;

import com.healthy.entity.Reminder;

public interface WechatService {
    /**
     * 用 code 换取 openid
     */
    String code2Openid(String code);

    /**
     * 获取 access_token（带缓存）
     */
    String getAccessToken();

    /**
     * 发送订阅消息
     * @return true 表示成功，false 表示失败
     */
    boolean sendSubscribeMessage(Reminder reminder, String accessToken);
}