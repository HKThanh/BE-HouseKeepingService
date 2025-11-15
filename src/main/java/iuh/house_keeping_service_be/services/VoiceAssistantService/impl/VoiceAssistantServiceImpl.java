package iuh.house_keeping_service_be.services.VoiceAssistantService.impl;

import com.assemblyai.api.AssemblyAI;
import com.assemblyai.api.resources.transcripts.types.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import iuh.house_keeping_service_be.dtos.Booking.request.BookingCreateRequest;
import iuh.house_keeping_service_be.dtos.Booking.request.BookingDetailRequest;
import iuh.house_keeping_service_be.dtos.Booking.summary.BookingCreationSummary;
import iuh.house_keeping_service_be.dtos.VoiceAssistant.internal.BookingIntentExtractionResult;
import iuh.house_keeping_service_be.dtos.VoiceAssistant.internal.VoiceProcessingResult;
import iuh.house_keeping_service_be.dtos.VoiceAssistant.response.VoiceBookingIntent;
import iuh.house_keeping_service_be.dtos.VoiceAssistant.response.VoiceBookingResponse;
import com.assemblyai.api.AssemblyAI;
import com.assemblyai.api.resources.transcripts.types.*;
import iuh.house_keeping_service_be.models.Address;
import iuh.house_keeping_service_be.models.Customer;
import iuh.house_keeping_service_be.repositories.CustomerRepository;
import iuh.house_keeping_service_be.repositories.ServiceRepository;
import iuh.house_keeping_service_be.services.AddressService.AddressService;
import iuh.house_keeping_service_be.services.BookingService.BookingService;
import iuh.house_keeping_service_be.services.VoiceAssistantService.VoiceAssistantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@org.springframework.stereotype.Service
@Slf4j
@RequiredArgsConstructor
public class VoiceAssistantServiceImpl implements VoiceAssistantService {
    
    private final BookingService bookingService;
    private final CustomerRepository customerRepository;
    private final ServiceRepository serviceRepository;
    private final AddressService addressService;
    private final ChatClient.Builder chatClientBuilder;
    private final ObjectMapper objectMapper;
    
    @Value("${voice.assistant.assemblyai.api-key:}")
    private String assemblyAiApiKey;
    
    @Value("${voice.assistant.temp-dir:${java.io.tmpdir}/voice-assistant}")
    private String tempDir;
    
    private static final Map<String, String> SERVICE_KEYWORDS_MAP = Map.of(
            "vệ sinh", "Vệ sinh nhà cửa",
            "giặt là", "Giặt là",
            "nấu ăn", "Nấu ăn",
            "chăm sóc", "Chăm sóc người già",
            "trông trẻ", "Trông trẻ",
            "sửa chữa", "Sửa chữa điện nước",
            "tổng vệ sinh", "Tổng vệ sinh",
            "lau nhà", "Vệ sinh nhà cửa",
            "quét nhà", "Vệ sinh nhà cửa"
    );
    
    @Override
    public VoiceProcessingResult processVoiceToText(MultipartFile audioFile) {
        long startTime = System.currentTimeMillis();
        
        try {
            if (audioFile == null || audioFile.isEmpty()) {
                return new VoiceProcessingResult(false, null, "Audio file is empty");
            }
            
            // Validate file type
            String contentType = audioFile.getContentType();
            if (contentType == null || !contentType.startsWith("audio/")) {
                return new VoiceProcessingResult(false, null, "Invalid audio file type");
            }
            
            // Save audio file temporarily
            File tempFile = saveTempFile(audioFile);
            
            try {
                // Use AssemblyAI for transcription
                String transcription = transcribeAudio(tempFile);
                
                if (transcription == null || transcription.trim().isEmpty()) {
                    return new VoiceProcessingResult(false, null, "Could not transcribe audio");
                }
                
                log.info("Voice transcription completed in {}ms: {}", 
                        System.currentTimeMillis() - startTime, transcription);
                
                return new VoiceProcessingResult(true, transcription, null);
                
            } finally {
                // Clean up temp file
                if (tempFile.exists()) {
                    tempFile.delete();
                }
            }
            
        } catch (Exception e) {
            log.error("Error processing voice to text", e);
            return new VoiceProcessingResult(false, null, "Error processing audio: " + e.getMessage());
        }
    }
    
