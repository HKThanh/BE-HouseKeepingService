package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.enums.BookingStatus;
import iuh.house_keeping_service_be.models.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {

    Optional<Booking> findByBookingCode(String bookingCode);

    @Query("SELECT b FROM Booking b WHERE b.customer.customerId = :customerId ORDER BY b.createdAt DESC")
    List<Booking> findByCustomerOrderByCreatedAtDesc(@Param("customerId") String customerId);

    @Query("SELECT b FROM Booking b WHERE b.customer.customerId = :customerId AND b.status = :status ORDER BY b.createdAt DESC")
    List<Booking> findByCustomerAndStatusOrderByCreatedAtDesc(
            @Param("customerId") String customerId,
            @Param("status") BookingStatus status
    );

    @Query("SELECT b FROM Booking b WHERE b.bookingTime BETWEEN :startTime AND :endTime ORDER BY b.bookingTime ASC")
    List<Booking> findByBookingTimeBetween(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = :status")
    Long countByStatus(@Param("status") BookingStatus status);

    @Query("SELECT b FROM Booking b " +
           "JOIN FETCH b.bookingDetails bd " +
           "JOIN FETCH bd.assignments a " +
           "WHERE a.employee.employeeId = :employeeId " +
           "AND b.bookingTime BETWEEN :startTime AND :endTime " +
           "ORDER BY b.bookingTime ASC")
    List<Booking> findByEmployeeAndDateRange(
            @Param("employeeId") String employeeId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    boolean existsByBookingCode(String bookingCode);

    @Query("SELECT MAX(CAST(SUBSTRING(b.bookingCode, 3) AS integer)) FROM Booking b WHERE b.bookingCode LIKE 'BK%'")
    Integer findMaxBookingCodeNumber();
}