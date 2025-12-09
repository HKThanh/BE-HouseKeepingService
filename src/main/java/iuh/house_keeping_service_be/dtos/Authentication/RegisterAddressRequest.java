package iuh.house_keeping_service_be.dtos.Authentication;

import jakarta.validation.constraints.NotBlank;

public record RegisterAddressRequest(
        @NotBlank(message = "Full address is required")
        String fullAddress,

        @NotBlank(message = "Ward is required")
        String ward,

        @NotBlank(message = "City is required")
        String city,

        Double latitude,
        Double longitude
) {}
