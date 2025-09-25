package iuh.house_keeping_service_be.dtos.Booking.response;

public record BookingHistoryResponse(
        String bookingId,
        String bookingCode,
        String customerId,
        String customerName,
        CustomerAddressInfo address,
        String bookingTime,
        String note,
        String formattedTotalAmount,
        String status,
        PromotionInfo promotion,
        PaymentInfo payment
) {
}
