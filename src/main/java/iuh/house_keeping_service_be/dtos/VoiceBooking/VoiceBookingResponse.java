package iuh.house_keeping_service_be.dtos.VoiceBooking;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.util.List;
import java.util.Map;

/**
 * Response DTO for voice booking processing
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record VoiceBookingResponse(
        boolean success,
        String message,
        String requestId, // UUID of voice_booking_request
        String status, // PENDING, PROCESSING, COMPLETED, FAILED, PARTIAL
        
        // Transcript data
        String transcript,
        Double confidenceScore,
        Integer processingTimeMs,
        
        // Booking result (if completed)
        String bookingId,
        
        // Missing fields (if partial)
        List<String> missingFields,
        String clarificationMessage,
        
        // Extracted/Understood information (for FE to display)
        Map<String, Object> extractedInfo,
        
        // Error details (if failed)
        String errorDetails
) {
    /**
     * Factory method for accepted async request
     */
    public static VoiceBookingResponse accepted(String requestId) {
        return VoiceBookingResponse.builder()
                .success(true)
                .message("Voice booking request accepted for processing")
                .requestId(requestId)
                .status("PROCESSING")
                .build();
    }

    /**
     * Factory method for immediate success
     */
    public static VoiceBookingResponse completed(
            String requestId,
            String bookingId,
            String transcript,
            Double confidenceScore,
            Integer processingTimeMs
    ) {
        return VoiceBookingResponse.builder()
                .success(true)
                .message("Booking created successfully from voice input")
                .requestId(requestId)
                .status("COMPLETED")
                .bookingId(bookingId)
                .transcript(transcript)
                .confidenceScore(confidenceScore)
                .processingTimeMs(processingTimeMs)
                .build();
    }

    /**
     * Factory method for partial success (missing fields)
     */
    public static VoiceBookingResponse partial(
            String requestId,
            String transcript,
            List<String> missingFields,
            String clarificationMessage,
            Map<String, Object> extractedInfo,
            Double confidenceScore,
            Integer processingTimeMs
    ) {
        return VoiceBookingResponse.builder()
                .success(false)
                .message("Could not extract all required information from voice input")
                .requestId(requestId)
                .status("PARTIAL")
                .transcript(transcript)
                .missingFields(missingFields)
                .clarificationMessage(clarificationMessage)
                .extractedInfo(extractedInfo)
                .confidenceScore(confidenceScore)
                .processingTimeMs(processingTimeMs)
                .build();
    }

    /**
     * Factory method for failure
     */
    public static VoiceBookingResponse failed(
            String requestId,
            String errorMessage,
            String errorDetails
    ) {
        return VoiceBookingResponse.builder()
                .success(false)
                .message(errorMessage)
                .requestId(requestId)
                .status("FAILED")
                .errorDetails(errorDetails)
                .build();
    }
}
