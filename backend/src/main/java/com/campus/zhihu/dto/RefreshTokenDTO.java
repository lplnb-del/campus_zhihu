package com.campus.zhihu.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 刷新Token请求DTO
 *
 * @author CampusZhihu Team
 */
@Data
public class RefreshTokenDTO {

    /**
     * 刷新Token
     */
    @NotBlank(message = "刷新Token不能为空")
    private String refreshToken;
}