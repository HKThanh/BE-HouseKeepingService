package iuh.house_keeping_service_be.controllers;

import iuh.house_keeping_service_be.dtos.payment.CreatePaymentRequest;
import iuh.house_keeping_service_be.dtos.payment.PaymentMethodResponse;
import iuh.house_keeping_service_be.dtos.payment.PaymentResponse;
import iuh.house_keeping_service_be.dtos.payment.UpdatePaymentStatusRequest;
import iuh.house_keeping_service_be.models.Account;
import iuh.house_keeping_service_be.models.Customer;
import iuh.house_keeping_service_be.repositories.AccountRepository;
import iuh.house_keeping_service_be.repositories.BookingRepository;
import iuh.house_keeping_service_be.repositories.CustomerRepository;
import iuh.house_keeping_service_be.services.PaymentService.PaymentMethodService;
import iuh.house_keeping_service_be.services.PaymentService.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customer/payments")
@Slf4j
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentMethodService paymentMethodService;
    private final BookingRepository bookingRepository;
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    /**
     * API cho khách hàng tạo một yêu cầu thanh toán cho một lịch đặt.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    public ResponseEntity<PaymentResponse> createPayment(@RequestBody CreatePaymentRequest request) {
        // TODO: Thêm logic để kiểm tra xem bookingId có thuộc về người dùng đang đăng nhập không
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Account account = accountRepository.findByUsername(username)
                .orElse(null);
        if (account == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Customer customer = customerRepository.findByAccount_AccountId(account.getAccountId())
                .orElse(null);
        if (customer == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        boolean ownsBooking = bookingRepository.existsByBookingIdAndCustomer_CustomerId(request.getBookingId(), customer.getCustomerId());
        if (!ownsBooking) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

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
    @GetMapping("/history/{customerId}")
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_ADMIN')")
    public ResponseEntity<Page<PaymentResponse>> getPaymentHistory(
            @PathVariable String customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(value = "sort", required = false) String sort) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // Get current user's account
        Account account = accountRepository.findByUsername(username)
                .orElse(null);
        if (account == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // Check if user is admin or accessing their own data
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            // If not admin, check if customer is accessing their own data
            Customer currentCustomer = customerRepository.findByAccount_AccountId(account.getAccountId())
                .orElse(null);
            if (currentCustomer == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            // Customer can only access their own payment history
            if (!currentCustomer.getCustomerId().equals(customerId)) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        }

        // Verify the requested customerId exists
        if (!customerRepository.existsById(customerId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        String[] sortParts = java.util.Arrays.stream(sort != null ? sort.split(",") : new String[0])
                .map(String::trim)
                .filter(part -> !part.isEmpty())
                .toArray(String[]::new);

        String sortProperty = sortParts.length > 0 ? sortParts[0] : "createdAt";
        String sortDirection = sortParts.length > 1 ? sortParts[1] : "desc";

        Sort.Direction direction;
        try {
            direction = Sort.Direction.fromString(sortDirection);
        } catch (IllegalArgumentException ex) {
            direction = Sort.Direction.DESC;
        }

        Sort.Order order = new Sort.Order(direction, sortProperty);

        Pageable pageable = PageRequest.of(page, size, Sort.by(order));

        Page<PaymentResponse> historyPage = paymentService.getPaymentHistoryByCustomerId(customerId, pageable);

        return ResponseEntity.ok(historyPage);
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

    @GetMapping("/methods")
    public ResponseEntity<List<PaymentMethodResponse>> getAvailablePaymentMethods() {
        List<PaymentMethodResponse> methods = paymentMethodService.getAllActivePaymentMethods();
        return ResponseEntity.ok(methods);
    }
}