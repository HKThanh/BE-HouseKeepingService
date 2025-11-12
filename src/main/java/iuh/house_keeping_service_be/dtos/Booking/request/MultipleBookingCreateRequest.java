package iuh.house_keeping_service_be.dtos.Booking.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

public record MultipleBookingCreateRequest(
        String addressId,

        @Valid
        NewAddressRequest newAddress,

        @NotEmpty(message = "Booking times cannot be empty")
        List<@NotNull(message = "Booking time cannot be null") 
             @Future(message = "Booking time must be in the future") 
             LocalDateTime> bookingTimes,

        @Size(max = 1000, message = "Note cannot exceed 1000 characters")
        String note,

        @Size(max = 255, message = "Title cannot exceed 255 characters")
        String title,

        List<@Size(max = 500, message = "Image URL cannot exceed 500 characters") String> imageUrls,

        @Size(max = 20, message = "Promo code cannot exceed 20 characters")
        String promoCode,

        @NotEmpty(message = "Booking details cannot be empty")
        @Valid
        List<BookingDetailRequest> bookingDetails,

        @Valid
        List<AssignmentRequest> assignments,

        int paymentMethodId
) {
        @AssertTrue(message = "Either addressId or newAddress must be provided")
        public boolean isAddressSelectionValid() {
                boolean hasAddressId = addressId != null && !addressId.isBlank();
                boolean hasNewAddress = newAddress != null;
                return (hasAddressId || hasNewAddress) && !(hasAddressId && hasNewAddress);
        }
}
