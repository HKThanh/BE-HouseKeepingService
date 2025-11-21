package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.models.BookingAdditionalFee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingAdditionalFeeRepository extends JpaRepository<BookingAdditionalFee, Long> {
    List<BookingAdditionalFee> findByBooking_BookingId(String bookingId);
}
