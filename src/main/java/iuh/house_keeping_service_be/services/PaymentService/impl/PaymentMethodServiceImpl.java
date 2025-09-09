package iuh.house_keeping_service_be.services.PaymentService.impl;

import iuh.house_keeping_service_be.dtos.payment.PaymentMethodResponse;
import iuh.house_keeping_service_be.repositories.PaymentMethodRepository;
import iuh.house_keeping_service_be.services.PaymentService.PaymentMethodService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentMethodServiceImpl implements PaymentMethodService {
    private final PaymentMethodRepository paymentMethodRepository;

    @Override
    public List<PaymentMethodResponse> getAllActivePaymentMethods() {
        return paymentMethodRepository.findAllActive().stream()
                .map(pm -> new PaymentMethodResponse(pm.getMethodId(), pm.getMethodCode().name(), pm.getMethodName()))
                .collect(Collectors.toList());
    }
}
