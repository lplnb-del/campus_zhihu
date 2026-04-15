package com.campus.zhihu.service.impl;

import com.campus.zhihu.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * 邮件服务实现类
 *
 * @author CampusZhihu Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Value("${campus.app.url:http://localhost:3000}")
    private String appUrl;

    @Override
    public void sendVerificationEmail(String to, String token, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject("CampusZhihu - 邮箱验证");

            String verificationUrl = appUrl + "/verify-email?token=" + token;
            String content = String.format(
                "亲爱的 %s：\n\n" +
                "欢迎加入 CampusZhihu！\n\n" +
                "请点击以下链接验证您的邮箱：\n" +
                "%s\n\n" +
                "该链接将在24小时后失效。\n\n" +
                "如果您没有注册 CampusZhihu 账户，请忽略此邮件。\n\n" +
                "CampusZhihu 团队",
                username,
                verificationUrl
            );

            message.setText(content);

            mailSender.send(message);
            log.info("邮箱验证邮件发送成功: to={}", to);
        } catch (Exception e) {
            log.error("邮箱验证邮件发送失败: to={}, error={}", to, e.getMessage());
            throw new RuntimeException("邮件发送失败，请稍后重试", e);
        }
    }

    @Override
    public void sendPasswordResetEmail(String to, String token, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject("CampusZhihu - 密码重置");

            String resetUrl = appUrl + "/reset-password?token=" + token;
            String content = String.format(
                "亲爱的 %s：\n\n" +
                "我们收到了您的密码重置请求。\n\n" +
                "请点击以下链接重置您的密码：\n" +
                "%s\n\n" +
                "该链接将在1小时后失效。\n\n" +
                "如果您没有请求重置密码，请忽略此邮件。\n\n" +
                "CampusZhihu 团队",
                username,
                resetUrl
            );

            message.setText(content);

            mailSender.send(message);
            log.info("密码重置邮件发送成功: to={}", to);
        } catch (Exception e) {
            log.error("密码重置邮件发送失败: to={}, error={}", to, e.getMessage());
            throw new RuntimeException("邮件发送失败，请稍后重试", e);
        }
    }
}