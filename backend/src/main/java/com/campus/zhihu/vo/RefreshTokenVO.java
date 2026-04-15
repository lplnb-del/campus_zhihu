package com.campus.zhihu.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 刷新Token响应VO
 *
 * @author CampusZhihu Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenVO {

    /**
     * 新的访问Token
     */
    private String accessToken;

    /**
     * Token类型
     */
    private String tokenType;

    /**
     * 过期时间（秒）
     */
    private Long expiresIn;

    /**
     * 新的刷新Token
     */
    private String refreshToken;
}