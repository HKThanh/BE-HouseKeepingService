package iuh.house_keeping_service_be.dtos.Chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationWebSocketDTO {
    private String conversationId;
    private String participantId;
    private String senderId;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private Long unreadCount;
}
