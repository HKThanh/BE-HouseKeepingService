package iuh.house_keeping_service_be.dtos.Booking.response;

import java.math.BigDecimal;
import java.util.List;

public record BookingDetailInfo(
        String detailId,
        ServiceInfo service,
        Integer quantity,
        BigDecimal pricePerUnit,
        BigDecimal subTotal,
        List<AssignmentInfo> assignments
) {}