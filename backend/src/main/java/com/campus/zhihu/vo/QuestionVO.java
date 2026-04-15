package com.campus.zhihu.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 问题 VO
 * 用于向前端返回问题详细信息
 *
 * @author CampusZhihu Team
 */
@Data
public class QuestionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 问题ID
     */
    private Long id;

    /**
     * 提问用户ID
     */
    private Long userId;

    /**
     * 提问用户信息
     */
    private UserVO user;

    /**
     * 问题标题
     */
    private String title;

    /**
     * 问题内容
     */
    private String content;

    /**
     * 图片URLs列表
     */
    private List<String> images;

    /**
     * 标签列表
     */
    private List<TagVO> tags;

    /**
     * 浏览次数
     */
    private Integer viewCount;

    /**
     * 回答数量
     */
    private Integer answerCount;

    /**
     * 收藏次数
     */
    private Integer collectionCount;

    /**
     * 点赞次数
     */
    private Integer likeCount;

    /**
     * 悬赏积分
     */
    private Integer rewardPoints;

    /**
     * 状态：0-草稿，1-已发布，2-已关闭
     */
    private Integer status;

    /**
     * 状态描述
     */
    private String statusText;

    /**
     * 是否已解决：0-未解决，1-已解决
     */
    private Integer isResolved;

    /**
     * 是否已解决描述
     */
    private String resolvedText;

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
     * 当前用户是否已收藏
     */
    private Boolean hasCollected;

    /**
     * 标签 VO（内部类）
     */
    @Data
    public static class TagVO implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 标签ID
         */
        private Long id;

        /**
         * 标签名称
         */
        private String name;

        /**
         * 标签描述
         */
        private String description;

        /**
         * 标签图标
         */
        private String icon;

        /**
         * 标签颜色
         */
        private String color;

        /**
         * 使用次数
         */
        private Integer useCount;
    }
}
