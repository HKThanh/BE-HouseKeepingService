package iuh.house_keeping_service_be.dtos.VoiceBooking;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Preview payload that will be shown to customers before persisting the booking.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record VoiceBookingPreview(
        String addressId,
        String fullAddress,
        String ward,
        String city,
        LocalDateTime bookingTime,
        String note,
        String promoCode,
        Integer paymentMethodId,
        BigDecimal totalAmount,
        String formattedTotalAmount,
        List<VoiceBookingPreviewServiceItem> services,
        List<VoiceBookingEmployeePreview> employees,
        boolean autoAssignedEmployees
) {
}
