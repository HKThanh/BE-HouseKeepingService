package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.models.Customer;
import iuh.house_keeping_service_be.models.VoiceBookingRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoiceBookingRequestRepository extends JpaRepository<VoiceBookingRequest, String> {

    /**
     * Find all voice booking requests by customer
     */
    Page<VoiceBookingRequest> findByCustomer(Customer customer, Pageable pageable);

    /**
     * Find by customer and status
     */
    Page<VoiceBookingRequest> findByCustomerAndStatus(Customer customer, String status, Pageable pageable);

    /**
     * Find all pending/processing requests for a customer
     */
    @Query("SELECT v FROM VoiceBookingRequest v WHERE v.customer = :customer AND v.status IN ('PENDING', 'PROCESSING')")
    List<VoiceBookingRequest> findPendingOrProcessingByCustomer(@Param("customer") Customer customer);

    /**
     * Find requests that failed processing
     */
    Page<VoiceBookingRequest> findByStatus(String status, Pageable pageable);

    /**
     * Find requests created within a time range
     */
    List<VoiceBookingRequest> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Find requests that require clarification (PARTIAL status)
     */
    @Query("SELECT v FROM VoiceBookingRequest v WHERE v.status = 'PARTIAL' AND v.customer = :customer ORDER BY v.createdAt DESC")
    List<VoiceBookingRequest> findPartialByCustomer(@Param("customer") Customer customer);

    /**
     * Count requests by status
     */
    long countByStatus(String status);

    /**
     * Find by booking ID
     */
    Optional<VoiceBookingRequest> findByBooking_BookingId(String bookingId);

    /**
     * Verify whether the voice booking request belongs to a customer (by username).
     */
    boolean existsByIdAndCustomer_Account_Username(String id, String username);
}
