package com.campus.zhihu.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论 VO
 * 用于向前端返回评论详细信息
 *
 * @author CampusZhihu Team
 */
@Data
public class CommentVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 评论ID
     */
    private Long id;

    /**
     * 评论用户ID
     */
    private Long userId;

    /**
     * 评论用户信息
     */
    private UserVO user;

    /**
     * 目标类型：1-问题，2-回答
     */
    private Integer targetType;

    /**
     * 目标类型描述
     */
    private String targetTypeText;

    /**
     * 目标ID（问题ID或回答ID）
     */
    private Long targetId;

    /**
     * 父评论ID（0表示顶级评论）
     */
    private Long parentId;

    /**
     * 回复的用户ID
     */
    private Long replyToUserId;

    /**
     * 回复的用户信息
     */
    private UserVO replyToUser;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 点赞次数
     */
    private Integer likeCount;

    /**
     * 状态：0-审核中，1-已发布，2-已屏蔽
     */
    private Integer status;

    /**
     * 状态描述
     */
    private String statusText;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    /**
     * 当前用户是否已点赞
     */
    private Boolean hasLiked;

    /**
     * 子评论列表（如果是顶级评论）
     */
    private List<CommentVO> replies;

    /**
     * 子评论数量
     */
    private Integer replyCount;

    /**
     * 目标标题（问题标题或回答内容摘要，用于列表展示）
     */
    private String targetTitle;
}
