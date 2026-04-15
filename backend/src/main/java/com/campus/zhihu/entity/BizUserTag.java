package com.campus.zhihu.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 用户关注标签实体类
 * 对应数据库表：biz_user_tag
 *
 * @author CampusZhihu Team
 */
@Data
@TableName("biz_user_tag")
public class BizUserTag implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关注ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 标签ID
     */
    @TableField("tag_id")
    private Long tagId;

    /**
     * 关注时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}