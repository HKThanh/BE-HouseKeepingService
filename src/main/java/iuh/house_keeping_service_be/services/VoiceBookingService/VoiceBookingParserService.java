package iuh.house_keeping_service_be.services.VoiceBookingService;

import iuh.house_keeping_service_be.dtos.Booking.request.BookingCreateRequest;
import iuh.house_keeping_service_be.dtos.Booking.request.BookingDetailRequest;
import iuh.house_keeping_service_be.dtos.Booking.request.NewAddressRequest;
import iuh.house_keeping_service_be.dtos.VoiceBooking.ParsedBookingInfo;
import iuh.house_keeping_service_be.models.Service;
import iuh.house_keeping_service_be.repositories.ServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for parsing voice transcripts into booking requests
 * Uses rule-based extraction with Vietnamese language support
 */
@org.springframework.stereotype.Service
@Slf4j
@RequiredArgsConstructor
public class VoiceBookingParserService {

    private final ServiceRepository serviceRepository;

    // Pattern for extracting time information
    private static final Pattern TIME_PATTERN = Pattern.compile(
            "(\\d{1,2})\\s*(giờ|h)\\s*(\\d{1,2})?\\s*(phút|p)?",
            Pattern.CASE_INSENSITIVE
    );

    // Pattern for extracting date information
    private static final Pattern DATE_PATTERN = Pattern.compile(
            "(hôm nay|ngày mai|ngày kia|(\\d{1,2})/(\\d{1,2})/(\\d{4}|\\d{2}))",
            Pattern.CASE_INSENSITIVE
    );

    // Service name keywords mapping
    private static final Map<String, List<String>> SERVICE_KEYWORDS = Map.of(
            "vệ sinh", List.of("vệ sinh", "dọn dẹp", "lau nhà", "quét nhà"),
            "giặt", List.of("giặt", "giặt ủi", "giặt là", "ủi đồ"),
            "nấu", List.of("nấu ăn", "nấu cơm", "nấu nướng", "làm cơm"),
            "chăm sóc", List.of("chăm sóc", "trông trẻ", "chăm trẻ", "giữ trẻ"),
            "sửa chữa", List.of("sửa chữa", "sửa", "bảo trì", "thay thế")
    );

    /**
     * Parse transcript into booking information
     */
    public ParsedBookingInfo parseTranscript(
            String transcript,
            String customerId,
            Map<String, Object> hints
    ) {
        log.info("Parsing transcript for customer {}: {}", customerId, transcript);

        Map<String, String> extractedFields = new HashMap<>();
        List<String> missingFields = new ArrayList<>();
        double parseConfidence = 1.0;

        try {
            // Extract service information
            List<Service> services = extractServices(transcript, hints);
            if (services.isEmpty()) {
                missingFields.add("service");
                parseConfidence -= 0.3;
                log.warn("No service found in transcript");
            } else {
                extractedFields.put("services", services.stream()
                        .map(Service::getName)
                        .reduce((a, b) -> a + ", " + b)
                        .orElse(""));
            }

            // Extract booking time
            LocalDateTime bookingTime = extractBookingTime(transcript, hints);
            if (bookingTime == null) {
                missingFields.add("bookingTime");
                parseConfidence -= 0.3;
                log.warn("No booking time found in transcript");
            } else {
                extractedFields.put("bookingTime", bookingTime.toString());
            }

            // Extract address information
            String address = extractAddress(transcript, hints);
            if (address == null || address.isBlank()) {
                missingFields.add("address");
                parseConfidence -= 0.2;
                log.warn("No address found in transcript");
            } else {
                extractedFields.put("address", address);
            }

            // Extract note/additional info
            String note = extractNote(transcript);
            if (note != null && !note.isBlank()) {
                extractedFields.put("note", note);
            }

            // If we have all required fields, create BookingCreateRequest
            if (missingFields.isEmpty() && !services.isEmpty() && bookingTime != null) {
                BookingCreateRequest bookingRequest = buildBookingRequest(
                        services, bookingTime, address, note, customerId, hints
                );

                return ParsedBookingInfo.builder()
                        .bookingRequest(bookingRequest)
                        .missingFields(List.of())
                        .extractedFields(extractedFields)
                        .parseConfidence(parseConfidence)
                        .clarificationMessage(null)
                        .build();
            }

            // Build clarification message for missing fields
            String clarificationMessage = buildClarificationMessage(missingFields, extractedFields);

            return ParsedBookingInfo.builder()
                    .bookingRequest(null)
                    .missingFields(missingFields)
                    .extractedFields(extractedFields)
                    .parseConfidence(parseConfidence)
                    .clarificationMessage(clarificationMessage)
                    .build();

        } catch (Exception e) {
            log.error("Error parsing transcript: {}", e.getMessage(), e);
            return ParsedBookingInfo.builder()
                    .bookingRequest(null)
                    .missingFields(List.of("all"))
                    .extractedFields(extractedFields)
                    .parseConfidence(0.0)
                    .clarificationMessage("Không thể phân tích yêu cầu đặt lịch. Vui lòng thử lại hoặc đặt lịch thủ công.")
                    .build();
        }
    }

