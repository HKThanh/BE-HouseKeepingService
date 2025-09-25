package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.enums.BookingStatus;
import iuh.house_keeping_service_be.models.Booking;
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
public interface BookingRepository extends JpaRepository<Booking, String> {

//     Optional<Booking> findByBookingCode(String bookingCode);

//     @Query("SELECT b FROM Booking b WHERE b.customer.customerId = :customerId ORDER BY b.createdAt DESC")
//     List<Booking> findByCustomerOrderByCreatedAtDesc(@Param("customerId") String customerId);

//     @Query("SELECT b FROM Booking b WHERE b.customer.customerId = :customerId AND b.status = :status ORDER BY b.createdAt DESC")
//     List<Booking> findByCustomerAndStatusOrderByCreatedAtDesc(
//             @Param("customerId") String customerId,
//             @Param("status") BookingStatus status
//     );

//     @Query("SELECT b FROM Booking b WHERE b.bookingTime BETWEEN :startTime AND :endTime ORDER BY b.bookingTime ASC")
//     List<Booking> findByBookingTimeBetween(
//             @Param("startTime") LocalDateTime startTime,
//             @Param("endTime") LocalDateTime endTime
//     );

//     @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = :status")
//     Long countByStatus(@Param("status") BookingStatus status);

//     @Query("SELECT b FROM Booking b " +
//            "JOIN FETCH b.bookingDetails bd " +
//            "JOIN FETCH bd.assignments a " +
//            "WHERE a.employee.employeeId = :employeeId " +
//            "AND b.bookingTime BETWEEN :startTime AND :endTime " +
//            "ORDER BY b.bookingTime ASC")
//     List<Booking> findByEmployeeAndDateRange(
//             @Param("employeeId") String employeeId,
//             @Param("startTime") LocalDateTime startTime,
//             @Param("endTime") LocalDateTime endTime
//     );

//     boolean existsByBookingCode(String bookingCode);

//     @Query("SELECT MAX(CAST(SUBSTRING(b.bookingCode, 3) AS integer)) FROM Booking b WHERE b.bookingCode LIKE 'BK%'")
//     Integer findMaxBookingCodeNumber();

    // Find by booking code
    Optional<Booking> findByBookingCode(String bookingCode);
    
    // Find by customer
    @Query("SELECT b FROM Booking b WHERE b.customer.customerId = :customerId ORDER BY b.createdAt DESC")
    List<Booking> findByCustomerIdOrderByCreatedAtDesc(@Param("customerId") String customerId);
    
    // Find by status
    List<Booking> findByStatusOrderByCreatedAtDesc(BookingStatus status);
    
    // Find bookings in date range
    @Query("SELECT b FROM Booking b WHERE b.bookingTime BETWEEN :startDate AND :endDate ORDER BY b.bookingTime")
    List<Booking> findBookingsByDateRange(@Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);
    
    // Check if customer has pending bookings
    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.customer.customerId = :customerId AND b.status = 'PENDING'")
    boolean hasActivePendingBookings(@Param("customerId") String customerId);
    
    // Get customer booking count
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.customer.customerId = :customerId")
    long countByCustomerId(@Param("customerId") String customerId);
    
    // Check booking code uniqueness
    boolean existsByBookingCode(String bookingCode);
    
    // Find recent bookings for conflict checking
    @Query("SELECT b FROM Booking b WHERE b.address.addressId = :addressId " +
           "AND b.bookingTime BETWEEN :startTime AND :endTime " +
           "AND b.status NOT IN (iuh.house_keeping_service_be.enums.BookingStatus.CANCELLED, iuh.house_keeping_service_be.enums.BookingStatus.COMPLETED)")
    List<Booking> findConflictingBookings(@Param("addressId") String addressId,
                                         @Param("startTime") LocalDateTime startTime,
                                         @Param("endTime") LocalDateTime endTime);
    
    // Find booking with all details for creating response
    @Query("SELECT b FROM Booking b " +
           "LEFT JOIN FETCH b.customer c " +
           "LEFT JOIN FETCH b.address a " +
           "LEFT JOIN FETCH b.promotion p " +
           "WHERE b.bookingId = :bookingId")
    Optional<Booking> findBookingWithDetails(@Param("bookingId") String bookingId);

