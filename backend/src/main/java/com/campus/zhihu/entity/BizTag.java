package com.campus.zhihu.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 标签实体类
 * 对应数据库表：biz_tag
 *
 * @author CampusZhihu Team
 */
@Data
@TableName("biz_tag")
public class BizTag implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 标签ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 标签名称
     */
    @TableField("name")
    private String name;

    /**
     * 标签描述
     */
    @TableField("description")
    private String description;

    /**
     * 标签图标
     */
    @TableField("icon")
    private String icon;

    /**
     * 标签颜色
     */
    @TableField("color")
    private String color;

    /**
     * 分类：tech-技术，study-学习，life-生活，career-职业，other-其他
     */
    @TableField("category")
    private String category;

    /**
     * 使用次数
     */
    @TableField("use_count")
    private Integer useCount;

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

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
