package com.campus.zhihu.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 验证邮箱DTO
 *
 * @author CampusZhihu Team
 */
@Data
public class VerifyEmailDTO {

    /**
     * 验证Token
     */
    @NotBlank(message = "验证Token不能为空")
    private String token;
}