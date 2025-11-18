package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.models.RecurringBookingDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecurringBookingDetailRepository extends JpaRepository<RecurringBookingDetail, String> {

    List<RecurringBookingDetail> findByRecurringBooking_RecurringBookingId(String recurringBookingId);

    void deleteByRecurringBooking_RecurringBookingId(String recurringBookingId);
}
