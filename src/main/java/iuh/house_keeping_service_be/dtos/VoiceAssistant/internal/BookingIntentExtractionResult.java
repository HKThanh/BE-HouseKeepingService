package iuh.house_keeping_service_be.dtos.VoiceAssistant.internal;

import iuh.house_keeping_service_be.dtos.Booking.request.BookingCreateRequest;
import iuh.house_keeping_service_be.dtos.VoiceAssistant.response.VoiceBookingIntent;

public record BookingIntentExtractionResult(
        boolean success,
        VoiceBookingIntent intent,
        BookingCreateRequest bookingRequest,
        String errorMessage
) {
}
