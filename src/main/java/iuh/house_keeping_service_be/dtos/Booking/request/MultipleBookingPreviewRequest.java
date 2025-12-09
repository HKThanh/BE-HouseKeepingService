package iuh.house_keeping_service_be.dtos.Booking.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Request DTO for previewing multiple bookings with different time slots.
 * Same services/address but different booking times.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MultipleBookingPreviewRequest(
        // Optional: Admin can specify customerId to preview for a specific customer
        String customerId,
        
        String addressId,

        @Valid
        NewAddressRequest newAddress,

        // Multiple booking times to preview
        @NotEmpty(message = "Booking times cannot be empty")
        List<LocalDateTime> bookingTimes,

        @Size(max = 1000, message = "Note cannot exceed 1000 characters")
        String note,

        @Size(max = 255, message = "Title cannot exceed 255 characters")
        String title,

        @Size(max = 20, message = "Promo code cannot exceed 20 characters")
        String promoCode,

        @NotEmpty(message = "Booking details cannot be empty")
        @Valid
        List<BookingDetailRequest> bookingDetails,

        int paymentMethodId,

        List<String> additionalFeeIds
) {

        @AssertTrue(message = "Either addressId or newAddress must be provided")
        public boolean isAddressSelectionValid() {
                boolean hasAddressId = addressId != null && !addressId.isBlank();
                boolean hasNewAddress = newAddress != null;
                return (hasAddressId || hasNewAddress) && !(hasAddressId && hasNewAddress);
        }
}
