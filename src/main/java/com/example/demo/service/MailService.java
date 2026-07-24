package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class MailService {
     private final JavaMailSender mailSender;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendResetPasswordEmail(String toEmail, String token) {
        if (fromEmail == null || fromEmail.isBlank()) {
            throw new RuntimeException("Mail username is not configured. Please set MAIL_USERNAME.");
        }

        String resetLink = baseUrl + "/reset-password?token=" + token;

        String subject = "Reset your DevQuery password";

        String htmlContent = """
                <div style="font-family: Arial, sans-serif; line-height: 1.6;">
                    <h2>Reset your password here</h2>

                    <p>We received a request to reset your DevQuery password.</p>

                    <p>
                        <a href="%s"
                           style="display: inline-block;
                                  padding: 10px 16px;
                                  background-color: #0a95ff;
                                  color: white;
                                  text-decoration: none;
                                  border-radius: 4px;">
                            Reset your password
                        </a>
                    </p>

                    <p>This link will expire in 5 minutes.</p>

                    <p>If you did not request this, you can ignore this email.</p>
                </div>
                """.formatted(resetLink);

        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send reset password email", e);
        }
    }
    public void sendThankYouEmail(String toEmail, String username) {
        if (fromEmail == null || fromEmail.isBlank()) {
            throw new RuntimeException("Mail username is not configured. Please set MAIL_USERNAME.");
        }

        String subject = "Thank you for your feedback!";

        String htmlContent = """
                <div style="font-family: Arial, sans-serif; line-height: 1.6;">
                    <h2>Thank you for your feedback!</h2>

                    <p>Dear %s,</p>

                    <p>We appreciate your valuable feedback and are grateful for your time and effort in helping us improve our services.</p>

                    <p>Your input is invaluable to us, and we will carefully consider your suggestions as we continue to enhance our platform.</p>

                    <p>Thank you once again for your support!</p>

                    <p>Best regards,<br/>The DevQuery Team</p>
                </div>
                """.formatted(username);

        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send thank you email", e);
        }
    }
}
