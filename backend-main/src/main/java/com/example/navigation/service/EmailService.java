package com.example.navigation.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * 邮件服务类，负责发送验证码邮件
 */
@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String fromAddress;
    private final int expireTime;

    @Autowired
    public EmailService(JavaMailSender mailSender, 
                       @Value("${spring.mail.username}") String fromAddress, 
                       @Value("${verification.code.expire-time}") int expireTime) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
        this.expireTime = expireTime;
    }

    /**
     * 发送邮箱验证码
     * @param toEmail 接收方邮箱
     * @param code 验证码
     * @throws MessagingException 邮件发送异常
     */
    public void sendVerificationCodeEmail(String toEmail, String code) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromAddress);
        helper.setTo(toEmail);
        helper.setSubject("【导航系统】邮箱验证码");

        // 构建邮件内容
        String content = String.format("<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px;'>" +
                "<h2 style='color: #333; text-align: center;'>导航系统邮箱验证</h2>" +
                "<p>尊敬的用户：</p>" +
                "<p>您正在进行邮箱验证，您的验证码是：</p>" +
                "<div style='font-size: 24px; font-weight: bold; color: #2c3e50; text-align: center; margin: 20px 0; letter-spacing: 4px;'>%s</div>" +
                "<p>验证码有效期为 %d 分钟，请尽快完成验证。</p>" +
                "<p style='color: #999; font-size: 12px; margin-top: 30px;'>本邮件为自动发送，请勿回复。</p>" +
                "</div>", code, expireTime);

        helper.setText(content, true); // true表示发送HTML格式邮件
        mailSender.send(message);
    }
}