package iuh.house_keeping_service_be.services.VoiceBookingService;

import com.cloudinary.Cloudinary;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import iuh.house_keeping_service_be.dtos.VoiceBooking.TextToSpeechResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class FptTextToSpeechService implements TextToSpeechService {

    private final Cloudinary cloudinary;
    private final ObjectMapper objectMapper;

    @Value("${fpt.tts.enabled:false}")
    private boolean enabled;

    @Value("${fpt.tts.api-url:https://api.fpt.ai/hmi/tts/v5}")
    private String apiUrl;

    @Value("${fpt.tts.api-key:}")
    private String apiKey;

    @Value("${fpt.tts.voice:banmai}")
    private String voice;

    @Value("${fpt.tts.speed:0}")
    private int speed;

    @Value("${fpt.tts.text-limit:1500}")
    private int textLimit;

    @Value("${fpt.tts.verify-attempts:10}")
    private int verifyAttempts;

    @Value("${fpt.tts.verify-delay:1200}")
    private long verifyDelayMs;

    @Value("${fpt.tts.verify-timeout:45000}")
    private long verifyTimeoutMs;

    @Value("${fpt.tts.cloudinary-folder:voice_booking/tts}")
    private String cloudinaryFolder;

    @Override
    public Optional<TextToSpeechResult> synthesize(String text) {
        if (!isEnabled() || !StringUtils.hasText(text)) {
            return Optional.empty();
        }

        String sanitizedText = sanitizeText(text);
        RestTemplate restTemplate = createRestTemplate();
        long start = System.currentTimeMillis();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("api-key", apiKey);
            headers.set("voice", voice);
            headers.set("speed", String.valueOf(speed));
            headers.setContentType(new MediaType("text", "plain", StandardCharsets.UTF_8));
            headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON, MediaType.ALL));

            HttpEntity<String> requestEntity = new HttpEntity<>(sanitizedText, headers);
            ResponseEntity<byte[]> response = restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity, byte[].class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.warn("FPT TTS request failed with status {}", response.getStatusCode());
                return Optional.empty();
            }

            String body = new String(response.getBody(), StandardCharsets.UTF_8);
            JsonNode json = objectMapper.readTree(body);
            String asyncUrl = json.path("async").asText(null);
            if (!StringUtils.hasText(asyncUrl)) {
                log.warn("FPT TTS response missing async url: {}", response.getBody());
                return Optional.empty();
            }

            AudioPayload audio = fetchAudio(restTemplate, asyncUrl);
            String audioUrl = uploadAudio(audio).orElse(asyncUrl);

            long processingTime = System.currentTimeMillis() - start;

            return Optional.of(TextToSpeechResult.builder()
                    .text(sanitizedText)
                    .audioUrl(audioUrl)
                    .provider("fpt-ai")
                    .processingTimeMs(processingTime)
                    .build());
        } catch (Exception ex) {
            log.error("FPT TTS synthesis failed: {}", ex.getMessage(), ex);
            return Optional.empty();
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled && StringUtils.hasText(apiKey);
    }

    private String sanitizeText(String text) {
        String normalized = text
                .replaceAll("[\\r\\n]+", ". ") // tránh xuống dòng khiến TTS đọc lẻ từng kí tự
                .replace("•", " ")
                .replace("●", " ")
                .replace("✓", " ")
                .replaceAll("\\s+", " ")
                .trim();

        if (!normalized.endsWith(".") && !normalized.endsWith("!") && !normalized.endsWith("?")) {
            normalized = normalized + ".";
        }

        if (normalized.length() > textLimit) {
            log.info("Truncating TTS text from {} to {} characters", normalized.length(), textLimit);
            normalized = normalized.substring(0, textLimit);
        }
        return normalized;
    }

    private RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        int timeout = (int) Math.min(Integer.MAX_VALUE, verifyTimeoutMs + 5000);
        requestFactory.setConnectTimeout(timeout);
        requestFactory.setReadTimeout(timeout);

        RestTemplate template = new RestTemplate(requestFactory);
        template.getMessageConverters().forEach(converter -> {
            if (converter instanceof StringHttpMessageConverter stringConverter) {
                stringConverter.setDefaultCharset(StandardCharsets.UTF_8);
            }
        });
        boolean hasString = template.getMessageConverters().stream().anyMatch(StringHttpMessageConverter.class::isInstance);
        if (!hasString) {
            template.getMessageConverters().add(new StringHttpMessageConverter(StandardCharsets.UTF_8));
        }
        return template;
    }

    private AudioPayload fetchAudio(RestTemplate restTemplate, String asyncUrl) {
        long start = System.currentTimeMillis();

        for (int attempt = 1; attempt <= verifyAttempts; attempt++) {
            if (System.currentTimeMillis() - start > verifyTimeoutMs) {
                break;
            }

            try {
                ResponseEntity<byte[]> response = restTemplate.exchange(
                        asyncUrl,
                        HttpMethod.GET,
                        new HttpEntity<>(new HttpHeaders()),
                        byte[].class
                );

                if (response.getStatusCode().is2xxSuccessful()
                        && response.getBody() != null
                        && response.getBody().length > 0) {

                    MediaType contentType = response.getHeaders().getContentType();
                    String resolvedContentType = contentType != null ? contentType.toString() : "audio/mpeg";

                    return new AudioPayload(response.getBody(), resolvedContentType);
                }

                log.debug("FPT TTS async not ready (attempt {}): status {}", attempt, response.getStatusCode());
            } catch (Exception ex) {
                log.debug("Failed to fetch FPT TTS audio (attempt {}): {}", attempt, ex.getMessage());
            }

            try {
                Thread.sleep(verifyDelayMs);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return null;
            }
        }

        return null;
    }

    private Optional<String> uploadAudio(AudioPayload audio) {
        if (audio == null || cloudinary == null) {
            return Optional.empty();
        }

        try {
            Map<String, Object> options = new HashMap<>();
            options.put("resource_type", "auto");

            if (StringUtils.hasText(cloudinaryFolder)) {
                options.put("folder", cloudinaryFolder);
            }

            Map<?, ?> uploadResult = cloudinary.uploader().upload(audio.bytes(), options);
            String secureUrl = (String) uploadResult.get("secure_url");

            if (!StringUtils.hasText(secureUrl)) {
                log.warn("Cloudinary upload returned empty url for TTS audio");
                return Optional.empty();
            }

            return Optional.of(secureUrl);
        } catch (Exception ex) {
            log.error("Failed to upload TTS audio to Cloudinary: {}", ex.getMessage(), ex);
            return Optional.empty();
        }
    }

    private record AudioPayload(byte[] bytes, String contentType) {
    }
}
