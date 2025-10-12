package iuh.house_keeping_service_be.controllers;

import iuh.house_keeping_service_be.dtos.Chat.request.ChatMessageRequest;
import iuh.house_keeping_service_be.dtos.Chat.response.MessageResponse;
import iuh.house_keeping_service_be.services.MessageService.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void handleChatMessage(@Payload @Valid ChatMessageRequest request, Principal principal) {
        if (principal == null) {
            log.warn("Received chat message without authenticated principal");
            return;
        }
        log.info("Received chat message from user {} for conversation {}", principal.getName(), request.conversationId());
        MessageResponse response = messageService.sendMessage(request, principal.getName());
        log.debug("Broadcasting message {} to conversation {}", response.messageId(), request.conversationId());
        messagingTemplate.convertAndSend("/topic/conversations/" + request.conversationId(), response);
        log.info("Sent message frame for conversation {} to /topic/conversations/{}", request.conversationId(), request.conversationId());
    }

    @MessageExceptionHandler
    public void handleWebSocketError(Throwable throwable, Principal principal) {
        if (principal != null) {
            log.warn("WebSocket error for user {}: {}", principal.getName(), throwable.getMessage());
        } else {
            log.warn("WebSocket error: {}", throwable.getMessage());
        }
    }
}