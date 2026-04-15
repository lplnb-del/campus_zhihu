package com.campus.zhihu.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 回答实体类
 * 对应数据库表：biz_answer
 *
 * @author CampusZhihu Team
 */
@Data
@TableName("biz_answer")
public class BizAnswer implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 回答ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 问题ID
     */
    @TableField("question_id")
    private Long questionId;

    /**
     * 回答用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 回答内容
     */
    @TableField("content")
    private String content;

    /**
     * 图片URLs（逗号分隔）
     */
    @TableField("images")
    private String images;

    /**
     * 点赞次数
     */
    @TableField("like_count")
    private Integer likeCount;

    /**
     * 评论次数
     */
    @TableField("comment_count")
    private Integer commentCount;

    /**
     * 是否被采纳：0-未采纳，1-已采纳
     */
    @TableField("is_accepted")
    private Integer isAccepted;

    /**
     * 状态：0-草稿，1-已发布
     */
    @TableField("status")
    private Integer status;

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
     * 采纳状态常量：未采纳
     */
    public static final int NOT_ACCEPTED = 0;

    /**
     * 采纳状态常量：已采纳
     */
    public static final int ACCEPTED = 1;

    /**
     * 状态常量：草稿
     */
    public static final int STATUS_DRAFT = 0;

    /**
     * 状态常量：已发布
     */
    public static final int STATUS_PUBLISHED = 1;
}
