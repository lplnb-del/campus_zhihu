package com.campus.zhihu.common;

import lombok.Getter;

/**
 * 自定义业务异常类
 * 用于业务逻辑中的异常处理，抛出后由全局异常处理器统一处理
 *
 * @author CampusZhihu Team
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private Integer code;

    /**
     * 错误消息
     */
    private String message;

    /**
     * 构造函数：使用默认错误码和消息
     */
    public BusinessException() {
        super(ResultCode.ERROR.getMessage());
        this.code = ResultCode.ERROR.getCode();
        this.message = ResultCode.ERROR.getMessage();
    }

    /**
     * 构造函数：自定义错误消息
     *
     * @param message 错误消息
     */
    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.ERROR.getCode();
        this.message = message;
    }

    /**
     * 构造函数：使用 ResultCode 枚举
     *
     * @param resultCode 结果码枚举
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }

    /**
     * 构造函数：自定义错误码和消息
     *
     * @param code    错误码
     * @param message 错误消息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * 构造函数：自定义错误消息和原始异常
     *
     * @param message 错误消息
     * @param cause   原始异常
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = ResultCode.ERROR.getCode();
        this.message = message;
    }

    /**
     * 构造函数：使用 ResultCode 枚举和原始异常
     *
     * @param resultCode 结果码枚举
     * @param cause      原始异常
     */
    public BusinessException(ResultCode resultCode, Throwable cause) {
        super(resultCode.getMessage(), cause);
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }

    @Override
    public String getMessage() {
        return message;
    }
}
