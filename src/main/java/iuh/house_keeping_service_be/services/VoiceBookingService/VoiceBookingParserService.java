package iuh.house_keeping_service_be.services.VoiceBookingService;

import iuh.house_keeping_service_be.dtos.Booking.request.BookingCreateRequest;
import iuh.house_keeping_service_be.dtos.Booking.request.BookingDetailRequest;
import iuh.house_keeping_service_be.dtos.Booking.request.NewAddressRequest;
import iuh.house_keeping_service_be.dtos.VoiceBooking.ParsedBookingInfo;
import iuh.house_keeping_service_be.models.Service;
import iuh.house_keeping_service_be.repositories.ServiceRepository;
import lombok.RequiredArgsConstructor;
import java.text.Normalizer;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;
import java.time.DayOfWeek;
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
    private final AddressNormalizationService addressNormalizationService;

    // Pattern for extracting time information
    // Captures: hour, optional minute, optional period (sáng/chiều/tối)
    private static final Pattern TIME_PATTERN = Pattern.compile(
            "(\\d{1,2})\\s*(giờ|h)\\s*(\\d{1,2})?\\s*(phút|p)?\\s*(sáng|chiều|tối|trưa)?",
            Pattern.CASE_INSENSITIVE
    );

    // Pattern for extracting date information
    private static final Pattern DATE_PATTERN = Pattern.compile(
            "(hôm nay|ngày mai|ngày kia|ngày mốt|ngày này tuần sau|ngày mai tuần sau|ngày mốt tuần sau|(thứ\\s+[2-7]|thứ\\s+(hai|ba|tư|năm|sáu|bảy|bay)|chủ nhật|chu nhat)(\\s+tuần sau)?|(\\d{1,2})/(\\d{1,2})/(\\d{4}|\\d{2}))",
            Pattern.CASE_INSENSITIVE
    );

    // Similarity threshold for fuzzy matching (0.0 - 1.0)
    private static final double SIMILARITY_THRESHOLD = 0.7;

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
            // Extract service information FIRST - this is most critical
            List<Service> services = extractServices(transcript, hints);
            List<Service> allAvailableServices = serviceRepository.findAll().stream()
                    .filter(Service::getIsActive)
                    .toList();
            
            // PRIORITY CHECK: If no service found, stop here and only report service missing
            if (services.isEmpty()) {
                missingFields.add("service");
                parseConfidence = 0.3; // Low confidence without service
                log.warn("No service found in transcript - this is critical, stopping other checks");
                
                // Add available services list to extracted fields for clarification
                if (!allAvailableServices.isEmpty()) {
                    String availableServicesList = allAvailableServices.stream()
                            .map(s -> "• " + s.getName())
                            .reduce((a, b) -> a + "\n" + b)
                            .orElse("");
                    extractedFields.put("availableServices", availableServicesList);
                }
                
                // Extract note if available (just for context)
                String note = extractNote(transcript);
                if (note != null && !note.isBlank()) {
                    extractedFields.put("note", note);
                }
                
                // Build clarification message and return immediately
                String clarificationMessage = buildClarificationMessage(missingFields, extractedFields);
                return ParsedBookingInfo.builder()
                        .bookingRequest(null)
                        .missingFields(missingFields)
                        .extractedFields(extractedFields)
                        .parseConfidence(parseConfidence)
                        .clarificationMessage(clarificationMessage)
                        .build();
            }
            
            // Service found - record it
            extractedFields.put("services", services.stream()
                    .map(Service::getName)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse(""));

            // Extract booking time
            LocalDateTime bookingTime = extractBookingTime(transcript, hints);
            if (bookingTime == null) {
                missingFields.add("bookingTime");
                parseConfidence -= 0.3;
                log.warn("No booking time found in transcript");
            } else {
                extractedFields.put("bookingTime", bookingTime.toString());
                log.info("Extracted booking time: {}", bookingTime);
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
        String normalizedTranscript = normalizeText(lowerTranscript);

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

        // Get all active services from database
        List<Service> allServices = serviceRepository.findAll().stream()
                .filter(Service::getIsActive)
                .toList();

        // Try explicit service name from hints (e.g., user typed it in FE)
        if (hints != null && hints.containsKey("explicitFields")) {
            Object explicit = hints.get("explicitFields");
            if (explicit instanceof Map<?, ?> explicitMap) {
                Object serviceNameObj = explicitMap.get("service") != null ? explicitMap.get("service") : explicitMap.get("services");
                if (serviceNameObj != null) {
                    String serviceNameHint = String.valueOf(serviceNameObj).trim();
                    String normalizedHint = normalizeText(serviceNameHint.toLowerCase());
                    for (Service svc : allServices) {
                        String svcNameLower = svc.getName().toLowerCase();
                        String svcNormalized = normalizeText(svcNameLower);
                        if (svcNameLower.equalsIgnoreCase(serviceNameHint)
                                || svcNormalized.equals(normalizedHint)
                                || normalizedHint.contains(svcNormalized)) {
                            matchedServices.add(svc);
                            log.info("Service matched from explicitFields hint: {}", svc.getName());
                            return matchedServices;
                        }
                    }
                }
            }
        }

        if (allServices.isEmpty()) {
            log.warn("No active services found in database");
            return matchedServices;
        }

        // Remove common Vietnamese filler words and generic terms before matching
        String cleanedTranscript = removeGenericServiceWords(lowerTranscript);
        String normalizedCleanedTranscript = normalizeText(cleanedTranscript);
        
        // Try exact name match first (case-insensitive)
        for (Service service : allServices) {
            String serviceName = service.getName().toLowerCase();
            String serviceNameNormalized = normalizeText(serviceName);
            // Check if the service name is present and is not just part of a generic phrase
            if (cleanedTranscript.contains(serviceName)
                    || normalizedCleanedTranscript.contains(serviceNameNormalized)
                    || normalizedTranscript.contains(serviceNameNormalized)) {
                matchedServices.add(service);
                log.info("Service matched exactly by name: {}", service.getName());
            }
        }

        // If exact match found, return immediately
        if (!matchedServices.isEmpty()) {
            return matchedServices;
        }

        // If no exact match, try fuzzy matching with similarity threshold
        // BUT only match against specific service keywords, not the whole transcript
        List<ServiceMatch> potentialMatches = new ArrayList<>();
        for (Service service : allServices) {
            String serviceName = service.getName().toLowerCase();
            
            // Extract potential service keywords from cleaned transcript
            // Only check if there are meaningful words that could be a service name
            String[] words = cleanedTranscript.split("\\s+");
            
            // Skip fuzzy matching if transcript is too generic (less than 3 meaningful words)
            if (words.length < 3 || isTranscriptTooGeneric(cleanedTranscript)) {
                log.info("Transcript too generic for fuzzy matching, skipping service: {}", service.getName());
                continue;
            }
            
            // Check if service name (or most of it) appears in ORIGINAL transcript first
            double originalSimilarity = calculateSimilarity(lowerTranscript, serviceName);
            double cleanedSimilarity = calculateSimilarity(cleanedTranscript, serviceName);
            
            // Use the better score but prefer original transcript match
            double similarity = Math.max(originalSimilarity, cleanedSimilarity * 0.9); // Slight penalty for cleaned
            
            if (similarity >= SIMILARITY_THRESHOLD) {
                potentialMatches.add(new ServiceMatch(service, similarity));
                log.info("Service '{}' has similarity score: {} (original: {}, cleaned: {})", 
                        service.getName(), similarity, originalSimilarity, cleanedSimilarity);
            }
        }

        // Sort by similarity score (highest first)
        potentialMatches.sort((a, b) -> Double.compare(b.similarity(), a.similarity()));

        // Only return if we have high confidence matches (> 0.8)
        for (ServiceMatch match : potentialMatches) {
            if (match.similarity() > 0.8) {
                matchedServices.add(match.service());
                log.info("Service matched by fuzzy matching: {} (score: {})", 
                        match.service().getName(), match.similarity());
            }
        }

        // If no high-confidence match found, log all available services
        if (matchedServices.isEmpty()) {
            log.warn("No matching service found. Available services: {}",
                    allServices.stream()
                            .map(Service::getName)
                            .reduce((a, b) -> a + ", " + b)
                            .orElse("None"));
        }

        return matchedServices;
    }
    
    /**
     * Remove generic service words that don't indicate a specific service
     */
    private String removeGenericServiceWords(String transcript) {
        // List of generic words that should be removed before service matching
        String[] genericWords = {
            "dịch vụ", "service", "đặt", "booking", "muốn", "cần", "yêu cầu",
            "tôi", "mình", "em", "anh", "chị", "xin", "làm ơn", "giúp",
            "vui lòng", "được không", "có thể", "giúp tôi", "cho tôi",
            // Time-related words that could cause false matches
            "giờ", "phút", "sáng", "chiều", "tối", "trưa", "đêm", "khuya",
            "hôm nay", "ngày mai", "ngày kia", "tuần sau", "tháng sau",
            "lúc", "vào", "trong", "ngoài", "khoảng", "tầm", "độ",
            // Location-related words
            "tại", "ở", "đến", "về", "từ", "đi",
            // Numbers that might cause confusion
            "một", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín", "mười"
        };
        
        String cleaned = transcript;
        for (String word : genericWords) {
            cleaned = cleaned.replaceAll("\\b" + word + "\\b", " ");
        }
        
        // Normalize multiple spaces
        cleaned = cleaned.replaceAll("\\s+", " ").trim();
        
        log.debug("Cleaned transcript for service matching: '{}' -> '{}'", transcript, cleaned);
        return cleaned;
    }
    
    /**
     * Check if transcript is too generic to perform fuzzy matching
     */
    private boolean isTranscriptTooGeneric(String transcript) {
        // If transcript is empty or too short after cleaning, it's too generic
        if (transcript == null || transcript.trim().isEmpty() || transcript.length() < 5) {
            return true;
        }
        
        // List of time/date related words that indicate no service mentioned
        String[] timeRelatedWords = {
            "mai", "hôm", "nay", "kia", "tuần", "tháng", "năm",
            "thứ", "chủ nhật"
        };
        
        int meaningfulWordCount = 0;
        int timeWordCount = 0;
        String[] words = transcript.split("\\s+");
        
        for (String word : words) {
            if (word.length() <= 2) {
                continue; // Skip very short words
            }
            
            boolean isTimeRelated = false;
            for (String timeWord : timeRelatedWords) {
                if (word.contains(timeWord)) {
                    isTimeRelated = true;
                    timeWordCount++;
                    break;
                }
            }
            
            if (!isTimeRelated) {
                meaningfulWordCount++;
            }
        }
        
        // If transcript is mostly time-related words, it's too generic for service matching
        // Or if there are less than 2 meaningful words
        return meaningfulWordCount < 2 || (timeWordCount > meaningfulWordCount);
    }

    /**
     * Calculate similarity between transcript and service name using Levenshtein distance
     */
    private double calculateSimilarity(String transcript, String serviceName) {
        // Check if service name is contained in transcript
        if (transcript.contains(serviceName)) {
            return 1.0;
        }

        // Extract relevant words from transcript (remove common words)
        String[] transcriptWords = transcript.split("\\s+");
        String[] serviceWords = serviceName.split("\\s+");

        // Count how many service words are found in transcript
        int matchedWords = 0;
        double totalSimilarity = 0.0;

        // Check each service word against transcript words
        for (String serviceWord : serviceWords) {
            if (serviceWord.length() < 2) continue; // Skip very short words
            
            double bestWordMatch = 0.0;
            for (String transcriptWord : transcriptWords) {
                if (transcriptWord.length() < 2) continue;
                
                double wordSimilarity = 1.0 - ((double) levenshteinDistance(transcriptWord, serviceWord) 
                        / Math.max(transcriptWord.length(), serviceWord.length()));
                bestWordMatch = Math.max(bestWordMatch, wordSimilarity);
            }
            
            // Consider word matched if similarity > 0.7
            if (bestWordMatch > 0.7) {
                matchedWords++;
                totalSimilarity += bestWordMatch;
            }
        }

        // Calculate average similarity weighted by word coverage
        // Require at least 60% of service words to be matched
        if (serviceWords.length == 0) return 0.0;
        
        double wordCoverage = (double) matchedWords / serviceWords.length;
        if (wordCoverage < 0.6) return 0.0; // Not enough words matched
        
        double avgSimilarity = totalSimilarity / matchedWords;
        
        // Final score combines coverage and average similarity
        return (wordCoverage * 0.5) + (avgSimilarity * 0.5);
    }

    private String normalizeText(String input) {
        if (input == null) return "";
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "");
    }

    /**
     * Calculate Levenshtein distance between two strings
     */
    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(
                        dp[i - 1][j] + 1,      // deletion
                        dp[i][j - 1] + 1),     // insertion
                        dp[i - 1][j - 1] + cost); // substitution
            }
        }

        return dp[s1.length()][s2.length()];
    }

    /**
     * Record to hold service match with similarity score
     */
    private record ServiceMatch(Service service, double similarity) {}

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
        String lower = transcript.toLowerCase();
        Matcher matcher = DATE_PATTERN.matcher(lower);

        if (matcher.find()) {
            String dateStr = matcher.group(1).toLowerCase();

            if (dateStr.contains("hôm nay")) {
                return LocalDate.now();
            } else if (dateStr.contains("ngày mai tuần sau")) {
                return LocalDate.now().plusWeeks(1).plusDays(1);
            } else if (dateStr.contains("ngày mốt tuần sau") || dateStr.contains("ngày kia tuần sau")) {
                return LocalDate.now().plusWeeks(1).plusDays(2);
            } else if (dateStr.contains("ngày này tuần sau")) {
                return LocalDate.now().plusWeeks(1);
            } else if (dateStr.contains("ngày mai")) {
                return LocalDate.now().plusDays(1);
            } else if (dateStr.contains("ngày mốt") || dateStr.contains("ngày kia")) {
                return LocalDate.now().plusDays(2);
            }

            LocalDate weekday = parseWeekdayDate(dateStr);
            if (weekday != null) {
                return weekday;
            }

            // Parse DD/MM/YYYY format (groups 6,7,8 in DATE_PATTERN)
            try {
                String day = matcher.group(6);
                String month = matcher.group(7);
                String year = matcher.group(8);

                if (day != null && month != null && year != null) {
                    if (year.length() == 2) {
                        year = "20" + year;
                    }
                    return LocalDate.of(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day));
                }
            } catch (Exception e) {
                log.warn("Failed to parse date: {}", dateStr);
            }
        }

        return null;
    }

    private LocalDate parseWeekdayDate(String dateStr) {
        boolean nextWeek = dateStr.contains("tuần sau");
        int targetDow = resolveWeekday(dateStr);
        if (targetDow == -1) {
            return null;
        }

        LocalDate today = LocalDate.now();

        if (nextWeek) {
            // start from next Monday then move to target weekday
            LocalDate nextMonday = today.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
            int deltaFromMonday = (targetDow - DayOfWeek.MONDAY.getValue() + 7) % 7;
            return nextMonday.plusDays(deltaFromMonday);
        }

        int todayDow = today.getDayOfWeek().getValue(); // Monday=1 ... Sunday=7
        int delta = (targetDow - todayDow + 7) % 7;
        if (delta == 0) {
            delta = 7; // move to next occurrence to avoid past date
        }
        return today.plusDays(delta);
    }

    private int resolveWeekday(String text) {
        String normalized = text.toLowerCase();
        if (normalized.contains("chủ nhật") || normalized.contains("chu nhat") || normalized.contains("cn")) return DayOfWeek.SUNDAY.getValue();
        if (normalized.contains("thứ hai") || normalized.contains("thứ 2") || normalized.contains("thu hai")) return DayOfWeek.MONDAY.getValue();
        if (normalized.contains("thứ ba") || normalized.contains("thứ 3") || normalized.contains("thu ba")) return DayOfWeek.TUESDAY.getValue();
        if (normalized.contains("thứ tư") || normalized.contains("thứ 4") || normalized.contains("thu tu")) return DayOfWeek.WEDNESDAY.getValue();
        if (normalized.contains("thứ năm") || normalized.contains("thứ 5") || normalized.contains("thu nam")) return DayOfWeek.THURSDAY.getValue();
        if (normalized.contains("thứ sáu") || normalized.contains("thứ 6") || normalized.contains("thu sau")) return DayOfWeek.FRIDAY.getValue();
        if (normalized.contains("thứ bảy") || normalized.contains("thứ 7") || normalized.contains("bay") || normalized.contains("bảy")) return DayOfWeek.SATURDAY.getValue();
        return -1;
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
                
                // Check for time period indicator (sáng/chiều/tối/trưa)
                String period = matcher.group(5);
                if (period != null) {
                    period = period.toLowerCase();
                    // Convert to 24-hour format
                    if (period.equals("chiều") || period.equals("tối")) {
                        // Afternoon/Evening: 1-11 chiều/tối -> 13-23
                        if (hour >= 1 && hour <= 11) {
                            hour += 12;
                        }
                    } else if (period.equals("trưa")) {
                        // Noon: 12 trưa -> 12:00
                        if (hour != 12) {
                            hour = 12;
                        }
                    }
                    // sáng (morning) keeps the hour as-is (1-11 AM)
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
                // Extract text after keyword until end of sentence
                String afterKeyword = transcript.substring(index + keyword.length()).trim();
                // Only split by semicolon or end of text, preserve periods for TP. abbreviation
                String[] parts = afterKeyword.split("[;]");
                if (parts.length > 0 && !parts[0].isBlank()) {
                    // Remove trailing period if it's at the very end
                    String address = parts[0].trim();
                    if (address.endsWith(".")) {
                        address = address.substring(0, address.length() - 1).trim();
                    }
                    return address;
                }
            }
        }
        
        return null;
    }

    /**
     * Parse Vietnamese address into components
     */
    private AddressComponents parseVietnameseAddress(String fullAddress) {
        if (fullAddress == null || fullAddress.isBlank()) {
            return new AddressComponents(null, null, null);
        }

        String ward = null;
        String city = null;
        String streetAddress = fullAddress;

        // Extract city (Thành phố / TP. / Tỉnh)
        // Match "TP. Hồ Chí Minh" or "Thành phố Hồ Chí Minh"
        Pattern cityPattern = Pattern.compile(
            "(?:thành phố|tp\\.?|tỉnh)\\s*([^,;]+?)\\s*$",
            Pattern.CASE_INSENSITIVE
        );
        Matcher cityMatcher = cityPattern.matcher(fullAddress);
        if (cityMatcher.find()) {
            city = cityMatcher.group(1).trim();
            // Remove trailing period if present
            if (city.endsWith(".")) {
                city = city.substring(0, city.length() - 1).trim();
            }
            // Handle common abbreviations and normalize
            if (city.equalsIgnoreCase("HCM") || city.equalsIgnoreCase("Hồ Chí Minh")) {
                city = "Hồ Chí Minh";
            } else if (city.equalsIgnoreCase("HN") || city.equalsIgnoreCase("Hà Nội")) {
                city = "Hà Nội";
            } else if (city.equalsIgnoreCase("Đà Nẵng") || city.equalsIgnoreCase("Da Nang")) {
                city = "Đà Nẵng";
            } else if (city.equalsIgnoreCase("Cần Thơ") || city.equalsIgnoreCase("Can Tho")) {
                city = "Cần Thơ";
            }
        }

        // Extract ward (Phường / Xã / Thị trấn / Quận)
        // Match patterns like "phường Thủ Dậu 1"
        Pattern wardPattern = Pattern.compile(
            "(?:phường|xã|thị trấn|quận)\\s+([^,]+?)(?:,|(?=\\s*(?:tp\\.|thành phố|tỉnh|quận)))",
            Pattern.CASE_INSENSITIVE
        );
        Matcher wardMatcher = wardPattern.matcher(fullAddress);
        if (wardMatcher.find()) {
            ward = wardMatcher.group(1).trim();
        }

        return new AddressComponents(streetAddress, ward, city);
    }

    /**
     * Record to hold parsed address components
     */
    private record AddressComponents(String fullAddress, String ward, String city) {}

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

        // Parse Vietnamese address into components
        AddressComponents addressComponents = parseVietnameseAddress(address);
        
        // Use parsed components or defaults
        String ward = addressComponents.ward() != null ? addressComponents.ward() : "Unknown";
        String city = addressComponents.city() != null ? addressComponents.city() : "Unknown";
        
        // Log the parsed address for debugging
        log.info("Parsed address - Full: {}, Ward: {}, City: {}", 
                addressComponents.fullAddress(), ward, city);

        // Normalize city and ward if they are not "Unknown"
        String normalizedCity = city;
        String normalizedWard = ward;
        
        if (!city.equals("Unknown") || !ward.equals("Unknown")) {
            try {
                AddressNormalizationService.NormalizedAddress normalizedAddress = 
                        addressNormalizationService.normalizeAddress(city, ward);
                
                if (normalizedAddress != null) {
                    if (normalizedAddress.normalizedCity() != null && !normalizedAddress.normalizedCity().isBlank()) {
                        normalizedCity = normalizedAddress.normalizedCity();
                        log.info("City normalized: {} -> {} (code: {})", city, normalizedCity, normalizedAddress.cityCode());
                    } else {
                        log.warn("City normalization returned null or blank for: {}", city);
                    }
                    
                    if (normalizedAddress.normalizedWard() != null && !normalizedAddress.normalizedWard().isBlank()) {
                        normalizedWard = normalizedAddress.normalizedWard();
                        log.info("Ward normalized: {} -> {} (code: {})", ward, normalizedWard, normalizedAddress.wardCode());
                    } else {
                        log.warn("Ward normalization returned null or blank for: {}", ward);
                    }
                } else {
                    log.warn("Address normalization returned null");
                }
            } catch (Exception e) {
                log.error("Error normalizing address, using original values: {}", e.getMessage());
                // Keep original values if normalization fails
            }
        }

        // Build address request with normalized values
        NewAddressRequest addressRequest = new NewAddressRequest(
                customerId,
                addressComponents.fullAddress(),
                normalizedWard,
                normalizedCity,
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
                1, // Default payment method
                null // additionalFeeIds
        );
    }

    /**
     * Build clarification message for missing fields
     */
    private String buildClarificationMessage(List<String> missingFields, Map<String, String> extractedFields) {
        StringBuilder message = new StringBuilder();

        // Check if service is missing (highest priority)
        boolean serviceMissing = missingFields.contains("service");
        
            if (serviceMissing && extractedFields.containsKey("availableServices")) {
                // CRITICAL: Service is missing - this is the most important field
                message.append("Xin lỗi, tôi không thể xác định được dịch vụ bạn muốn đặt.\n\n");
                message.append("📋 Các dịch vụ hiện có:\n");
                message.append(extractedFields.get("availableServices"));
                message.append("\n\n💡 Vui lòng nói lại và chỉ rõ dịch vụ bạn cần.");
        } else {
            // Standard handling for other missing fields without echoing understood fields
            if (!missingFields.isEmpty()) {
                message.append("⚠️ Tôi cần thêm thông tin về:\n");
                for (String field : missingFields) {
                    message.append("  • ").append(formatFieldName(field)).append("\n");
                }
                message.append("\n💡 Vui lòng cung cấp thêm thông tin để hoàn tất đặt lịch.");
            }
        }

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
