package iuh.house_keeping_service_be.controllers;

import iuh.house_keeping_service_be.dtos.Chat.ConversationRequest;
import iuh.house_keeping_service_be.dtos.Chat.ConversationResponse;
import iuh.house_keeping_service_be.services.ChatService.ConversationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/conversations")
@Slf4j
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_EMPLOYEE', 'ROLE_ADMIN')")
    public ResponseEntity<?> createConversation(@RequestBody ConversationRequest request) {
        try {
            ConversationResponse response = conversationService.createConversation(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "success", true,
                    "message", "Conversation created successfully",
                    "data", response
            ));
        } catch (Exception e) {
            log.error("Error creating conversation: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to create conversation: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/{conversationId}")
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_EMPLOYEE', 'ROLE_ADMIN')")
    public ResponseEntity<?> getConversationById(@PathVariable String conversationId) {
        try {
            ConversationResponse response = conversationService.getConversationById(conversationId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", response
            ));
        } catch (Exception e) {
            log.error("Error getting conversation: ", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "Conversation not found: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/account/{accountId}")
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_EMPLOYEE', 'ROLE_ADMIN')")
    public ResponseEntity<?> getConversationsByAccount(
            @PathVariable String accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ConversationResponse> conversations = conversationService.getConversationsByAccount(accountId, pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", conversations.getContent());
            response.put("currentPage", conversations.getNumber());
            response.put("totalItems", conversations.getTotalElements());
            response.put("totalPages", conversations.getTotalPages());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting conversations: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to get conversations: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/get-or-create")
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_EMPLOYEE', 'ROLE_ADMIN')")
    public ResponseEntity<?> getOrCreateConversation(
            @RequestParam String customerId,
            @RequestParam String employeeId) {
        try {
            ConversationResponse response = conversationService.getOrCreateConversation(customerId, employeeId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", response
            ));
        } catch (Exception e) {
            log.error("Error getting/creating conversation: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to get/create conversation: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/booking/{bookingId}")
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_EMPLOYEE', 'ROLE_ADMIN')")
    public ResponseEntity<?> getConversationByBooking(@PathVariable String bookingId) {
        try {
            ConversationResponse response = conversationService.getConversationByBooking(bookingId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", response
            ));
        } catch (Exception e) {
            log.error("Error getting conversation by booking: ", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "Conversation not found for booking: " + e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{conversationId}")
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_EMPLOYEE', 'ROLE_ADMIN')")
    public ResponseEntity<?> deleteConversation(@PathVariable String conversationId) {
        try {
            conversationService.deleteConversation(conversationId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Conversation deleted successfully"
            ));
        } catch (Exception e) {
            log.error("Error deleting conversation: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to delete conversation: " + e.getMessage()
            ));
        }
    }
}
