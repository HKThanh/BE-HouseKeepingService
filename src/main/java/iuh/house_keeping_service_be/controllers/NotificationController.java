package iuh.house_keeping_service_be.controllers;

import iuh.house_keeping_service_be.config.JwtUtil;
import iuh.house_keeping_service_be.dtos.Notification.NotificationRequest;
import iuh.house_keeping_service_be.dtos.Notification.NotificationResponse;
import iuh.house_keeping_service_be.repositories.AccountRepository;
import iuh.house_keeping_service_be.services.NotificationService.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@Slf4j
@RequiredArgsConstructor
public class NotificationController {
    
    private final NotificationService notificationService;
    private final JwtUtil jwtUtil;
    private final AccountRepository accountRepository;
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_CUSTOMER', 'ROLE_EMPLOYEE', 'ROLE_ADMIN')")
    public ResponseEntity<?> getMyNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Boolean unreadOnly,
            @RequestHeader("Authorization") String authHeader) {
        
        log.info("Getting notifications (page: {}, size: {}, unreadOnly: {})", page, size, unreadOnly);
        
        try {
            // Extract account ID from token
            String accountId = extractAccountIdFromToken(authHeader);
            if (accountId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "Token không hợp lệ"
                ));
            }
            
            // Validate pagination
            if (page < 0) page = 0;
            if (size <= 0 || size > 100) size = 10;
            
            Pageable pageable = PageRequest.of(page, size);
            
            Page<NotificationResponse> notifications;
            if (Boolean.TRUE.equals(unreadOnly)) {
                notifications = notificationService.getUnreadNotifications(accountId, pageable);
            } else {
                notifications = notificationService.getNotificationsByAccountId(accountId, pageable);
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", notifications.getContent(),
                "currentPage", notifications.getNumber(),
                "totalItems", notifications.getTotalElements(),
                "totalPages", notifications.getTotalPages()
            ));
        } catch (Exception e) {
            log.error("Error getting notifications: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Đã xảy ra lỗi khi lấy danh sách thông báo"
            ));
        }
    }
    
    @GetMapping("/unread-count")
    @PreAuthorize("hasAnyRole('ROLE_CUSTOMER', 'ROLE_EMPLOYEE', 'ROLE_ADMIN')")
    public ResponseEntity<?> getUnreadCount(@RequestHeader("Authorization") String authHeader) {
        log.info("Getting unread notification count");
        
        try {
            String accountId = extractAccountIdFromToken(authHeader);
            if (accountId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "Token không hợp lệ"
                ));
            }
            
            long count = notificationService.getUnreadCount(accountId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "count", count
            ));
        } catch (Exception e) {
            log.error("Error getting unread count: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Đã xảy ra lỗi khi đếm thông báo chưa đọc"
            ));
        }
    }
    
    @GetMapping("/{notificationId}")
    @PreAuthorize("hasAnyRole('ROLE_CUSTOMER', 'ROLE_EMPLOYEE', 'ROLE_ADMIN')")
    public ResponseEntity<?> getNotificationById(
            @PathVariable String notificationId,
            @RequestHeader("Authorization") String authHeader) {
        
        log.info("Getting notification by ID: {}", notificationId);
        
        try {
            String accountId = extractAccountIdFromToken(authHeader);
            if (accountId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "Token không hợp lệ"
                ));
            }
            
            NotificationResponse notification = notificationService.getNotificationById(notificationId);
            
            // Verify ownership
            if (!notification.accountId().equals(accountId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "success", false,
                    "message", "Bạn không có quyền truy cập thông báo này"
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", notification
            ));
        } catch (RuntimeException e) {
            log.error("Error getting notification: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Đã xảy ra lỗi khi lấy thông báo"
            ));
        }
    }
    
    @PutMapping("/{notificationId}/read")
    @PreAuthorize("hasAnyRole('ROLE_CUSTOMER', 'ROLE_EMPLOYEE', 'ROLE_ADMIN')")
    public ResponseEntity<?> markAsRead(
            @PathVariable String notificationId,
            @RequestHeader("Authorization") String authHeader) {
        
        log.info("Marking notification as read: {}", notificationId);
        
        try {
            String accountId = extractAccountIdFromToken(authHeader);
            if (accountId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "Token không hợp lệ"
                ));
            }
            
            // Verify ownership before marking as read
            NotificationResponse current = notificationService.getNotificationById(notificationId);
            if (!current.accountId().equals(accountId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "success", false,
                    "message", "Bạn không có quyền cập nhật thông báo này"
                ));
            }
            
            NotificationResponse notification = notificationService.markAsRead(notificationId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đánh dấu đã đọc thành công",
                "data", notification
            ));
        } catch (RuntimeException e) {
            log.error("Error marking notification as read: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Đã xảy ra lỗi khi cập nhật thông báo"
            ));
        }
    }
    
    @PutMapping("/mark-all-read")
    @PreAuthorize("hasAnyRole('ROLE_CUSTOMER', 'ROLE_EMPLOYEE', 'ROLE_ADMIN')")
    public ResponseEntity<?> markAllAsRead(@RequestHeader("Authorization") String authHeader) {
        log.info("Marking all notifications as read");
        
        try {
            String accountId = extractAccountIdFromToken(authHeader);
            if (accountId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "Token không hợp lệ"
                ));
            }
            
            int updated = notificationService.markAllAsRead(accountId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đánh dấu tất cả thông báo đã đọc thành công",
                "updated", updated
            ));
        } catch (Exception e) {
            log.error("Error marking all as read: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Đã xảy ra lỗi khi cập nhật thông báo"
            ));
        }
    }
    
    @DeleteMapping("/{notificationId}")
    @PreAuthorize("hasAnyRole('ROLE_CUSTOMER', 'ROLE_EMPLOYEE', 'ROLE_ADMIN')")
    public ResponseEntity<?> deleteNotification(
            @PathVariable String notificationId,
            @RequestHeader("Authorization") String authHeader) {
        
        log.info("Deleting notification: {}", notificationId);
        
        try {
            String accountId = extractAccountIdFromToken(authHeader);
            if (accountId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "Token không hợp lệ"
                ));
            }
            
            // Verify ownership before deleting
            NotificationResponse current = notificationService.getNotificationById(notificationId);
            if (!current.accountId().equals(accountId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "success", false,
                    "message", "Bạn không có quyền xóa thông báo này"
                ));
            }
            
            notificationService.deleteNotification(notificationId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Xóa thông báo thành công"
            ));
        } catch (RuntimeException e) {
            log.error("Error deleting notification: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Đã xảy ra lỗi khi xóa thông báo"
            ));
        }
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> createNotification(@Valid @RequestBody NotificationRequest request) {
        log.info("Admin creating notification for account: {}", request.accountId());
        
        try {
            NotificationResponse notification = notificationService.createNotification(request);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Tạo thông báo thành công",
                "data", notification
            ));
        } catch (Exception e) {
            log.error("Error creating notification: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Đã xảy ra lỗi khi tạo thông báo"
            ));
        }
    }
    
    // Helper method to extract account ID from JWT token
    private String extractAccountIdFromToken(String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return null;
            }
            
            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);
            
            if (username == null || !jwtUtil.validateToken(token, username)) {
                return null;
            }
            
            return accountRepository.findByUsername(username)
                    .map(account -> account.getAccountId())
                    .orElse(null);
        } catch (Exception e) {
            log.error("Error extracting account ID from token: {}", e.getMessage());
            return null;
        }
    }
}
