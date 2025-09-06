package iuh.house_keeping_service_be.dtos.Booking.response;

import iuh.house_keeping_service_be.enums.DiscountType;
import java.math.BigDecimal;

public record PromotionInfo(
        Integer promotionId,
        String promoCode,
        String description,
        DiscountType discountType,
        BigDecimal discountValue,
        BigDecimal maxDiscountAmount
) {}