    /**
     * Extract services from transcript
     */
    private List<Service> extractServices(String transcript, Map<String, Object> hints) {
        List<Service> matchedServices = new ArrayList<>();
        String lowerTranscript = transcript.toLowerCase();

        // Check hints first
        if (hints.containsKey("serviceId")) {
            try {
                Integer serviceId = Integer.parseInt(hints.get("serviceId").toString());
                Optional<Service> service = serviceRepository.findById(serviceId);
                service.ifPresent(matchedServices::add);
                if (service.isPresent()) {
                    log.info("Service found from hints: {}", service.get().getName());
                    return matchedServices;
                }
            } catch (Exception e) {
                log.warn("Invalid serviceId in hints: {}", hints.get("serviceId"));
            }
        }

        // Get all available services
        List<Service> allServices = serviceRepository.findAll();

        // Try exact name match first
        for (Service service : allServices) {
            String serviceName = service.getName().toLowerCase();
            if (lowerTranscript.contains(serviceName)) {
                matchedServices.add(service);
                log.info("Service matched by name: {}", service.getName());
            }
        }

        // If no exact match, try keyword matching
        if (matchedServices.isEmpty()) {
            for (Map.Entry<String, List<String>> entry : SERVICE_KEYWORDS.entrySet()) {
                for (String keyword : entry.getValue()) {
                    if (lowerTranscript.contains(keyword)) {
                        // Find services containing this keyword
                        for (Service service : allServices) {
                            String serviceName = service.getName().toLowerCase();
                            if (serviceName.contains(entry.getKey()) && !matchedServices.contains(service)) {
                                matchedServices.add(service);
                                log.info("Service matched by keyword '{}': {}", keyword, service.getName());
                            }
                        }
                    }
                }
            }
        }

        return matchedServices;
    }

    /**
     * Extract booking time from transcript
     */
    private LocalDateTime extractBookingTime(String transcript, Map<String, Object> hints) {
        // Check hints first
        if (hints.containsKey("bookingTime")) {
            try {
                return LocalDateTime.parse(hints.get("bookingTime").toString());
            } catch (Exception e) {
                log.warn("Invalid bookingTime in hints: {}", hints.get("bookingTime"));
            }
        }

        LocalDate date = extractDate(transcript);
        LocalTime time = extractTime(transcript);

        if (date != null && time != null) {
            return LocalDateTime.of(date, time);
        } else if (date != null) {
            // Default to 9 AM if only date is provided
            return LocalDateTime.of(date, LocalTime.of(9, 0));
        } else if (time != null) {
            // Use tomorrow if only time is provided and time is in the future
            LocalDate targetDate = LocalDate.now();
            if (LocalDateTime.of(targetDate, time).isBefore(LocalDateTime.now())) {
                targetDate = targetDate.plusDays(1);
            }
            return LocalDateTime.of(targetDate, time);
        }

        return null;
    }

    /**
     * Extract date from transcript
     */
    private LocalDate extractDate(String transcript) {
        Matcher matcher = DATE_PATTERN.matcher(transcript);
        
        if (matcher.find()) {
            String dateStr = matcher.group(1).toLowerCase();
            
            if (dateStr.contains("hôm nay")) {
                return LocalDate.now();
            } else if (dateStr.contains("ngày mai")) {
                return LocalDate.now().plusDays(1);
            } else if (dateStr.contains("ngày kia")) {
                return LocalDate.now().plusDays(2);
            } else {
                // Parse DD/MM/YYYY format
                try {
                    String day = matcher.group(2);
                    String month = matcher.group(3);
                    String year = matcher.group(4);
                    
                    if (year.length() == 2) {
                        year = "20" + year;
                    }
                    
                    return LocalDate.of(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day));
                } catch (Exception e) {
                    log.warn("Failed to parse date: {}", dateStr);
                }
            }
        }
        
