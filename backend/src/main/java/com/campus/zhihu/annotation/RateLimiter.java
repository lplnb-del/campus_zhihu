package com.campus.zhihu.annotation;

import java.lang.annotation.*;

/**
 * 请求限流注解
 * 用于限制接口的访问频率，防止恶意请求和DDoS攻击
 *
 * @author CampusZhihu Team
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimiter {

    /**
     * 限流key，支持SpEL表达式
     * 默认使用方法全限定名作为key
     * 示例："'user:' + #username" 或 "'login:' + #loginDTO.username"
     *
     * @return 限流key
     */
    String key() default "";

    /**
     * 时间窗口内的最大请求次数
     * 默认：10次
     *
     * @return 最大请求次数
     */
    int limit() default 10;

    /**
     * 时间窗口大小（秒）
     * 默认：60秒
     *
     * @return 时间窗口大小
     */
    int timeout() default 60;

    /**
     * 限流提示信息
     *
     * @return 提示信息
     */
    String message() default "请求过于频繁，请稍后再试";
}