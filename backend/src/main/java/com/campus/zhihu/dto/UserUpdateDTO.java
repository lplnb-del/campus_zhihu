package com.campus.zhihu.dto;

import lombok.Data;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 用户更新 DTO
 *
 * @author CampusZhihu Team
 */
@Data
public class UserUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID（从 Token 中获取，不需要传参）
     */
    private Long userId;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 邮箱
     */
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@(.+)$", message = "邮箱格式不正确")
    private String email;

    /**
     * 昵称
     */
    @Size(max = 50, message = "昵称不能超过50个字符")
    private String nickname;

    /**
     * 个人简介
     */
    @Size(max = 500, message = "个人简介不能超过500个字符")
    private String bio;

    /**
     * 学校
     */
    @Size(max = 100, message = "学校名称不能超过100个字符")
    private String school;

    /**
     * 专业
     */
    @Size(max = 100, message = "专业名称不能超过100个字符")
    private String major;

    /**
     * 年级
     */
    @Size(max = 20, message = "年级不能超过20个字符")
    private String grade;

    /**
     * 学号（注册后不可修改，此字段仅用于展示）
     */
    private String studentId;
}
