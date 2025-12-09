package iuh.house_keeping_service_be.dtos.Booking.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Request DTO for booking preview/quote.
 * Similar to BookingCreateRequest but with optional customerId for Admin to preview on behalf of customers.
 * Booking time validation is relaxed for preview purposes.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BookingPreviewRequest(
        // Optional: Admin can specify customerId to preview for a specific customer
        String customerId,
        
        String addressId,

        @Valid
        NewAddressRequest newAddress,

        // Booking time is optional for preview (allows checking prices without specific time)
        LocalDateTime bookingTime,

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
