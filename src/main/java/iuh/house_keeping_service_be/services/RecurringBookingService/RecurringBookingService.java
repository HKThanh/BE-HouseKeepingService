package iuh.house_keeping_service_be.services.RecurringBookingService;

import iuh.house_keeping_service_be.dtos.RecurringBooking.request.RecurringBookingCancelRequest;
import iuh.house_keeping_service_be.dtos.RecurringBooking.request.RecurringBookingCreateRequest;
import iuh.house_keeping_service_be.dtos.RecurringBooking.response.RecurringBookingCreationSummary;
import iuh.house_keeping_service_be.dtos.RecurringBooking.response.RecurringBookingResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RecurringBookingService {
    
    /**
     * Create a recurring booking
     */
    RecurringBookingCreationSummary createRecurringBooking(RecurringBookingCreateRequest request, String customerId);
    
    /**
     * Cancel a recurring booking and delete all future bookings
     */
    RecurringBookingResponse cancelRecurringBooking(String recurringBookingId, String customerId, RecurringBookingCancelRequest request);
    
    /**
     * Get all recurring bookings by customer
     */
    Page<RecurringBookingResponse> getRecurringBookingsByCustomer(String customerId, Pageable pageable);
    
    /**
     * Get recurring booking details
     */
    RecurringBookingResponse getRecurringBookingDetails(String recurringBookingId, String customerId);
    
    /**
     * Generate bookings for active recurring bookings (called by scheduler)
     */
    void generateBookingsForActiveRecurringBookings();
}
