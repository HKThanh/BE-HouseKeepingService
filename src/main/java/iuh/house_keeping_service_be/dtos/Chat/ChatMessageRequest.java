package iuh.house_keeping_service_be.dtos.Chat;

import iuh.house_keeping_service_be.enums.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageRequest {
    @NotBlank(message = "Conversation ID is required")
    private String conversationId;
    
    @NotBlank(message = "Sender ID is required")
    private String senderId;
    
    @NotNull(message = "Message type is required")
    private MessageType messageType;
    
    private String content;
    
    private MultipartFile imageFile;
}