    @Override
    public BookingIntentExtractionResult extractBookingIntent(String transcription, String customerId) {
        try {
            // Get customer information
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new RuntimeException("Customer not found"));
            
            // Get customer's default address
            Address defaultAddress = addressService.findByCustomerId(customerId);
            
            // Use AI to extract booking information from transcription
            String extractionPrompt = buildExtractionPrompt(transcription, customer, defaultAddress);
            
            ChatClient chatClient = chatClientBuilder.build();
            String aiResponse = chatClient.prompt()
                    .user(extractionPrompt)
                    .call()
                    .content();
            
            log.info("AI extraction response: {}", aiResponse);
            
            // Parse AI response to extract booking details
            VoiceBookingIntent intent = parseAIResponse(aiResponse, transcription);
            
            // Build BookingCreateRequest from intent
            BookingCreateRequest bookingRequest = buildBookingRequest(intent, customerId, defaultAddress);
            
            return new BookingIntentExtractionResult(true, intent, bookingRequest, null);
            
        } catch (Exception e) {
            log.error("Error extracting booking intent", e);
            return new BookingIntentExtractionResult(false, null, null, 
                    "Could not extract booking information: " + e.getMessage());
        }
    }
    
    @Override
    public VoiceBookingResponse processVoiceBooking(MultipartFile audioFile, String customerId) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Step 1: Convert voice to text
            VoiceProcessingResult voiceResult = processVoiceToText(audioFile);
            
            if (!voiceResult.success()) {
                return new VoiceBookingResponse(
                        false,
                        voiceResult.errorMessage(),
                        null,
                        null,
                        null,
                        System.currentTimeMillis() - startTime
                );
            }
            
            String transcription = voiceResult.transcription();
            
            // Step 2: Extract booking intent
            BookingIntentExtractionResult intentResult = extractBookingIntent(transcription, customerId);
            
            if (!intentResult.success()) {
                return new VoiceBookingResponse(
                        false,
                        intentResult.errorMessage(),
                        transcription,
                        null,
                        null,
                        System.currentTimeMillis() - startTime
                );
            }
            
            // Step 3: Create booking
            try {
                BookingCreationSummary bookingResult = bookingService.createBooking(intentResult.bookingRequest());
                
                return new VoiceBookingResponse(
                        true,
                        "Đặt lịch thành công qua giọng nói",
                        transcription,
                        intentResult.intent(),
                        bookingResult,
                        System.currentTimeMillis() - startTime
                );
                
            } catch (Exception e) {
                log.error("Error creating booking from voice", e);
                return new VoiceBookingResponse(
                        false,
                        "Không thể tạo booking: " + e.getMessage(),
                        transcription,
                        intentResult.intent(),
                        null,
                        System.currentTimeMillis() - startTime
                );
            }
            
        } catch (Exception e) {
            log.error("Error in voice booking process", e);
            return new VoiceBookingResponse(
                    false,
                    "Lỗi xử lý đặt lịch bằng giọng nói: " + e.getMessage(),
                    null,
                    null,
                    null,
                    System.currentTimeMillis() - startTime
            );
        }
    }
    
    private File saveTempFile(MultipartFile audioFile) throws IOException {
        // Create temp directory if not exists
        Path tempDirPath = Path.of(tempDir);
        if (!Files.exists(tempDirPath)) {
            Files.createDirectories(tempDirPath);
        }
        
        // Generate unique filename
        String originalFilename = audioFile.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".wav";
        
        String filename = "voice_" + System.currentTimeMillis() + extension;
        Path filePath = tempDirPath.resolve(filename);
        
        // Save file
        Files.copy(audioFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        return filePath.toFile();
    }
    
    private String transcribeAudio(File audioFile) {
        try {
            if (assemblyAiApiKey == null || assemblyAiApiKey.trim().isEmpty()) {
                log.warn("AssemblyAI API key not configured, using fallback method");
                return transcribeAudioFallback(audioFile);
            }
            
            // AssemblyAI client implementation
            // Note: The AssemblyAI Java SDK API may vary by version
            // For production, configure the actual implementation based on SDK version 1.2.0
            log.warn("AssemblyAI integration not fully implemented yet - using fallback");
            return transcribeAudioFallback(audioFile);
            
        } catch (Exception e) {
            log.error("Error with AssemblyAI transcription, using fallback", e);
            return transcribeAudioFallback(audioFile);
        }
    }
    
    private String transcribeAudioFallback(File audioFile) {
        // Fallback: return a placeholder or use alternative method
        // In production, you might want to integrate with other services like Google Speech-to-Text
        log.warn("Using fallback transcription method - returning empty result");
        return "Không thể chuyển đổi giọng nói. Vui lòng cấu hình API key hoặc sử dụng phương thức nhập liệu khác.";
    }
    
    private String buildExtractionPrompt(String transcription, Customer customer, Address defaultAddress) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Bạn là trợ lý AI cho dịch vụ giúp việc nhà. Nhiệm vụ của bạn là phân tích câu nói của khách hàng và trích xuất thông tin đặt lịch.\n\n");
        prompt.append("Thông tin khách hàng:\n");
        prompt.append("- Tên: ").append(customer.getFullName()).append("\n");
        
        if (defaultAddress != null) {
            prompt.append("- Địa chỉ mặc định: ").append(defaultAddress.getFullAddress()).append("\n");
        }
        
        prompt.append("\nCác dịch vụ có sẵn:\n");
        List<iuh.house_keeping_service_be.models.Service> availableServices = serviceRepository.findAll();
        for (iuh.house_keeping_service_be.models.Service service : availableServices) {
            prompt.append("- ").append(service.getName()).append("\n");
        }
        
        prompt.append("\nCâu nói của khách hàng:\n");
        prompt.append("\"").append(transcription).append("\"\n\n");
        
        prompt.append("Hãy trích xuất các thông tin sau và trả về dưới dạng JSON:\n");
        prompt.append("{\n");
        prompt.append("  \"serviceType\": \"Tên dịch vụ (phải khớp với danh sách dịch vụ trên)\",\n");
        prompt.append("  \"bookingTime\": \"Thời gian đặt lịch (định dạng: yyyy-MM-dd HH:mm)\",\n");
        prompt.append("  \"address\": \"Địa chỉ (nếu có, nếu không dùng địa chỉ mặc định)\",\n");
        prompt.append("  \"note\": \"Ghi chú thêm từ khách hàng\",\n");
        prompt.append("  \"confidence\": \"Độ tin cậy (0.0-1.0)\"\n");
        prompt.append("}\n\n");
        prompt.append("Lưu ý:\n");
        prompt.append("- Nếu không có thời gian cụ thể, đặt thời gian là ngày mai lúc 9:00 sáng\n");
        prompt.append("- Nếu chỉ có ngày không có giờ, mặc định là 9:00 sáng\n");
        prompt.append("- Nếu không có địa chỉ, để trống để dùng địa chỉ mặc định\n");
        prompt.append("- Chỉ trả về JSON, không có text thêm\n");
        
        return prompt.toString();
    }
    
    private VoiceBookingIntent parseAIResponse(String aiResponse, String originalTranscription) {
        try {
            // Clean the response - remove markdown code blocks if present
            String jsonResponse = aiResponse.trim();
            if (jsonResponse.startsWith("```json")) {
                jsonResponse = jsonResponse.substring(7);
            }
            if (jsonResponse.startsWith("```")) {
                jsonResponse = jsonResponse.substring(3);
            }
            if (jsonResponse.endsWith("```")) {
                jsonResponse = jsonResponse.substring(0, jsonResponse.length() - 3);
            }
            jsonResponse = jsonResponse.trim();
            
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            
            String serviceType = rootNode.path("serviceType").asText();
            String bookingTimeStr = rootNode.path("bookingTime").asText();
            String address = rootNode.path("address").asText();
            String note = rootNode.path("note").asText();
            double confidence = rootNode.path("confidence").asDouble(0.8);
            
            LocalDateTime bookingTime = parseBookingTime(bookingTimeStr);
            
            List<String> detectedServices = new ArrayList<>();
            if (!serviceType.isEmpty()) {
                detectedServices.add(serviceType);
            }
            
            return new VoiceBookingIntent(
                    serviceType,
                    bookingTime,
                    address.isEmpty() ? null : address,
                    note,
                    detectedServices,
                    confidence
            );
            
        } catch (Exception e) {
            log.error("Error parsing AI response, using fallback", e);
            return createFallbackIntent(originalTranscription);
        }
    }
    
    private VoiceBookingIntent createFallbackIntent(String transcription) {
        // Simple keyword-based extraction as fallback
        String detectedService = detectServiceFromKeywords(transcription);
        LocalDateTime defaultTime = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).withSecond(0);
        
        return new VoiceBookingIntent(
                detectedService,
                defaultTime,
                null,
                transcription,
                detectedService != null ? List.of(detectedService) : Collections.emptyList(),
                0.5
        );
    }
    
    private String detectServiceFromKeywords(String text) {
        String lowerText = text.toLowerCase();
        
        for (Map.Entry<String, String> entry : SERVICE_KEYWORDS_MAP.entrySet()) {
            if (lowerText.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        
        // Try to match with database services
        List<iuh.house_keeping_service_be.models.Service> services = serviceRepository.findAll();
        for (iuh.house_keeping_service_be.models.Service service : services) {
            if (lowerText.contains(service.getName().toLowerCase())) {
                return service.getName();
            }
        }
        
        return "Vệ sinh nhà cửa"; // Default service
    }
    
    private LocalDateTime parseBookingTime(String timeStr) {
        try {
            // Try various date formats
            DateTimeFormatter[] formatters = {
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                    DateTimeFormatter.ofPattern("dd/MM/yyyy")
            };
            
            for (DateTimeFormatter formatter : formatters) {
                try {
                    if (timeStr.contains(":")) {
                        return LocalDateTime.parse(timeStr, formatter);
                    } else {
                        return LocalDateTime.parse(timeStr + " 09:00", 
                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                    }
                } catch (DateTimeParseException ignored) {
                }
            }
        } catch (Exception e) {
            log.warn("Could not parse booking time: {}", timeStr);
        }
        
        // Default to tomorrow at 9 AM
        return LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0);
    }
    
    private BookingCreateRequest buildBookingRequest(VoiceBookingIntent intent, 
                                                     String customerId, 
                                                     Address defaultAddress) {
        // Find service by name
        iuh.house_keeping_service_be.models.Service service = serviceRepository.findAll().stream()
                .filter(s -> s.getName().equalsIgnoreCase(intent.serviceType()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Service not found: " + intent.serviceType()));
        
        // Create booking detail
        BookingDetailRequest detailRequest = new BookingDetailRequest(
                service.getServiceId(),
                1, // default quantity
                service.getBasePrice(), // expected price
                null, // service options
                null  // note
        );
        
        // Use address from intent or default address
        String addressId = null;
        if (intent.address() == null || intent.address().isEmpty()) {
            if (defaultAddress != null) {
                addressId = defaultAddress.getAddressId();
            }
        }
        
        return new BookingCreateRequest(
                addressId,
                null, // newAddress - for now use default address
                intent.bookingTime(),
                intent.note(),
                "Đặt lịch bằng giọng nói", // title
                null, // imageUrls
                null, // promoCode
                List.of(detailRequest),
                null, // assignments
                1 // paymentMethodId - default to cash
        );
    }
}
