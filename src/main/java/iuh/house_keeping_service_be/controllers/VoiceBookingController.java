package iuh.house_keeping_service_be.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import iuh.house_keeping_service_be.config.JwtUtil;
import iuh.house_keeping_service_be.dtos.VoiceBooking.VoiceBookingRequest;
import iuh.house_keeping_service_be.dtos.VoiceBooking.VoiceBookingResponse;
import iuh.house_keeping_service_be.services.VoiceBookingService.VoiceBookingService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * REST Controller for voice booking operations
 * Provides endpoint for creating bookings via voice input
 */
@RestController
@RequestMapping("/api/v1/customer/bookings/voice")
@Slf4j
@RequiredArgsConstructor
public class VoiceBookingController {

    private final VoiceBookingService voiceBookingService;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    /**
     * Create booking from voice input
     * POST /api/v1/customer/bookings/voice
     * 
     * @param audio Audio file (multipart)
     * @param hints Optional JSON hints for better parsing
     * @param authHeader JWT authorization header
     * @return VoiceBookingResponse with processing result
     */
    @PostMapping(consumes = {"multipart/form-data"})
    @PreAuthorize("hasAnyRole('ROLE_CUSTOMER', 'ROLE_ADMIN')")
    public ResponseEntity<?> createVoiceBooking(
            @RequestPart(value = "audio", required = true) @NotNull MultipartFile audio,
            @RequestPart(value = "hints", required = false) String hints,
            @RequestHeader("Authorization") String authHeader
    ) {
        log.info("Received voice booking request, audio size: {} bytes", audio.getSize());

        try {
            // Validate Whisper service is enabled
            if (!voiceBookingService.isVoiceBookingEnabled()) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
                        Map.of(
                                "success", false,
                                "message", "Voice booking service is currently unavailable"
                        )
                );
            }

