package com.campus.zhihu.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 回答查询 DTO
 * 用于接收前端提交的回答查询条件
 *
 * @author CampusZhihu Team
 */
@Data
public class AnswerQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关键词（搜索内容）
     */
    private String keyword;

    /**
     * 问题ID（查询某个问题的回答）
     */
    private Long questionId;

    /**
     * 用户ID（查询某个用户的回答）
     */
    private Long userId;

    /**
     * 是否被采纳：0-未采纳，1-已采纳
     */
    private Integer isAccepted;

    /**
     * 回答状态：0-草稿，1-已发布
     */
    private Integer status;

    /**
     * 排序字段：create_time, like_count, comment_count
     */
    private String orderBy;

    /**
     * 排序方向：asc, desc
     */
    private String orderDirection;

    /**
     * 当前页码（默认为1）
     */
    private Integer pageNum = 1;

    /**
     * 每页大小（默认为10）
     */
    private Integer pageSize = 10;

    /**
     * 最小点赞数
     */
    private Integer minLikeCount;

    /**
     * 最大点赞数
     */
    private Integer maxLikeCount;
}
