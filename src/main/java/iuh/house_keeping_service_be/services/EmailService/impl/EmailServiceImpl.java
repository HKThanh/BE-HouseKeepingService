package iuh.house_keeping_service_be.services.EmailService.impl;

import iuh.house_keeping_service_be.config.MailProperties;
import iuh.house_keeping_service_be.models.Notification;
import iuh.house_keeping_service_be.services.EmailService.EmailService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private static final DateTimeFormatter EMAIL_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final JavaMailSender mailSender;
    private final MailProperties mailProperties;

    @Override
    public void sendEmail(String to, String subject, String content) {
        sendEmailInternal(to, subject, content, true);
    }

    @Override
    public void sendNotificationEmail(String to, Notification notification) {
        if (notification == null) {
            log.debug("Skip sending notification email because notification payload is null");
            return;
        }
        String subject = StringUtils.hasText(notification.getTitle())
                ? notification.getTitle()
                : "Thông báo mới từ House Keeping Service";
        String template = buildNotificationTemplate(notification);
        sendEmailInternal(to, subject, template, true);
    }

    private void sendEmailInternal(String to, String subject, String content, boolean isHtml) {
        if (!mailProperties.isEnabled()) {
            log.debug("Mail delivery disabled. Subject '{}' skipped.", subject);
            return;
        }
        if (!StringUtils.hasText(to)) {
            log.warn("Skip sending email '{}' because recipient is empty", subject);
            return;
        }
        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );
            helper.setTo(to.trim());
            helper.setSubject(subject);
            helper.setText(content, isHtml);
            helper.setFrom(resolveFromAddress(), mailProperties.getFromName());
            mailSender.send(message);
            log.info("Email with subject '{}' sent to {}", subject, to);
        } catch (MailException | MessagingException | UnsupportedEncodingException ex) {
            log.error("Failed to send email '{}' to {}", subject, to, ex);
        }
    }

    private String resolveFromAddress() {
        if (StringUtils.hasText(mailProperties.getFromAddress())) {
            return mailProperties.getFromAddress().trim();
        }
        if (mailSender instanceof JavaMailSenderImpl senderImpl && StringUtils.hasText(senderImpl.getUsername())) {
            return senderImpl.getUsername();
        }
        return "no-reply@localhost";
    }

    private String buildNotificationTemplate(Notification notification) {
        StringBuilder builder = new StringBuilder();
        builder.append("<div style=\"font-family:Arial,sans-serif;color:#0f172a;background:#f8fafc;padding:24px;\">")
                .append("<div style=\"max-width:640px;margin:auto;background:#ffffff;border-radius:12px;padding:24px;box-shadow:0 10px 25px rgba(15,23,42,0.08);\">")
                .append("<h2 style=\"color:#2563eb;margin-top:0;\">")
                .append(escapeHtml(notification.getTitle()))
                .append("</h2>")
                .append("<p style=\"font-size:15px;line-height:1.6;color:#1f2937;\">")
                .append(escapeHtml(notification.getMessage()))
                .append("</p>");

        if (notification.getRelatedType() != null) {
            builder.append("<p style=\"font-size:13px;color:#6b7280;margin-top:16px;\">Liên quan: ")
                    .append(notification.getRelatedType().name());
            if (StringUtils.hasText(notification.getRelatedId())) {
                builder.append(" • ").append(notification.getRelatedId());
            }
            builder.append("</p>");
        }

        String actionUrl = resolveActionUrl(notification);
        if (StringUtils.hasText(actionUrl)) {
            builder.append("<a href=\"")
                    .append(actionUrl)
                    .append("\" style=\"display:inline-block;margin-top:16px;padding:12px 20px;background:#2563eb;color:#ffffff;text-decoration:none;border-radius:8px;font-weight:600;\">Xem chi tiết</a>");
        }

        builder.append("<p style=\"font-size:12px;color:#94a3b8;margin-top:24px;\">Được gửi lúc: ")
                .append(resolveCreatedAt(notification))
                .append("</p>")
                .append("</div>")
                .append("<p style=\"text-align:center;font-size:12px;color:#94a3b8;margin-top:16px;\">")
                .append("Bạn nhận được email này vì đã bật thông báo trong ứng dụng House Keeping Service.")
                .append("</p>")
                .append("</div>");

        return builder.toString();
    }

    private String resolveCreatedAt(Notification notification) {
        LocalDateTime timestamp = notification.getCreatedAt() != null
                ? notification.getCreatedAt()
                : LocalDateTime.now();
        return EMAIL_DATE_FORMATTER.format(timestamp);
    }

    private String resolveActionUrl(Notification notification) {
        if (notification == null) {
            return mailProperties.getBaseUrl();
        }
        if (StringUtils.hasText(notification.getActionUrl())) {
            return notification.getActionUrl();
        }
        return mailProperties.getBaseUrl();
    }

    private String escapeHtml(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
