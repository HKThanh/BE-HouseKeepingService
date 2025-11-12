package iuh.house_keeping_service_be.scheduled;

import iuh.house_keeping_service_be.enums.BookingStatus;
import iuh.house_keeping_service_be.enums.RoleName;
import iuh.house_keeping_service_be.models.Account;
import iuh.house_keeping_service_be.models.Booking;
import iuh.house_keeping_service_be.models.Notification;
import iuh.house_keeping_service_be.repositories.AccountRepository;
import iuh.house_keeping_service_be.repositories.BookingRepository;
import iuh.house_keeping_service_be.services.NotificationService.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Scheduled job to check for urgent bookings that need admin approval
 * Runs every 10 minutes to notify admins about bookings happening soon
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UrgentBookingNotificationScheduler {

    private final BookingRepository bookingRepository;
    private final AccountRepository accountRepository;
    private final NotificationService notificationService;
    
    // Track bookings we've already sent notifications for to avoid duplicates
    private final Set<String> notifiedBookingIds = new HashSet<>();

    /**
     * Check for bookings that need verification and are happening within 1 hour
     * Runs every 5 minutes
     */
    @Scheduled(cron = "0 */5 * * * *") // Every 5 minutes
    public void checkUrgentBookings() {
        log.info("Running urgent booking notification check...");
        
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime oneHourFromNow = now.plusHours(1);
            
            // Find bookings that are PENDING or AWAITING_EMPLOYEE and start within 1 hour
            List<Booking> urgentBookings = bookingRepository.findByStatusInAndBookingTimeBetween(
                List.of(BookingStatus.PENDING, BookingStatus.AWAITING_EMPLOYEE),
                now,
                oneHourFromNow
            );
            
            if (urgentBookings.isEmpty()) {
                log.debug("No urgent bookings found");
                return;
            }
            
            // Get all admin accounts
            // Query all accounts and filter by ADMIN role
            List<Account> allAccounts = accountRepository.findAll();
            List<Account> adminAccounts = allAccounts.stream()
                .filter(account -> account.getRoles() != null && 
                       account.getRoles().stream()
                           .anyMatch(role -> role.getRoleName() == RoleName.ADMIN))
                .toList();
            
            if (adminAccounts.isEmpty()) {
                log.warn("No admin accounts found to notify");
                return;
            }
            
            int notificationsSent = 0;
            
            for (Booking booking : urgentBookings) {
                // Skip if we've already notified about this booking
                if (notifiedBookingIds.contains(booking.getBookingId())) {
                    continue;
                }
                
                long minutesUntilBooking = java.time.Duration.between(now, booking.getBookingTime()).toMinutes();
                
                // Send notification to all admins
                for (Account admin : adminAccounts) {
                    try {
                        notificationService.createNotification(
                            new iuh.house_keeping_service_be.dtos.Notification.NotificationRequest(
                                admin.getAccountId(),
                                "ADMIN", // Target role - notification for admin only
                                Notification.NotificationType.BOOKING_VERIFIED, // Reuse existing type
                                "⚠️ Booking cần duyệt gấp",
                                String.format("Booking %s (trạng thái: %s) cần được xử lý, sẽ bắt đầu trong %d phút",
                                    booking.getBookingCode(),
                                    booking.getStatus() == BookingStatus.PENDING ? "chờ duyệt" : "chờ nhân viên",
                                    minutesUntilBooking),
                                booking.getBookingId(),
                                Notification.RelatedEntityType.BOOKING,
                                Notification.NotificationPriority.URGENT,
                                "/admin/bookings/" + booking.getBookingId()
                            )
                        );
                        notificationsSent++;
                    } catch (Exception e) {
                        log.error("Failed to send urgent booking notification to admin {}: {}", 
                            admin.getAccountId(), e.getMessage());
                    }
                }
                
                // Mark this booking as notified
                notifiedBookingIds.add(booking.getBookingId());
            }
            
            log.info("Sent {} urgent booking notifications for {} bookings", 
                notificationsSent, urgentBookings.size());
            
            // Clean up old notification tracking (bookings that have passed)
            cleanupOldNotifications(now);
            
        } catch (Exception e) {
            log.error("Error in urgent booking notification scheduler", e);
        }
    }
    
    /**
     * Remove booking IDs from tracking if their booking time has passed
     */
    private void cleanupOldNotifications(LocalDateTime now) {
        try {
            List<Booking> trackedBookings = bookingRepository.findAllById(notifiedBookingIds);
            
            notifiedBookingIds.removeIf(bookingId -> {
                Booking booking = trackedBookings.stream()
                    .filter(b -> b.getBookingId().equals(bookingId))
                    .findFirst()
                    .orElse(null);
                
                // Remove if booking not found or booking time has passed
                return booking == null || booking.getBookingTime().isBefore(now);
            });
            
        } catch (Exception e) {
            log.error("Error cleaning up old notification tracking", e);
        }
    }
}
