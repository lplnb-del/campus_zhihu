package com.campus.zhihu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 问题发布 DTO
 * 用于接收前端提交的问题发布数据
 *
 * @author CampusZhihu Team
 */
@Data
public class QuestionPublishDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 问题标题
     */
    @NotBlank(message = "问题标题不能为空")
    @Size(min = 5, max = 200, message = "问题标题长度必须在5-200个字符之间")
    private String title;

    /**
     * 问题内容
     */
    @NotBlank(message = "问题内容不能为空")
    @Size(min = 10, max = 10000, message = "问题内容长度必须在10-10000个字符之间")
    private String content;

    /**
     * 图片URLs列表
     */
    private List<String> images;

    /**
     * 标签ID列表
     */
    @NotNull(message = "至少需要选择一个标签")
    @Size(min = 1, max = 5, message = "标签数量必须在1-5个之间")
    private List<Long> tagIds;

    /**
     * 悬赏积分
     */
    private Integer rewardPoints;

    /**
     * 是否立即发布（false为草稿）
     */
    private Boolean publish;
}
