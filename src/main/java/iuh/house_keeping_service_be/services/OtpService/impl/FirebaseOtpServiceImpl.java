package iuh.house_keeping_service_be.services.OtpService.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuth;
import iuh.house_keeping_service_be.enums.OtpType;
import iuh.house_keeping_service_be.services.OtpService.OtpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Firebase OTP Service Implementation
 * 
 * Sử dụng Firebase Identity Toolkit API để gửi OTP qua SMS
 * API Docs: https://firebase.google.com/docs/reference/rest/auth
 */
@Service
@Slf4j
public class FirebaseOtpServiceImpl implements OtpService {

    private static final String OTP_PREFIX = "otp:";
    private static final String OTP_ATTEMPTS_PREFIX = "otp_attempts:";
    private static final String OTP_COOLDOWN_PREFIX = "otp_cooldown:";
    private static final String OTP_VERIFICATION_TOKEN_PREFIX = "otp_verified:";
    
    // Firebase Identity Toolkit API endpoints
    private static final String FIREBASE_SEND_OTP_URL = "https://identitytoolkit.googleapis.com/v1/accounts:sendVerificationCode";
    private static final String FIREBASE_VERIFY_OTP_URL = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPhoneNumber";

    @Autowired(required = false)
    private FirebaseAuth firebaseAuth;

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    @Value("${firebase.enabled:false}")
    private boolean enabled;

    @Value("${firebase.api-key:}")
    private String firebaseApiKey;

    @Value("${firebase.phone.otp-length:6}")
    private int otpLength;

    @Value("${firebase.phone.session-ttl:120}")
    private int sessionTtlSeconds;

    @Value("${otp.max-attempts:5}")
    private int maxAttempts;

    @Value("${otp.resend-cooldown-seconds:60}")
    private int resendCooldownSeconds;

    @Value("${otp.expiration-minutes:5}")
    private int expirationMinutes;

    // Fallback mode: tạo OTP giả và log ra console khi Firebase không khả dụng
    @Value("${firebase.phone.fallback-to-log:true}")
    private boolean fallbackToLog;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final SecureRandom secureRandom = new SecureRandom();

    public FirebaseOtpServiceImpl() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public boolean isEnabled() {
        return enabled && StringUtils.hasText(firebaseApiKey);
    }

    @Override
    public String sendOtp(String phoneNumber, OtpType otpType, String recaptchaToken) {
        String normalizedPhone = normalizePhoneNumber(phoneNumber);
        String cooldownKey = buildKey(OTP_COOLDOWN_PREFIX, normalizedPhone, otpType);

        // Kiểm tra cooldown
        if (Boolean.TRUE.equals(redisTemplate.hasKey(cooldownKey))) {
            Long ttl = redisTemplate.getExpire(cooldownKey, TimeUnit.SECONDS);
            throw new IllegalStateException("Vui lòng chờ " + (ttl != null ? ttl : resendCooldownSeconds) + " giây trước khi gửi lại OTP");
        }

        String sessionInfo;

        if (!isEnabled()) {
            // Fallback mode: tạo OTP giả
            if (fallbackToLog) {
                sessionInfo = handleFallbackMode(normalizedPhone, otpType);
            } else {
                throw new RuntimeException("Firebase OTP service không được cấu hình");
            }
        } else {
            // Gửi OTP qua Firebase
            sessionInfo = sendOtpViaFirebase(normalizedPhone, recaptchaToken);
        }

        // Đặt cooldown
        redisTemplate.opsForValue().set(cooldownKey, "1", resendCooldownSeconds, TimeUnit.SECONDS);

        // Reset số lần thử
        String attemptsKey = buildKey(OTP_ATTEMPTS_PREFIX, normalizedPhone, otpType);
        redisTemplate.delete(attemptsKey);

        log.info("OTP sent to phone: {} for type: {}", maskPhoneNumber(normalizedPhone), otpType);
        return sessionInfo;
    }

