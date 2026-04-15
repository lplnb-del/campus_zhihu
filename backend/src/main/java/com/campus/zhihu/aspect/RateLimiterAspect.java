package com.campus.zhihu.aspect;

import com.campus.zhihu.annotation.RateLimiter;
import com.campus.zhihu.common.BusinessException;
import com.campus.zhihu.common.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * 请求限流切面
 * 基于 Redis + AOP 实现接口限流
 *
 * @author CampusZhihu Team
 */
@Slf4j
@Aspect
@Component
public class RateLimiterAspect {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private Environment environment;

    /**
     * 限流key前缀
     */
    private static final String RATE_LIMIT_KEY_PREFIX = "rate_limit:";

    @Around("@annotation(rateLimiter)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimiter rateLimiter) throws Throwable {
        // 测试环境跳过限流检查
        if (environment != null && Arrays.asList(environment.getActiveProfiles()).contains("test")) {
            log.debug("测试环境，跳过限流检查");
            return joinPoint.proceed();
        }

        // 1. 构建限流key
        String key = buildKey(joinPoint, rateLimiter);

        // 2. 获取当前请求次数
        Long count = redisTemplate.opsForValue().increment(key);

        // 3. 首次访问，设置过期时间
        if (count != null && count == 1) {
            redisTemplate.expire(key, rateLimiter.timeout(), TimeUnit.SECONDS);
        }

        // 4. 检查是否超过限制
        if (count != null && count > rateLimiter.limit()) {
            log.warn("请求限流触发：key={}, count={}, limit={}", key, count, rateLimiter.limit());
            throw new BusinessException(ResultCode.RATE_LIMIT_EXCEEDED.getCode(), rateLimiter.message());
        }

        // 5. 执行目标方法
        return joinPoint.proceed();
    }

    /**
     * 构建限流key
     *
     * @param joinPoint    切点
     * @param rateLimiter 限流注解
     * @return 限流key
     */
    private String buildKey(ProceedingJoinPoint joinPoint, RateLimiter rateLimiter) {
        // 如果自定义了key，直接使用
        if (!rateLimiter.key().isEmpty()) {
            return RATE_LIMIT_KEY_PREFIX + rateLimiter.key();
        }

        // 否则使用方法全限定名 + IP地址作为key
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String className = method.getDeclaringClass().getSimpleName();
        String methodName = method.getName();

        // 获取客户端IP
        String clientIp = getClientIp();

        return RATE_LIMIT_KEY_PREFIX + className + ":" + methodName + ":" + clientIp;
    }

    /**
     * 获取客户端IP地址
     *
     * @return IP地址
     */
    private String getClientIp() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return "unknown";
            }

            HttpServletRequest request = attributes.getRequest();
            String ip = request.getHeader("X-Forwarded-For");

            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("X-Real-IP");
            }

            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }

            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }

            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }

            // 如果是多个IP，取第一个
            if (ip != null && ip.contains(",")) {
                ip = ip.split(",")[0].trim();
            }

            return ip;
        } catch (Exception e) {
            log.error("获取客户端IP失败", e);
            return "unknown";
        }
    }
}