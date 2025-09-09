package iuh.house_keeping_service_be.services.PaymentService;

import iuh.house_keeping_service_be.dtos.payment.PaymentMethodResponse;

import java.util.List;

public interface PaymentMethodService {
    List<PaymentMethodResponse> getAllActivePaymentMethods();
}