    /**
     * Gửi OTP qua Firebase Identity Toolkit API
     */
    private String sendOtpViaFirebase(String phoneNumber, String recaptchaToken) {
        try {
            String url = FIREBASE_SEND_OTP_URL + "?key=" + firebaseApiKey;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("phoneNumber", phoneNumber);
            
            // reCAPTCHA token nếu có (required cho web clients)
            if (StringUtils.hasText(recaptchaToken)) {
                requestBody.put("recaptchaToken", recaptchaToken);
            }

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            log.debug("Sending OTP request to Firebase for phone: {}", maskPhoneNumber(phoneNumber));

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                
                if (jsonResponse.has("sessionInfo")) {
                    String sessionInfo = jsonResponse.get("sessionInfo").asText();
                    log.info("Firebase OTP sent successfully to: {}", maskPhoneNumber(phoneNumber));
                    return sessionInfo;
                }
            }

            log.error("Firebase send OTP failed: {}", response.getBody());
            throw new RuntimeException("Không thể gửi mã OTP. Vui lòng thử lại sau");

        } catch (Exception e) {
            log.error("Failed to send OTP via Firebase: {}", e.getMessage());
            
            if (fallbackToLog) {
                log.warn("Falling back to DEV MODE for OTP");
                return handleFallbackMode(phoneNumber, OtpType.VERIFY_PHONE);
            }
            
            throw new RuntimeException("Không thể gửi mã OTP: " + e.getMessage());
        }
    }

    /**
     * Fallback mode: Tạo OTP giả và lưu vào Redis
     */
    private String handleFallbackMode(String phoneNumber, OtpType otpType) {
        String otp = generateOtp();
        String sessionInfo = "DEV_" + UUID.randomUUID().toString();
        String otpKey = buildKey(OTP_PREFIX, phoneNumber, otpType);

        // Lưu OTP vào Redis
        redisTemplate.opsForValue().set(otpKey, otp, expirationMinutes, TimeUnit.MINUTES);
        // Lưu sessionInfo mapping
        redisTemplate.opsForValue().set("session:" + sessionInfo, phoneNumber + ":" + otpType.name(), expirationMinutes, TimeUnit.MINUTES);

        log.warn("=========================================");
        log.warn("[DEV MODE - FALLBACK] OTP for phone: {}", phoneNumber);
        log.warn("[DEV MODE - FALLBACK] OTP Code: {}", otp);
        log.warn("[DEV MODE - FALLBACK] Session: {}", sessionInfo);
        log.warn("=========================================");

        return sessionInfo;
    }

    @Override
    public String verifyOtp(String phoneNumber, String otp, OtpType otpType, String sessionInfo) {
        String normalizedPhone = normalizePhoneNumber(phoneNumber);
        String attemptsKey = buildKey(OTP_ATTEMPTS_PREFIX, normalizedPhone, otpType);

        // Kiểm tra số lần thử
        Object attemptsObj = redisTemplate.opsForValue().get(attemptsKey);
        int attempts = attemptsObj != null ? Integer.parseInt(attemptsObj.toString()) : 0;

        if (attempts >= maxAttempts) {
            cleanupOtpData(normalizedPhone, otpType);
            throw new IllegalStateException("Đã vượt quá số lần thử cho phép. Vui lòng yêu cầu OTP mới");
        }

        boolean verified;

        // Kiểm tra DEV MODE
        if (sessionInfo != null && sessionInfo.startsWith("DEV_")) {
            verified = verifyDevModeOtp(normalizedPhone, otp, otpType);
        } else if (!isEnabled()) {
            throw new RuntimeException("Firebase OTP service không được cấu hình");
        } else {
            verified = verifyOtpViaFirebase(otp, sessionInfo);
        }

        if (!verified) {
            // Tăng số lần thử
            redisTemplate.opsForValue().set(attemptsKey, String.valueOf(attempts + 1), expirationMinutes, TimeUnit.MINUTES);
            int remainingAttempts = maxAttempts - attempts - 1;
            throw new IllegalArgumentException("Mã OTP không chính xác. Còn " + remainingAttempts + " lần thử");
        }

        // OTP hợp lệ - Cleanup và tạo verification token
        cleanupOtpData(normalizedPhone, otpType);

        String verificationToken = UUID.randomUUID().toString();
        String tokenKey = buildKey(OTP_VERIFICATION_TOKEN_PREFIX, normalizedPhone, otpType);
        redisTemplate.opsForValue().set(tokenKey, verificationToken, 15, TimeUnit.MINUTES);

        log.info("OTP verified successfully for phone: {} type: {}", maskPhoneNumber(normalizedPhone), otpType);
        return verificationToken;
    }

    /**
     * Verify OTP trong DEV MODE
     */
    private boolean verifyDevModeOtp(String phoneNumber, String otp, OtpType otpType) {
        String otpKey = buildKey(OTP_PREFIX, phoneNumber, otpType);
        Object storedOtpObj = redisTemplate.opsForValue().get(otpKey);

        if (storedOtpObj == null) {
            throw new IllegalArgumentException("Mã OTP đã hết hạn hoặc không tồn tại");
        }

        return storedOtpObj.toString().equals(otp);
    }

    /**
     * Verify OTP qua Firebase Identity Toolkit API
     */
    private boolean verifyOtpViaFirebase(String otp, String sessionInfo) {
        try {
            String url = FIREBASE_VERIFY_OTP_URL + "?key=" + firebaseApiKey;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("sessionInfo", sessionInfo);
            requestBody.put("code", otp);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                
                // Nếu có idToken hoặc phoneNumber trong response => verified
                if (jsonResponse.has("idToken") || jsonResponse.has("phoneNumber")) {
                    log.info("Firebase OTP verified successfully");
                    return true;
                }
            }

            return false;

        } catch (Exception e) {
            log.error("Firebase OTP verification failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean validateVerificationToken(String phoneNumber, String token, OtpType otpType) {
        String normalizedPhone = normalizePhoneNumber(phoneNumber);
        String tokenKey = buildKey(OTP_VERIFICATION_TOKEN_PREFIX, normalizedPhone, otpType);

        Object storedTokenObj = redisTemplate.opsForValue().get(tokenKey);
        if (storedTokenObj == null) {
            return false;
        }

        return storedTokenObj.toString().equals(token);
    }

    @Override
    public void invalidateVerificationToken(String phoneNumber, OtpType otpType) {
        String normalizedPhone = normalizePhoneNumber(phoneNumber);
        String tokenKey = buildKey(OTP_VERIFICATION_TOKEN_PREFIX, normalizedPhone, otpType);
        redisTemplate.delete(tokenKey);
    }

    @Override
    public long getResendCooldown(String phoneNumber, OtpType otpType) {
        String normalizedPhone = normalizePhoneNumber(phoneNumber);
        String cooldownKey = buildKey(OTP_COOLDOWN_PREFIX, normalizedPhone, otpType);

        if (Boolean.TRUE.equals(redisTemplate.hasKey(cooldownKey))) {
            Long ttl = redisTemplate.getExpire(cooldownKey, TimeUnit.SECONDS);
            return ttl != null ? ttl : 0;
        }
        return 0;
    }

    private void cleanupOtpData(String phoneNumber, OtpType otpType) {
        String otpKey = buildKey(OTP_PREFIX, phoneNumber, otpType);
        String attemptsKey = buildKey(OTP_ATTEMPTS_PREFIX, phoneNumber, otpType);
        redisTemplate.delete(otpKey);
        redisTemplate.delete(attemptsKey);
    }

    private String generateOtp() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            otp.append(secureRandom.nextInt(10));
        }
        return otp.toString();
    }

    private String buildKey(String prefix, String phoneNumber, OtpType otpType) {
        return prefix + phoneNumber + ":" + otpType.name();
    }

    /**
     * Chuẩn hóa số điện thoại về định dạng quốc tế (+84...)
     */
    private String normalizePhoneNumber(String phoneNumber) {
        String normalized = phoneNumber.trim().replaceAll("\\s+", "");

        // Chuyển đầu số 0 thành +84 (Việt Nam)
        if (normalized.startsWith("0")) {
            normalized = "+84" + normalized.substring(1);
        }
        // Thêm + nếu bắt đầu bằng 84
        else if (normalized.startsWith("84") && !normalized.startsWith("+84")) {
            normalized = "+" + normalized;
        }
        // Thêm +84 nếu chưa có mã quốc gia
        else if (!normalized.startsWith("+")) {
            normalized = "+84" + normalized;
        }

        return normalized;
    }

    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 6) {
            return "***";
        }
        return phoneNumber.substring(0, 4) + "****" + phoneNumber.substring(phoneNumber.length() - 2);
    }
}
