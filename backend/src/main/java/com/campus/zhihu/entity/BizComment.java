package com.campus.zhihu.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 评论实体类
 * 对应数据库表：biz_comment
 *
 * @author CampusZhihu Team
 */
@Data
@TableName("biz_comment")
public class BizComment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 评论ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 评论用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 目标类型：1-问题，2-回答
     */
    @TableField("target_type")
    private Integer targetType;

    /**
     * 目标ID（问题ID或回答ID）
     */
    @TableField("target_id")
    private Long targetId;

    /**
     * 父评论ID（0表示顶级评论）
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 评论内容
     */
    @TableField("content")
    private String content;

    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;

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
     * 目标类型常量：问题
     */
    public static final int TARGET_TYPE_QUESTION = 1;

    /**
     * 目标类型常量：回答
     */
    public static final int TARGET_TYPE_ANSWER = 2;

    /**
     * 状态常量：审核中
     */
    public static final int STATUS_REVIEWING = 0;

    /**
     * 状态常量：已发布
     */
    public static final int STATUS_PUBLISHED = 1;

    /**
     * 状态常量：已屏蔽
     */
    public static final int STATUS_BLOCKED = 2;

    /**
     * 顶级评论的父ID
     */
    public static final long TOP_LEVEL_PARENT_ID = 0L;
}
