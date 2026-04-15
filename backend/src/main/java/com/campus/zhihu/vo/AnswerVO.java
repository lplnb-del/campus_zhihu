package com.campus.zhihu.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 回答 VO
 * 用于向前端返回回答详细信息
 *
 * @author CampusZhihu Team
 */
@Data
public class AnswerVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 回答ID
     */
    private Long id;

    /**
     * 问题ID
     */
    private Long questionId;

    /**
     * 回答用户ID
     */
    private Long userId;

    /**
     * 回答用户信息
     */
    private UserVO user;

    /**
     * 回答内容
     */
    private String content;

    /**
     * 图片URLs列表
     */
    private List<String> images;

    /**
     * 点赞次数
     */
    private Integer likeCount;

    /**
     * 评论次数
     */
    private Integer commentCount;

    /**
     * 是否被采纳：0-未采纳，1-已采纳
     */
    private Integer isAccepted;

    /**
     * 是否被采纳描述
     */
    private String acceptedText;

    /**
     * 状态：0-草稿，1-已发布
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
     * 问题标题（可选，用于列表展示）
     */
    private String questionTitle;
}
