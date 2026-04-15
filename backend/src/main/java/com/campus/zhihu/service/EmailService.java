package com.campus.zhihu.service;

/**
 * 邮件服务接口
 *
 * @author CampusZhihu Team
 */
public interface EmailService {

    /**
     * 发送邮箱验证邮件
     *
     * @param to      收件人邮箱
     * @param token   验证Token
     * @param username 用户名
     */
    void sendVerificationEmail(String to, String token, String username);

    /**
     * 发送密码重置邮件
     *
     * @param to      收件人邮箱
     * @param token   重置Token
     * @param username 用户名
     */
    void sendPasswordResetEmail(String to, String token, String username);
}