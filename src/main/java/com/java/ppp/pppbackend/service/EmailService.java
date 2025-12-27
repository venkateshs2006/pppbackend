package com.java.ppp.pppbackend.service;


import com.java.ppp.pppbackend.dto.EmailRequest;
import com.java.ppp.pppbackend.entity.EmailTemplate;
import com.java.ppp.pppbackend.repository.EmailTemplateRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.messaging.MessagingException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private EmailTemplateRepository emailTemplateRepository;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${notification.email.from:noreply@ppp.com}")
    private String notificationFromEmail;

    @Value("${app.name:Knowledge Management System}")
    private String appName;

    /**
     * Send simple text email
     */
    @Async
    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(notificationFromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);
            log.info("Simple email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Error sending simple email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Send HTML email
     */
    @Async
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(notificationFromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("HTML email sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Error sending HTML email to: {}", to, e);
            throw new RuntimeException("Failed to send HTML email", e);
        } catch (jakarta.mail.MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Send email using template
     */
    @Async
    public void sendTemplatedEmail(String to, String templateName, Map<String, String> variables) {
        try {
            EmailTemplate template = emailTemplateRepository.findByNameAndIsActive(templateName, true)
                    .orElseThrow(() -> new RuntimeException("Email template not found: " + templateName));

            String subject = replaceVariables(template.getSubject(), variables);
            String body = replaceVariables(template.getBody(), variables);

            sendHtmlEmail(to, subject, body);
            log.info("Templated email sent successfully to: {} using template: {}", to, templateName);
        } catch (Exception e) {
            log.error("Error sending templated email to: {}", to, e);
            throw new RuntimeException("Failed to send templated email", e);
        }
    }

    /**
     * Send email with attachment
     */
    @Async
    public void sendEmailWithAttachment(EmailRequest request) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(notificationFromEmail);
            helper.setTo(request.getTo());
            helper.setSubject(request.getSubject());
            helper.setText(request.getBody(), request.isHtml());

            if (request.getCc() != null && !request.getCc().isEmpty()) {
                helper.setCc(request.getCc().toArray(new String[0]));
            }

            if (request.getBcc() != null && !request.getBcc().isEmpty()) {
                helper.setBcc(request.getBcc().toArray(new String[0]));
            }

            if (request.getAttachments() != null) {
                request.getAttachments().forEach((name, resource) -> {
                    try {
                        helper.addAttachment(name, resource);
                    } catch (MessagingException | jakarta.mail.MessagingException e) {
                        log.error("Error adding attachment: {}", name, e);
                    }
                });
            }

            mailSender.send(message);
            log.info("Email with attachment sent successfully to: {}", request.getTo());
        } catch (MessagingException | jakarta.mail.MessagingException e) {
            log.error("Error sending email with attachment to: {}", request.getTo(), e);
            throw new RuntimeException("Failed to send email with attachment", e);
        }
    }

    /**
     * Send verification email
     */
    @Async
    public void sendVerificationEmail(String to, String token) {
        String subject = "Verify Your Email - " + appName;
        String verificationUrl = "http://localhost:8080/api/auth/verify-email?token=" + token;

        String htmlContent = buildVerificationEmailHtml(to, verificationUrl);
        sendHtmlEmail(to, subject, htmlContent);
    }

    /**
     * Send password reset email
     */
    @Async
    public void sendPasswordResetEmail(String to, String token) {
        String subject = "Password Reset Request - " + appName;
        String resetUrl = "http://localhost:8080/reset-password?token=" + token;

        String htmlContent = buildPasswordResetEmailHtml(to, resetUrl);
        sendHtmlEmail(to, subject, htmlContent);
    }

    /**
     * Send welcome email
     */
    @Async
    public void sendWelcomeEmail(String to, String username) {
        String subject = "Welcome to " + appName;
        String htmlContent = buildWelcomeEmailHtml(username);
        sendHtmlEmail(to, subject, htmlContent);
    }

    /**
     * Send content notification email
     */
    @Async
    public void sendContentNotificationEmail(String to, String title, String message, String linkUrl) {
        String subject = "New Content Notification - " + appName;
        String htmlContent = buildContentNotificationEmailHtml(title, message, linkUrl);
        sendHtmlEmail(to, subject, htmlContent);
    }

    /**
     * Replace variables in template
     */
    private String replaceVariables(String template, Map<String, String> variables) {
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return result;
    }

    /**
     * Build verification email HTML
     */
    private String buildVerificationEmailHtml(String email, String verificationUrl) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }
                        .content { padding: 20px; background-color: #f9f9f9; }
                        .button { display: inline-block; padding: 12px 24px; background-color: #4CAF50; 
                                 color: white; text-decoration: none; border-radius: 4px; margin: 20px 0; }
                        .footer { padding: 20px; text-align: center; font-size: 12px; color: #666; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>%s</h1>
                        </div>
                        <div class="content">
                            <h2>Verify Your Email Address</h2>
                            <p>Hello,</p>
                            <p>Thank you for registering with %s. To complete your registration, 
                               please verify your email address by clicking the button below:</p>
                            <div style="text-align: center;">
                                <a href="%s" class="button">Verify Email</a>
                            </div>
                            <p>Or copy and paste this link into your browser:</p>
                            <p style="word-break: break-all; color: #4CAF50;">%s</p>
                            <p>This link will expire in 24 hours.</p>
                            <p>If you didn't create an account, please ignore this email.</p>
                        </div>
                        <div class="footer">
                            <p>&copy; 2024 %s. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(appName, appName, verificationUrl, verificationUrl, appName);
    }

    /**
     * Build password reset email HTML
     */
    private String buildPasswordResetEmailHtml(String email, String resetUrl) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #2196F3; color: white; padding: 20px; text-align: center; }
                        .content { padding: 20px; background-color: #f9f9f9; }
                        .button { display: inline-block; padding: 12px 24px; background-color: #2196F3; 
                                 color: white; text-decoration: none; border-radius: 4px; margin: 20px 0; }
                        .warning { background-color: #fff3cd; border-left: 4px solid #ffc107; 
                                  padding: 12px; margin: 20px 0; }
                        .footer { padding: 20px; text-align: center; font-size: 12px; color: #666; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>%s</h1>
                        </div>
                        <div class="content">
                            <h2>Password Reset Request</h2>
                            <p>Hello,</p>
                            <p>We received a request to reset your password. Click the button below to reset it:</p>
                            <div style="text-align: center;">
                                <a href="%s" class="button">Reset Password</a>
                            </div>
                            <p>Or copy and paste this link into your browser:</p>
                            <p style="word-break: break-all; color: #2196F3;">%s</p>
                            <div class="warning">
                                <strong>Security Note:</strong> This link will expire in 24 hours. 
                                If you didn't request a password reset, please ignore this email 
                                or contact support if you have concerns.
                            </div>
                        </div>
                        <div class="footer">
                            <p>&copy; 2024 %s. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(appName, resetUrl, resetUrl, appName);
    }

    /**
     * Build welcome email HTML
     */
    private String buildWelcomeEmailHtml(String username) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #673AB7; color: white; padding: 20px; text-align: center; }
                        .content { padding: 20px; background-color: #f9f9f9; }
                        .feature { padding: 15px; margin: 10px 0; background-color: white; 
                                  border-left: 4px solid #673AB7; }
                        .footer { padding: 20px; text-align: center; font-size: 12px; color: #666; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Welcome to %s!</h1>
                        </div>
                        <div class="content">
                            <h2>Hello %s!</h2>
                            <p>We're excited to have you on board. Your account has been successfully created.</p>
                            <h3>Here's what you can do:</h3>
                            <div class="feature">
                                <strong>📚 Browse Knowledge Base</strong><br>
                                Access thousands of articles and resources
                            </div>
                            <div class="feature">
                                <strong>✍️ Create Content</strong><br>
                                Share your knowledge with the community
                            </div>
                            <div class="feature">
                                <strong>💬 Collaborate</strong><br>
                                Comment, rate, and discuss with other users
                            </div>
                            <div class="feature">
                                <strong>🔔 Stay Updated</strong><br>
                                Get notifications about new content and updates
                            </div>
                            <p>If you have any questions, feel free to contact our support team.</p>
                        </div>
                        <div class="footer">
                            <p>&copy; 2024 %s. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(appName, username, appName);
    }

    /**
     * Build content notification email HTML
     */
    private String buildContentNotificationEmailHtml(String title, String message, String linkUrl) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #FF9800; color: white; padding: 20px; text-align: center; }
                        .content { padding: 20px; background-color: #f9f9f9; }
                        .button { display: inline-block; padding: 12px 24px; background-color: #FF9800; 
                                 color: white; text-decoration: none; border-radius: 4px; margin: 20px 0; }
                        .footer { padding: 20px; text-align: center; font-size: 12px; color: #666; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>%s</h1>
                        </div>
                        <div class="content">
                            <h2>%s</h2>
                            <p>%s</p>
                            <div style="text-align: center;">
                                <a href="%s" class="button">View Content</a>
                            </div>
                        </div>
                        <div class="footer">
                            <p>&copy; 2024 %s. All rights reserved.</p>
                            <p><a href="#">Unsubscribe</a> | <a href="#">Manage Preferences</a></p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(appName, title, message, linkUrl, appName);
    }
}