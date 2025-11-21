package iuh.house_keeping_service_be.dtos.Review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingReviewWebSocketEvent {
    public enum Action {
        ADD,
        REMOVE
    }

    private Action action;
    private PendingReviewResponse payload;
    private String bookingId;
    private String employeeId;
}
