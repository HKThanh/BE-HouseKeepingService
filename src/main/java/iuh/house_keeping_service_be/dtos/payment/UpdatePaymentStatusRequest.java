package iuh.house_keeping_service_be.dtos.payment;

import iuh.house_keeping_service_be.enums.PaymentStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UpdatePaymentStatusRequest {
    private String transactionCode;
    private PaymentStatus status;
    private LocalDateTime paidAt;

    // (Tùy chọn) Có thể thêm các thông tin khác mà cổng thanh toán trả về
    // private String paymentDetails;
}