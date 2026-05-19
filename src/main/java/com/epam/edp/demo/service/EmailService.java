package com.epam.edp.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.password-reset.from-email}")
    private String fromEmail;

    public void sendPasswordResetCode(String to, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Password Reset Verification Code - Travel Agency");
            message.setText(buildPasswordResetEmailBody(code));
            
            mailSender.send(message);
            log.info("Password reset email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", to, e);
            throw new RuntimeException("Failed to send email. Please try again later.");
        }
    }

    private String buildPasswordResetEmailBody(String code) {
        return String.format(
            "Hello,\n\n" +
            "You have requested to reset your password for your Travel Agency account.\n\n" +
            "Your verification code is: %s\n\n" +
            "This code will expire in 15 minutes.\n\n" +
            "If you did not request this password reset, please ignore this email.\n\n" +
            "Best regards,\n" +
            "Travel Agency Team",
            code
        );
    }
}
