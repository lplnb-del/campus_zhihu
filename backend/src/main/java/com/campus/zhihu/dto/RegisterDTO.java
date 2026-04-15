package com.campus.zhihu.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

/**
 * 用户注册 DTO
 *
 * @author CampusZhihu Team
 */
@Data
public class RegisterDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20个字符之间")
    @Pattern(
        regexp = "^[a-zA-Z0-9_]+$",
        message = "用户名只能包含字母、数字和下划线"
    )
    private String username;

    /**
     * 邮箱
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度必须在6-32个字符之间")
    private String password;

    /**
     * 学号（可选）
     */
    @Pattern(
        regexp = "^[0-9]{6,12}$",
        message = "学号格式不正确，应为6-12位数字"
    )
    private String studentId;

    /**
     * 真实姓名（可选）
     */
    @Size(max = 50, message = "真实姓名长度不能超过50个字符")
    private String realName;

    /**
     * 专业（可选）
     */
    @Size(max = 100, message = "专业长度不能超过100个字符")
    private String major;

    /**
     * 年级（可选）
     */
    @Size(max = 20, message = "年级长度不能超过20个字符")
    private String grade;

    /**
     * 头像（可选）
     */
    private String avatar;
}
