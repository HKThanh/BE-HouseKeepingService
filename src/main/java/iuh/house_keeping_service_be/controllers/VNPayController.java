package iuh.house_keeping_service_be.controllers;

import iuh.house_keeping_service_be.config.VNPayConfig;
import iuh.house_keeping_service_be.dtos.payment.VNPayCallbackResponse;
import iuh.house_keeping_service_be.dtos.payment.VNPayPaymentRequest;
import iuh.house_keeping_service_be.dtos.payment.VNPayPaymentResponse;
import iuh.house_keeping_service_be.services.PaymentService.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payment/vnpay")
@RequiredArgsConstructor
@Slf4j
public class VNPayController {

    private final VNPayService vnPayService;
    private final VNPayConfig vnPayConfig;

    /**
     * Create VNPay payment URL
     * Endpoint: POST /api/v1/payment/vnpay/create
     */
    @PostMapping("/create")
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_ADMIN')")
    public ResponseEntity<?> createPayment(@Valid @RequestBody VNPayPaymentRequest request,
                                          HttpServletRequest httpRequest) {
        try {
            String ipAddress = vnPayService.getIpAddress(httpRequest);
            VNPayPaymentResponse response = vnPayService.createPayment(request, ipAddress);

            if ("00".equals(response.getCode())) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Tạo URL thanh toán thành công",
                        "data", Map.of(
                                "paymentUrl", response.getPaymentUrl()
                        )
                ));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                        "success", false,
                        "message", response.getMessage()
                ));
            }
        } catch (Exception e) {
            log.error("Error creating VNPay payment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Lỗi khi tạo thanh toán: " + e.getMessage()
            ));
        }
    }

    /**
     * Handle VNPay callback (return URL)
     * Endpoint: GET /api/v1/payment/vnpay/callback
     * This endpoint will be called by VNPay after payment
     */
    @GetMapping("/callback")
    public ResponseEntity<?> handleCallback(@RequestParam Map<String, String> params) {
        try {
            log.info("Received VNPay callback with params: {}", params.keySet());
            
            VNPayCallbackResponse response = vnPayService.handlePaymentCallback(params);

            if ("00".equals(response.getResponseCode())) {
                // Payment successful
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Thanh toán thành công",
                        "data", Map.of(
                                "transactionNo", response.getTransactionNo(),
                                "amount", response.getAmount(),
                                "bankCode", response.getBankCode(),
                                "cardType", response.getCardType(),
                                "orderInfo", response.getOrderInfo(),
                                "payDate", response.getPayDate()
                        )
                ));
            } else {
                // Payment failed
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "Thanh toán không thành công",
                        "data", Map.of(
                                "responseCode", response.getResponseCode(),
                                "orderInfo", response.getOrderInfo()
                        )
                ));
            }
        } catch (Exception e) {
            log.error("Error handling VNPay callback: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Lỗi xử lý callback: " + e.getMessage()
            ));
        }
    }

    /**
     * Handle VNPay IPN (Instant Payment Notification)
     * Endpoint: POST /api/v1/payment/vnpay/ipn
     * This endpoint will be called by VNPay server to notify payment status
     */
    @PostMapping("/ipn")
    public ResponseEntity<?> handleIPN(@RequestParam Map<String, String> params) {
        try {
            log.info("Received VNPay IPN with params: {}", params.keySet());
            
            VNPayCallbackResponse response = vnPayService.handlePaymentCallback(params);

            Map<String, String> result = new HashMap<>();
            if ("00".equals(response.getResponseCode())) {
                result.put("RspCode", "00");
                result.put("Message", "Confirm Success");
            } else {
                result.put("RspCode", "99");
                result.put("Message", "Unknown error");
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error handling VNPay IPN: {}", e.getMessage(), e);
            Map<String, String> result = new HashMap<>();
            result.put("RspCode", "99");
            result.put("Message", "System error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * Callback endpoint that redirects users (web/mobile) back to their app
     * Endpoint: GET /api/v1/payment/vnpay/callback/redirect?client=web|mobile
     */
    @GetMapping("/callback/redirect")
    public ResponseEntity<?> handleCallbackWithRedirect(
            @RequestParam Map<String, String> params,
            @RequestParam(name = "client", defaultValue = "web") String client) {
        try {
            log.info("Received VNPay redirect callback for client {} with params {}", client, params.keySet());
            VNPayCallbackResponse response = vnPayService.handlePaymentCallback(params);
            URI redirectUri = buildClientRedirectUri(client, response);
            return ResponseEntity.status(HttpStatus.FOUND).location(redirectUri).build();
        } catch (Exception e) {
            log.error("Error handling VNPay redirect callback: {}", e.getMessage(), e);
            URI fallback = URI.create(resolveRedirectBase(client));
            return ResponseEntity.status(HttpStatus.FOUND).location(fallback).build();
        }
    }

    /**
     * Get payment status
     * Endpoint: GET /api/v1/payment/vnpay/status/{bookingId}
     */
    @GetMapping("/status/{bookingId}")
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_ADMIN')")
    public ResponseEntity<?> getPaymentStatus(@PathVariable String bookingId) {
        try {
            // TODO: Implement payment status query
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Get payment status successfully",
                    "data", Map.of(
                            "bookingId", bookingId,
                            "status", "PENDING"
                    )
            ));
        } catch (Exception e) {
            log.error("Error getting payment status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Lỗi khi lấy trạng thái thanh toán: " + e.getMessage()
            ));
        }
    }

    private URI buildClientRedirectUri(String client, VNPayCallbackResponse response) {
        String base = resolveRedirectBase(client);
        boolean success = response != null && "00".equals(response.getResponseCode());

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(base)
                .queryParam("status", success ? "success" : "failed");

        if (response != null) {
            addQueryParam(builder, "responseCode", response.getResponseCode());
            addQueryParam(builder, "orderInfo", response.getOrderInfo());
            addQueryParam(builder, "transactionNo", response.getTransactionNo());
            addQueryParam(builder, "amount", response.getAmount());
            addQueryParam(builder, "bankCode", response.getBankCode());
            addQueryParam(builder, "cardType", response.getCardType());
        }

        return builder.build()
                .encode()
                .toUri();
    }

    private void addQueryParam(UriComponentsBuilder builder, String key, Object value) {
        if (value != null) {
            builder.queryParam(key, value);
        }
    }

    private String resolveRedirectBase(String client) {
        String base;
        if ("mobile".equalsIgnoreCase(client) || "app".equalsIgnoreCase(client)) {
            base = vnPayConfig.getMobileRedirectUrl();
        } else {
            base = vnPayConfig.getFrontendRedirectUrl();
        }
        if (base == null || base.isBlank()) {
            base = "/";
        }
        return base;
    }
}
