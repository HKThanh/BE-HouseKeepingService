package iuh.house_keeping_service_be.controllers;

import iuh.house_keeping_service_be.dtos.Chat.request.ChatMessagePayload;
import iuh.house_keeping_service_be.dtos.Chat.request.RevokeMessageRequest;
import iuh.house_keeping_service_be.dtos.Chat.response.ChatMessageResponse;
import iuh.house_keeping_service_be.services.ChatService.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat/send")
    public void handleSendMessage(@Valid @Payload ChatMessagePayload payload, Principal principal) {
        if (principal == null) {
            throw new AccessDeniedException("Người dùng chưa xác thực");
        }
        try {
            ChatMessageResponse response = chatService.sendMessage(payload, principal.getName());
            messagingTemplate.convertAndSend("/topic/chat/" + response.chatRoomId(), response);
        } catch (AccessDeniedException ex) {
            log.warn("Send message denied for account {}: {}", principal.getName(), ex.getMessage());
            throw ex;
        } catch (IllegalArgumentException ex) {
            log.error("Send message failed: {}", ex.getMessage());
            throw ex;
        }
    }

    @MessageMapping("/chat/revoke")
    public void handleRevokeMessage(@Valid @Payload RevokeMessageRequest payload, Principal principal) {
        if (principal == null) {
            throw new AccessDeniedException("Người dùng chưa xác thực");
        }
        try {
            ChatMessageResponse response = chatService.revokeMessage(payload.messageId(), principal.getName());
            messagingTemplate.convertAndSend("/topic/chat/" + response.chatRoomId(), response);
        } catch (AccessDeniedException ex) {
            log.warn("Revoke message denied for account {}: {}", principal.getName(), ex.getMessage());
            throw ex;
        } catch (IllegalArgumentException ex) {
            log.error("Revoke message failed: {}", ex.getMessage());
            throw ex;
        }
    }
}