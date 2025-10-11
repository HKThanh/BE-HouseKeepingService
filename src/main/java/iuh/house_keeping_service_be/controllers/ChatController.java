package iuh.house_keeping_service_be.controllers;

import iuh.house_keeping_service_be.dtos.Chat.response.AttachmentUploadResponse;
import iuh.house_keeping_service_be.dtos.Chat.response.ConversationResponse;
import iuh.house_keeping_service_be.dtos.Chat.response.MessageResponse;
import iuh.house_keeping_service_be.services.ConversationService.ConversationService;
import iuh.house_keeping_service_be.services.MessageService.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ConversationService conversationService;
    private final MessageService messageService;

    @PostMapping("/conversations/by-booking/{bookingId}")
    @PreAuthorize("hasAnyRole('ROLE_CUSTOMER', 'ROLE_EMPLOYEE', 'ROLE_ADMIN')")
    public ResponseEntity<ConversationResponse> createOrGetConversation(@PathVariable String bookingId,
                                                                        Authentication authentication) {
        log.info("Request to create/fetch conversation for booking {} by user {}", bookingId, authentication.getName());
        ConversationResponse response = conversationService.getOrCreateConversation(bookingId, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/conversations/{conversationId}/messages")
    @PreAuthorize("hasAnyRole('ROLE_CUSTOMER', 'ROLE_EMPLOYEE', 'ROLE_ADMIN')")
    public ResponseEntity<Page<MessageResponse>> getMessages(@PathVariable String conversationId,
                                                             @RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "20") int size,
                                                             @RequestParam(defaultValue = "createdAt,asc") String[] sort,
                                                             Authentication authentication) {
        if (page < 0) {
            page = 0;
        }
        if (size <= 0 || size > 100) {
            size = 20;
        }
        String sortProperty = sort.length > 0 ? sort[0] : "createdAt";
        Sort.Direction direction = Sort.Direction.ASC;
        if (sort.length > 1) {
            try {
                direction = Sort.Direction.fromString(sort[1]);
            } catch (IllegalArgumentException ex) {
                direction = Sort.Direction.ASC;
            }
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(new Sort.Order(direction, sortProperty)));
        Page<MessageResponse> responses = messageService.getConversationMessages(conversationId, pageable, authentication.getName());
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/conversations/{conversationId}/attachments")
    @PreAuthorize("hasAnyRole('ROLE_CUSTOMER', 'ROLE_EMPLOYEE', 'ROLE_ADMIN')")
    public ResponseEntity<AttachmentUploadResponse> uploadAttachment(@PathVariable String conversationId,
                                                                     @RequestParam("file") MultipartFile file,
                                                                     Authentication authentication) {
        log.info("User {} uploading attachment for conversation {}", authentication.getName(), conversationId);
        AttachmentUploadResponse response = conversationService.uploadAttachment(conversationId, file, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/messages/{messageId}/recall")
    @PreAuthorize("hasAnyRole('ROLE_CUSTOMER', 'ROLE_EMPLOYEE', 'ROLE_ADMIN')")
    public ResponseEntity<MessageResponse> recallMessage(@PathVariable String messageId,
                                                         Authentication authentication) {
        log.info("User {} recalling message {}", authentication.getName(), messageId);
        MessageResponse response = messageService.recallMessage(messageId, authentication.getName());
        return ResponseEntity.ok(response);
    }
}