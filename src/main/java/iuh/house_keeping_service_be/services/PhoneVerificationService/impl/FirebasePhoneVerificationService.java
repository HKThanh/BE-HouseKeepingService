package iuh.house_keeping_service_be.services.PhoneVerificationService.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import iuh.house_keeping_service_be.config.FirebaseProperties;
import iuh.house_keeping_service_be.dtos.Authentication.PhoneOtpRequest;
import iuh.house_keeping_service_be.dtos.Authentication.PhoneOtpSendResponse;
import iuh.house_keeping_service_be.dtos.Authentication.PhoneOtpVerifyRequest;
import iuh.house_keeping_service_be.dtos.Authentication.PhoneOtpVerifyResponse;
import iuh.house_keeping_service_be.models.Account;
import iuh.house_keeping_service_be.repositories.AccountRepository;
import iuh.house_keeping_service_be.services.PhoneVerificationService.PhoneVerificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class FirebasePhoneVerificationService implements PhoneVerificationService {
    private static final String FIREBASE_SEND_OTP_URL = "https://identitytoolkit.googleapis.com/v1/accounts:sendVerificationCode?key=%s";
    private static final String FIREBASE_VERIFY_OTP_URL = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPhoneNumber?key=%s";

    private final FirebaseProperties firebaseProperties;
    private final RestTemplate restTemplate;
    private final AccountRepository accountRepository;
    private final ObjectMapper objectMapper;

    @Override
    public PhoneOtpSendResponse sendOtp(PhoneOtpRequest request) {
        ensurePhoneAuthEnabled();

        String apiKey = firebaseProperties.getApiKey();
        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalStateException("Chưa cấu hình Firebase API key");
        }

        String formattedPhone = formatPhoneNumber(request.phoneNumber());
        Map<String, Object> payload = Map.ofEntries(
            Map.entry("phoneNumber", formattedPhone)
            // Map.entry(resolveVerificationProofKey(request), resolveVerificationProofValue(request))
        );

        try {
            ResponseEntity<FirebaseSendOtpResponse> response = restTemplate.postForEntity(
                String.format(FIREBASE_SEND_OTP_URL, apiKey),
                payload,
                FirebaseSendOtpResponse.class
            );

            FirebaseSendOtpResponse body = response.getBody();
            if (body == null || !StringUtils.hasText(body.sessionInfo())) {
                throw new IllegalStateException("Firebase không trả về sessionInfo");
            }

            return new PhoneOtpSendResponse(body.sessionInfo(), firebaseProperties.getPhoneAuth().getSessionTtlSeconds());
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            throw new IllegalStateException(parseFirebaseError(ex), ex);
        }
    }

    @Override
    @Transactional
    public PhoneOtpVerifyResponse verifyOtp(PhoneOtpVerifyRequest request) {
        ensurePhoneAuthEnabled();

        String apiKey = firebaseProperties.getApiKey();
        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalStateException("Chưa cấu hình Firebase API key");
        }

        Map<String, Object> payload = Map.of(
            "sessionInfo", request.sessionInfo().trim(),
            "code", request.otpCode().trim()
        );

        try {
            ResponseEntity<FirebaseVerifyOtpResponse> response = restTemplate.postForEntity(
                String.format(FIREBASE_VERIFY_OTP_URL, apiKey),
                payload,
                FirebaseVerifyOtpResponse.class
            );

            FirebaseVerifyOtpResponse body = response.getBody();
            if (body == null || !StringUtils.hasText(body.phoneNumber())) {
                throw new IllegalStateException("Firebase không trả về số điện thoại");
            }

            Optional<Account> accountOptional = resolveAccountByPhone(body.phoneNumber());
            String accountId = null;
            boolean phoneVerified = false;

            if (accountOptional.isPresent()) {
                Account account = accountOptional.get();
                accountId = account.getAccountId();

                if (!Boolean.TRUE.equals(account.getIsPhoneVerified())) {
                    account.setIsPhoneVerified(true);
                    accountRepository.save(account);
                }

                phoneVerified = Boolean.TRUE.equals(account.getIsPhoneVerified());
            }

            return new PhoneOtpVerifyResponse(
                body.idToken(),
                body.refreshToken(),
                body.localId(),
                body.phoneNumber(),
                accountId,
                phoneVerified,
                Boolean.TRUE.equals(body.isNewUser()),
                parseExpiresIn(body.expiresIn())
            );
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            throw new IllegalStateException(parseFirebaseError(ex), ex);
        }
    }

    private void ensurePhoneAuthEnabled() {
        if (firebaseProperties == null || !firebaseProperties.isEnabled() || firebaseProperties.getPhoneAuth() == null || !firebaseProperties.getPhoneAuth().isEnabled()) {
            throw new IllegalStateException("Tính năng xác thực số điện thoại bằng Firebase chưa được bật");
        }
    }

    private String resolveVerificationProofKey(PhoneOtpRequest request) {
        if (StringUtils.hasText(request.recaptchaToken())) {
            return "recaptchaToken";
        }
        if (StringUtils.hasText(request.safetyNetToken())) {
            return "safetyNetToken";
        }
        throw new IllegalArgumentException("recaptchaToken hoặc safetyNetToken là bắt buộc");
    }

    private Object resolveVerificationProofValue(PhoneOtpRequest request) {
        if (StringUtils.hasText(request.recaptchaToken())) {
            return request.recaptchaToken().trim();
        }
        if (StringUtils.hasText(request.safetyNetToken())) {
            return request.safetyNetToken().trim();
        }
        throw new IllegalArgumentException("recaptchaToken hoặc safetyNetToken là bắt buộc");
    }

    private String formatPhoneNumber(String phoneNumber) {
        if (!StringUtils.hasText(phoneNumber)) {
            throw new IllegalArgumentException("Số điện thoại không hợp lệ");
        }

        String sanitized = phoneNumber.replaceAll("\\s+", "");
        if (sanitized.startsWith("+")) {
            return sanitized;
        }
        if (sanitized.startsWith("00")) {
            return "+" + sanitized.substring(2);
        }
        // Default to Vietnam (+84) when users provide local 0xxx numbers
        if (sanitized.startsWith("0")) {
            return "+84" + sanitized.substring(1);
        }
        return "+" + sanitized;
    }

    private Optional<Account> resolveAccountByPhone(String firebasePhone) {
        Set<String> candidates = new LinkedHashSet<>();
        if (StringUtils.hasText(firebasePhone)) {
            String trimmed = firebasePhone.trim();
            candidates.add(trimmed);
            candidates.add(trimmed.replace("+", ""));
        }

        String localFormat = toLocalPhoneNumber(firebasePhone);
        if (StringUtils.hasText(localFormat)) {
            candidates.add(localFormat);
        }

        for (String candidate : candidates) {
            if (!StringUtils.hasText(candidate)) {
                continue;
            }
            Optional<Account> account = accountRepository.findByPhoneNumber(candidate);
            if (account.isPresent()) {
                return account;
            }
        }

        return Optional.empty();
    }

    private String toLocalPhoneNumber(String firebasePhone) {
        if (!StringUtils.hasText(firebasePhone)) {
            return null;
        }

        String sanitized = firebasePhone.replaceAll("\\s+", "");
        if (sanitized.startsWith("+84") && sanitized.length() > 3) {
            return "0" + sanitized.substring(3);
        }
        if (sanitized.startsWith("84") && sanitized.length() > 2) {
            return "0" + sanitized.substring(2);
        }
        return sanitized;
    }

    private long parseExpiresIn(String expiresIn) {
        if (!StringUtils.hasText(expiresIn)) {
            return firebaseProperties.getPhoneAuth().getSessionTtlSeconds();
        }
        try {
            return Long.parseLong(expiresIn);
        } catch (NumberFormatException ex) {
            log.warn("Không thể parse expiresIn: {}", expiresIn);
            return firebaseProperties.getPhoneAuth().getSessionTtlSeconds();
        }
    }

    private String parseFirebaseError(RestClientResponseException ex) {
        try {
            JsonNode node = objectMapper.readTree(ex.getResponseBodyAsString());
            JsonNode errorNode = node.path("error");
            String message = errorNode.path("message").asText();
            return translateFirebaseError(message);
        } catch (Exception e) {
            log.warn("Không thể parse lỗi Firebase: {}", ex.getResponseBodyAsString());
            return "Firebase OTP error: " + ex.getStatusText();
        }
    }

    private String translateFirebaseError(String firebaseMessage) {
        return switch (firebaseMessage) {
            case "INVALID_PHONE_NUMBER" -> "Số điện thoại không hợp lệ";
            case "TOO_MANY_ATTEMPTS_TRY_LATER" -> "Đã vượt quá số lần thử, vui lòng thử lại sau";
            case "SESSION_EXPIRED" -> "Mã OTP đã hết hạn, hãy yêu cầu mã mới";
            case "INVALID_CODE" -> "Mã OTP không chính xác";
            default -> "Firebase error: " + firebaseMessage;
        };
    }

    private record FirebaseSendOtpResponse(String sessionInfo) {
    }

    private record FirebaseVerifyOtpResponse(
        String idToken,
        String refreshToken,
        String phoneNumber,
        String localId,
        Boolean isNewUser,
        String expiresIn
    ) {
    }
}
