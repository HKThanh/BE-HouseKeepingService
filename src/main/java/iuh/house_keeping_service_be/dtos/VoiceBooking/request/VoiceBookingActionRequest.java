package iuh.house_keeping_service_be.dtos.VoiceBooking.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Simple request body for confirm/cancel actions on a voice booking draft.
 */
public record VoiceBookingActionRequest(
        @NotBlank(message = "requestId is required")
        String requestId
) {
}
