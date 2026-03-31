package com.healthy.interceptor;

import com.healthy.constant.JwtClaimsConstant;
import com.healthy.context.BaseContext;
import com.healthy.properties.JwtProperties;
import com.healthy.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;

/**
 * jwt令牌校验的拦截器
 */
@Component
@Slf4j
public class JwtTokenUserInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 校验jwt
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        //判断当前拦截到的是Controller的方法还是其他资源
        if (!(handler instanceof HandlerMethod)) {
            //当前拦截到的不是动态方法，直接放行
            return true;
        }

        //1、从请求头中获取令牌
        String tokenName = jwtProperties.getUserTokenName();
        String token = request.getHeader(tokenName);

        log.info("JWT拦截器 - 请求路径: {}", request.getRequestURI());
        log.info("JWT拦截器 - Token头名称: {}", tokenName);
        log.info("JWT拦截器 - Token值: {}", token);
        log.info("JWT拦截器 - 所有请求头: {}", getHeadersInfo(request));

        //2、校验令牌
        try {
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            Long userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
            String role = claims.get(JwtClaimsConstant.ROLE, String.class); // 获取角色
            Long familyId = claims.get(JwtClaimsConstant.FAMILY_ID, Long.class); // 获取家庭ID

            BaseContext.setCurrentId(userId);
            // 如果需要，可以扩展 BaseContext 存储 role 和 familyId
            // BaseContext.setCurrentRole(role);
            // BaseContext.setCurrentFamilyId(familyId);

            log.info("当前用户id：{}，角色：{}，家庭ID：{}", userId, role, familyId);
            return true;
        } catch (Exception ex) {
            log.error("JWT校验失败: {}", ex.getMessage());
            response.setStatus(401);
            return false;
        }
    }

    private String getHeadersInfo(HttpServletRequest request) {
        StringBuilder headers = new StringBuilder();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            headers.append(headerName).append(": ").append(headerValue).append("; ");
        }
        return headers.toString();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 请求结束后清除 ThreadLocal 中的用户 ID，避免内存泄漏和线程复用问题
        BaseContext.removeCurrentId();
    }
}