package iuh.house_keeping_service_be.controllers; // Hoặc package tương ứng

import iuh.house_keeping_service_be.dtos.payment.CreatePaymentRequest;
import iuh.house_keeping_service_be.dtos.payment.PaymentResponse;
import iuh.house_keeping_service_be.dtos.payment.UpdatePaymentStatusRequest;
import iuh.house_keeping_service_be.services.PaymentService.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@Slf4j
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * API cho khách hàng tạo một yêu cầu thanh toán cho một lịch đặt.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    public ResponseEntity<PaymentResponse> createPayment(@RequestBody CreatePaymentRequest request) {
        // TODO: Thêm logic để kiểm tra xem bookingId có thuộc về người dùng đang đăng nhập không
        PaymentResponse response = paymentService.createPayment(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * API cho khách hàng hoặc nhân viên xem thông tin thanh toán của một lịch đặt cụ thể.
     */
    @GetMapping("/booking/{bookingId}")
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_EMPLOYEE', 'ROLE_ADMIN')")
    public ResponseEntity<PaymentResponse> getPaymentForBooking(@PathVariable String bookingId) {
        PaymentResponse response = paymentService.getPaymentByBookingId(bookingId);
        return ResponseEntity.ok(response);
    }

    /**
     * API cho khách hàng xem lịch sử thanh toán của chính mình.
     */
    @GetMapping("/history/me")
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    public ResponseEntity<List<PaymentResponse>> getMyPaymentHistory() {
        // Lấy thông tin người dùng đã xác thực từ SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("Authenticated user: {}", authentication.getName());
        String customerId = authentication.getName();
        List<PaymentResponse> history = paymentService.getPaymentHistoryByCustomerId(customerId);
        return ResponseEntity.ok(history);
    }

    /**
     * API Webhook DÀNH RIÊNG cho các cổng thanh toán (Momo, VNPAY) gọi vào.
     */
    @PostMapping("/webhook/update-status")
    public ResponseEntity<Void> handlePaymentWebhook(@RequestBody UpdatePaymentStatusRequest request) {
        // TODO: Thêm logic xác thực request đến từ cổng thanh toán
        paymentService.updatePaymentStatus(request);
        return ResponseEntity.ok().build(); // Chỉ cần trả về 200 OK để xác nhận đã nhận
    }
}