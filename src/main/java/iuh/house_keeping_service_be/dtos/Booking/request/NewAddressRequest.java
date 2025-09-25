package iuh.house_keeping_service_be.dtos.Booking.request;

import jakarta.validation.constraints.NotBlank;

public record NewAddressRequest(
        @NotBlank(message = "Customer ID is required for new address")
        String customerId,

        @NotBlank(message = "Full address is required")
        String fullAddress,

        @NotBlank(message = "Ward is required")
        String ward,

        @NotBlank(message = "City is required")
        String city,

        Double latitude,
        Double longitude
) {}