            // Extract customer ID from JWT token
            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);

            if (username == null || !jwtUtil.validateToken(token, username)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        Map.of(
                                "success", false,
                                "message", "Invalid or expired token"
                        )
                );
            }

            // Validate voice booking request
            VoiceBookingRequest request = new VoiceBookingRequest(audio, hints);
            try {
                request.validateAudio();
            } catch (IllegalArgumentException e) {
                log.warn("Audio validation failed: {}", e.getMessage());
                return ResponseEntity.badRequest().body(
                        Map.of(
                                "success", false,
                                "message", "Invalid audio file: " + e.getMessage()
                        )
                );
            }

            // Parse hints JSON if provided
            Map<String, Object> hintsMap = request.parseHints();

            // Process voice booking synchronously for now
            // Can be switched to async based on configuration
            log.info("Processing voice booking for user: {}", username);
            VoiceBookingResponse response = voiceBookingService.processVoiceBooking(
                    audio,
                    username, // Using username as customer ID for now
                    hintsMap
            );

            // Return appropriate status code based on result
            if (response.success()) {
                return ResponseEntity.ok(response);
            } else if ("PARTIAL".equals(response.status())) {
                // Partial success - need clarification
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response);
            } else {
                // Failed
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

        } catch (IllegalArgumentException e) {
            log.error("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "message", e.getMessage()
                    )
            );
        } catch (Exception e) {
            log.error("Error processing voice booking: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of(
                            "success", false,
                            "message", "Internal server error: " + e.getMessage()
                    )
            );
        }
    }

    /**
     * Get voice booking request status
     * GET /api/v1/customer/bookings/voice/{requestId}
     * 
     * @param requestId Voice booking request ID
     * @param authHeader JWT authorization header
     * @return Voice booking request details
     */
    @GetMapping("/{requestId}")
    @PreAuthorize("hasAnyRole('ROLE_CUSTOMER', 'ROLE_ADMIN')")
    public ResponseEntity<?> getVoiceBookingRequest(
            @PathVariable String requestId,
            @RequestHeader("Authorization") String authHeader
    ) {
        log.info("Getting voice booking request: {}", requestId);

        try {
            // Validate JWT token
            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);

            if (username == null || !jwtUtil.validateToken(token, username)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        Map.of(
                                "success", false,
                                "message", "Invalid or expired token"
                        )
                );
            }

            // Get voice booking request
            var voiceRequest = voiceBookingService.getVoiceBookingRequest(requestId);

            // Check authorization (customer can only see their own requests)
            // TODO: Add proper authorization check

            // Build response data map
            Map<String, Object> data = new java.util.HashMap<>();
            data.put("id", voiceRequest.getId());
            data.put("status", voiceRequest.getStatus());
            data.put("transcript", voiceRequest.getTranscript());
            data.put("confidenceScore", voiceRequest.getConfidenceScore() != null ? 
                    voiceRequest.getConfidenceScore() : null);
            data.put("processingTimeMs", voiceRequest.getProcessingTimeMs());
            data.put("bookingId", voiceRequest.getBooking() != null ? 
                    voiceRequest.getBooking().getBookingId() : null);
            data.put("missingFields", voiceRequest.getMissingFields());
            data.put("errorMessage", voiceRequest.getErrorMessage());
            data.put("createdAt", voiceRequest.getCreatedAt());

            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "data", data
                    )
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of(
                            "success", false,
                            "message", e.getMessage()
                    )
            );
        } catch (Exception e) {
            log.error("Error getting voice booking request: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of(
                            "success", false,
                            "message", "Internal server error: " + e.getMessage()
                    )
            );
        }
    }

    /**
     * Continue a partial voice booking with additional information
     * POST /api/v1/customer/bookings/voice/continue
     * 
     * @param requestId Original voice booking request ID
     * @param audio Optional additional audio file
     * @param additionalText Optional text with missing information
     * @param explicitFields Optional JSON with explicit field values
     * @param authHeader JWT authorization header
     * @return VoiceBookingResponse with updated result
     */
    @PostMapping(value = "/continue", consumes = {"multipart/form-data"})
    @PreAuthorize("hasAnyRole('ROLE_CUSTOMER', 'ROLE_ADMIN')")
    public ResponseEntity<?> continueVoiceBooking(
            @RequestPart(value = "requestId", required = true) String requestId,
            @RequestPart(value = "audio", required = false) MultipartFile audio,
            @RequestPart(value = "additionalText", required = false) String additionalText,
            @RequestPart(value = "explicitFields", required = false) String explicitFields,
            @RequestHeader("Authorization") String authHeader
    ) {
        log.info("Received continue voice booking request for: {}", requestId);

        try {
            // Validate JWT token
            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);

            if (username == null || !jwtUtil.validateToken(token, username)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        Map.of(
                                "success", false,
                                "message", "Invalid or expired token"
                        )
                );
            }

            // Validate that at least one source is provided
            boolean hasAudio = audio != null && !audio.isEmpty();
            boolean hasText = additionalText != null && !additionalText.isBlank();
            boolean hasExplicit = explicitFields != null && !explicitFields.isBlank();

            if (!hasAudio && !hasText && !hasExplicit) {
                return ResponseEntity.badRequest().body(
                        Map.of(
                                "success", false,
                                "message", "At least one of audio, additionalText, or explicitFields must be provided"
                        )
                );
            }

            // Parse explicit fields if provided
            Map<String, String> explicitFieldsMap = null;
            if (hasExplicit) {
                try {
                    explicitFieldsMap = objectMapper.readValue(
                            explicitFields,
                            new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {}
                    );
                } catch (Exception e) {
                    return ResponseEntity.badRequest().body(
                            Map.of(
                                    "success", false,
                                    "message", "Invalid JSON format for explicitFields: " + e.getMessage()
                            )
                    );
                }
            }

            // Process continue request
            log.info("Continuing voice booking for user: {}", username);
            VoiceBookingResponse response = voiceBookingService.continueVoiceBooking(
                    requestId,
                    audio,
                    additionalText,
                    explicitFieldsMap,
                    username
            );

            // Return appropriate status code based on result
            if (response.success()) {
                return ResponseEntity.ok(response);
            } else if ("PARTIAL".equals(response.status())) {
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

        } catch (IllegalArgumentException e) {
            log.error("Invalid continue request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "message", e.getMessage()
                    )
            );
        } catch (Exception e) {
            log.error("Error continuing voice booking: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of(
                            "success", false,
                            "message", "Internal server error: " + e.getMessage()
                    )
            );
        }
    }

    /**
     * Check if voice booking is available
     * GET /api/v1/customer/bookings/voice/status
     * 
     * @return Service availability status
     */
    @GetMapping("/status")
    public ResponseEntity<?> getServiceStatus() {
        boolean enabled = voiceBookingService.isVoiceBookingEnabled();
        
        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "voiceBookingEnabled", enabled,
                        "message", enabled ? 
                                "Voice booking service is available" : 
                                "Voice booking service is currently unavailable"
                )
        );
    }
}
