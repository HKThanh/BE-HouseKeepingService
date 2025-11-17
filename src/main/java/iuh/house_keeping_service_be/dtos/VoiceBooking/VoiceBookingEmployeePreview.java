package iuh.house_keeping_service_be.dtos.VoiceBooking;

import java.util.List;

/**
 * Employee preview data that indicates which staff will handle the booking.
 */
public record VoiceBookingEmployeePreview(
        String employeeId,
        String fullName,
        String avatar,
        String rating,
        Boolean hasWorkedWithCustomer,
        List<Integer> serviceIds,
        boolean autoAssigned
) {
}
