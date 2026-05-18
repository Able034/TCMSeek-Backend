package com.tcmseek.service.impl;

import com.tcmseek.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * 邮件服务实现类
 */
@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * 发送验证码邮件
     * @param toEmail 收件人邮箱
     * @param code 验证码
     * @return 是否发送成功
     */
    @Override
    public boolean sendVerificationCode(String toEmail, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("TCMSeek 注册验证码"+"\n\nTCMSeek Registration verification code");
            message.setText("您的验证码是：" + code + "\n\n验证码5分钟内有效，请勿泄露给他人。\n\n如非本人操作，请忽略此邮件。"
            + "\n\nYour verification code is "+  code + "\n\nThis verification code is valid for 5 minutes. Please do not disclose to others.\n\nIf this is not your operation, please ignore this email.");
            
            mailSender.send(message);
            log.info("验证码邮件发送成功，收件人：{}", toEmail);
            return true;
        } catch (Exception e) {
            log.error("验证码邮件发送失败，收件人：{}，错误：{}", toEmail, e.getMessage());
            return false;
        }
    }
}



