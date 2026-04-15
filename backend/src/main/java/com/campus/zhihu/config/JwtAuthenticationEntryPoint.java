package com.campus.zhihu.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 认证入口点
 * 处理认证失败的情况，返回统一的 JSON 响应
 *
 * @author CampusZhihu Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        log.warn("认证失败: uri={}, error={}", request.getRequestURI(), authException.getMessage());

        // 设置响应状态码为200（返回JSON格式）
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");

        // 构建错误响应
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("code", 1001); // UNAUTHORIZED
        errorResponse.put("msg", "未登录或登录已过期");
        errorResponse.put("data", null);

        // 写入响应
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}