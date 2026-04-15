package com.campus.zhihu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 评论发布 DTO
 * 用于接收前端提交的评论发布数据
 *
 * @author CampusZhihu Team
 */
@Data
public class CommentPublishDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 目标类型：1-问题，2-回答
     */
    @NotNull(message = "目标类型不能为空")
    private Integer targetType;

    /**
     * 目标ID（问题ID或回答ID）
     */
    @NotNull(message = "目标ID不能为空")
    private Long targetId;

    /**
     * 父评论ID（0表示顶级评论）
     */
    private Long parentId;

    /**
     * 回复的用户ID（回复评论时使用）
     */
    private Long replyToUserId;

    /**
     * 评论内容
     */
    @NotBlank(message = "评论内容不能为空")
    @Size(min = 1, max = 500, message = "评论内容长度必须在1-500个字符之间")
    private String content;
}
