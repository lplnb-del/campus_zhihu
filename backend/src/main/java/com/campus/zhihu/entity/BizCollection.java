package com.campus.zhihu.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 收藏实体类
 * 对应数据库表：biz_collection
 *
 * @author CampusZhihu Team
 */
@Data
@TableName("biz_collection")
public class BizCollection implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 收藏ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 目标类型：1-问题，2-回答
     */
    @TableField("target_type")
    private Integer targetType;

    /**
     * 目标ID（问题ID或回答ID）
     */
    @TableField("target_id")
    private Long targetId;

    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
