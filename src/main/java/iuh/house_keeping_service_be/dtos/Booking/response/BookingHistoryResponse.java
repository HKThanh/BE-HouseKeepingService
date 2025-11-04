package iuh.house_keeping_service_be.dtos.Booking.response;

import java.util.List;

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
        PaymentInfo payment,
        String title,
        String imageUrl,
        Boolean isVerified,
        List<EmployeeInfo> assignedEmployees,
        List<ServiceInfo> services
) {
}
