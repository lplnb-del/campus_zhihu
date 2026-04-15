package com.campus.zhihu.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户信息 VO
 * 返回给前端的用户信息（不包含密码等敏感信息）
 *
 * @author CampusZhihu Team
 */
@Data
public class UserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 个人简介
     */
    private String bio;

    /**
     * 积分
     */
    private Integer points;

    /**
     * 学校
     */
    private String school;

    /**
     * 专业
     */
    private String major;

    /**
     * 年级
     */
    private String grade;

    /**
     * 学号
     */
    private String studentId;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 提问数量（扩展字段，可选）
     */
    private Integer questionCount;

    /**
     * 回答数量（扩展字段，可选）
     */
    private Integer answerCount;

    /**
     * 被采纳数量（扩展字段，可选）
     */
    private Integer acceptedCount;
}
