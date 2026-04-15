package com.campus.zhihu.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 用户实体类
 * 对应数据库表：sys_user
 *
 * @author CampusZhihu Team
 */
@Data
@TableName("sys_user")
public class SysUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 邮箱
     */
    @TableField("email")
    private String email;

    /**
     * 密码(BCrypt加密)
     */
    @TableField("password")
    private String password;

    /**
     * 积分
     */
    @TableField("points")
    private Integer points;

    /**
     * 学校
     */
    @TableField("school")
    private String school;

    /**
     * 专业
     */
    @TableField("major")
    private String major;

    /**
     * 年级
     */
    @TableField("grade")
    private String grade;

    /**
     * 学号
     */
    @TableField("student_id")
    private String studentId;

    /**
     * 昵称
     */
    @TableField("nickname")
    private String nickname;

    /**
     * 个人简介
     */
    @TableField("bio")
    private String bio;

    /**
     * 头像URL
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除(0:未删除 1:已删除)
     */
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;

    /**
     * 乐观锁版本号（用于并发控制，防止积分扣减出现负数）
     */
    @Version
    @TableField("version")
    private Integer version;
}
