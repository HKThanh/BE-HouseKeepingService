package iuh.house_keeping_service_be.dtos.RecurringBooking.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import iuh.house_keeping_service_be.dtos.Chat.ConversationResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Summary response after creating a recurring booking
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecurringBookingCreationSummary {
    private boolean success;
    private String message;
    private RecurringBookingResponse recurringBooking;
    private List<String> generatedBookingIds = new ArrayList<>();
    private Integer totalBookingsToBeCreated; // Estimated number of bookings in the current window
    private Integer expectedBookingsInWindow;
    private Integer generatedBookingsInWindow;
    private Integer generationWindowDays;
    private Double generationProgressPercent;
    private ConversationResponse conversation;
}
