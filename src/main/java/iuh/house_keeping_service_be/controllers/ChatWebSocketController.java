package iuh.house_keeping_service_be.controllers;

import iuh.house_keeping_service_be.dtos.Chat.request.RecallMessageRequest;
import iuh.house_keeping_service_be.dtos.Chat.request.SendMessageRequest;
import iuh.house_keeping_service_be.dtos.Chat.response.ChatMessageResponse;
import iuh.house_keeping_service_be.services.ChatService.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.security.access.AccessDeniedException;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat/send")
    public void handleSendMessage(@Valid SendMessageRequest request, Principal principal) {
        String accountId = resolvePrincipal(principal);
        ChatMessageResponse response = chatService.sendMessage(accountId, request);
        log.debug("Account {} gửi tin nhắn tới cuộc hội thoại {}", accountId, response.conversationId());
        messagingTemplate.convertAndSend(destination(response.conversationId()), response);
    }

    @MessageMapping("/chat/recall")
    public void handleRecallMessage(@Valid RecallMessageRequest request, Principal principal) {
        String accountId = resolvePrincipal(principal);
        ChatMessageResponse response = chatService.recallMessage(accountId, request.messageId());
        log.debug("Account {} thu hồi tin nhắn {}", accountId, response.messageId());
        messagingTemplate.convertAndSend(destination(response.conversationId()), response);
    }

    private String destination(String conversationId) {
        return "/topic/conversations/" + conversationId;
    }

    private String resolvePrincipal(Principal principal) {
        if (principal == null) {
            throw new AccessDeniedException("Người dùng chưa được xác thực");
        }
        return principal.getName();
    }
}