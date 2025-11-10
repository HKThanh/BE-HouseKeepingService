package iuh.house_keeping_service_be.services.EmailService;

import iuh.house_keeping_service_be.models.Notification;

public interface EmailService {

    /**
     * Send a generic email message. Content defaults to HTML.
     */
    void sendEmail(String to, String subject, String content);

    /**
     * Send a notification-styled email that mirrors an in-app notification.
     */
    void sendNotificationEmail(String to, Notification notification);
}
