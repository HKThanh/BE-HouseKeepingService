package iuh.house_keeping_service_be.dtos.payment;

import iuh.house_keeping_service_be.enums.PaymentStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder // Dùng Builder pattern để dễ dàng tạo đối tượng trong Service
public class PaymentResponse {
    private String paymentId;
    private String bookingCode; // Lấy từ booking liên quan để dễ hiển thị
    private BigDecimal amount;
    private PaymentStatus status;
    private String paymentMethodName; // Tên phương thức thanh toán (ví dụ: "Ví điện tử Momo")
    private String iconUrl; // URL icon của phương thức thanh toán
    private String transactionCode;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    // private String paymentUrl;
}