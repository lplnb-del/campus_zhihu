package com.campus.zhihu.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 问题-标签关联实体类
 * 对应数据库表：biz_question_tag
 *
 * @author CampusZhihu Team
 */
@Data
@TableName("biz_question_tag")
public class BizQuestionTag implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关联ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 问题ID
     */
    @TableField("question_id")
    private Long questionId;

    /**
     * 标签ID
     */
    @TableField("tag_id")
    private Long tagId;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
