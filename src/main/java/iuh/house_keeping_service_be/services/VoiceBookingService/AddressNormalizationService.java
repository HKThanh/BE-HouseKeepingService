package iuh.house_keeping_service_be.services.VoiceBookingService;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

/**
 * Service for normalizing Vietnamese addresses using external API
 * Converts city and ward names to standardized format
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AddressNormalizationService {

    private static final String PROVINCES_API_URL = "https://production.cas.so/address-kit/2025-07-01/provinces";
    private static final String COMMUNES_API_URL_TEMPLATE = "https://production.cas.so/address-kit/2025-07-01/provinces/%s/communes";
    private static final double SIMILARITY_THRESHOLD = 0.6; // Lower threshold for address matching

    private final RestTemplate restTemplate;

    /**
     * Normalize city and ward names
     */
    public NormalizedAddress normalizeAddress(String city, String ward) {
        return normalizeAddress(city, ward, null);
    }

    /**
     * Normalize city and ward names, and update fullAddress with normalized values
     */
    public NormalizedAddress normalizeAddress(String city, String ward, String fullAddress) {
        log.info("Normalizing address - City: {}, Ward: {}, FullAddress: {}", city, ward, fullAddress);

        try {
            // Step 1: Normalize city
            ProvinceData normalizedProvince = normalizeCity(city);
            if (normalizedProvince == null) {
                log.warn("Could not normalize city: {}", city);
                return new NormalizedAddress(city, ward, null, null, fullAddress);
            }

            log.info("City normalized: {} -> {} (code: {})", city, normalizedProvince.getName(), normalizedProvince.getCode());

            // Step 2: Normalize ward using the province code
            String normalizedWard = null;
            String wardCode = null;
            if (ward != null && !ward.isBlank()) {
                CommuneData normalizedCommune = normalizeWard(normalizedProvince.getCode(), ward);
                if (normalizedCommune != null) {
                    normalizedWard = normalizedCommune.getName();
                    wardCode = normalizedCommune.getCode();
                    log.info("Ward normalized: {} -> {} (code: {})", ward, normalizedWard, wardCode);
                } else {
                    log.warn("Could not normalize ward: {} for province: {}", ward, normalizedProvince.getName());
                    normalizedWard = ward; // Keep original if can't normalize
                }
            }

            // Step 3: Update fullAddress with normalized city and ward
            String normalizedFullAddress = updateFullAddress(
                    fullAddress,
                    city,
                    normalizedProvince.getName(),
                    ward,
                    normalizedWard != null ? normalizedWard : ward
            );

            return new NormalizedAddress(
                    normalizedProvince.getName(),
                    normalizedWard != null ? normalizedWard : ward,
                    normalizedProvince.getCode(),
                    wardCode,
                    normalizedFullAddress
            );

        } catch (Exception e) {
            log.error("Error normalizing address: {}", e.getMessage(), e);
            return new NormalizedAddress(city, ward, null, null, fullAddress);
        }
    }

    /**
     * Update fullAddress by replacing original city and ward with normalized values
     */
    private String updateFullAddress(String fullAddress, String originalCity, String normalizedCity,
                                      String originalWard, String normalizedWard) {
        if (fullAddress == null || fullAddress.isBlank()) {
            return fullAddress;
        }

        String updatedAddress = fullAddress;

        // Replace city in fullAddress
        if (originalCity != null && !originalCity.isBlank() && normalizedCity != null) {
            updatedAddress = replaceAddressPart(updatedAddress, originalCity, normalizedCity);
            log.debug("Updated city in fullAddress: {} -> {}", originalCity, normalizedCity);
        }

        // Replace ward in fullAddress
        if (originalWard != null && !originalWard.isBlank() && normalizedWard != null) {
            updatedAddress = replaceAddressPart(updatedAddress, originalWard, normalizedWard);
            log.debug("Updated ward in fullAddress: {} -> {}", originalWard, normalizedWard);
        }

        log.info("FullAddress updated: '{}' -> '{}'", fullAddress, updatedAddress);
        return updatedAddress;
    }

    /**
     * Replace address part using case-insensitive matching with fuzzy support
     */
    private String replaceAddressPart(String fullAddress, String original, String replacement) {
        if (fullAddress == null || original == null || replacement == null) {
            return fullAddress;
        }

        // Skip if original and replacement are the same
        if (original.equalsIgnoreCase(replacement)) {
            return fullAddress;
        }

        // Clean both original and replacement to get names without prefixes
        String cleanedOriginal = cleanAddressPart(original);
        String cleanedReplacement = cleanAddressPart(replacement);
        
        // Check if replacement has prefix (like "Phường", "Thành phố")
        boolean replacementHasWardPrefix = replacement.toLowerCase().matches("^(phường|xã|thị trấn)\\s+.*");
        boolean replacementHasCityPrefix = replacement.toLowerCase().matches("^(thành phố|tp\\.?|tỉnh)\\s+.*");
        
        String result = fullAddress;
        
        // Strategy 1: If fullAddress already has prefix + name, replace with just the cleaned name
        // e.g., "phường Gò Vấp" in fullAddress should become "Phường Gò Vấp" (not "phường Phường Gò Vấp")
        if (replacementHasWardPrefix) {
            // Pattern to match existing "phường + ward name" and replace with normalized version
            java.util.regex.Pattern wardWithPrefixPattern = java.util.regex.Pattern.compile(
                "(phường|Phường|PHƯỜNG|xã|Xã|thị trấn|Thị trấn)\\s+" + 
                java.util.regex.Pattern.quote(cleanedOriginal),
                java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.UNICODE_CASE
            );
            java.util.regex.Matcher wardMatcher = wardWithPrefixPattern.matcher(result);
            if (wardMatcher.find()) {
                result = wardMatcher.replaceAll(replacement);
                log.debug("Replaced ward with prefix: {}", replacement);
                return result;
            }
        }
        
        if (replacementHasCityPrefix) {
            // Pattern to match existing "thành phố/tp + city name" and replace with normalized version
            java.util.regex.Pattern cityWithPrefixPattern = java.util.regex.Pattern.compile(
                "(thành phố|Thành phố|THÀNH PHỐ|tp\\.?|TP\\.?|tỉnh|Tỉnh)\\s+" + 
                java.util.regex.Pattern.quote(cleanedOriginal),
                java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.UNICODE_CASE
            );
            java.util.regex.Matcher cityMatcher = cityWithPrefixPattern.matcher(result);
            if (cityMatcher.find()) {
                result = cityMatcher.replaceAll(replacement);
                log.debug("Replaced city with prefix: {}", replacement);
                return result;
            }
        }

        // Strategy 2: Try exact match (case-insensitive)
        result = fullAddress.replaceAll("(?i)" + java.util.regex.Pattern.quote(original), replacement);
        if (!result.equals(fullAddress)) {
            return result;
        }
        
        // Strategy 3: Match just the name without prefix and replace with cleaned replacement
        // This avoids adding prefix when fullAddress already has one
        if (!cleanedOriginal.equals(original.toLowerCase().trim())) {
            // Original had prefix, try to match the cleaned name
            result = fullAddress.replaceAll("(?i)" + java.util.regex.Pattern.quote(cleanedOriginal), 
                    cleanedReplacement);
            if (!result.equals(fullAddress)) {
                return result;
            }
        }

        // Strategy 4: Fuzzy ward replacement
        if (result.equals(fullAddress) && (original.toLowerCase().contains("phường") || 
                replacement.toLowerCase().contains("phường"))) {
            result = fuzzyReplaceWard(fullAddress, original, replacement);
        }

        return result;
    }

    /**
     * Fuzzy replace ward in fullAddress by finding similar ward pattern
     */
    private String fuzzyReplaceWard(String fullAddress, String originalWard, String normalizedWard) {
        if (fullAddress == null || normalizedWard == null) {
            return fullAddress;
        }

        // Extract ward name without prefix
        String normalizedWardName = cleanAddressPart(normalizedWard);
        String originalWardName = cleanAddressPart(originalWard);
        
        // Pattern to match "phường/Phường + ward name" with flexible spacing
        // This will find ward patterns like "phường Tây Thành", "Phường Tây Thạnh", etc.
        java.util.regex.Pattern wardPattern = java.util.regex.Pattern.compile(
            "(phường|Phường|PHƯỜNG)\\s+([^,]+?)(?=,|$)",
            java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.UNICODE_CASE
        );
        
        java.util.regex.Matcher matcher = wardPattern.matcher(fullAddress);
        
        while (matcher.find()) {
            String foundWardName = matcher.group(2).trim();
            // Calculate similarity between found ward and original ward name
            double similarity = calculateSimilarity(
                normalizeVietnamese(foundWardName.toLowerCase()),
                normalizeVietnamese(originalWardName.toLowerCase())
            );
            
            log.debug("Comparing ward in fullAddress: '{}' with original ward: '{}', similarity: {}", 
                foundWardName, originalWardName, similarity);
            
            // If similarity is high enough, replace with normalized ward
            if (similarity >= 0.6) {
                String matchedPart = matcher.group(0);
                String replacementPart = "Phường " + normalizedWardName.substring(0, 1).toUpperCase() + 
                        normalizedWardName.substring(1);
                
                // Use the full normalized ward name if it has proper capitalization
                if (normalizedWard.startsWith("Phường ")) {
                    replacementPart = normalizedWard;
                }
                
                log.info("Fuzzy replacing ward in fullAddress: '{}' -> '{}'", matchedPart, replacementPart);
                return fullAddress.replace(matchedPart, replacementPart);
            }
        }
        
        return fullAddress;
    }

    /**
     * Normalize Vietnamese text by removing diacritics for comparison
     */
    private String normalizeVietnamese(String text) {
        if (text == null) return "";
        String normalized = java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "").toLowerCase();
    }

    /**
     * Clean address part by removing common prefixes
     */
    private String cleanAddressPart(String part) {
        if (part == null) {
            return "";
        }
        String cleaned = part.toLowerCase().trim();
        // Remove common prefixes for city
        cleaned = cleaned.replaceAll("^(thành phố|tp\\.?|tỉnh)\\s*", "");
        // Remove common prefixes for ward
        cleaned = cleaned.replaceAll("^(phường|xã|thị trấn|quận)\\s*", "");
        return cleaned.trim();
    }

    /**
     * Normalize city name by matching against provinces API
     */
    private ProvinceData normalizeCity(String city) {
        if (city == null || city.isBlank()) {
            return null;
        }

        try {
            // Fetch provinces from API
            log.debug("Fetching provinces from API: {}", PROVINCES_API_URL);
            
            // Try to get as String first to debug response format
            String rawResponse = null;
            try {
                rawResponse = restTemplate.getForObject(PROVINCES_API_URL, String.class);
                log.debug("Raw API response (first 500 chars): {}", 
                    rawResponse != null && rawResponse.length() > 500 ? rawResponse.substring(0, 500) : rawResponse);
            } catch (Exception e) {
                log.warn("Could not fetch raw response for debugging: {}", e.getMessage());
            }
            
            ProvinceResponse provinceResponse = restTemplate.getForObject(PROVINCES_API_URL, ProvinceResponse.class);

            if (provinceResponse == null) {
                log.error("Provinces API returned null response. Raw response available: {}", rawResponse != null);
                return null;
            }
            
            if (provinceResponse.getProvinces() == null) {
                log.error("Provinces API response has null provinces field. Response object: {}", provinceResponse);
                return null;
            }
            
            if (provinceResponse.getProvinces().isEmpty()) {
                log.error("Provinces API returned empty provinces list");
                return null;
            }
            
            log.debug("Successfully fetched {} provinces from API", provinceResponse.getProvinces().size());

            // Clean input city name
            String cleanedCity = cleanCityName(city);
            log.debug("Cleaned city name: '{}' -> '{}'", city, cleanedCity);

            // Try exact match first
            Optional<ProvinceData> exactMatch = provinceResponse.getProvinces().stream()
                    .filter(p -> cleanCityName(p.getName()).equalsIgnoreCase(cleanedCity))
                    .findFirst();

            if (exactMatch.isPresent()) {
                log.info("City exact match found: {} -> {}", city, exactMatch.get().getName());
                return exactMatch.get();
            }

            // Try fuzzy matching
            ProvinceData bestMatch = null;
            double bestSimilarity = 0.0;

            for (ProvinceData province : provinceResponse.getProvinces()) {
                String cleanedProvinceName = cleanCityName(province.getName());
                double similarity = calculateSimilarity(cleanedCity, cleanedProvinceName);

                if (similarity > bestSimilarity && similarity >= SIMILARITY_THRESHOLD) {
                    bestSimilarity = similarity;
                    bestMatch = province;
                }
            }

            if (bestMatch != null) {
                log.info("City matched with similarity {}: {} -> {}", bestSimilarity, city, bestMatch.getName());
            } else {
                log.warn("No city match found for: {} (cleaned: {})", city, cleanedCity);
            }

            return bestMatch;

        } catch (Exception e) {
            log.error("Error normalizing city: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Normalize ward name by matching against communes API
     */
    private CommuneData normalizeWard(String provinceCode, String ward) {
        if (provinceCode == null || ward == null || ward.isBlank()) {
            return null;
        }

        try {
            // Fetch communes for the province
            String url = String.format(COMMUNES_API_URL_TEMPLATE, provinceCode);
            log.debug("Fetching communes from API: {}", url);
            CommuneResponse communeResponse = restTemplate.getForObject(url, CommuneResponse.class);

            if (communeResponse == null) {
                log.error("Communes API returned null response for province: {}", provinceCode);
                return null;
            }
            
            if (communeResponse.getCommunes() == null) {
                log.error("Communes API response has null communes field for province: {}", provinceCode);
                return null;
            }
            
            if (communeResponse.getCommunes().isEmpty()) {
                log.error("Communes API returned empty communes list for province: {}", provinceCode);
                return null;
            }
            
            log.debug("Successfully fetched {} communes for province {}", communeResponse.getCommunes().size(), provinceCode);

            // Clean input ward name
            String cleanedWard = cleanWardName(ward);
            log.debug("Cleaned ward name: '{}' -> '{}'", ward, cleanedWard);

            // Try exact match first
            Optional<CommuneData> exactMatch = communeResponse.getCommunes().stream()
                    .filter(c -> cleanWardName(c.getName()).equalsIgnoreCase(cleanedWard))
                    .findFirst();

            if (exactMatch.isPresent()) {
                log.info("Ward exact match found: {} -> {}", ward, exactMatch.get().getName());
                return exactMatch.get();
            }

            // Try fuzzy matching
            CommuneData bestMatch = null;
            double bestSimilarity = 0.0;

            for (CommuneData commune : communeResponse.getCommunes()) {
                String cleanedCommuneName = cleanWardName(commune.getName());
                double similarity = calculateSimilarity(cleanedWard, cleanedCommuneName);

                if (similarity > bestSimilarity && similarity >= SIMILARITY_THRESHOLD) {
                    bestSimilarity = similarity;
                    bestMatch = commune;
                }
            }

            if (bestMatch != null) {
                log.info("Ward matched with similarity {}: {} -> {}", bestSimilarity, ward, bestMatch.getName());
            } else {
                log.warn("No ward match found for: {} (cleaned: {}) in province: {}", ward, cleanedWard, provinceCode);
            }

            return bestMatch;

        } catch (Exception e) {
            log.error("Error normalizing ward: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Clean city name by removing common prefixes and normalizing
     */
    private String cleanCityName(String city) {
        if (city == null) {
            return "";
        }

        String cleaned = city.toLowerCase().trim();

        // Remove common prefixes
        cleaned = cleaned.replaceAll("^(thành phố|tp\\.?|tỉnh)\\s*", "");
        
        // Normalize special characters
        cleaned = cleaned.replaceAll("[.]+", "");
        cleaned = cleaned.replaceAll("\\s+", " ").trim();

        return cleaned;
    }

    /**
     * Clean ward name by removing common prefixes and normalizing
     */
    private String cleanWardName(String ward) {
        if (ward == null) {
            return "";
        }

        String cleaned = ward.toLowerCase().trim();

        // Remove common prefixes
        cleaned = cleaned.replaceAll("^(phường|xã|thị trấn|quận)\\s*", "");
        
        // Normalize special characters
        cleaned = cleaned.replaceAll("[.]+", "");
        cleaned = cleaned.replaceAll("\\s+", " ").trim();

        return cleaned;
    }

    /**
     * Calculate similarity between two strings using Levenshtein distance
     * Enhanced with phonetic normalization for Vietnamese
     */
    private double calculateSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return 0.0;
        }

        if (s1.equals(s2)) {
            return 1.0;
        }

        // Check if one string contains the other
        if (s1.contains(s2) || s2.contains(s1)) {
            return 0.9;
        }

        // Try phonetic normalization for Vietnamese
        String phonetic1 = normalizeVietnamesePhonetic(s1);
        String phonetic2 = normalizeVietnamesePhonetic(s2);
        
        if (phonetic1.equals(phonetic2)) {
            return 0.95; // High confidence for phonetic match
        }

        // Calculate distance on original strings
        int distance = levenshteinDistance(s1, s2);
        int maxLength = Math.max(s1.length(), s2.length());
        double originalSimilarity = maxLength == 0 ? 1.0 : 1.0 - ((double) distance / maxLength);

        // Calculate distance on phonetically normalized strings
        int phoneticDistance = levenshteinDistance(phonetic1, phonetic2);
        int phoneticMaxLength = Math.max(phonetic1.length(), phonetic2.length());
        double phoneticSimilarity = phoneticMaxLength == 0 ? 1.0 : 1.0 - ((double) phoneticDistance / phoneticMaxLength);

        // Return the higher similarity score
        return Math.max(originalSimilarity, phoneticSimilarity);
    }

    /**
     * Normalize Vietnamese phonetic variations
     * Handles common speech-to-text errors like "Gòi Dắp" -> "Gò Vấp"
     */
    private String normalizeVietnamesePhonetic(String text) {
        if (text == null) {
            return "";
        }
        
        String normalized = text.toLowerCase().trim();
        
        // Remove diacritics for base comparison
        normalized = java.text.Normalizer.normalize(normalized, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        
        // Common phonetic substitutions for Vietnamese speech-to-text errors
        // "oi" often misheard as "o" (e.g., "goi" -> "go")
        normalized = normalized.replaceAll("oi", "o");
        // "ap" and "up" and "op" are often confused (e.g., "dap" -> "vap")
        normalized = normalized.replaceAll("[aou]p", "ap");
        // "d" and "v" are often confused in Vietnamese speech recognition
        normalized = normalized.replaceAll("^d", "v"); // at start of word
        normalized = normalized.replaceAll("\\bd", "v"); // at word boundary
        // "gi" and "d" sounds similar
        normalized = normalized.replaceAll("gi", "d");
        // "ng" and "n" sometimes confused
        normalized = normalized.replaceAll("ngh?", "n");
        // "kh" and "k" or "c"
        normalized = normalized.replaceAll("kh", "k");
        // "tr" and "ch" confusion
        normalized = normalized.replaceAll("tr", "ch");
        // "s" and "x" confusion  
        normalized = normalized.replaceAll("x", "s");
        // Remove double consonants
        normalized = normalized.replaceAll("(.)\\1+", "$1");
        // Normalize spaces
        normalized = normalized.replaceAll("\\s+", " ").trim();
        
        return normalized;
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
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;

                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }

        return dp[s1.length()][s2.length()];
    }

    // DTOs for API responses
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProvinceResponse {
        private List<ProvinceData> provinces;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProvinceData {
        private String code;
        private String name;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CommuneResponse {
        private List<CommuneData> communes;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CommuneData {
        private String code;
        private String name;
    }

    /**
     * Record to hold normalized address data
     */
    public record NormalizedAddress(
            String normalizedCity,
            String normalizedWard,
            String cityCode,
            String wardCode,
            String normalizedFullAddress
    ) {}
}
