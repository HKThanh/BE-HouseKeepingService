package iuh.house_keeping_service_be.services.PaymentService.impl;

import iuh.house_keeping_service_be.dtos.payment.CreatePaymentRequest;
import iuh.house_keeping_service_be.dtos.payment.PaymentMethodResponse;
import iuh.house_keeping_service_be.dtos.payment.PaymentResponse;
import iuh.house_keeping_service_be.dtos.payment.UpdatePaymentStatusRequest;
import iuh.house_keeping_service_be.models.Account;
import iuh.house_keeping_service_be.models.Booking;
import iuh.house_keeping_service_be.models.Customer;
import iuh.house_keeping_service_be.models.Payment;
import iuh.house_keeping_service_be.models.PaymentMethod;
import iuh.house_keeping_service_be.enums.PaymentStatus;
import iuh.house_keeping_service_be.repositories.BookingRepository;
import iuh.house_keeping_service_be.repositories.PaymentMethodRepository;
import iuh.house_keeping_service_be.repositories.PaymentRepository;
import iuh.house_keeping_service_be.services.NotificationService.NotificationService;
import iuh.house_keeping_service_be.services.PaymentService.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Booking với ID: " + request.getBookingId()));

        PaymentMethod paymentMethod = paymentMethodRepository.findById(request.getMethodId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Payment Method với ID: " + request.getMethodId()));

        Payment newPayment = new Payment();
        newPayment.setBooking(booking);
        newPayment.setPaymentMethod(paymentMethod);
        newPayment.setAmount(booking.getTotalAmount());
        // Trạng thái PENDING được gán mặc định trong Entity Payment

        // 4. TODO: Gọi API của cổng thanh toán (Momo/VNPAY) ở đây để lấy link thanh toán
        // ...
        // newPayment.setTransactionCode(...); // Lưu mã giao dịch từ cổng thanh toán

        Payment savedPayment = paymentRepository.save(newPayment);

        return convertToPaymentResponse(savedPayment);
    }

    @Override
    @Transactional
    public void updatePaymentStatus(UpdatePaymentStatusRequest request) {
        Payment payment = paymentRepository.findByTransactionCode(request.getTransactionCode())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch với mã: " + request.getTransactionCode()));

        PaymentStatus previousStatus = payment.getPaymentStatus();
        PaymentStatus newStatus = request.getStatus();

        payment.setPaymentStatus(newStatus);
        if (newStatus == PaymentStatus.PAID) {
            payment.setPaidAt(LocalDateTime.now());
        }

        Payment savedPayment = paymentRepository.save(payment);
        if (newStatus == PaymentStatus.PAID && previousStatus != PaymentStatus.PAID) {
            dispatchPaymentSuccessNotification(savedPayment);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByBookingId(String bookingId) {
        Payment payment = paymentRepository.findFirstByBooking_BookingIdOrderByCreatedAtDesc(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thanh toán cho Booking ID: " + bookingId));
        return convertToPaymentResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getPaymentHistoryByCustomerId(String customerId, Pageable pageable) {
        // Validate and fix sort parameters
        Pageable validatedPageable = validateAndFixPageable(pageable);

        Page<Payment> paymentPage = paymentRepository.findByCustomerId(customerId, validatedPageable);
        return paymentPage.map(this::convertToPaymentResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentMethodResponse> getAllActivePaymentMethods() {
        List<PaymentMethod> methods = paymentMethodRepository.findAllActive();

        return methods.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private PaymentResponse convertToPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .bookingCode(payment.getBooking().getBookingCode())
                .amount(payment.getAmount())
                .status(payment.getPaymentStatus())
                .paymentMethodName(payment.getPaymentMethod().getMethodName())
//                .iconUrl(payment.getPaymentMethod().getIconUrl())
                .transactionCode(payment.getTransactionCode())
                .createdAt(payment.getCreatedAt())
                .paidAt(payment.getPaidAt())
                .build();
    }

    private PaymentMethodResponse convertToResponse(PaymentMethod method) {
        // Giả sử bạn có DTO PaymentMethodResponse đã tạo trước đó
        return new PaymentMethodResponse(
                method.getMethodId(),
                method.getMethodCode().name(), // Lấy tên từ Enum
                method.getMethodName()
//                method.getIconUrl()
        );
    }

    private Pageable validateAndFixPageable(Pageable pageable) {
        // Valid sortable fields for Payment entity
        Set<String> validSortFields = Set.of(
                "id", "amount", "paymentStatus", "transactionCode",
                "createdAt", "paidAt"
        );

        // Check if sort is valid
        if (pageable.getSort().isSorted()) {
            boolean hasInvalidSort = pageable.getSort().stream()
                    .anyMatch(order -> !validSortFields.contains(order.getProperty()));

            if (hasInvalidSort) {
                // Return default sort: createdAt DESC
                return PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        Sort.by(Sort.Direction.DESC, "createdAt")
                );
            }
        }

        // If no sort specified, use default
        if (pageable.getSort().isUnsorted()) {
            return PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "createdAt")
            );
        }

        return pageable;
    }

    private void dispatchPaymentSuccessNotification(Payment payment) {
        if (payment == null) {
            return;
        }

        Booking booking = payment.getBooking();
        if (booking == null) {
            return;
        }

        Optional<String> accountId = Optional.ofNullable(booking.getCustomer())
                .map(Customer::getAccount)
                .map(Account::getAccountId);

        if (accountId.isEmpty()) {
            return;
        }

        double amount = payment.getAmount() != null ? payment.getAmount().doubleValue() : 0D;
        notificationService.sendPaymentSuccessNotification(
                accountId.get(),
                payment.getId(),
                amount
        );
    }
}
