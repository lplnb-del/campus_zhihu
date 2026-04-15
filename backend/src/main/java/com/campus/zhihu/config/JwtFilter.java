package com.campus.zhihu.config;

import com.campus.zhihu.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT 认证过滤器
 * 拦截请求，验证 JWT Token，设置用户认证信息
 *
 * @author CampusZhihu Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            // 从请求头中获取 Token
            String token = getTokenFromRequest(request);

            // 验证 Token
            if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
                // 从 Token 中获取用户ID
                Long userId = jwtUtil.getUserIdFromToken(token);

                if (userId != null) {
                    // 创建认证对象
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userId,
                                    null,
                                    Collections.emptyList()
                            );

                    // 设置详细信息
                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // 将认证信息设置到 SecurityContext 中
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.debug("用户认证成功: userId={}", userId);
                }
            }
        } catch (Exception e) {
            log.error("Token验证失败: {}", e.getMessage());
            sendErrorResponse(response, 1001, "认证失败");
            return; // 中断请求
        }

        // 继续过滤链
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求中获取 Token
     *
     * @param request HTTP 请求
     * @return JWT Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(jwtUtil.getHeader());
        return jwtUtil.getTokenFromHeader(bearerToken);
    }

    /**
     * 发送错误响应
     *
     * @param response HTTP响应
     * @param status   状态码
     * @param message  错误消息
     */
    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(String.format("{\"code\":%d,\"msg\":\"%s\"}", status, message));
    }
}