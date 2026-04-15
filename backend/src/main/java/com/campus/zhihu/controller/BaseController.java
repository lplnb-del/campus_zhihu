package com.campus.zhihu.controller;

import com.campus.zhihu.common.BusinessException;
import com.campus.zhihu.common.Result;
import com.campus.zhihu.common.ResultCode;
import com.campus.zhihu.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

/**
 * 基础控制器
 * 提供通用的用户认证、Token解析等方法，消除子类中的重复代码
 *
 * @author CampusZhihu Team
 */
@Slf4j
public abstract class BaseController {

    @Autowired
    protected JwtUtil jwtUtil;

    /**
     * 获取当前登录用户ID（必须登录）
     * 如果用户未登录或Token无效，抛出异常
     *
     * @param request HTTP请求
     * @return 用户ID
     * @throws BusinessException 如果未登录或Token无效
     */
    protected Long getCurrentUserId(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        if (token == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }
        return userId;
    }

    /**
     * 获取当前用户ID（允许为空，用于未登录用户访问）
     * 如果用户未登录或Token无效，返回null
     *
     * @param request HTTP请求
     * @return 用户ID，如果未登录则返回null
     */
    protected Long getCurrentUserIdOrNull(HttpServletRequest request) {
        try {
            String token = getTokenFromRequest(request);
            if (token == null || token.isEmpty()) {
                return null;
            }
            return jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            log.debug("获取用户ID失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 验证用户是否已登录
     *
     * @param request HTTP请求
     * @return true: 已登录, false: 未登录
     */
    protected boolean isUserLoggedIn(HttpServletRequest request) {
        return getCurrentUserIdOrNull(request) != null;
    }

    /**
     * 从请求中获取Token
     *
     * @param request HTTP请求
     * @return Token字符串（不包含Bearer前缀），如果未携带Token则返回null
     */
    protected String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 构建未登录错误响应
     *
     * @return 错误响应
     */
    protected Result<Void> unauthorizedResponse() {
        return Result.error(ResultCode.UNAUTHORIZED.getCode(), "请先登录");
    }

    /**
     * 构建无权限错误响应
     *
     * @return 错误响应
     */
    protected Result<Void> forbiddenResponse() {
        return Result.error(ResultCode.FORBIDDEN.getCode(), "无权限访问");
    }

    /**
     * 获取客户端IP地址
     *
     * @param request HTTP请求
     * @return IP地址
     */
    protected String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多个IP的情况（取第一个）
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 记录操作日志
     *
     * @param request HTTP请求
     * @param operation 操作名称
     */
    protected void logOperation(HttpServletRequest request, String operation) {
        Long userId = getCurrentUserIdOrNull(request);
        String ip = getClientIp(request);
        log.info("操作记录 - userId: {}, 操作: {}, IP: {}", userId, operation, ip);
    }
}
