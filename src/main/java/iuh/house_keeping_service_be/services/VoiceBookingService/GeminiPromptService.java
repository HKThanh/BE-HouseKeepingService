package iuh.house_keeping_service_be.services.VoiceBookingService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiPromptService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${gemini.enabled:false}")
    private boolean enabled;

    @Value("${gemini.api-key:}")
    private String apiKey;

    @Value("${gemini.model:gemini-1.5-flash}")
    private String model;

    @Value("${gemini.prompt-prefix:Bạn là trợ lý đặt lịch dịch vụ giúp việc. Hãy trả lời ngắn gọn, thân thiện dựa trên nội dung:}")
    private String promptPrefix;

    public Optional<String> generatePrompt(String transcript) {
        return generatePromptWithText(transcript, null);
    }

    public Optional<String> generatePromptWithInstruction(String baseText, String extraInstruction) {
        return generatePromptWithText(baseText, extraInstruction);
    }

    private Optional<String> generatePromptWithText(String body, String extraInstruction) {
        if (!enabled || !StringUtils.hasText(apiKey) || !StringUtils.hasText(body)) {
            return Optional.empty();
        }
        try {
            String url = String.format("https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s", model, apiKey);
            String payload;
            try {
                String promptBody = promptPrefix + "\n";
                if (StringUtils.hasText(extraInstruction)) {
                    promptBody += extraInstruction + "\n";
                }
                promptBody += body;
                payload = objectMapper.writeValueAsString(Map.of(
                        "contents", new Object[]{
                                Map.of("parts", new Object[]{
                                        Map.of("text", promptBody)
                                })
                        }
                ));
            } catch (Exception jsonEx) {
                log.warn("Gemini prompt serialization failed: {}", jsonEx.getMessage());
                return Optional.empty();
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(payload, headers);

            var response = restTemplate.postForEntity(url, entity, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.warn("Gemini prompt generation failed with status {}", response.getStatusCode());
                return Optional.empty();
            }
            String responseBody = response.getBody();
            if (!StringUtils.hasText(responseBody)) {
                return Optional.empty();
            }
            try {
                JsonNode root = objectMapper.readTree(responseBody);
                JsonNode textNode = root.at("/candidates/0/content/parts/0/text");
                if (textNode != null && textNode.isTextual()) {
                    return Optional.of(textNode.asText());
                }
            } catch (Exception parseEx) {
                log.warn("Gemini prompt parse failed: {}", parseEx.getMessage());
            }
        } catch (RestClientException ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("503")) {
                log.warn("Gemini prompt overloaded (503), skipping prompt this round");
            } else {
                log.warn("Gemini prompt generation failed: {}", ex.getMessage());
            }
        }
        return Optional.empty();
    }
}
