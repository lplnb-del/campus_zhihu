package com.campus.zhihu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 回答发布 DTO
 * 用于接收前端提交的回答发布数据
 *
 * @author CampusZhihu Team
 */
@Data
public class AnswerPublishDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 问题ID
     */
    @NotNull(message = "问题ID不能为空")
    private Long questionId;

    /**
     * 回答内容
     */
    @NotBlank(message = "回答内容不能为空")
    @Size(min = 10, max = 10000, message = "回答内容长度必须在10-10000个字符之间")
    private String content;

    /**
     * 图片URLs列表
     */
    private List<String> images;

    /**
     * 是否立即发布（false为草稿）
     */
    private Boolean publish;
}
