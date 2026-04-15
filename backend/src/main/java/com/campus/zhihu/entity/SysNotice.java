package com.campus.zhihu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息通知实体类
 *
 * @author CampusZhihu Team
 */
@Data
@TableName("sys_notice")
public class SysNotice implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 通知ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 接收者ID
     */
    @TableField("receiver_id")
    private Long receiverId;

    /**
     * 发送者ID
     */
    @TableField("sender_id")
    private Long senderId;

    /**
     * 通知类型(1:点赞 2:回答 3:采纳 4:评论)
     */
    @TableField("type")
    private Integer type;

    /**
     * 通知内容
     */
    @TableField("content")
    private String content;

    /**
     * 目标类型：1-问题，2-回答，3-评论
     */
    @TableField("target_type")
    private Integer targetType;

    /**
     * 目标ID（问题ID、回答ID或评论ID）
     */
    @TableField("target_id")
    private Long targetId;

    /**
     * 是否已读(0:未读 1:已读)
     */
    @TableField("is_read")
    private Integer isRead;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 逻辑删除(0:未删除 1:已删除)
     */
    @TableField("is_deleted")
    @TableLogic
    private Integer isDeleted;
}