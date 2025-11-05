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
public class ConversationResponse {
    private String conversationId;
    private String customerId;
    private String customerName;
    private String customerAvatar;
    private String employeeId;
    private String employeeName;
    private String employeeAvatar;
    private String bookingId;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private Boolean isActive;
    private Boolean canChat; // Flag để kiểm tra quyền chat
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
