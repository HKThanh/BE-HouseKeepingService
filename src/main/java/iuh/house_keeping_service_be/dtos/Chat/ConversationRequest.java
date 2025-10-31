package iuh.house_keeping_service_be.dtos.Chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationRequest {
    private String customerId;
    private String employeeId;
    private String bookingId;
}
