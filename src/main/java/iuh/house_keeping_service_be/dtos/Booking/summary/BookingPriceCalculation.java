package iuh.house_keeping_service_be.dtos.Booking.summary;

import java.math.BigDecimal;
import java.util.List;

public record BookingPriceCalculation(
        BigDecimal subtotal,
        BigDecimal discountAmount,
        BigDecimal totalAmount,
        String promoCode,
        List<PriceBreakdown> breakdown
) {}