package com.healthy.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.healthy.entity.MedicineInfo;
import com.healthy.entity.Reminder;
import com.healthy.entity.UserMedicine;
import com.healthy.mapper.MedicineInfoMapper;
import com.healthy.mapper.UserMedicineMapper;
import com.healthy.properties.WeChatProperties;
import com.healthy.service.WechatService;
import com.healthy.utils.HttpClientUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WechatServiceImpl implements WechatService {

    private final WeChatProperties weChatProperties;
    //private final StringRedisTemplate redisTemplate;
    private final RestTemplate restTemplate;
    private final UserMedicineMapper userMedicineMapper;
    private final MedicineInfoMapper medicineInfoMapper;

    // 内存缓存(新增测试)
    private String accessTokenCache;
    private long expireTime;

    private static final String ACCESS_TOKEN_KEY = "wechat:access_token";
    private static final long TOKEN_EXPIRE_SECONDS = 7000; // 略小于2小时（7200秒）

    @Override
    public String code2Openid(String code) {
        String url = "https://api.weixin.qq.com/sns/jscode2session";
        Map<String, String> params = new HashMap<>();
        params.put("appid", weChatProperties.getAppid());
        params.put("secret", weChatProperties.getSecret());
        params.put("js_code", code);
        params.put("grant_type", "authorization_code");

        String result = HttpClientUtil.doGet(url, params);
        log.info("jscode2session 返回: {}", result);

        JSONObject json = JSON.parseObject(result);
        String openid = json.getString("openid");
        if (openid == null) {
            String errMsg = json.getString("errmsg");
            throw new RuntimeException("换取openid失败：" + errMsg);
        }
        return openid;
    }

    @Override
    public String getAccessToken() {
//        // 先从Redis获取
//        String token = redisTemplate.opsForValue().get(ACCESS_TOKEN_KEY);
//        if (token != null) {
//            return token;
//        }
//
//        // 不存在则请求微信
//        String url = "https://api.weixin.qq.com/cgi-bin/token";
//        Map<String, String> params = new HashMap<>();
//        params.put("grant_type", "client_credential");
//        params.put("appid", weChatProperties.getAppid());
//        params.put("secret", weChatProperties.getSecret());
//
//        String result = HttpClientUtil.doGet(url, params);
//        log.info("获取access_token返回: {}", result);
//
//        JSONObject json = JSON.parseObject(result);
//        token = json.getString("access_token");
//        if (token == null) {
//            String errMsg = json.getString("errmsg");
//            throw new RuntimeException("获取access_token失败：" + errMsg);
//        }
//
//        // 存入Redis，过期时间比微信实际过期时间略短
//        redisTemplate.opsForValue().set(ACCESS_TOKEN_KEY, token, TOKEN_EXPIRE_SECONDS, TimeUnit.SECONDS);
//        return token;
        // 1. 检查内存缓存
        if (accessTokenCache != null && System.currentTimeMillis() < expireTime) {
            return accessTokenCache;
        }

        // 2. 请求微信获取新 token
        String url = "https://api.weixin.qq.com/cgi-bin/token";
        Map<String, String> params = new HashMap<>();
        params.put("grant_type", "client_credential");
        params.put("appid", weChatProperties.getAppid());
        params.put("secret", weChatProperties.getSecret());

        String result = HttpClientUtil.doGet(url, params);
        log.info("获取access_token返回: {}", result);

        JSONObject json = JSON.parseObject(result);
        String token = json.getString("access_token");
        if (token == null) {
            String errMsg = json.getString("errmsg");
            throw new RuntimeException("获取access_token失败：" + errMsg);
        }

        // 3. 更新内存缓存
        int expiresIn = json.getIntValue("expires_in");
        accessTokenCache = token;
        expireTime = System.currentTimeMillis() + (expiresIn - 200) * 1000; // 提前200秒过期
        return token;
    }

    @Override
    public boolean sendSubscribeMessage(Reminder reminder, String accessToken) {
        // 获取药品名称
        String medicineName = "未知药品";
        if (reminder.getUserMedicineId() != null) {
            UserMedicine userMedicine = userMedicineMapper.selectById(reminder.getUserMedicineId());
            if (userMedicine != null) {
                MedicineInfo medicine = medicineInfoMapper.selectById(userMedicine.getMedicineId());
                if (medicine != null) {
                    medicineName = medicine.getName();
                }
            }
        }

        String url = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + accessToken;

        Map<String, Object> data = new HashMap<>();
        data.put("thing5", Map.of("value", medicineName));
        data.put("time10", Map.of("value", reminder.getReminderTime().format(DateTimeFormatter.ofPattern("HH:mm"))));
        data.put("time6", Map.of("value", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"))));
        data.put("short_thing7", Map.of("value", reminder.getDosage() != null ? reminder.getDosage() : ""));
        data.put("thing11", Map.of("value", "请按时服药"));

        Map<String, Object> body = new HashMap<>();
        body.put("touser", reminder.getOpenid());   // openid 来自临时字段
        body.put("template_id", weChatProperties.getTemplateId());
        body.put("page", "pages/index/index");
        body.put("data", data);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(JSON.toJSONString(body), headers);

        try {
            String response = restTemplate.postForObject(url, request, String.class);
            log.info("订阅消息推送返回: {}", response);
            JSONObject json = JSON.parseObject(response);
            int errcode = json.getIntValue("errcode");
            return errcode == 0;
        } catch (Exception e) {
            log.error("调用微信订阅消息接口异常", e);
            return false;
        }
    }
}