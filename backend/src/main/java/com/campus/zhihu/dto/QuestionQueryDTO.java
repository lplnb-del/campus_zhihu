package com.campus.zhihu.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 问题查询 DTO
 * 用于接收前端提交的问题查询条件
 *
 * @author CampusZhihu Team
 */
@Data
public class QuestionQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关键词（搜索标题和内容）
     */
    private String keyword;

    /**
     * 标签ID
     */
    private Long tagId;

    /**
     * 用户ID（查询某个用户的问题）
     */
    private Long userId;

    /**
     * 问题状态：0-草稿，1-已发布，2-已关闭
     */
    private Integer status;

    /**
     * 是否已解决：0-未解决，1-已解决
     */
    private Integer isResolved;

    /**
     * 排序字段：create_time, view_count, answer_count, like_count
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
     * 是否只查询有悬赏的问题
     */
    private Boolean hasReward;

    /**
     * 最小悬赏积分
     */
    private Integer minRewardPoints;

    /**
     * 最大悬赏积分
     */
    private Integer maxRewardPoints;
}
