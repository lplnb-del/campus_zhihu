package com.campus.zhihu.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 问题实体类
 * 对应数据库表：biz_question
 *
 * @author CampusZhihu Team
 */
@Data
@TableName("biz_question")
public class BizQuestion implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 问题ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 提问用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 问题标题
     */
    @TableField("title")
    private String title;

    /**
     * 问题内容
     */
    @TableField("content")
    private String content;

    /**
     * 图片URLs（逗号分隔）
     */
    @TableField("images")
    private String images;

    /**
     * 浏览次数
     */
    @TableField("view_count")
    private Integer viewCount;

    /**
     * 回答数量
     */
    @TableField("answer_count")
    private Integer answerCount;

    /**
     * 收藏次数
     */
    @TableField("collection_count")
    private Integer collectionCount;

    /**
     * 点赞次数
     */
    @TableField("like_count")
    private Integer likeCount;

    /**
     * 悬赏积分
     */
    @TableField("reward_points")
    private Integer rewardPoints;

    /**
     * 状态：0-草稿，1-已发布，2-已关闭
     */
    @TableField("status")
    private Integer status;

    /**
     * 是否为草稿：0-否，1-是
     */
    @TableField("is_draft")
    private Integer isDraft;

    /**
     * 是否已解决：0-未解决，1-已解决
     */
    @TableField("is_solved")
    private Integer isSolved;

    /**
     * 被采纳的回答ID
     */
    @TableField("accepted_answer_id")
    private Long acceptedAnswerId;

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
     * 状态常量：草稿
     */
    public static final int STATUS_DRAFT = 0;

    /**
     * 状态常量：已发布
     */
    public static final int STATUS_PUBLISHED = 1;

    /**
     * 状态常量：已关闭
     */
    public static final int STATUS_CLOSED = 2;

    /**
     * 解决状态常量：未解决
     */
    public static final int RESOLVED_NO = 0;

    /**
     * 解决状态常量：已解决
     */
    public static final int RESOLVED_YES = 1;
}
