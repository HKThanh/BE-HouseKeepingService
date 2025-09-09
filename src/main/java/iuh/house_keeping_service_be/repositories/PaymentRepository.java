package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.enums.PaymentMethodCode;
import iuh.house_keeping_service_be.models.Payment;
import iuh.house_keeping_service_be.enums.PaymentStatus;
import iuh.house_keeping_service_be.enums.PaymentMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    
    // Find by booking ID
    @Query("SELECT p FROM Payment p WHERE p.booking.bookingId = :bookingId ORDER BY p.createdAt DESC")
    List<Payment> findByBookingIdOrderByCreatedAtDesc(@Param("bookingId") String bookingId);
    
    // Find by transaction code
    Optional<Payment> findByTransactionCode(String transactionCode);
    
    // Find by status
    List<Payment> findByPaymentStatusOrderByCreatedAtDesc(PaymentStatus status);
    
    // Find pending payments older than cutoff time
    @Query("SELECT p FROM Payment p WHERE p.paymentStatus = 'PENDING' AND p.createdAt < :cutoffTime")
    List<Payment> findExpiredPendingPayments(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    // Get total payment amount for a booking
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
           "WHERE p.booking.bookingId = :bookingId AND p.paymentStatus = 'PAID'")
    BigDecimal getTotalPaidAmountByBooking(@Param("bookingId") String bookingId);
    
    // Check if transaction code exists
    boolean existsByTransactionCode(String transactionCode);
    
    // Get payment statistics by method and date range
    @Query("SELECT p.paymentMethod, COUNT(p), SUM(p.amount) FROM Payment p " +
           "WHERE p.paymentStatus = 'PAID' AND p.paidAt BETWEEN :startDate AND :endDate " +
           "GROUP BY p.paymentMethod")
    List<Object[]> getPaymentStatistics(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);
    
    // Find latest payment for booking
    @Query("SELECT p FROM Payment p WHERE p.booking.bookingId = :bookingId " +
           "ORDER BY p.createdAt DESC LIMIT 1")
    Optional<Payment> findLatestPaymentByBookingId(@Param("bookingId") String bookingId);

    @Query("SELECT p FROM Payment p WHERE p.booking.customer.customerId = :customerId")
    Page<Payment> findByCustomerId(@Param("customerId") String customerId, Pageable pageable);


    @Query("SELECT p FROM Payment p WHERE p.booking.customer.customerId = :customerId ORDER BY p.createdAt DESC")
    List<Payment> findByCustomerIdOrderByCreatedAtDesc(@Param("customerId") String customerId);

    List<Payment> findByPaymentMethod_MethodCode(PaymentMethodCode methodCode);

    List<Payment> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
}