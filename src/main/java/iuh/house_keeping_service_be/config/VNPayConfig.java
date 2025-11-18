package iuh.house_keeping_service_be.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "vnpay")
@Getter
@Setter
public class VNPayConfig {
    
    /**
     * URL của VNPay Payment Gateway
     */
    private String payUrl;
    
    /**
     * URL return sau khi thanh toán thành công/thất bại
     */
    private String returnUrl;
    
    /**
     * Mã website của merchant (do VNPay cung cấp)
     */
    private String tmnCode;
    
    /**
     * Secret key để hash data (do VNPay cung cấp)
     */
    private String secretKey;
    
    /**
     * API URL để query transaction status
     */
    private String apiUrl;
    
    /**
     * Version của VNPay API
     */
    private String version = "2.1.0";
    
    /**
     * Command code
     */
    private String command = "pay";
    
    /**
     * Order type
     */
    private String orderType = "other";

    /**
     * Frontend (web) URL to redirect user after payment
     */
    private String frontendRedirectUrl;

    /**
     * Mobile deep link / URL to redirect user after payment
     */
    private String mobileRedirectUrl;
}
