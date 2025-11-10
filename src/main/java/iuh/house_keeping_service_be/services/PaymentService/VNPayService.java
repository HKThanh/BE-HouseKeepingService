package iuh.house_keeping_service_be.services.PaymentService;

import iuh.house_keeping_service_be.dtos.payment.VNPayCallbackResponse;
import iuh.house_keeping_service_be.dtos.payment.VNPayPaymentRequest;
import iuh.house_keeping_service_be.dtos.payment.VNPayPaymentResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

/**
 * Service interface for VNPay payment integration
 */
public interface VNPayService {
    
    /**
     * Create payment URL for VNPay
     * @param request Payment request details
     * @param ipAddress Client IP address
     * @return Payment response with URL to redirect
     */
    VNPayPaymentResponse createPayment(VNPayPaymentRequest request, String ipAddress);
    
    /**
     * Handle payment callback from VNPay
     * @param params Query parameters from VNPay callback
     * @return Callback response with transaction details
     */
    VNPayCallbackResponse handlePaymentCallback(Map<String, String> params);
    
    /**
     * Get client IP address from request
     * @param request HTTP request
     * @return Client IP address
     */
    String getIpAddress(HttpServletRequest request);
    
    /**
     * Validate VNPay signature
     * @param params Query parameters from VNPay
     * @return true if signature is valid, false otherwise
     */
    boolean validateSignature(Map<String, String> params);
}