        return null;
    }

    /**
     * Extract time from transcript
     */
    private LocalTime extractTime(String transcript) {
        Matcher matcher = TIME_PATTERN.matcher(transcript);
        
        if (matcher.find()) {
            try {
                int hour = Integer.parseInt(matcher.group(1));
                int minute = 0;
                
                if (matcher.group(3) != null) {
                    minute = Integer.parseInt(matcher.group(3));
                }
                
                return LocalTime.of(hour, minute);
            } catch (Exception e) {
                log.warn("Failed to parse time from: {}", matcher.group(0));
            }
        }
        
        return null;
    }

    /**
     * Extract address from transcript
     */
    private String extractAddress(String transcript, Map<String, Object> hints) {
        // Check hints first
        if (hints.containsKey("address")) {
            return hints.get("address").toString();
        }

        // Look for address keywords
        String[] addressKeywords = {"địa chỉ", "tại", "ở", "đến"};
        String lowerTranscript = transcript.toLowerCase();
        
        for (String keyword : addressKeywords) {
            int index = lowerTranscript.indexOf(keyword);
            if (index != -1) {
                // Extract text after keyword until punctuation or end
                String afterKeyword = transcript.substring(index + keyword.length()).trim();
                String[] parts = afterKeyword.split("[.,;]");
                if (parts.length > 0 && !parts[0].isBlank()) {
                    return parts[0].trim();
                }
            }
        }
        
        return null;
    }

    /**
     * Extract note from transcript
     */
    private String extractNote(String transcript) {
        // Look for note keywords
        String[] noteKeywords = {"lưu ý", "ghi chú", "chú ý", "quan trọng"};
        String lowerTranscript = transcript.toLowerCase();
        
        for (String keyword : noteKeywords) {
            int index = lowerTranscript.indexOf(keyword);
            if (index != -1) {
                return transcript.substring(index).trim();
            }
        }
        
        // Return full transcript as note if it's short
        if (transcript.length() < 200) {
            return transcript;
        }
        
        return null;
    }

    /**
     * Build BookingCreateRequest from extracted information
     */
    private BookingCreateRequest buildBookingRequest(
            List<Service> services,
            LocalDateTime bookingTime,
            String address,
            String note,
            String customerId,
            Map<String, Object> hints
    ) {
        // Build booking details
        List<BookingDetailRequest> bookingDetails = new ArrayList<>();
        for (Service service : services) {
            BookingDetailRequest detail = new BookingDetailRequest(
                    service.getServiceId(),
                    1, // Default quantity
                    service.getBasePrice(),
                    service.getBasePrice(),
                    List.of() // No options selected
            );
            bookingDetails.add(detail);
        }

        // Build address request
        NewAddressRequest addressRequest = new NewAddressRequest(
                customerId,
                address,
                "Unknown", // Will need clarification
                "Unknown", // Will need clarification
                null,
                null
        );

        return new BookingCreateRequest(
                null, // addressId
                addressRequest,
                bookingTime,
                note,
                null, // title
                List.of(), // imageUrls
                null, // promoCode
                bookingDetails,
                null, // assignments
                1 // Default payment method
        );
    }

    /**
     * Build clarification message for missing fields
     */
    private String buildClarificationMessage(List<String> missingFields, Map<String, String> extractedFields) {
        StringBuilder message = new StringBuilder("Tôi đã hiểu được một phần yêu cầu của bạn:\n\n");

        if (!extractedFields.isEmpty()) {
            for (Map.Entry<String, String> entry : extractedFields.entrySet()) {
                message.append("- ").append(formatFieldName(entry.getKey()))
                       .append(": ").append(entry.getValue()).append("\n");
            }
            message.append("\n");
        }

        message.append("Tuy nhiên, tôi cần thêm thông tin về:\n");
        for (String field : missingFields) {
            message.append("- ").append(formatFieldName(field)).append("\n");
        }

        message.append("\nVui lòng cung cấp thêm thông tin hoặc đặt lịch thủ công.");

        return message.toString();
    }

    /**
     * Format field name for display
     */
    private String formatFieldName(String fieldName) {
        return switch (fieldName) {
            case "service" -> "Dịch vụ";
            case "services" -> "Dịch vụ";
            case "bookingTime" -> "Thời gian";
            case "address" -> "Địa chỉ";
            case "note" -> "Ghi chú";
            default -> fieldName;
        };
    }
}
