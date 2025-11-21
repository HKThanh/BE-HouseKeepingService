package iuh.house_keeping_service_be.dtos.Booking.response;

import java.math.BigDecimal;
import java.util.List;

import iuh.house_keeping_service_be.dtos.Booking.response.FeeBreakdownResponse;

public record BookingHistoryResponse(
        String bookingId,
        String bookingCode,
        String customerId,
        String customerName,
        CustomerAddressInfo address,
        String bookingTime,
        String note,
        BigDecimal totalAmount,
        String formattedTotalAmount,
        String status,
        PromotionInfo promotion,
        PaymentInfo payment,
        String title,
        List<String> imageUrls,
        Boolean isVerified,
        List<EmployeeInfo> assignedEmployees,
        List<ServiceInfo> services,
        BigDecimal baseAmount,
        BigDecimal totalFees,
        List<FeeBreakdownResponse> fees
) {
}
