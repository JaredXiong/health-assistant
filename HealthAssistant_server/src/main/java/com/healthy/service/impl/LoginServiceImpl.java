package com.healthy.service.impl;

import com.healthy.constant.JwtClaimsConstant;
import com.healthy.dto.LoginDTO;
import com.healthy.entity.Family;
import com.healthy.entity.FamilyMember;
import com.healthy.entity.Users;
import com.healthy.exception.UserException;
import com.healthy.mapper.FamilyMapper;
import com.healthy.mapper.FamilyMemberMapper;
import com.healthy.mapper.UserMapper;
import com.healthy.properties.JwtProperties;
import com.healthy.service.LoginService;
import com.healthy.service.WechatService;
import com.healthy.utils.JwtUtil;
import com.healthy.vo.LoginResultVO;
import com.healthy.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class LoginServiceImpl implements LoginService {

    private final UserMapper userMapper;
    private final JwtProperties jwtProperties;
    private final WechatService wechatService;     // 注入 WechatService
    private final FamilyMemberMapper familyMemberMapper; // 新增
    private final FamilyMapper familyMapper;             // 新增

//    @Override
//    public LoginResultVO wxLogin(LoginDTO loginDTO) {
//        log.info("微信登录请求：code={}", loginDTO.getCode());
//
//        // 1. 参数校验
//        if (loginDTO == null || !StringUtils.hasText(loginDTO.getCode())) {
//            throw new UserException("登录参数错误：code不能为空");
//        }
//
//        // 2. 模拟获取openid（暂时避免调用微信接口）
//        String openid = "test_openid_" + loginDTO.getCode().hashCode();
//        log.info("模拟openid：{}", openid);
//
//        // 3. 查询用户是否存在
//        Users user = userMapper.getByOpenid(openid);
//
//        // 4. 新用户自动注册
//        if (user == null) {
//            user = Users.builder()
//                    .openid(openid)
//                    .nickname(StringUtils.hasText(loginDTO.getNickname()) ?
//                            loginDTO.getNickname() : "微信用户")
//                    .name(null)
//                    .phone(null)
//                    .build();
//
//            userMapper.insert(user);
//
//            // 重新查询获取ID
//            user = userMapper.getByOpenid(openid);
//            log.info("新用户注册成功，用户ID：{}", user.getId());
//        } else {
//            log.info("老用户登录，用户ID：{}", user.getId());
//        }
//
//        // 5. 生成JWT token
//        String token = generateToken(user.getId());
//
//        // 6. 构建返回结果
//        UserVO userVO = convertToUserVO(user);
//        LoginResultVO loginResult = LoginResultVO.builder()
//                .token(token)
//                .userInfo(userVO)
//                .build();
//
//        log.info("登录成功，用户ID：{}, token生成成功", user.getId());
//        return loginResult;
//    }

    @Override
    public LoginResultVO wxLogin(LoginDTO loginDTO) {
        log.info("微信登录请求：code={}", loginDTO.getCode());

        // 1. 参数校验
        if (loginDTO == null || !StringUtils.hasText(loginDTO.getCode())) {
            throw new UserException("登录参数错误：code不能为空");
        }

        // 2. 调用微信接口获取 openid
        String openid;
        if ("999999".equals(loginDTO.getCode())) {
            // 测试 code，使用固定 openid（可配置）
            openid = "test_openid_001"; // 建议从配置读取，如 @Value("${test.openid}")
            log.info("使用测试 openid: {}", openid);
        } else {
            try {
                openid = wechatService.code2Openid(loginDTO.getCode());
            } catch (Exception e) {
                log.error("获取openid失败", e);
                throw new UserException("微信登录失败：" + e.getMessage());
            }
        }
        log.info("获取到真实openid：{}", openid);

        // 3. 查询用户是否存在
        Users user = userMapper.getByOpenid(openid);

        // 4. 新用户自动注册
        if (user == null) {
            user = Users.builder()
                    .openid(openid)
                    .nickname(StringUtils.hasText(loginDTO.getNickname()) ?
                            loginDTO.getNickname() : "微信用户")
                    .name(null)
                    .phone(null)
                    .build();
            userMapper.insert(user);
            user = userMapper.getByOpenid(openid);
            log.info("新用户注册成功，用户ID：{}", user.getId());
        } else {
            log.info("老用户登录，用户ID：{}", user.getId());
        }

        // 5. 查询用户角色和家庭信息
        String role = null;
        Long familyId = null;
        String familyName = null;
        FamilyMember member = familyMemberMapper.selectActiveByUserId(user.getId());
        if (member != null) {
            role = member.getRole();
            familyId = member.getFamilyId();
            Family family = familyMapper.selectById(familyId);
            if (family != null) {
                familyName = family.getName();
            }
        }

        // 6. 生成JWT token，并在claims中添加角色和家庭ID
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        if (role != null) {
            claims.put(JwtClaimsConstant.ROLE, role);
        }
        if (familyId != null) {
            claims.put(JwtClaimsConstant.FAMILY_ID, familyId);
        }
        String token = JwtUtil.createJWT(
                jwtProperties.getUserSecretKey(),
                jwtProperties.getUserTtl(),
                claims
        );

        // 7. 构建返回结果
        UserVO userVO = convertToUserVO(user);
        LoginResultVO loginResult = LoginResultVO.builder()
                .token(token)
                .userInfo(userVO)
                .role(role)
                .familyId(familyId)
                .familyName(familyName)
                .build();

        log.info("登录成功，用户ID：{}，角色：{}，token生成成功", user.getId(), role);
        return loginResult;
    }

    @Override
    public UserVO getUserInfo(Long userId) {
        Users user = userMapper.getById(userId);
        if (user == null) {
            log.warn("用户不存在，userId={}", userId);
            return null;
        }
        return convertToUserVO(user);
    }

    /**
     * 生成JWT token
     */
    private String generateToken(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, userId);

        String token = JwtUtil.createJWT(
                jwtProperties.getUserSecretKey(),
                jwtProperties.getUserTtl(),
                claims
        );

        return token;
    }

    /**
     * 转换实体为VO
     */
    private UserVO convertToUserVO(Users user) {
        return UserVO.builder()
                .id(user.getId())
                .openid(user.getOpenid())
                .name(user.getName())
                .phone(user.getPhone())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
}