package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.enums.RecurringBookingStatus;
import iuh.house_keeping_service_be.models.RecurringBooking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecurringBookingRepository extends JpaRepository<RecurringBooking, String> {

    // Find by customer
    Page<RecurringBooking> findByCustomer_CustomerIdOrderByCreatedAtDesc(String customerId, Pageable pageable);

    // Find active recurring bookings by customer
    Page<RecurringBooking> findByCustomer_CustomerIdAndStatusOrderByCreatedAtDesc(
            String customerId,
            RecurringBookingStatus status,
            Pageable pageable
    );

    // Find by customer and id
    Optional<RecurringBooking> findByRecurringBookingIdAndCustomer_CustomerId(String recurringBookingId, String customerId);

    // Find all active recurring bookings that need to generate bookings
    @Query("SELECT rb FROM RecurringBooking rb " +
            "WHERE rb.status = :status " +
            "AND (rb.endDate IS NULL OR rb.endDate >= :currentDate) " +
            "AND rb.startDate <= :currentDate")
    List<RecurringBooking> findActiveRecurringBookingsForGeneration(
            @Param("status") RecurringBookingStatus status,
            @Param("currentDate") LocalDate currentDate
    );

    // Count recurring bookings by customer
    long countByCustomer_CustomerId(String customerId);

    // Count active recurring bookings by customer
    long countByCustomer_CustomerIdAndStatus(String customerId, RecurringBookingStatus status);

    // Find recurring booking with details eagerly loaded
    @Query("SELECT rb FROM RecurringBooking rb " +
            "LEFT JOIN FETCH rb.recurringBookingDetails rbd " +
            "LEFT JOIN FETCH rbd.service " +
            "WHERE rb.recurringBookingId = :recurringBookingId")
    Optional<RecurringBooking> findByIdWithDetails(@Param("recurringBookingId") String recurringBookingId);
}
