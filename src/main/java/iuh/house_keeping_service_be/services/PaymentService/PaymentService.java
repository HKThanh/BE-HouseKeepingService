package iuh.house_keeping_service_be.services.PaymentService;

import iuh.house_keeping_service_be.dtos.payment.CreatePaymentRequest;
import iuh.house_keeping_service_be.dtos.payment.PaymentResponse;
import iuh.house_keeping_service_be.dtos.payment.UpdatePaymentStatusRequest;

import java.util.List;

public interface PaymentService {

    /**
     * Tạo một giao dịch thanh toán mới khi một lịch đặt được xác nhận.
     * @param request Chứa thông tin bookingId và methodId.
     * @return Thông tin chi tiết về thanh toán vừa được tạo.
     */
    PaymentResponse createPayment(CreatePaymentRequest request);

    /**
     * Cập nhật trạng thái của một thanh toán (thường được gọi bởi webhook từ cổng thanh toán).
     * @param request Chứa transactionCode, status mới, và các thông tin liên quan.
     */
    void updatePaymentStatus(UpdatePaymentStatusRequest request);

    /**
     * Lấy thông tin thanh toán của một lịch đặt cụ thể.
     * @param bookingId ID của lịch đặt.
     * @return Thông tin thanh toán.
     */
    PaymentResponse getPaymentByBookingId(String bookingId);

    /**
     * Lấy toàn bộ lịch sử thanh toán của một khách hàng.
     * @param customerId ID của khách hàng.
     * @return Danh sách các thanh toán.
     */
    List<PaymentResponse> getPaymentHistoryByCustomerId(String customerId);
}