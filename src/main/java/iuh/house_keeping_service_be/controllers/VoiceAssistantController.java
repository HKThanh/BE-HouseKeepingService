package iuh.house_keeping_service_be.controllers;

import iuh.house_keeping_service_be.config.JwtUtil;
import iuh.house_keeping_service_be.dtos.VoiceAssistant.internal.BookingIntentExtractionResult;
import iuh.house_keeping_service_be.dtos.VoiceAssistant.internal.VoiceProcessingResult;
import iuh.house_keeping_service_be.dtos.VoiceAssistant.response.VoiceBookingResponse;
import iuh.house_keeping_service_be.dtos.VoiceAssistant.response.VoiceTranscriptionResponse;
import iuh.house_keeping_service_be.services.VoiceAssistantService.VoiceAssistantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/voice-assistant")
@Slf4j
@RequiredArgsConstructor
public class VoiceAssistantController {
    
    private final VoiceAssistantService voiceAssistantService;
    private final JwtUtil jwtUtil;
    
    /**
     * Transcribe audio to text only
     * POST /api/v1/voice-assistant/transcribe
     */
    @PostMapping(value = "/transcribe", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'ADMIN')")
    public ResponseEntity<?> transcribeVoice(
            @RequestParam("audio") MultipartFile audioFile,
            @RequestHeader("Authorization") String authHeader) {
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Validate token
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        Map.of("success", false, "message", "Token không hợp lệ")
                );
            }
            
            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);
            
            if (username == null || !jwtUtil.validateToken(token, username)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        Map.of("success", false, "message", "Token không hợp lệ")
                );
            }
            
            // Validate audio file
            if (audioFile == null || audioFile.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        Map.of("success", false, "message", "File âm thanh không được để trống")
                );
            }
            
            // Process voice to text
            VoiceProcessingResult result = voiceAssistantService.processVoiceToText(audioFile);
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            if (result.success()) {
                return ResponseEntity.ok(
                        new VoiceTranscriptionResponse(
                                true,
                                result.transcription(),
                                "Chuyển đổi giọng nói thành công",
                                processingTime
                        )
                );
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                        new VoiceTranscriptionResponse(
                                false,
                                null,
                                result.errorMessage(),
                                processingTime
                        )
                );
            }
            
        } catch (Exception e) {
            log.error("Error in voice transcription", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of(
                            "success", false,
                            "message", "Lỗi khi xử lý giọng nói: " + e.getMessage()
                    )
            );
        }
    }
    
    /**
     * Extract booking intent from text
     * POST /api/v1/voice-assistant/extract-intent
     */
    @PostMapping("/extract-intent")
    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'ADMIN')")
    public ResponseEntity<?> extractBookingIntent(
            @RequestBody Map<String, String> request,
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            // Validate token
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        Map.of("success", false, "message", "Token không hợp lệ")
                );
            }
            
            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);
            
            if (username == null || !jwtUtil.validateToken(token, username)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        Map.of("success", false, "message", "Token không hợp lệ")
                );
            }
            
            String transcription = request.get("transcription");
            String customerId = request.get("customerId");
            
            if (transcription == null || transcription.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                        Map.of("success", false, "message", "Transcription không được để trống")
                );
            }
            
            if (customerId == null || customerId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                        Map.of("success", false, "message", "Customer ID không được để trống")
                );
            }
            
            // Extract intent
            BookingIntentExtractionResult result = voiceAssistantService.extractBookingIntent(
                    transcription, customerId);
            
            if (result.success()) {
                return ResponseEntity.ok(
                        Map.of(
                                "success", true,
                                "message", "Trích xuất thông tin thành công",
                                "intent", result.intent(),
                                "bookingRequest", result.bookingRequest()
                        )
                );
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                        Map.of(
                                "success", false,
                                "message", result.errorMessage()
                        )
                );
            }
            
        } catch (Exception e) {
            log.error("Error extracting booking intent", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of(
                            "success", false,
                            "message", "Lỗi khi trích xuất thông tin: " + e.getMessage()
                    )
            );
        }
    }
    
    /**
     * Complete voice booking flow (transcribe + extract + create booking)
     * POST /api/v1/voice-assistant/book
     */
    @PostMapping(value = "/book", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('CUSTOMER')")
    public ResponseEntity<?> createVoiceBooking(
            @RequestParam("audio") MultipartFile audioFile,
            @RequestParam("customerId") String customerId,
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            // Validate token
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        Map.of("success", false, "message", "Token không hợp lệ")
                );
            }
            
            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);
            
            if (username == null || !jwtUtil.validateToken(token, username)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        Map.of("success", false, "message", "Token không hợp lệ")
                );
            }
            
            // Validate audio file
            if (audioFile == null || audioFile.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        Map.of("success", false, "message", "File âm thanh không được để trống")
                );
            }
            
            if (customerId == null || customerId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                        Map.of("success", false, "message", "Customer ID không được để trống")
                );
            }
            
            // Process complete voice booking
            VoiceBookingResponse response = voiceAssistantService.processVoiceBooking(
                    audioFile, customerId);
            
            if (response.success()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
        } catch (Exception e) {
            log.error("Error in voice booking", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of(
                            "success", false,
                            "message", "Lỗi khi đặt lịch bằng giọng nói: " + e.getMessage()
                    )
            );
        }
    }
    
    /**
     * Test endpoint to check if voice assistant is configured properly
     * GET /api/v1/voice-assistant/health
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "message", "Voice Assistant API is running",
                        "features", Map.of(
                                "transcription", "available",
                                "intentExtraction", "available",
                                "voiceBooking", "available"
                        )
                )
        );
    }
}
