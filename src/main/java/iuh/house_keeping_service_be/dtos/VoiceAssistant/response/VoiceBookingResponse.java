package iuh.house_keeping_service_be.dtos.VoiceAssistant.response;

import iuh.house_keeping_service_be.dtos.Booking.summary.BookingCreationSummary;

public record VoiceBookingResponse(
        boolean success,
        String message,
        String transcription,
        VoiceBookingIntent intent,
        BookingCreationSummary bookingResult,
        Long processingTimeMs
) {
}
