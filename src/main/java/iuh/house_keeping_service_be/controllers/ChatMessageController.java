package iuh.house_keeping_service_be.controllers;

import iuh.house_keeping_service_be.dtos.ChatMessage.request.ChatMessageDeleteRequest;
import iuh.house_keeping_service_be.dtos.ChatMessage.request.ChatMessageRecallRequest;
import iuh.house_keeping_service_be.dtos.ChatMessage.request.ChatMessageReplyRequest;
import iuh.house_keeping_service_be.dtos.ChatMessage.request.ChatMessageSendRequest;
import iuh.house_keeping_service_be.dtos.ChatMessage.response.ChatMessageResponse;
import iuh.house_keeping_service_be.dtos.EmployeeSchedule.ApiResponse;
import iuh.house_keeping_service_be.models.ChatRoom;
import iuh.house_keeping_service_be.repositories.ChatRoomRepository;
import iuh.house_keeping_service_be.services.ChatMessageService.ChatMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chat/messages")
@RequiredArgsConstructor
@Slf4j
public class ChatMessageController {

    private final ChatMessageService chatMessageService;
    private final ChatRoomRepository chatRoomRepository;

    @PostMapping("/{chatRoomId}")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendMessage(
            @PathVariable String chatRoomId,
            @Valid @RequestBody ChatMessageSendRequest request) {
        log.info("Sending message to chat room {} by {}", chatRoomId, request.senderAccountId());
        ChatMessageResponse response = chatMessageService.sendMessage(chatRoomId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Gửi tin nhắn thành công", response));
    }

    @PostMapping("/{messageId}/reply")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> replyMessage(
            @PathVariable String messageId,
            @Valid @RequestBody ChatMessageReplyRequest request) {
        log.info("Replying to message {} by {}", messageId, request.senderAccountId());
        ChatMessageResponse response = chatMessageService.replyMessage(messageId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Trả lời tin nhắn thành công", response));
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> deleteMessage(
            @PathVariable String messageId,
            @Valid @RequestBody ChatMessageDeleteRequest request) {
        log.info("Deleting message {} by {}", messageId, request.accountId());
        ChatMessageResponse response = chatMessageService.deleteMessage(messageId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Xóa tin nhắn thành công", response));
    }

    @PostMapping("/{messageId}/recall")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> recallMessage(
            @PathVariable String messageId,
            @Valid @RequestBody ChatMessageRecallRequest request) {
        log.info("Recalling message {} by {}", messageId, request.accountId());
        ChatMessageResponse response = chatMessageService.recallMessage(messageId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Thu hồi tin nhắn thành công", response));
    }

    // Debug endpoint to inspect chat room participants
    @GetMapping("/debug/chatroom/{chatRoomId}")
    public ResponseEntity<ApiResponse<Object>> debugChatRoom(@PathVariable String chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElse(null);
        if (chatRoom == null) {
            return ResponseEntity.notFound().build();
        }
        
        Object debugInfo = new Object() {
            public final String chatRoomId = chatRoom.getChatRoomId();
            public final String customerAccountId = chatRoom.getCustomerAccount() != null ? 
                chatRoom.getCustomerAccount().getAccountId() : null;
            public final String customerUsername = chatRoom.getCustomerAccount() != null ? 
                chatRoom.getCustomerAccount().getUsername() : null;
            public final String employeeAccountId = chatRoom.getEmployeeAccount() != null ? 
                chatRoom.getEmployeeAccount().getAccountId() : null;
            public final String employeeUsername = chatRoom.getEmployeeAccount() != null ? 
                chatRoom.getEmployeeAccount().getUsername() : null;
        };
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Chat room debug info", debugInfo));
    }

    // Debug endpoint to add participants to chat rooms
    @PostMapping("/debug/chatroom/{chatRoomId}/participants/{accountId}")
    public ResponseEntity<ApiResponse<String>> addParticipant(@PathVariable String chatRoomId, @PathVariable String accountId) {
        boolean success = chatMessageService.addParticipantToChatRoom(chatRoomId, accountId);
        if (success) {
            return ResponseEntity.ok(new ApiResponse<>(true, "Participant added successfully", "Account " + accountId + " added to chat room " + chatRoomId));
        } else {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Failed to add participant", "Could not add account " + accountId + " to chat room " + chatRoomId));
        }
    }
}