    // Find bookings awaiting employee with no assignments
    @Query("SELECT b FROM Booking b WHERE b.status = :status " +
            "AND NOT EXISTS (SELECT a FROM Assignment a WHERE a.bookingDetail.booking = b)")
    Page<Booking> findByStatusWithoutAssignments(@Param("status") BookingStatus status, Pageable pageable);

    boolean existsByBookingIdAndCustomer_CustomerId(String bookingId, String customerId);

    @Query("SELECT DISTINCT b FROM Booking b " +
            "LEFT JOIN b.bookingDetails bd " +
            "LEFT JOIN bd.assignments a " +
            "WHERE b.status = iuh.house_keeping_service_be.enums.BookingStatus.AWAITING_EMPLOYEE " +
            "AND a IS NULL")
    List<Booking> findAwaitingEmployeeBookings(Pageable pageable);

//    @Query(value = "SELECT DISTINCT b FROM Booking b " +
//            "WHERE b.status = iuh.house_keeping_service_be.enums.BookingStatus.AWAITING_EMPLOYEE " +
//            "AND (:zoneKeys IS NULL OR LOWER(CONCAT(b.address.district, '|', b.address.city)) IN :zoneKeys)",
//            countQuery = "SELECT COUNT(DISTINCT b.bookingId) FROM Booking b " +
//                    "WHERE b.status = iuh.house_keeping_service_be.enums.BookingStatus.AWAITING_EMPLOYEE " +
//                    "AND (:zoneKeys IS NULL OR LOWER(CONCAT(b.address.district, '|', b.address.city)) IN :zoneKeys)")
//    Page<Booking> findAwaitingEmployeeBookingsByZones(@Param("zoneKeys") List<String> zoneKeys, Pageable pageable);

    @Query(value = "SELECT DISTINCT b FROM Booking b " +
            "LEFT JOIN b.bookingDetails bd " +
            "LEFT JOIN bd.assignments a " +
            "WHERE b.status = iuh.house_keeping_service_be.enums.BookingStatus.AWAITING_EMPLOYEE " +
            "AND a IS NULL " +
            "AND (:zoneKeys IS NULL OR LOWER(CONCAT(b.address.district, '|', b.address.city)) IN :zoneKeys)",
            countQuery = "SELECT COUNT(DISTINCT b.bookingId) FROM Booking b " +
                    "LEFT JOIN b.bookingDetails bd " +
                    "LEFT JOIN bd.assignments a " +
                    "WHERE b.status = iuh.house_keeping_service_be.enums.BookingStatus.AWAITING_EMPLOYEE " +
                    "AND a IS NULL " +
                    "AND (:zoneKeys IS NULL OR LOWER(CONCAT(b.address.district, '|', b.address.city)) IN :zoneKeys)")
    Page<Booking> findAwaitingEmployeeBookingsByZones(@Param("zoneKeys") List<String> zoneKeys, Pageable pageable);

    @Query(value = "SELECT DISTINCT b FROM Booking b " +
            "LEFT JOIN b.bookingDetails bd " +
            "LEFT JOIN bd.assignments a " +
            "WHERE b.status = iuh.house_keeping_service_be.enums.BookingStatus.AWAITING_EMPLOYEE " +
            "AND a IS NULL " +
            "AND (:zoneKeys IS NULL OR LOWER(CONCAT(b.address.district, '|', b.address.city)) NOT IN :zoneKeys)")
    List<Booking> findAwaitingEmployeeBookingsOutsideZones(@Param("zoneKeys") List<String> zoneKeys);

    @Query("SELECT b FROM Booking b " +
           "LEFT JOIN FETCH b.customer c " +
           "LEFT JOIN FETCH b.address a " +
           "LEFT JOIN FETCH b.promotion p " +
           "WHERE b.customer.customerId = :customerId " +
           "ORDER BY b.createdAt DESC")
    Page<Booking> findByCustomerIdWithPagination(@Param("customerId") String customerId, Pageable pageable);
}