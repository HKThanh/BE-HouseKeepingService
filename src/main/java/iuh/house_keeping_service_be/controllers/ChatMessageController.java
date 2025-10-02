package iuh.house_keeping_service_be.controllers;

import iuh.house_keeping_service_be.dtos.ChatMessage.request.ChatMessageDeleteRequest;
import iuh.house_keeping_service_be.dtos.ChatMessage.request.ChatMessageRecallRequest;
import iuh.house_keeping_service_be.dtos.ChatMessage.request.ChatMessageReplyRequest;
import iuh.house_keeping_service_be.dtos.ChatMessage.request.ChatMessageSendRequest;
import iuh.house_keeping_service_be.dtos.ChatMessage.response.ChatMessageResponse;
import iuh.house_keeping_service_be.dtos.EmployeeSchedule.ApiResponse;
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
}