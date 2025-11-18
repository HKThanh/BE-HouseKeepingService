package iuh.house_keeping_service_be.dtos.VoiceBooking;

import iuh.house_keeping_service_be.dtos.Booking.request.BookingCreateRequest;
import lombok.Builder;

import java.util.List;
import java.util.Map;

/**
 * Internal DTO for parsed booking information from transcript
 */
@Builder
public record ParsedBookingInfo(
        BookingCreateRequest bookingRequest, // null if parsing failed
        List<String> missingFields,
        Map<String, String> extractedFields,
        double parseConfidence, // 0.0 to 1.0
        String clarificationMessage // Message to ask user for missing info
) {
    public boolean isComplete() {
        return bookingRequest != null && (missingFields == null || missingFields.isEmpty());
    }

    public boolean requiresClarification() {
        return missingFields != null && !missingFields.isEmpty();
    }
}
