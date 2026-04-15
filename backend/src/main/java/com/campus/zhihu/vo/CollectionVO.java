package com.campus.zhihu.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 收藏 VO
 * 包含收藏记录及其关联的问题/回答信息
 *
 * @author CampusZhihu Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 收藏ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 目标类型：1-问题，2-回答
     */
    private Integer targetType;

    /**
     * 目标ID（问题ID或回答ID）
     */
    private Long targetId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 问题信息（当 targetType=1 时存在）
     */
    private QuestionVO question;

    /**
     * 回答信息（当 targetType=2 时存在）
     */
    private AnswerVO answer;
}
