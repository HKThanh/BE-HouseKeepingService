package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.models.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Integer> {
    @Query("SELECT pm.methodName FROM PaymentMethod pm JOIN Payment p ON pm.methodId = p.paymentMethod.methodId WHERE p.id = :paymentId")
    String findPaymentMethodNameByPaymentId(String paymentId);
}
