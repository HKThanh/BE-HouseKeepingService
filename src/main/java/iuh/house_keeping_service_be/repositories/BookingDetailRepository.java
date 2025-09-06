package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.models.BookingDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingDetailRepository extends JpaRepository<BookingDetail, String> {
    
    // Find by booking ID with service details
    @Query("SELECT bd FROM BookingDetail bd " +
           "LEFT JOIN FETCH bd.service s " +
           "LEFT JOIN FETCH s.category " +
           "WHERE bd.booking.bookingId = :bookingId")
    List<BookingDetail> findByBookingIdWithService(@Param("bookingId") String bookingId);
    
    // Find by service ID
    @Query("SELECT bd FROM BookingDetail bd WHERE bd.service.serviceId = :serviceId")
    List<BookingDetail> findByServiceId(@Param("serviceId") Integer serviceId);
    
    // Get total quantity for a service in a booking
    @Query("SELECT COALESCE(SUM(bd.quantity), 0) FROM BookingDetail bd " +
           "WHERE bd.booking.bookingId = :bookingId AND bd.service.serviceId = :serviceId")
    Integer getTotalQuantityByBookingAndService(@Param("bookingId") String bookingId, 
                                               @Param("serviceId") Integer serviceId);
    
    // Find booking details with specific choices (PostgreSQL syntax)
    @Query("SELECT bd FROM BookingDetail bd WHERE bd.selectedChoiceIds LIKE %:choiceId%")
    List<BookingDetail> findBySelectedChoiceIdsContaining(@Param("choiceId") String choiceId);
    
    // Get popular service combinations
    @Query("SELECT bd.service.serviceId, COUNT(bd) as count FROM BookingDetail bd " +
           "GROUP BY bd.service.serviceId ORDER BY count DESC")
    List<Object[]> findPopularServices();
    
    // Validate booking detail belongs to customer
    @Query("SELECT COUNT(bd) > 0 FROM BookingDetail bd " +
           "WHERE bd.id = :bookingDetailId AND bd.booking.customer.customerId = :customerId")
    boolean validateBookingDetailOwnership(@Param("bookingDetailId") String bookingDetailId,
                                          @Param("customerId") String customerId);
}