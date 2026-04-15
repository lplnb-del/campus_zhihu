package com.campus.zhihu.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 回答更新 DTO
 * 用于接收前端提交的回答更新数据
 *
 * @author CampusZhihu Team
 */
@Data
public class AnswerUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 回答ID（从路径参数获取）
     */
    private Long answerId;

    /**
     * 用户ID（从 Token 中获取，用于权限校验）
     */
    private Long userId;

    /**
     * 回答内容
     */
    @Size(min = 10, max = 10000, message = "回答内容长度必须在10-10000个字符之间")
    private String content;

    /**
     * 图片URLs列表
     */
    private List<String> images;

    /**
     * 回答状态：0-草稿，1-已发布
     */
    private Integer status;

    /**
     * 乐观锁版本号
     */
    private Integer version;
}
