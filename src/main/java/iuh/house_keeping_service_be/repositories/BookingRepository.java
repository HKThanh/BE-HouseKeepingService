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

    // Check if booking exists for recurring booking at specific time
    boolean existsByRecurringBooking_RecurringBookingIdAndBookingTime(
            String recurringBookingId, 
            LocalDateTime bookingTime
    );

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
            "AND (:zoneKeys IS NULL OR LOWER(CONCAT(b.address.ward, '|', b.address.city)) IN :zoneKeys)",
            countQuery = "SELECT COUNT(DISTINCT b.bookingId) FROM Booking b " +
                    "LEFT JOIN b.bookingDetails bd " +
                    "LEFT JOIN bd.assignments a " +
                    "WHERE b.status = iuh.house_keeping_service_be.enums.BookingStatus.AWAITING_EMPLOYEE " +
                    "AND a IS NULL " +
                    "AND (:zoneKeys IS NULL OR LOWER(CONCAT(b.address.ward, '|', b.address.city)) IN :zoneKeys)")
    Page<Booking> findAwaitingEmployeeBookingsByZones(@Param("zoneKeys") List<String> zoneKeys, Pageable pageable);

    @Query(value = "SELECT DISTINCT b FROM Booking b " +
            "LEFT JOIN b.bookingDetails bd " +
            "LEFT JOIN bd.assignments a " +
            "WHERE b.status = iuh.house_keeping_service_be.enums.BookingStatus.AWAITING_EMPLOYEE " +
            "AND a IS NULL " +
            "AND (:zoneKeys IS NULL OR LOWER(CONCAT(b.address.ward, '|', b.address.city)) NOT IN :zoneKeys)")
    List<Booking> findAwaitingEmployeeBookingsOutsideZones(@Param("zoneKeys") List<String> zoneKeys);

    // Pending bookings queries
    @Query("SELECT DISTINCT b FROM Booking b " +
            "LEFT JOIN b.bookingDetails bd " +
            "LEFT JOIN bd.assignments a " +
            "WHERE b.status = iuh.house_keeping_service_be.enums.BookingStatus.PENDING " +
            "AND a IS NULL")
    List<Booking> findPendingBookings(Pageable pageable);

    @Query(value = "SELECT DISTINCT b FROM Booking b " +
            "LEFT JOIN b.bookingDetails bd " +
            "LEFT JOIN bd.assignments a " +
            "WHERE b.status = iuh.house_keeping_service_be.enums.BookingStatus.PENDING " +
            "AND a IS NULL " +
            "AND (:zoneKeys IS NULL OR LOWER(CONCAT(b.address.ward, '|', b.address.city)) IN :zoneKeys)",
            countQuery = "SELECT COUNT(DISTINCT b.bookingId) FROM Booking b " +
                    "LEFT JOIN b.bookingDetails bd " +
                    "LEFT JOIN bd.assignments a " +
                    "WHERE b.status = iuh.house_keeping_service_be.enums.BookingStatus.PENDING " +
                    "AND a IS NULL " +
                    "AND (:zoneKeys IS NULL OR LOWER(CONCAT(b.address.ward, '|', b.address.city)) IN :zoneKeys)")
    Page<Booking> findPendingBookingsByZones(@Param("zoneKeys") List<String> zoneKeys, Pageable pageable);

    @Query(value = "SELECT DISTINCT b FROM Booking b " +
            "LEFT JOIN b.bookingDetails bd " +
            "LEFT JOIN bd.assignments a " +
            "WHERE b.status = iuh.house_keeping_service_be.enums.BookingStatus.PENDING " +
            "AND a IS NULL " +
            "AND (:zoneKeys IS NULL OR LOWER(CONCAT(b.address.ward, '|', b.address.city)) NOT IN :zoneKeys)")
    List<Booking> findPendingBookingsOutsideZones(@Param("zoneKeys") List<String> zoneKeys);

    @Query("SELECT b FROM Booking b " +
           "LEFT JOIN FETCH b.customer c " +
           "LEFT JOIN FETCH b.address a " +
           "LEFT JOIN FETCH b.promotion p " +
           "WHERE b.customer.customerId = :customerId " +
           "ORDER BY b.createdAt DESC")
    Page<Booking> findByCustomerIdWithPagination(@Param("customerId") String customerId, Pageable pageable);

    // Find bookings by customer with date filter
    @Query("SELECT b FROM Booking b " +
           "LEFT JOIN FETCH b.customer c " +
           "LEFT JOIN FETCH b.address a " +
           "LEFT JOIN FETCH b.promotion p " +
           "WHERE b.customer.customerId = :customerId " +
           "AND b.bookingTime >= :fromDate " +
           "ORDER BY b.createdAt DESC")
    Page<Booking> findByCustomerIdWithPaginationAndDate(@Param("customerId") String customerId, 
                                                         @Param("fromDate") java.time.LocalDateTime fromDate, 
                                                         Pageable pageable);

    // Find unverified bookings (posts) ordered by created date descending
    @Query("SELECT b FROM Booking b " +
           "LEFT JOIN FETCH b.customer c " +
           "LEFT JOIN FETCH b.address a " +
           "WHERE b.isVerified = false " +
           "ORDER BY b.createdAt DESC")
    Page<Booking> findUnverifiedBookingsOrderByCreatedAtDesc(Pageable pageable);

    // Find unverified bookings without pagination
    @Query("SELECT b FROM Booking b WHERE b.isVerified = false ORDER BY b.createdAt DESC")
    List<Booking> findUnverifiedBookings();

    // Find verified bookings that are still awaiting employee assignment
    @Query("SELECT b FROM Booking b " +
           "LEFT JOIN FETCH b.customer c " +
           "LEFT JOIN FETCH b.address a " +
           "LEFT JOIN FETCH b.promotion p " +
           "WHERE b.isVerified = true " +
           "AND b.status = iuh.house_keeping_service_be.enums.BookingStatus.AWAITING_EMPLOYEE " +
           "ORDER BY b.createdAt DESC")
    Page<Booking> findVerifiedAwaitingEmployeeBookings(Pageable pageable);

    // Find verified bookings awaiting employee with date filter
    @Query("SELECT b FROM Booking b " +
           "LEFT JOIN FETCH b.customer c " +
           "LEFT JOIN FETCH b.address a " +
           "LEFT JOIN FETCH b.promotion p " +
           "WHERE b.isVerified = true " +
           "AND b.status = iuh.house_keeping_service_be.enums.BookingStatus.AWAITING_EMPLOYEE " +
           "AND b.bookingTime >= :fromDate " +
           "ORDER BY b.bookingTime ASC")
    Page<Booking> findVerifiedAwaitingEmployeeBookingsWithDate(@Param("fromDate") java.time.LocalDateTime fromDate, Pageable pageable);

    // Find all verified bookings awaiting employee without pagination
    @Query("SELECT b FROM Booking b " +
           "LEFT JOIN FETCH b.customer c " +
           "LEFT JOIN FETCH b.address a " +
           "LEFT JOIN FETCH b.promotion p " +
           "WHERE b.isVerified = true " +
           "AND b.status = iuh.house_keeping_service_be.enums.BookingStatus.AWAITING_EMPLOYEE " +
           "ORDER BY b.bookingTime ASC")
    List<Booking> findAllVerifiedAwaitingEmployeeBookings();

    // Find all verified bookings awaiting employee from a date without pagination
    @Query("SELECT b FROM Booking b " +
           "LEFT JOIN FETCH b.customer c " +
           "LEFT JOIN FETCH b.address a " +
           "LEFT JOIN FETCH b.promotion p " +
           "WHERE b.isVerified = true " +
           "AND b.status = iuh.house_keeping_service_be.enums.BookingStatus.AWAITING_EMPLOYEE " +
           "AND b.bookingTime >= :fromDate " +
           "ORDER BY b.bookingTime ASC")
    List<Booking> findAllVerifiedAwaitingEmployeeBookingsFromDate(@Param("fromDate") java.time.LocalDateTime fromDate);

    // Find all bookings ordered by booking time descending (for admin)
    @Query("SELECT b FROM Booking b " +
           "LEFT JOIN FETCH b.customer c " +
           "LEFT JOIN FETCH b.address a " +
           "LEFT JOIN FETCH b.promotion p " +
           "ORDER BY b.bookingTime DESC")
    Page<Booking> findAllBookingsOrderByBookingTimeDesc(Pageable pageable);

    // Find all bookings with date filter ordered by booking time descending (for admin)
    @Query("SELECT b FROM Booking b " +
           "LEFT JOIN FETCH b.customer c " +
           "LEFT JOIN FETCH b.address a " +
           "LEFT JOIN FETCH b.promotion p " +
           "WHERE b.bookingTime >= :fromDate " +
           "ORDER BY b.bookingTime DESC")
    Page<Booking> findAllBookingsOrderByBookingTimeDescWithDate(@Param("fromDate") java.time.LocalDateTime fromDate, Pageable pageable);

    // Find unverified bookings with date filter ordered by created date descending
    @Query("SELECT b FROM Booking b " +
           "LEFT JOIN FETCH b.customer c " +
           "LEFT JOIN FETCH b.address a " +
           "WHERE b.isVerified = false " +
           "AND b.bookingTime >= :fromDate " +
           "ORDER BY b.createdAt DESC")
    Page<Booking> findUnverifiedBookingsOrderByCreatedAtDescWithDate(@Param("fromDate") java.time.LocalDateTime fromDate, Pageable pageable);

    // Get service booking statistics for a date range
    @Query("SELECT bd.service.serviceId, bd.service.name, COUNT(bd.id) " +
           "FROM BookingDetail bd " +
           "WHERE bd.booking.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY bd.service.serviceId, bd.service.name " +
           "ORDER BY COUNT(bd.id) DESC")
    List<Object[]> getServiceBookingStatistics(@Param("startDate") LocalDateTime startDate, 
                                               @Param("endDate") LocalDateTime endDate);

    // Get revenue statistics for a date range
    @Query("SELECT " +
           "COALESCE(SUM(b.totalAmount), 0), " +
           "COUNT(b.bookingId), " +
           "COALESCE(AVG(b.totalAmount), 0), " +
           "COALESCE(MAX(b.totalAmount), 0), " +
           "COALESCE(MIN(b.totalAmount), 0) " +
           "FROM Booking b " +
           "WHERE b.bookingTime BETWEEN :startDate AND :endDate " +
           "AND b.status = iuh.house_keeping_service_be.enums.BookingStatus.COMPLETED")
    Object[] getRevenueStatistics(@Param("startDate") LocalDateTime startDate, 
                                  @Param("endDate") LocalDateTime endDate);

    // Find bookings where employee has assignment, ordered by booking time
    @Query("SELECT DISTINCT b FROM Booking b " +
           "LEFT JOIN FETCH b.customer c " +
           "LEFT JOIN FETCH b.address addr " +
           "LEFT JOIN FETCH b.promotion p " +
           "JOIN b.bookingDetails bd " +
           "JOIN bd.assignments asn " +
           "WHERE asn.employee.employeeId = :employeeId " +
           "ORDER BY b.bookingTime ASC")
    Page<Booking> findBookingsByEmployeeIdOrderByBookingTime(@Param("employeeId") String employeeId, Pageable pageable);

    // Find bookings by employee with date filter
    @Query("SELECT DISTINCT b FROM Booking b " +
           "LEFT JOIN FETCH b.customer c " +
           "LEFT JOIN FETCH b.address addr " +
           "LEFT JOIN FETCH b.promotion p " +
           "JOIN b.bookingDetails bd " +
           "JOIN bd.assignments asn " +
           "WHERE asn.employee.employeeId = :employeeId " +
           "AND b.bookingTime >= :fromDate " +
           "ORDER BY b.bookingTime ASC")
    Page<Booking> findBookingsByEmployeeIdOrderByBookingTimeWithDate(@Param("employeeId") String employeeId, 
                                                                      @Param("fromDate") java.time.LocalDateTime fromDate, 
                                                                      Pageable pageable);
    
    // Find bookings by status list and booking time range (for urgent booking notifications)
    @Query("SELECT b FROM Booking b WHERE b.status IN :statuses " +
           "AND b.bookingTime BETWEEN :startTime AND :endTime " +
           "ORDER BY b.bookingTime ASC")
    List<Booking> findByStatusInAndBookingTimeBetween(@Param("statuses") List<BookingStatus> statuses,
                                                       @Param("startTime") LocalDateTime startTime,
                                                       @Param("endTime") LocalDateTime endTime);
    
    // Count bookings by recurring booking ID
    long countByRecurringBooking_RecurringBookingId(String recurringBookingId);

    @Query("SELECT b.bookingTime FROM Booking b " +
           "WHERE b.recurringBooking.recurringBookingId = :recurringBookingId " +
           "AND b.bookingTime BETWEEN :startDate AND :endDate")
    List<LocalDateTime> findBookingTimesByRecurringBooking(
            @Param("recurringBookingId") String recurringBookingId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT b FROM Booking b " +
           "WHERE b.recurringBooking.recurringBookingId = :recurringBookingId " +
           "AND b.bookingTime > :after " +
           "AND b.status IN :statuses")
    List<Booking> findFutureByRecurringAndStatuses(
            @Param("recurringBookingId") String recurringBookingId,
            @Param("after") LocalDateTime after,
            @Param("statuses") List<BookingStatus> statuses);

    @Query("SELECT COUNT(b) FROM Booking b " +
           "WHERE b.recurringBooking.recurringBookingId = :recurringBookingId " +
           "AND b.bookingTime > :after " +
           "AND b.status IN :statuses")
    long countUpcomingByRecurringAndStatuses(
            @Param("recurringBookingId") String recurringBookingId,
            @Param("after") LocalDateTime after,
            @Param("statuses") List<BookingStatus> statuses);
    
    // Count bookings by customer and status within date range
    @Query("SELECT COUNT(b) FROM Booking b " +
           "WHERE b.customer.customerId = :customerId " +
           "AND b.status = :status " +
           "AND b.bookingTime BETWEEN :startDate AND :endDate")
    long countByCustomerIdAndStatusAndDateRange(@Param("customerId") String customerId,
                                                 @Param("status") BookingStatus status,
                                                 @Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);
    
    // Count bookings by customer within date range
    @Query("SELECT COUNT(b) FROM Booking b " +
           "WHERE b.customer.customerId = :customerId " +
           "AND b.bookingTime BETWEEN :startDate AND :endDate")
    long countByCustomerIdAndDateRange(@Param("customerId") String customerId,
                                        @Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);
}
