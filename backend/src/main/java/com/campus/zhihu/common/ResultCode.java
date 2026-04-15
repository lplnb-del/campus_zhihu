package com.campus.zhihu.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * API 响应状态码枚举
 *
 * @author CampusZhihu Team
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    /**
     * 成功
     */
    SUCCESS(200, "操作成功"),

    /**
     * 失败
     */
    ERROR(500, "操作失败"),

    /**
     * 参数错误
     */
    PARAM_ERROR(400, "参数错误"),

    /**
     * 参数为空
     */
    PARAM_IS_BLANK(401, "参数为空"),

    /**
     * 参数类型错误
     */
    PARAM_TYPE_ERROR(402, "参数类型错误"),

    /**
     * 参数缺失
     */
    PARAM_NOT_COMPLETE(403, "参数缺失"),

    /**
     * 未授权
     */
    UNAUTHORIZED(1001, "未授权，请先登录"),

    /**
     * Token 无效
     */
    TOKEN_INVALID(1002, "Token 无效或已过期"),

    /**
     * Token 已过期
     */
    TOKEN_EXPIRED(1003, "Token 已过期，请重新登录"),

    /**
     * 权限不足
     */
    FORBIDDEN(1004, "权限不足，无法访问"),

    /**
     * 用户不存在
     */
    USER_NOT_EXIST(2001, "用户不存在"),

    /**
     * 用户名或密码错误
     */
    USERNAME_OR_PASSWORD_ERROR(2002, "用户名或密码错误"),

    /**
     * 用户名已存在
     */
    USERNAME_ALREADY_EXISTS(2003, "用户名已存在"),

    /**
     * 邮箱已存在
     */
    EMAIL_ALREADY_EXISTS(2004, "邮箱已被注册"),

    /**
     * 学号已存在
     */
    STUDENT_ID_ALREADY_EXISTS(2005, "学号已存在"),

    /**
     * 用户积分不足
     */
    INSUFFICIENT_POINTS(2006, "积分不足"),

    /**
     * 问题不存在
     */
    QUESTION_NOT_EXIST(3001, "问题不存在"),

    /**
     * 问题已关闭
     */
    QUESTION_CLOSED(3002, "问题已关闭"),

    /**
     * 问题已解决
     */
    QUESTION_SOLVED(3003, "问题已解决"),

    /**
     * 回答不存在
     */
    ANSWER_NOT_EXIST(4001, "回答不存在"),

    /**
     * 回答已被采纳
     */
    ANSWER_ALREADY_ACCEPTED(4002, "已有回答被采纳"),

    /**
     * 不能采纳自己的回答
     */
    CANNOT_ACCEPT_OWN_ANSWER(4003, "不能采纳自己的回答"),

    /**
     * 不能点赞自己的回答
     */
    CANNOT_LIKE_OWN_ANSWER(4004, "不能点赞自己的回答"),

    /**
     * 已经点赞过
     */
    ALREADY_LIKED(4005, "已经点赞过该回答"),

    /**
     * 评论不存在
     */
    COMMENT_NOT_EXIST(5001, "评论不存在"),

    /**
     * 标签不存在
     */
    TAG_NOT_EXIST(6001, "标签不存在"),

    /**
     * 标签名已存在
     */
    TAG_NAME_ALREADY_EXISTS(6002, "标签名已存在"),

    /**
     * 已收藏
     */
    ALREADY_COLLECTED(7001, "已收藏该问题"),

    /**
     * 未收藏
     */
    NOT_COLLECTED(7002, "未收藏该问题"),

    /**
     * 文件上传失败
     */
    FILE_UPLOAD_ERROR(8001, "文件上传失败"),

    /**
     * 文件类型不支持
     */
    FILE_TYPE_NOT_SUPPORTED(8002, "文件类型不支持"),

    /**
     * 文件大小超出限制
     */
    FILE_SIZE_EXCEEDED(8003, "文件大小超出限制"),

    /**
     * 请求过于频繁
     */
    RATE_LIMIT_EXCEEDED(8004, "请求过于频繁，请稍后再试"),

    /**
     * 数据库操作失败
     */
    DATABASE_ERROR(9001, "数据库操作失败"),

    /**
     * 系统异常
     */
    SYSTEM_ERROR(9999, "系统异常，请联系管理员");

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 响应消息
     */
    private final String message;
}
