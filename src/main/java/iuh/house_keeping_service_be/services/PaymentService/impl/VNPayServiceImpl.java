package iuh.house_keeping_service_be.services.PaymentService.impl;

import iuh.house_keeping_service_be.config.VNPayConfig;
import iuh.house_keeping_service_be.dtos.payment.VNPayCallbackResponse;
import iuh.house_keeping_service_be.dtos.payment.VNPayPaymentRequest;
import iuh.house_keeping_service_be.dtos.payment.VNPayPaymentResponse;
import iuh.house_keeping_service_be.enums.PaymentStatus;
import iuh.house_keeping_service_be.models.Booking;
import iuh.house_keeping_service_be.models.Payment;
import iuh.house_keeping_service_be.repositories.BookingRepository;
import iuh.house_keeping_service_be.repositories.PaymentRepository;
import iuh.house_keeping_service_be.services.PaymentService.VNPayService;
import iuh.house_keeping_service_be.utils.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class VNPayServiceImpl implements VNPayService {

    private static final int VNP_TXN_REF_MAX_LENGTH = 20;
    private static final DateTimeFormatter TXN_REF_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyMMddHHmmss");

    private final VNPayConfig vnPayConfig;
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

    @Override
    public VNPayPaymentResponse createPayment(VNPayPaymentRequest request, String ipAddress) {
        try {
            // Validate booking exists
            Booking booking = bookingRepository.findById(request.getBookingId())
                    .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

            // Create or get payment record
            Payment payment = paymentRepository.findFirstByBooking_BookingIdOrderByCreatedAtDesc(request.getBookingId())
                    .orElseGet(() -> {
                        Payment newPayment = new Payment();
                        newPayment.setBooking(booking);
                        newPayment.setAmount(BigDecimal.valueOf(request.getAmount()));
                        newPayment.setPaymentStatus(PaymentStatus.PENDING);
                        return paymentRepository.save(newPayment);
                    });

            // Generate transaction reference that satisfies VNPay constraints
            String vnpTxnRef = generateTransactionReference(payment.getId());
            payment.setTransactionCode(vnpTxnRef);
            paymentRepository.save(payment);
            
            // Amount must be in VND (smallest unit - no decimals)
            long amount = request.getAmount() * 100; // Convert to VNPay format

            // Build payment parameters
            Map<String, String> vnpParams = new TreeMap<>();
            vnpParams.put("vnp_Version", vnPayConfig.getVersion());
            vnpParams.put("vnp_Command", vnPayConfig.getCommand());
            vnpParams.put("vnp_TmnCode", vnPayConfig.getTmnCode());
            vnpParams.put("vnp_Amount", String.valueOf(amount));
            vnpParams.put("vnp_CurrCode", "VND");
            
            if (request.getBankCode() != null && !request.getBankCode().isEmpty()) {
                vnpParams.put("vnp_BankCode", request.getBankCode());
            }
            
            vnpParams.put("vnp_TxnRef", vnpTxnRef);
            // Always send pure booking_id to make reconciliation deterministic regardless of client payload
            vnpParams.put("vnp_OrderInfo", booking.getBookingId());
            vnpParams.put("vnp_OrderType", request.getOrderType() != null ? 
                    request.getOrderType() : vnPayConfig.getOrderType());
            
            String locale = request.getLocale() != null ? request.getLocale() : "vn";
            vnpParams.put("vnp_Locale", locale);
            
            vnpParams.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
            vnpParams.put("vnp_IpAddr", ipAddress);

            // Create date format
            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String vnpCreateDate = formatter.format(cld.getTime());
            vnpParams.put("vnp_CreateDate", vnpCreateDate);
            
            cld.add(Calendar.MINUTE, 15); // Payment expires in 15 minutes
            String vnpExpireDate = formatter.format(cld.getTime());
            vnpParams.put("vnp_ExpireDate", vnpExpireDate);

            // Build query string and hash
            String queryUrl = VNPayUtil.getPaymentURL(vnpParams, true);
            String vnpSecureHash = VNPayUtil.hmacSHA512(vnPayConfig.getSecretKey(), queryUrl);
            queryUrl += "&vnp_SecureHash=" + vnpSecureHash;
            String paymentUrl = vnPayConfig.getPayUrl() + "?" + queryUrl;

            log.info("VNPay payment URL created for booking: {}, txnRef: {}", request.getBookingId(), vnpTxnRef);
            log.debug("VNPay params: {}", vnpParams);
            log.debug("VNPay queryUrl before hash: {}", queryUrl.split("&vnp_SecureHash=")[0]);
            log.debug("VNPay full URL: {}", paymentUrl);

            return new VNPayPaymentResponse("00", "success", paymentUrl);

        } catch (Exception e) {
            log.error("Error creating VNPay payment URL: {}", e.getMessage(), e);
            return new VNPayPaymentResponse("99", "Error: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public VNPayCallbackResponse handlePaymentCallback(Map<String, String> params) {
        try {
            // Validate signature
            if (!validateSignature(params)) {
                log.warn("Invalid VNPay signature");
                return new VNPayCallbackResponse("97", null, null, null, null, null, "97", null);
            }

            // VNPay returns vnp_TransactionStatus in callback, not vnp_ResponseCode
            String vnpResponseCode = params.get("vnp_ResponseCode");
            String vnpTransactionStatus = params.get("vnp_TransactionStatus");
            String vnpTransactionNo = params.get("vnp_TransactionNo");
            String vnpBankCode = params.get("vnp_BankCode");
            String vnpCardType = params.get("vnp_CardType");
            String vnpOrderInfo = params.get("vnp_OrderInfo");
            String vnpPayDate = params.get("vnp_PayDate");
            String vnpTxnRef = params.get("vnp_TxnRef");
            String vnpAmountStr = params.get("vnp_Amount");

            // Use TransactionStatus if ResponseCode is not available
            String statusCode = vnpResponseCode != null ? vnpResponseCode : vnpTransactionStatus;

            Long vnpAmount = null;
            if (vnpAmountStr != null) {
                vnpAmount = Long.parseLong(vnpAmountStr) / 100; // Convert back from VNPay format
            }

            Payment payment = paymentRepository.findByTransactionCode(vnpTxnRef)
                    .orElseThrow(() -> new IllegalArgumentException("Payment not found for transaction reference: " + vnpTxnRef));

            // Update payment status based on response code
            if ("00".equals(statusCode)) {
                payment.setPaymentStatus(PaymentStatus.PAID);
                
                // Parse payment date
                if (vnpPayDate != null && !vnpPayDate.isEmpty()) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                    payment.setPaidAt(LocalDateTime.parse(vnpPayDate, formatter));
                } else {
                    payment.setPaidAt(LocalDateTime.now());
                }
                
                log.info("Payment successful for booking: {}, txnNo: {}", 
                        payment.getBooking().getBookingId(), vnpTransactionNo);
            } else {
                payment.setPaymentStatus(PaymentStatus.FAILED);
                log.warn("Payment failed for booking: {}, statusCode: {}", 
                        payment.getBooking().getBookingId(), statusCode);
            }

            paymentRepository.save(payment);

            return new VNPayCallbackResponse(
                    statusCode,
                    vnpTransactionNo,
                    vnpBankCode,
                    vnpCardType,
                    vnpOrderInfo,
                    vnpPayDate,
                    "00".equals(statusCode) ? "00" : "01",
                    vnpAmount
            );

        } catch (Exception e) {
            log.error("Error handling VNPay callback: {}", e.getMessage(), e);
            return new VNPayCallbackResponse("99", null, null, null, null, null, "99", null);
        }
    }

    @Override
    public String getIpAddress(HttpServletRequest request) {
        String ipAddress;
        try {
            ipAddress = request.getHeader("X-FORWARDED-FOR");
            if (ipAddress == null || ipAddress.isEmpty()) {
                ipAddress = request.getRemoteAddr();
            }
        } catch (Exception e) {
            ipAddress = "Invalid IP:" + e.getMessage();
        }
        return ipAddress;
    }

    @Override
    public boolean validateSignature(Map<String, String> params) {
        try {
            String vnpSecureHash = params.get("vnp_SecureHash");
            if (vnpSecureHash == null) {
                return false;
            }

            // Remove hash params
            Map<String, String> fields = new TreeMap<>(params);
            fields.entrySet().removeIf(entry -> entry.getKey() == null || !entry.getKey().startsWith("vnp_"));
            fields.remove("vnp_SecureHashType");
            fields.remove("vnp_SecureHash");

            // Build hash data
            String signValue = VNPayUtil.getPaymentURL(fields, true);
            String checkSum = VNPayUtil.hmacSHA512(vnPayConfig.getSecretKey(), signValue);

            return checkSum.equals(vnpSecureHash);
        } catch (Exception e) {
            log.error("Error validating VNPay signature: {}", e.getMessage());
            return false;
        }
    }

    private String generateTransactionReference(String paymentId) {
        for (int attempt = 0; attempt < 5; attempt++) {
            String candidate = buildTransactionReferenceCandidate(paymentId);
            if (!paymentRepository.existsByTransactionCode(candidate)) {
                return candidate;
            }
        }
        return buildTransactionReferenceCandidate(UUID.randomUUID().toString());
    }

    private String buildTransactionReferenceCandidate(String seed) {
        String sanitized = seed == null ? "" : seed.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        if (sanitized.isEmpty()) {
            sanitized = VNPayUtil.getRandomNumber(8);
        }
        String raw = sanitized + TXN_REF_TIME_FORMATTER.format(LocalDateTime.now()) + VNPayUtil.getRandomNumber(6);
        if (raw.length() > VNP_TXN_REF_MAX_LENGTH) {
            return raw.substring(raw.length() - VNP_TXN_REF_MAX_LENGTH);
        }
        return raw;
    }
}
