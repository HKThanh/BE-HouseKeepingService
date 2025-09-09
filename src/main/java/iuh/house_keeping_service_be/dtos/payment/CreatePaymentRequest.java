package iuh.house_keeping_service_be.dtos.payment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePaymentRequest {
    private String bookingId;
    private Integer methodId;
}