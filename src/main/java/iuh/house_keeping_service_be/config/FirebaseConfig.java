package iuh.house_keeping_service_be.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

/**
 * Firebase Configuration for Phone OTP Authentication
 * 
 * Hỗ trợ 3 cách cấu hình Firebase credentials:
 * 1. FIREBASE_SERVICE_ACCOUNT_BASE64: Base64 encoded JSON (khuyến nghị cho Docker/Cloud)
 * 2. FIREBASE_SERVICE_ACCOUNT_JSON: JSON string trực tiếp
 * 3. FIREBASE_SERVICE_ACCOUNT_PATH: Đường dẫn file JSON
 */
@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.enabled:false}")
    private boolean enabled;

    @Value("${firebase.project-id:}")
    private String projectId;

    @Value("${firebase.service-account.path:}")
    private String serviceAccountPath;

    @Value("${firebase.service-account.base64:}")
    private String serviceAccountBase64;

    @Value("${firebase.service-account.json:}")
    private String serviceAccountJson;

    private boolean initialized = false;

    @PostConstruct
    public void initialize() {
        if (!enabled) {
            log.info("Firebase is disabled. Skipping initialization.");
            return;
        }

        if (FirebaseApp.getApps().isEmpty()) {
            try {
                GoogleCredentials credentials = getCredentials();
                
                if (credentials == null) {
                    log.warn("Firebase credentials not configured. Firebase features will be disabled.");
                    return;
                }

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(credentials)
                        .setProjectId(projectId)
                        .build();

                FirebaseApp.initializeApp(options);
                initialized = true;
                log.info("Firebase initialized successfully for project: {}", projectId);
                
            } catch (Exception e) {
                log.error("Failed to initialize Firebase: {}", e.getMessage(), e);
            }
        } else {
            initialized = true;
            log.info("Firebase already initialized.");
        }
    }

    private GoogleCredentials getCredentials() throws IOException {
        // Option 1: Base64 encoded JSON (recommended for Docker/Cloud)
        if (StringUtils.hasText(serviceAccountBase64)) {
            log.info("Loading Firebase credentials from Base64 encoded string");
            byte[] decoded = Base64.getDecoder().decode(serviceAccountBase64);
            return GoogleCredentials.fromStream(new ByteArrayInputStream(decoded));
        }

        // Option 2: JSON string directly
        if (StringUtils.hasText(serviceAccountJson)) {
            log.info("Loading Firebase credentials from JSON string");
            return GoogleCredentials.fromStream(
                    new ByteArrayInputStream(serviceAccountJson.getBytes())
            );
        }

        // Option 3: File path
        if (StringUtils.hasText(serviceAccountPath)) {
            log.info("Loading Firebase credentials from file: {}", serviceAccountPath);
            return GoogleCredentials.fromStream(new FileInputStream(serviceAccountPath));
        }

        return null;
    }

    @Bean
    public FirebaseAuth firebaseAuth() {
        if (!initialized || FirebaseApp.getApps().isEmpty()) {
            log.warn("Firebase not initialized. FirebaseAuth bean will be null.");
            return null;
        }
        return FirebaseAuth.getInstance();
    }

    public boolean isInitialized() {
        return initialized;
    }
}
