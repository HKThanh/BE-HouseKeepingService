package iuh.house_keeping_service_be.dtos.Booking.response;

public record CustomerAddressInfo(
        String addressId,
        String fullAddress,
        String ward,
        String city,
        Double latitude,
        Double longitude,
        Boolean isDefault
) {}