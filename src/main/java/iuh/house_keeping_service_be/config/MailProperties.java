package iuh.house_keeping_service_be.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "mail")
public class MailProperties {

    /**
     * Toggle to disable mail without touching code.
     */
    private boolean enabled = true;

    /**
     * Address that appears in the "from" header.
     */
    private String fromAddress = "no-reply@housekeeping.local";

    /**
     * Friendly name for the sender.
     */
    private String fromName = "House Keeping Service";

    /**
     * Frontend base URL for deep links inside emails.
     */
    private String baseUrl = "http://localhost:3000";
}
