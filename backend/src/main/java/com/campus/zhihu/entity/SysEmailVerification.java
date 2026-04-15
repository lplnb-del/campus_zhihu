package com.campus.zhihu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 邮箱验证实体类
 *
 * @author CampusZhihu Team
 */
@Data
@TableName("sys_email_verification")
public class SysEmailVerification implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 邮箱地址
     */
    @TableField("email")
    private String email;

    /**
     * 验证Token
     */
    @TableField("token")
    private String token;

    /**
     * 验证类型（1: 注册验证, 2: 密码重置）
     */
    @TableField("type")
    private Integer type;

    /**
     * 是否已验证（0: 未验证, 1: 已验证）
     */
    @TableField("is_verified")
    private Integer isVerified;

    /**
     * 验证时间
     */
    @TableField("verified_time")
    private LocalDateTime verifiedTime;

    /**
     * 过期时间
     */
    @TableField("expire_time")
    private LocalDateTime expireTime;

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
     * 逻辑删除标记（0: 未删除, 1: 已删除）
     */
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;

    /**
     * 乐观锁版本号
     */
    @Version
    @TableField("version")
    private Integer version;

    /**
     * 验证类型常量
     */
    public static final int TYPE_REGISTER = 1;
    public static final int TYPE_PASSWORD_RESET = 2;

    /**
     * 验证状态常量
     */
    public static final int NOT_VERIFIED = 0;
    public static final int VERIFIED = 1;
}