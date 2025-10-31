package iuh.house_keeping_service_be.dtos.Chat;

import iuh.house_keeping_service_be.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageWebSocketDTO {
    private String messageId;
    private String conversationId;
    private String senderId;
    private String senderName;
    private MessageType messageType;
    private String content;
    private String imageUrl;
    private LocalDateTime timestamp;
}
