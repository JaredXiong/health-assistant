package com.healthy.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResultVO {
    private String token;
    private UserVO userInfo;
    private String role;      // 角色：PARENT 或 CHILD，未加入家庭则为 null
    private Long familyId;    // 家庭ID，未加入家庭则为 null
    private String familyName; // 家庭名称（可选）
}