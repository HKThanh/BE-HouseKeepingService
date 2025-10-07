package iuh.house_keeping_service_be.controllers;

import iuh.house_keeping_service_be.dtos.Chat.response.ConversationSummaryResponse;
import iuh.house_keeping_service_be.dtos.Chat.response.MessagePageResponse;
import iuh.house_keeping_service_be.models.Account;
import iuh.house_keeping_service_be.repositories.AccountRepository;
import iuh.house_keeping_service_be.services.ChatService.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final AccountRepository accountRepository;

    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationSummaryResponse>> getConversations(Authentication authentication) {
        String accountId = resolveAccountId(authentication);
        return ResponseEntity.ok(chatService.getUserConversations(accountId));
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<MessagePageResponse> getConversationMessages(Authentication authentication,
                                                                       @PathVariable String conversationId,
                                                                       @RequestParam(defaultValue = "0") int page,
                                                                       @RequestParam(defaultValue = "20") int size) {
        String accountId = resolveAccountId(authentication);
        return ResponseEntity.ok(chatService.getConversationMessages(accountId, conversationId, page, size));
    }

    private String resolveAccountId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Người dùng chưa đăng nhập");
        }

        String username = authentication.getName();
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new AccessDeniedException("Không tìm thấy tài khoản"));
        return account.getAccountId();
    }
}