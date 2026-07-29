package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
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
    public void sendThankYouEmail(String toEmail, String username , String email, String feedbackMessage) {
        if (fromEmail == null || fromEmail.isBlank()) {
            throw new RuntimeException("Mail username is not configured. Please set MAIL_USERNAME.");
        }

        String subject = "Thank you for your feedback!";

String htmlContent = """
<!DOCTYPE html>
<html>

<head>
<meta charset="UTF-8">
</head>

<body style="
margin:0;
padding:40px;
background:#EEF5FC;
font-family:Arial,Helvetica,sans-serif;">

<table width="100%%" cellpadding="0" cellspacing="0">
<tr>
<td align="center">

<table width="650"
       cellpadding="0"
       cellspacing="0"
       style="
       background:#ffffff;
       border:1px solid #E5E7EB;
       border-radius:14px;
       overflow:hidden;
       box-shadow:0 6px 20px rgba(0,0,0,.08);">

<!-- ================= HEADER ================= -->

<tr>

<td style="
padding:30px 35px;
background:linear-gradient(135deg,#0A95FF,#1976D2);">

<table width="100%%">
<tr>

<td width="70" align="left">

<img src="cid:devqueryLogo"
     width="58"
     alt="DevQuery"
     style="display:block;">

</td>

<td align="center">

<h2 style="
margin:0;
font-size:34px;
font-weight:bold;
color:#ffffff;">

Thank You for Your Feedback!

</h2>

<p style="
margin-top:10px;
margin-bottom:0;
font-size:15px;
color:#E3F2FD;">

Your feedback helps us improve DevQuery every day.

</p>

</td>

<td width="70"></td>

</tr>
</table>

</td>

</tr>

<!-- ================= BODY ================= -->

<tr>

<td style="padding:38px;">

<p style="
font-size:16px;
color:#374151;
margin-top:0;">

Dear <strong>%s</strong>,

</p>

<p style="
font-size:15px;
line-height:1.9;
color:#4B5563;">

Thank you for taking the time to share your feedback with
<strong>DevQuery</strong>.

Your opinion is extremely valuable to us and helps us improve
our platform and provide a better experience for every member
of our developer community.

</p>

<!-- ================= USER INFORMATION ================= -->

<table width="100%%"
       cellpadding="0"
       cellspacing="0"
       style="
       margin-top:30px;
       border-collapse:collapse;
       background:#F8FBFF;
       border:1px solid #D8EAFB;
       border-radius:10px;
       overflow:hidden;">

<tr>

<td style="
padding:15px;
width:140px;
background:#EAF4FF;
font-weight:bold;
color:#1565C0;
border-bottom:1px solid #D8EAFB;">

Name

</td>

<td style="
padding:15px;
border-bottom:1px solid #D8EAFB;">

%s

</td>

</tr>

<tr>

<td style="
padding:15px;
background:#EAF4FF;
font-weight:bold;
color:#1565C0;
border-bottom:1px solid #D8EAFB;">

Email

</td>

<td style="
padding:15px;
border-bottom:1px solid #D8EAFB;">

%s

</td>

</tr>

<tr>

<td style="
padding:15px;
background:#EAF4FF;
font-weight:bold;
color:#1565C0;
vertical-align:top;">

Feedback

</td>

<td style="
padding:15px;
line-height:1.8;">

%s

</td>

</tr>

</table>

<!-- ================= THANK YOU MESSAGE ================= -->

<div style="
margin-top:32px;
padding:22px;
background:#F0F9FF;
border-left:5px solid #0A95FF;
border-radius:8px;">

<p style="
margin:0;
font-size:15px;
line-height:1.9;
color:#374151;">

We sincerely appreciate your support and the time you invested in helping us improve DevQuery.

Every suggestion is carefully reviewed by our team and contributes to building a better learning and collaboration environment for developers.

Thank you once again for being part of our community!

</p>

</div>

<!-- ================= SIGNATURE ================= -->

<table width="100%%" style="margin-top:45px;">

<tr>

<td align="left">

<p style="
margin:0;
font-size:15px;
color:#6B7280;">

Best regards,

</p>

<p style="
margin:8px 0 0;
font-size:24px;
font-weight:bold;
color:#0A95FF;">

DevQuery Team

</p>

<p style="
margin-top:6px;
font-size:14px;
font-style:italic;
color:#F48024;">

Empowering Developers Through Knowledge

</p>

</td>

</tr>

</table>

</td>

</tr>

<!-- ================= FOOTER ================= -->

<tr>

<td style="
padding:18px;
background:#F8FAFC;
border-top:1px solid #E5E7EB;
text-align:center;
font-size:13px;
color:#94A3B8;">

© 2026 DevQuery • Thank you for helping us build a better developer community.

</td>

</tr>

</table>

</td>

</tr>
</table>

</body>
</html>
""".formatted(
        username,
        username,
        email,
        feedbackMessage
);

        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.addInline(
            "devqueryLogo",
                new ClassPathResource(
                    "static/assets/img/logo-icon.png"
                )
            );

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send thank you email", e);
        }
    }
}
