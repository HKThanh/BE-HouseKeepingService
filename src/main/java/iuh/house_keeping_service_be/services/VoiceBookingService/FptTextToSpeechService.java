package iuh.house_keeping_service_be.services.VoiceBookingService;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import iuh.house_keeping_service_be.dtos.VoiceBooking.VoiceBookingPreview;
import iuh.house_keeping_service_be.dtos.VoiceBooking.VoiceBookingResponse;
import iuh.house_keeping_service_be.dtos.VoiceBooking.VoiceBookingSpeech;
import iuh.house_keeping_service_be.services.MediaService.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * Lightweight client that sends booking messages to FPT.AI Text-To-Speech service.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FptTextToSpeechService {

    private static final String PROVIDER = "fpt.ai";
    private static final Locale VI_LOCALE = new Locale("vi", "VN");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm 'ngày' dd/MM/yyyy");
    private static final Map<String, String> FIELD_LABELS = buildFieldLabels();

    private final RestTemplate restTemplate;
    private final CloudinaryService cloudinaryService;

    @Value("${tts.fpt.enabled:false}")
    private boolean enabled;

    @Value("${tts.fpt.api-url:https://api.fpt.ai/hmi/tts/v5}")
    private String apiUrl;

    @Value("${tts.fpt.api-key:}")
    private String apiKey;

    @Value("${tts.fpt.voice:banmai}")
    private String defaultVoice;

    @Value("${tts.fpt.speed:0}")
    private String defaultSpeed;

    @Value("${tts.fpt.text-limit:1500}")
    private int textLimit;

    @Value("${tts.fpt.verify-attempts:15}")
    private int verifyAttempts;

    @Value("${tts.fpt.verify-delay-ms:2000}")
    private long verifyDelayMs;

    @Value("${tts.fpt.verify-timeout-ms:60000}")
    private long verifyTimeoutMs;

    @Value("${tts.fpt.cloudinary-folder:voice_booking/tts}")
    private String cloudinaryFolder;

    @Value("${tts.fpt.provider-retries:2}")
    private int providerRetries;

    @Value("${tts.fpt.provider-retry-delay-ms:5000}")
    private long providerRetryDelayMs;

    @Value("${tts.fpt.download-initial-delay-ms:2000}")
    private long downloadInitialDelayMs;

    /**
     * Generates speech metadata for a VoiceBookingResponse if the integration is enabled.
     *
     * @param response response payload that should be narrated
     * @return synthesized speech metadata (if TTS succeeds)
     */
    public Optional<VoiceBookingSpeech> synthesizeResponseSpeech(VoiceBookingResponse response) {
        if (!enabled) {
            return Optional.empty();
        }

        if (response == null) {
            return Optional.empty();
        }

        String speechBody = buildSpeechBody(response);
        if (!StringUtils.hasText(speechBody)) {
            return Optional.empty();
        }

        if (!StringUtils.hasText(apiKey)) {
            log.warn("FPT.AI TTS is enabled but api-key is missing. Skip speech synthesis.");
            return Optional.empty();
        }

        return synthesize(speechBody, response.requestId());
    }

    private Optional<VoiceBookingSpeech> synthesize(String text, String requestId) {
        String cleanedText = text.replaceAll("\\s+", " ").trim();
        if (textLimit > 0 && cleanedText.length() > textLimit) {
            cleanedText = cleanedText.substring(0, textLimit);
        }

        HttpEntity<String> entity = buildRequestEntity(cleanedText);
        int attempts = Math.max(providerRetries, 1);
        for (int attempt = 1; attempt <= attempts; attempt++) {
            Optional<FptTextToSpeechApiResponse> providerResponse = invokeProvider(entity, requestId, attempt, attempts);
            if (providerResponse.isEmpty()) {
                sleep(providerRetryDelayMs);
                continue;
            }

            FptTextToSpeechApiResponse body = providerResponse.get();
            String uploadedAudioUrl = prepareAudioLink(body.asyncUrl, requestId);
            if (StringUtils.hasText(uploadedAudioUrl)) {
                return Optional.of(
                        VoiceBookingSpeech.builder()
                                .provider(PROVIDER)
                                .voice(defaultVoice)
                                .speed(defaultSpeed)
                                .audioUrl(uploadedAudioUrl)
                                .requestId(StringUtils.hasText(body.providerRequestId) ? body.providerRequestId : requestId)
                                .spokenText(cleanedText)
                                .build()
                );
            }

            if (attempt < attempts) {
                log.warn("FPT.AI TTS audio not available yet for request {} (provider attempt {}/{}). Retrying provider call...", 
                        requestId, attempt, attempts);
                sleep(providerRetryDelayMs);
            }
        }

        log.warn("FPT.AI TTS could not provide downloadable audio for request {} after {} attempts", requestId, attempts);
        return Optional.empty();
    }

    private HttpEntity<String> buildRequestEntity(String text) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "plain", StandardCharsets.UTF_8));
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set("api-key", apiKey);
        if (StringUtils.hasText(defaultVoice)) {
            headers.set("voice", defaultVoice);
        }
        if (StringUtils.hasText(defaultSpeed)) {
            headers.set("speed", defaultSpeed);
        }
        return new HttpEntity<>(text, headers);
    }

    private Optional<FptTextToSpeechApiResponse> invokeProvider(
            HttpEntity<String> entity,
            String requestId,
            int attempt,
            int totalAttempts
    ) {
        try {
            ResponseEntity<FptTextToSpeechApiResponse> responseEntity =
                    restTemplate.postForEntity(apiUrl, entity, FptTextToSpeechApiResponse.class);

            if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                log.warn("FPT.AI TTS returned non-success status: {} for request {} (attempt {}/{})",
                        responseEntity.getStatusCode(), requestId, attempt, totalAttempts);
                return Optional.empty();
            }

            FptTextToSpeechApiResponse body = responseEntity.getBody();
            if (body == null) {
                log.warn("FPT.AI TTS returned empty body for request {} (attempt {}/{})", requestId, attempt, totalAttempts);
                return Optional.empty();
            }

            if (body.errorCode != null && body.errorCode != 0) {
                log.warn("FPT.AI TTS error {} - {} for request {} (attempt {}/{})",
                        body.errorCode, body.message, requestId, attempt, totalAttempts);
                return Optional.empty();
            }

            log.info("FPT.AI TTS request successful for {}, async URL: {}", requestId, body.asyncUrl);
            return Optional.of(body);
        } catch (Exception ex) {
            log.error("Failed to call FPT.AI TTS for request {} on attempt {}/{}: {}",
                    requestId, attempt, totalAttempts, ex.getMessage(), ex);
            return Optional.empty();
        }
    }

    private String prepareAudioLink(String asyncUrl, String requestId) {
        if (!StringUtils.hasText(asyncUrl)) {
            log.warn("FPT TTS returned empty async URL for request {}", requestId);
            return null;
        }

        log.info("Preparing audio link for request {}, async URL: {}", requestId, asyncUrl);

        DownloadedAudio downloadedAudio = downloadAudioWithRetry(asyncUrl, requestId);
        if (downloadedAudio == null) {
            log.warn("Unable to download audio from FPT for request {} after configured attempts", requestId);
            return null;
        }

        try {
            log.info("Uploading TTS audio to Cloudinary for request {}, size: {} bytes",
                    requestId, downloadedAudio.data().length);
            Map<String, Object> uploadResult = cloudinaryService.uploadBytes(
                    downloadedAudio.data(),
                    downloadedAudio.contentType(),
                    cloudinaryFolder,
                    "voice_booking_tts_" + requestId
            );
            Object secureUrl = uploadResult.get("secureUrl");
            if (secureUrl instanceof String secure && StringUtils.hasText(secure)) {
                log.info("Successfully uploaded TTS audio to Cloudinary for request {}: {}", requestId, secure);
                return secure;
            }
            log.warn("Cloudinary upload did not return secure url for request {}", requestId);
        } catch (Exception ex) {
            log.error("Failed to upload TTS audio to Cloudinary for request {}: {}", requestId, ex.getMessage(), ex);
        }

        return null;
    }

    private DownloadedAudio downloadAudioWithRetry(String asyncUrl, String requestId) {
        int attempts = Math.max(verifyAttempts, 1);
        long delay = Math.max(verifyDelayMs, 300L);
        long timeout = verifyTimeoutMs > 0 ? verifyTimeoutMs : delay * attempts;

        long start = System.currentTimeMillis();
        log.info("Starting audio download for request {} with up to {} attempts, {}ms delay, {}ms timeout",
                requestId, attempts, delay, timeout);

        if (downloadInitialDelayMs > 0) {
            log.debug("Waiting {}ms before first download attempt for request {}", downloadInitialDelayMs, requestId);
            sleep(downloadInitialDelayMs);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "HouseKeepingService/1.0");
        headers.set("Accept", "audio/mpeg,audio/*;q=0.9,*/*;q=0.8");
        headers.setCacheControl("no-cache");
        if (StringUtils.hasText(apiKey)) {
            headers.set("api-key", apiKey);
        }
        if (StringUtils.hasText(defaultVoice)) {
            headers.set("voice", defaultVoice);
        }
        if (StringUtils.hasText(defaultSpeed)) {
            headers.set("speed", defaultSpeed);
        }

        int executedAttempts = 0;
        boolean timedOut = false;

        for (int i = 0; i < attempts; i++) {
            long elapsedBeforeAttempt = System.currentTimeMillis() - start;
            if (timeout > 0 && elapsedBeforeAttempt >= timeout) {
                timedOut = true;
                break;
            }

            String attemptUrl = appendCacheBuster(asyncUrl);
            log.debug("Attempt {}/{} to download FPT audio from: {}", i + 1, attempts, attemptUrl);

            try {
                ResponseEntity<byte[]> response = restTemplate.exchange(
                        attemptUrl,
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        byte[].class
                );
                executedAttempts = i + 1;
                if (response.getStatusCode().is2xxSuccessful()
                        && response.getBody() != null
                        && response.getBody().length > 0) {
                    MediaType contentType = response.getHeaders().getContentType();
                    String resolvedContentType = contentType != null ? contentType.toString() : "audio/mpeg";
                    log.info("Successfully downloaded FPT audio for request {} on attempt {}/{}, size: {} bytes",
                            requestId, executedAttempts, attempts, response.getBody().length);
                    return new DownloadedAudio(response.getBody(), resolvedContentType);
                } else {
                    log.debug("Attempt {}/{} returned status {} but no valid audio data",
                            executedAttempts, attempts, response.getStatusCode());
                }
            } catch (HttpStatusCodeException ex) {
                executedAttempts = i + 1;
                if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                    log.debug("Attempt {}/{} to download FPT audio for {} returned 404 (audio not ready yet)",
                            executedAttempts, attempts, requestId);
                } else {
                    log.warn("Attempt {}/{} to download FPT audio for {} returned {} {}",
                            executedAttempts, attempts, requestId, ex.getStatusCode(), ex.getStatusText());
                }
            } catch (Exception ex) {
                executedAttempts = i + 1;
                log.warn("Attempt {}/{} to download FPT audio for {} failed: {}",
                        executedAttempts, attempts, requestId, ex.getMessage());
            }

            long elapsedAfterAttempt = System.currentTimeMillis() - start;
            if (timeout > 0 && elapsedAfterAttempt >= timeout) {
                timedOut = true;
                break;
            }

            if (i == attempts - 1) {
                break;
            }

            long effectiveDelay = delay;
            if (timeout > 0) {
                long remaining = timeout - elapsedAfterAttempt;
                if (remaining <= 0) {
                    timedOut = true;
                    break;
                }
                effectiveDelay = Math.min(effectiveDelay, remaining);
            }
            sleep(effectiveDelay);
        }

        long waited = System.currentTimeMillis() - start;
        String timeoutSuffix = timedOut ? " (timed out waiting for provider)" : "";
        log.warn("Failed to download FPT audio after {} attempts (waited {}ms{}) for {}. "
                        + "Consider increasing FPT_TTS_VERIFY_ATTEMPTS or FPT_TTS_VERIFY_TIMEOUT if this keeps happening.",
                Math.max(executedAttempts, 0), waited, timeoutSuffix, requestId);
        return null;
    }

    private String appendCacheBuster(String url) {
        if (!StringUtils.hasText(url)) {
            return url;
        }
        // Do not mutate signed URLs that already contain query parameters
        if (url.contains("?")) {
            return url;
        }
        return url + "?ts=" + System.currentTimeMillis();
    }

    private record DownloadedAudio(byte[] data, String contentType) {
    }
    
    private void sleep(long delayMs) {
        long safeDelay = Math.max(delayMs, 0L);
        if (safeDelay <= 0) {
            return;
        }
        try {
            Thread.sleep(safeDelay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String buildSpeechBody(VoiceBookingResponse response) {
        List<String> segments = new ArrayList<>();
        segments.add(describeStatus(response));

        if (StringUtils.hasText(response.clarificationMessage())) {
            segments.add(response.clarificationMessage());
        }

        if (!CollectionUtils.isEmpty(response.missingFields())) {
            segments.add("Thông tin còn thiếu: " + response.missingFields().stream()
                    .map(this::translateField)
                    .collect(Collectors.joining(", ")));
        }

        if (StringUtils.hasText(response.bookingId())) {
            segments.add("Mã đơn của bạn là " + response.bookingId() + ".");
        }

        VoiceBookingPreview preview = response.preview();
        if (preview != null) {
            segments.addAll(buildPreviewSegments(preview));
        }

        if ("FAILED".equalsIgnoreCase(response.status()) && StringUtils.hasText(response.errorDetails())) {
            segments.add("Chi tiết lỗi: " + response.errorDetails());
        }

        String combined = segments.stream()
                .filter(StringUtils::hasText)
                .map(this::sanitizeSentence)
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(". "));

        if (!StringUtils.hasText(combined) && StringUtils.hasText(response.message())) {
            combined = sanitizeSentence(response.message());
        }

        return combined;
    }

    private List<String> buildPreviewSegments(VoiceBookingPreview preview) {
        List<String> previewSegments = new ArrayList<>();

        if (StringUtils.hasText(preview.fullAddress())) {
            previewSegments.add("Địa chỉ " + preview.fullAddress());
        }
        if (preview.bookingTime() != null) {
            previewSegments.add("Thời gian dự kiến " + formatBookingTime(preview.bookingTime()));
        }
        if (StringUtils.hasText(preview.note())) {
            previewSegments.add("Ghi chú " + preview.note());
        }
        if (StringUtils.hasText(preview.promoCode())) {
            previewSegments.add("Áp dụng mã khuyến mãi " + preview.promoCode());
        }
        if (preview.paymentMethodId() != null) {
            previewSegments.add("Phương thức thanh toán " + preview.paymentMethodId());
        }
        String amountDescription = formatCurrency(preview);
        if (StringUtils.hasText(amountDescription)) {
            previewSegments.add(amountDescription);
        }

        return previewSegments;
    }

    private String formatCurrency(VoiceBookingPreview preview) {
        BigDecimal totalAmount = preview.totalAmount();
        if (totalAmount == null) {
            return null;
        }
        NumberFormat formatter = NumberFormat.getInstance(VI_LOCALE);
        formatter.setMaximumFractionDigits(0);
        formatter.setMinimumFractionDigits(0);
        formatter.setGroupingUsed(true);
        String formatted = formatter.format(totalAmount.setScale(0, RoundingMode.HALF_UP));
        return "Tổng chi phí dự kiến " + formatted + " đồng";
    }

    private String describeStatus(VoiceBookingResponse response) {
        if (response == null || !StringUtils.hasText(response.status())) {
            return response != null ? response.message() : null;
        }
        return switch (response.status()) {
            case "COMPLETED" -> "Đơn đặt dịch vụ đã hoàn tất thành công";
            case "AWAITING_CONFIRMATION" -> "Đã dựng đơn nháp, vui lòng xác nhận để hoàn tất";
            case "PARTIAL" -> "Xin lỗi, tôi cần thêm thông tin để hoàn thành yêu cầu";
            case "FAILED" -> "Không thể xử lý yêu cầu đặt lịch";
            case "CANCELLED" -> "Đơn đặt dịch vụ bằng giọng nói đã bị hủy";
            default -> response.message();
        };
    }

    private String sanitizeSentence(String input) {
        if (!StringUtils.hasText(input)) {
            return null;
        }
        String sanitized = input
                .replace("•", ", ")
                .replaceAll("[\\r\\n]+", " ")
                .replaceAll("\\s+,", ",")
                .replaceAll("\\s+", " ")
                .trim();
        if (sanitized.endsWith(",")) {
            sanitized = sanitized.substring(0, sanitized.length() - 1);
        }
        return sanitized;
    }

    private String translateField(String field) {
        if (!StringUtils.hasText(field)) {
            return field;
        }
        return FIELD_LABELS.getOrDefault(field, field);
    }

    private String formatBookingTime(LocalDateTime bookingTime) {
        return bookingTime.format(TIME_FORMATTER);
    }

    private static Map<String, String> buildFieldLabels() {
        Map<String, String> labels = new LinkedHashMap<>();
        labels.put("service", "dịch vụ");
        labels.put("services", "dịch vụ");
        labels.put("bookingTime", "thời gian");
        labels.put("address", "địa chỉ");
        labels.put("paymentMethod", "phương thức thanh toán");
        labels.put("employees", "nhân viên");
        return labels;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record FptTextToSpeechApiResponse(
            @JsonProperty("async") String asyncUrl,
            @JsonProperty("message") String message,
            @JsonProperty("error") Integer errorCode,
            @JsonProperty("request_id") String providerRequestId
    ) {
    }
}
