package iuh.house_keeping_service_be.controllers;

import iuh.house_keeping_service_be.config.JwtUtil;
import iuh.house_keeping_service_be.dtos.Chat.request.CreateChatRoomRequest;
import iuh.house_keeping_service_be.dtos.Chat.response.ChatMessageResponse;
import iuh.house_keeping_service_be.dtos.Chat.response.ChatRoomResponse;
import iuh.house_keeping_service_be.models.Account;
import iuh.house_keeping_service_be.repositories.AccountRepository;
import iuh.house_keeping_service_be.services.ChatService.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/chat")
@Slf4j
public class ChatController {

    private final ChatService chatService;
    private final JwtUtil jwtUtil;
    private final AccountRepository accountRepository;

    @PostMapping("/rooms")
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_EMPLOYEE', 'ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> createChatRoom(@Valid @RequestBody CreateChatRoomRequest request,
                                                              @RequestHeader("Authorization") String authHeader) {
        try {
            String accountId = resolveAccountId(authHeader);
            ChatRoomResponse response = chatService.createOrGetChatRoom(request.bookingId(), accountId);
            log.info("Chat room {} ready for booking {}", response.chatRoomId(), request.bookingId());
            return ResponseEntity.ok(success(response));
        } catch (AccessDeniedException ex) {
            log.warn("Access denied when creating chat room for booking {}: {}", request.bookingId(), ex.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error(ex.getMessage()));
        } catch (IllegalArgumentException ex) {
            log.error("Invalid chat room creation request: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(error(ex.getMessage()));
        } catch (Exception ex) {
            log.error("Unexpected error while creating chat room", ex);
            return ResponseEntity.internalServerError().body(error("Không thể khởi tạo cuộc trò chuyện"));
        }
    }

    @GetMapping("/rooms/{bookingId}")
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_EMPLOYEE', 'ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> getChatRoomByBooking(@PathVariable String bookingId,
                                                                    @RequestHeader("Authorization") String authHeader) {
        try {
            String accountId = resolveAccountId(authHeader);
            ChatRoomResponse response = chatService.getChatRoomByBooking(bookingId, accountId);
            return ResponseEntity.ok(success(response));
        } catch (AccessDeniedException ex) {
            log.warn("Access denied when fetching chat room for booking {}: {}", bookingId, ex.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error(ex.getMessage()));
        } catch (IllegalArgumentException ex) {
            log.error("Cannot fetch chat room for booking {}: {}", bookingId, ex.getMessage());
            return ResponseEntity.badRequest().body(error(ex.getMessage()));
        } catch (Exception ex) {
            log.error("Unexpected error while fetching chat room for booking {}", bookingId, ex);
            return ResponseEntity.internalServerError().body(error("Không thể lấy thông tin cuộc trò chuyện"));
        }
    }

    @GetMapping("/rooms/{chatRoomId}/messages")
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_EMPLOYEE', 'ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> getMessages(@PathVariable String chatRoomId,
                                                           @RequestHeader("Authorization") String authHeader,
                                                           @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "20") int size) {
        try {
            String accountId = resolveAccountId(authHeader);
            Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
            Page<ChatMessageResponse> messages = chatService.getMessages(chatRoomId, pageable, accountId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", messages.getContent());
            response.put("currentPage", messages.getNumber());
            response.put("totalItems", messages.getTotalElements());
            response.put("totalPages", messages.getTotalPages());
            return ResponseEntity.ok(response);
        } catch (AccessDeniedException ex) {
            log.warn("Access denied when fetching messages for room {}: {}", chatRoomId, ex.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error(ex.getMessage()));
        } catch (IllegalArgumentException ex) {
            log.error("Invalid request when listing messages: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(error(ex.getMessage()));
        } catch (Exception ex) {
            log.error("Unexpected error while listing messages for room {}", chatRoomId, ex);
            return ResponseEntity.internalServerError().body(error("Không thể lấy danh sách tin nhắn"));
        }
    }

    @PostMapping("/messages/{messageId}/revoke")
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_EMPLOYEE', 'ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> revokeMessage(@PathVariable String messageId,
                                                             @RequestHeader("Authorization") String authHeader) {
        try {
            String accountId = resolveAccountId(authHeader);
            var response = chatService.revokeMessage(messageId, accountId);
            return ResponseEntity.ok(success(response));
        } catch (AccessDeniedException ex) {
            log.warn("Access denied when revoking message {}: {}", messageId, ex.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error(ex.getMessage()));
        } catch (IllegalArgumentException ex) {
            log.error("Invalid revoke message request: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(error(ex.getMessage()));
        } catch (Exception ex) {
            log.error("Unexpected error while revoking message {}", messageId, ex);
            return ResponseEntity.internalServerError().body(error("Không thể thu hồi tin nhắn"));
        }
    }

    private String resolveAccountId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Thiếu thông tin xác thực");
        }
        String token = authHeader.substring(7);
        String username = jwtUtil.extractUsername(token);
        return accountRepository.findByUsername(username)
                .map(Account::getAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));
    }

    private Map<String, Object> success(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        return response;
    }

    private Map<String, Object> error(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }
}