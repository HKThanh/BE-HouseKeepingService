package iuh.house_keeping_service_be.dtos.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PaymentMethodResponse {
    private Integer methodId;
    private String methodCode;
    private String methodName;
//    private String iconUrl;
}
