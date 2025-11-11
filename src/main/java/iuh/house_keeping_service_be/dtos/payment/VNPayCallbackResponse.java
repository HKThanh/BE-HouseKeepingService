package iuh.house_keeping_service_be.dtos.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VNPayCallbackResponse {
    private String responseCode; // 00: Success, other: Fail
    private String transactionNo; // VNPay Transaction No
    private String bankCode; // Bank code
    private String cardType; // Card type
    private String orderInfo; // Order information
    private String payDate; // Payment date
    private String transactionStatus; // Transaction status
    private Long amount; // Amount
}
