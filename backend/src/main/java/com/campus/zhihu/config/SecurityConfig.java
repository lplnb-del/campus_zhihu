package com.campus.zhihu.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security 配置类
 * 配置安全策略、密码加密、CORS 跨域等
 *
 * @author CampusZhihu Team
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;

    /**
     * 配置 Security 过滤链
     * 禁用默认的表单登录和 Session，使用自定义 JWT 认证
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
        http
                // 禁用 CSRF（因为使用 JWT，不需要 CSRF 保护）
                .csrf(AbstractHttpConfigurer::disable)

                // 配置 CORS（Spring 会根据 active profile 自动注入对应的配置）
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                // 禁用表单登录
                .formLogin(AbstractHttpConfigurer::disable)

                // 禁用 HTTP Basic 认证
                .httpBasic(AbstractHttpConfigurer::disable)

                // 禁用默认的登出
                .logout(AbstractHttpConfigurer::disable)

                // 配置 Session 管理为无状态（使用 JWT）
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 添加 JWT 过滤器
                .addFilterBefore(jwtFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)

                // 配置请求授权规则
                .authorizeHttpRequests(authorize -> authorize
                        // 允许所有人访问的接口（注册、登录等）
                        .requestMatchers("/user/register", "/user/login").permitAll()
                        .requestMatchers("/user/check/**").permitAll()

                        // 允许用户信息查询端点（只开放查询接口，修改接口需要认证）
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/user/*", "/user/*/stats", "/user/username/**").permitAll()

                        // 文件上传接口（需要认证，但在业务逻辑中处理）
                        .requestMatchers("/file/upload").authenticated()
                        .requestMatchers("/file/delete").authenticated()
                        .requestMatchers("/file/**").permitAll()

                        // Druid 监控
                        .requestMatchers("/druid/**").permitAll()

                        // 静态资源
                        .requestMatchers("/", "/index.html", "/favicon.ico").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()

                        // Swagger 文档（如果有）
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/doc.html").permitAll()

                        // 允许所有 GET 请求（问题列表、详情等可以匿名访问）
                        .requestMatchers(org.springframework.http.HttpMethod.GET,
                                "/question/**", "/answer/**", "/tag/**", "/comment/**").permitAll()

                        // 其他所有请求都需要认证
                        .anyRequest().authenticated()
                )

                // 配置认证失败处理
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                );

        return http.build();
    }

    /**
     * 配置 BCrypt 密码加密器
     * 用于用户密码的加密和验证
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 配置 CORS 跨域 - 开发环境（宽松配置）
     * 允许所有来源，便于本地开发和调试
     */
    @Bean
    @Profile("dev")
    public CorsConfigurationSource devCorsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 开发环境：允许所有来源
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));

        // 允许的请求方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // 允许的请求头
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // 允许携带认证信息
        configuration.setAllowCredentials(true);

        // 预检请求的有效期（秒）
        configuration.setMaxAge(3600L);

        // 暴露的响应头
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Disposition"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * 配置 CORS 跨域 - 生产环境（严格配置）
     * 只允许指定的域名，提高安全性
     */
    @Bean
    @Primary
    @Profile("prod")
    public CorsConfigurationSource prodCorsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // ⚠️ 生产环境：只允许指定域名（请根据实际情况修改）
        // 示例：configuration.setAllowedOrigins(Arrays.asList("https://campuszhihu.com", "https://www.campuszhihu.com"));
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:5173",  // 本地开发前端地址（如 Vite 默认端口）
            "http://localhost:3000",  // 本地开发前端地址（如 Create React App 默认端口）
            "https://lplnb-del.github.io",  // GitHub Pages
            "https://your-frontend-domain.com"  // ⚠️ 请替换为实际的前端域名
        ));

        // 允许的请求方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // 允许的请求头
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));

        // 允许携带认证信息
        configuration.setAllowCredentials(true);

        // 预检请求的有效期（秒）
        configuration.setMaxAge(3600L);

        // 暴露的响应头
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Disposition"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
