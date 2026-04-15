package com.campus.zhihu.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 问题更新 DTO
 * 用于接收前端提交的问题更新数据
 *
 * @author CampusZhihu Team
 */
@Data
public class QuestionUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 问题ID（从路径参数获取）
     */
    private Long questionId;

    /**
     * 用户ID（从 Token 中获取，用于权限校验）
     */
    private Long userId;

    /**
     * 问题标题
     */
    @Size(min = 5, max = 200, message = "问题标题长度必须在5-200个字符之间")
    private String title;

    /**
     * 问题内容
     */
    @Size(min = 10, max = 10000, message = "问题内容长度必须在10-10000个字符之间")
    private String content;

    /**
     * 图片URLs列表
     */
    private List<String> images;

    /**
     * 标签ID列表
     */
    @Size(min = 1, max = 5, message = "标签数量必须在1-5个之间")
    private List<Long> tagIds;

    /**
     * 悬赏积分
     */
    private Integer rewardPoints;

    /**
     * 问题状态：0-草稿，1-已发布，2-已关闭
     */
    private Integer status;

    /**
     * 乐观锁版本号
     */
    private Integer version;
}
