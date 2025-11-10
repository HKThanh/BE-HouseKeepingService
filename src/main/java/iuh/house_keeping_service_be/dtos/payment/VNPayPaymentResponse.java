package iuh.house_keeping_service_be.dtos.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VNPayPaymentResponse {
    private String code; // Response code
    private String message; // Response message
    private String paymentUrl; // URL to redirect user to VNPay
}
