package iuh.house_keeping_service_be.services.VoiceBookingService;

import com.theokanning.openai.audio.CreateTranscriptionRequest;
import com.theokanning.openai.service.OpenAiService;
import iuh.house_keeping_service_be.dtos.VoiceBooking.VoiceToTextResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.*;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * OpenAI Whisper implementation of VoiceToTextService
 * Provides voice-to-text conversion using OpenAI's Whisper API
 */
@Service
@Slf4j
public class WhisperVoiceToTextService implements VoiceToTextService {

    @Value("${whisper.enabled:true}")
    private boolean enabled;

    @Value("${whisper.api-key:}")
    private String apiKey;

    @Value("${whisper.model:whisper-1}")
    private String model;

    @Value("${whisper.timeout-seconds:30}")
    private int timeoutSeconds;

    @Value("${whisper.max-retries:2}")
    private int maxRetries;

    @Value("${whisper.audio.max-size-mb:5}")
    private int maxSizeMb;

    @Value("${whisper.audio.max-duration-seconds:120}")
    private int maxDurationSeconds;

    private OpenAiService openAiService;

    @Override
    public VoiceToTextResult transcribe(MultipartFile audioFile, String language) throws IOException {
        if (!enabled) {
            throw new IllegalStateException("Whisper service is disabled");
        }

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Whisper API key is not configured");
        }

        validateAudioFile(audioFile);

        long startTime = System.currentTimeMillis();
        
        try {
            // Initialize OpenAI service lazily
            if (openAiService == null) {
                openAiService = new OpenAiService(apiKey, Duration.ofSeconds(timeoutSeconds));
            }

            // Convert MultipartFile to File for OpenAI API
            File tempFile = convertMultipartFileToFile(audioFile);
            
            try {
                // Create transcription request
                CreateTranscriptionRequest request = CreateTranscriptionRequest.builder()
                        .model(model)
                        .language(language != null ? language : "vi") // Default to Vietnamese
                        .build();

                // Call Whisper API with retry logic
                String transcript = transcribeWithRetry(tempFile, request);
                
                long processingTime = System.currentTimeMillis() - startTime;

                log.info("Whisper transcription completed in {}ms, transcript length: {}", 
                        processingTime, transcript.length());

                // Build metadata
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("model", model);
                metadata.put("language", language != null ? language : "vi");
                metadata.put("audioFileName", audioFile.getOriginalFilename());
                metadata.put("audioSize", audioFile.getSize());

                return VoiceToTextResult.builder()
                        .transcript(transcript)
                        .confidenceScore(null) // Whisper API doesn't provide confidence
                        .processingTimeMs(processingTime)
                        .language(language != null ? language : "vi")
                        .metadata(metadata)
                        .build();

            } finally {
                // Clean up temp file
                if (tempFile.exists()) {
                    tempFile.delete();
                }
            }

        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            log.error("Whisper transcription failed after {}ms: {}", processingTime, e.getMessage(), e);
            throw new IOException("Voice-to-text conversion failed: " + e.getMessage(), e);
        }
    }

    /**
     * Transcribe with retry logic
     */
    private String transcribeWithRetry(File audioFile, CreateTranscriptionRequest request) throws IOException {
        int attempts = 0;
        Exception lastException = null;

        while (attempts < maxRetries) {
            try {
                return openAiService.createTranscription(request, audioFile).getText();
            } catch (Exception e) {
                attempts++;
                lastException = e;
                log.warn("Whisper API call failed (attempt {}/{}): {}", attempts, maxRetries, e.getMessage());
                
                if (attempts < maxRetries) {
                    try {
                        // Exponential backoff
                        Thread.sleep(1000L * attempts);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Transcription interrupted", ie);
                    }
                }
            }
        }

        throw new IOException("Whisper API failed after " + maxRetries + " attempts", lastException);
    }

    @Override
    public boolean isEnabled() {
        return enabled && apiKey != null && !apiKey.isBlank();
    }

    @Override
    public void validateAudioFile(MultipartFile audioFile) {
        if (audioFile == null || audioFile.isEmpty()) {
            throw new IllegalArgumentException("Audio file cannot be empty");
        }

        // Check file size
        long maxSizeBytes = maxSizeMb * 1024L * 1024L;
        if (audioFile.getSize() > maxSizeBytes) {
            throw new IllegalArgumentException(
                    String.format("Audio file size exceeds %dMB limit", maxSizeMb));
        }

        // Check content type
        String contentType = audioFile.getContentType();
        if (contentType != null && !isValidAudioType(contentType)) {
            throw new IllegalArgumentException(
                    "Invalid audio format. Supported formats: mp3, wav, m4a, webm, ogg, flac");
        }

        // Check duration if possible
        try {
            validateAudioDuration(audioFile);
        } catch (Exception e) {
            log.warn("Could not validate audio duration: {}", e.getMessage());
            // Don't fail if we can't check duration
        }
    }

    /**
     * Check if content type is valid audio format
     */
    private boolean isValidAudioType(String contentType) {
        return contentType.startsWith("audio/") || 
               contentType.equals("application/octet-stream");
    }

    /**
     * Validate audio duration
     */
    private void validateAudioDuration(MultipartFile audioFile) throws Exception {
        try (InputStream inputStream = audioFile.getInputStream();
             AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(inputStream)) {
            
            long frames = audioInputStream.getFrameLength();
            float frameRate = audioInputStream.getFormat().getFrameRate();
            
            if (frames > 0 && frameRate > 0) {
                float durationSeconds = frames / frameRate;
                
                if (durationSeconds > maxDurationSeconds) {
                    throw new IllegalArgumentException(
                            String.format("Audio duration exceeds %d seconds limit", maxDurationSeconds));
                }
                
                log.debug("Audio duration: {:.2f} seconds", durationSeconds);
            }
        }
    }

    /**
     * Convert MultipartFile to temporary File
     */
    private File convertMultipartFileToFile(MultipartFile multipartFile) throws IOException {
        String originalFilename = multipartFile.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".tmp";

        File tempFile = File.createTempFile("voice_booking_", extension);
        
        try (InputStream inputStream = multipartFile.getInputStream();
             FileOutputStream outputStream = new FileOutputStream(tempFile)) {
            
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }

        return tempFile;
    }
}
