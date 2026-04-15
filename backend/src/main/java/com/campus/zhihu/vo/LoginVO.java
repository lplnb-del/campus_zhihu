package com.campus.zhihu.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 登录响应 VO
 * 返回给前端的登录结果（包含 Token 和用户信息）
 *
 * @author CampusZhihu Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * JWT Token
     */
    private String token;

    /**
     * Token 类型（默认：Bearer）
     */
    private String tokenType = "Bearer";

    /**
     * Token 过期时间（时间戳，毫秒）
     */
    private Long expiresIn;

    /**
     * 用户信息
     */
    private UserVO userInfo;

    /**
     * 刷新Token（用于获取新的Access Token）
     */
    private String refreshToken;

    /**
     * 构造函数（简化版，只传 token 和 userInfo）
     */
    public LoginVO(String token, UserVO userInfo) {
        this.token = token;
        this.userInfo = userInfo;
        this.tokenType = "Bearer";
    }

    /**
     * 构造函数（完整版）
     */
    public LoginVO(String token, Long expiresIn, UserVO userInfo) {
        this.token = token;
        this.expiresIn = expiresIn;
        this.userInfo = userInfo;
        this.tokenType = "Bearer";
    }
}
