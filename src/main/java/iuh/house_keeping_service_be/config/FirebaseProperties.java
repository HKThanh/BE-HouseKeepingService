package iuh.house_keeping_service_be.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "firebase")
public class FirebaseProperties {

    /**
     * Toggle whether Firebase integration is active.
     */
    private boolean enabled = false;

    /**
     * Firebase project identifier used for logging and telemetry.
     */
    private String projectId;

    /**
     * Web API key used for Identity Toolkit REST calls.
     */
    private String apiKey;

    /**
     * Service account credentials used to initialise the Admin SDK.
     */
    private final ServiceAccount serviceAccount = new ServiceAccount();

    /**
     * Configuration block for phone-based OTP flow.
     */
    private final PhoneAuth phoneAuth = new PhoneAuth();

    /**
     * Helper that resolves the configured service account into an input stream.
     */
    public InputStream getServiceAccountInputStream() throws IOException {
        if (serviceAccount == null) {
            return null;
        }

        if (StringUtils.hasText(serviceAccount.getFilePath())) {
            return new FileInputStream(serviceAccount.getFilePath());
        }

        if (StringUtils.hasText(serviceAccount.getBase64())) {
            byte[] decoded = Base64.getDecoder().decode(
                serviceAccount.getBase64().trim().getBytes(StandardCharsets.UTF_8)
            );
            return new ByteArrayInputStream(decoded);
        }

        if (StringUtils.hasText(serviceAccount.getInlineJson())) {
            return new ByteArrayInputStream(serviceAccount.getInlineJson().getBytes(StandardCharsets.UTF_8));
        }

        return null;
    }

    @Getter
    @Setter
    public static class ServiceAccount {
        /**
         * Absolute path that points to the service account JSON file.
         */
        private String filePath;

        /**
         * Base64 encoded JSON string (useful for Docker secrets).
         */
        private String base64;

        /**
         * Direct JSON content (only recommended for local development).
         */
        private String inlineJson;
    }

    @Getter
    @Setter
    public static class PhoneAuth {
        /**
         * Whether the phone auth endpoints should be available.
         */
        private boolean enabled = false;

        /**
         * Default OTP length, helps validate incoming codes.
         */
        private int otpLength = 6;

        /**
         * Expected validity window in seconds for Firebase session info.
         */
        private long sessionTtlSeconds = 120;
    }
}

