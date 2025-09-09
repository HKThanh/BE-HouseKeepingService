package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.models.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Integer> {
    @Query("SELECT pm.methodName FROM PaymentMethod pm JOIN Payment p ON pm.methodId = p.paymentMethod.methodId WHERE p.id = :paymentId")
    String findPaymentMethodNameByPaymentId(String paymentId);

    /**
     * Tìm tất cả các phương thức thanh toán đang được kích hoạt (active).
     * @return Danh sách các PaymentMethod đang hoạt động.
     */
    @Query("SELECT pm FROM PaymentMethod pm WHERE pm.isActive = TRUE ORDER BY pm.methodId")
    List<PaymentMethod> findAllActive();
}
