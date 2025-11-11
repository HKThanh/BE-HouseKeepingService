package iuh.house_keeping_service_be.dtos.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VNPayPaymentRequest {
    private String bookingId;
    private Long amount;
    private String orderInfo;
    private String orderType;
    private String locale; // vn or en
    private String bankCode; // Optional: specific bank code
}
