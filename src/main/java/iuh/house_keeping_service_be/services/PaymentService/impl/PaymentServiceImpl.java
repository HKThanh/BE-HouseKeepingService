package iuh.house_keeping_service_be.services.PaymentService.impl;

import iuh.house_keeping_service_be.dtos.payment.CreatePaymentRequest;
import iuh.house_keeping_service_be.dtos.payment.PaymentMethodResponse;
import iuh.house_keeping_service_be.dtos.payment.PaymentResponse;
import iuh.house_keeping_service_be.dtos.payment.UpdatePaymentStatusRequest;
import iuh.house_keeping_service_be.models.Booking;
import iuh.house_keeping_service_be.models.Payment;
import iuh.house_keeping_service_be.models.PaymentMethod;
import iuh.house_keeping_service_be.enums.PaymentStatus;
import iuh.house_keeping_service_be.repositories.BookingRepository;
import iuh.house_keeping_service_be.repositories.PaymentMethodRepository;
import iuh.house_keeping_service_be.repositories.PaymentRepository;
import iuh.house_keeping_service_be.services.PaymentService.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final PaymentMethodRepository paymentMethodRepository;

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

        payment.setPaymentStatus(request.getStatus());
        if (request.getStatus() == PaymentStatus.PAID) {
            payment.setPaidAt(LocalDateTime.now());
        }

        paymentRepository.save(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByBookingId(String bookingId) {
        Payment payment = paymentRepository.findLatestPaymentByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thanh toán cho Booking ID: " + bookingId));
        return convertToPaymentResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getPaymentHistoryByCustomerId(String customerId, Pageable pageable) {
        // Gọi phương thức repository đã được cập nhật
        Page<Payment> paymentPage = paymentRepository.findByCustomerId(customerId, pageable);

        // Dùng hàm map của Page để chuyển đổi nội dung
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
}