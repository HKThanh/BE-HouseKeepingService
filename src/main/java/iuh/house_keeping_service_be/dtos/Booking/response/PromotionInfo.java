package iuh.house_keeping_service_be.dtos.Booking.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import iuh.house_keeping_service_be.enums.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PromotionInfo(
        String promotionId,
        String promoCode,
        String name,
        String description,
        DiscountType discountType,
        BigDecimal discountValue,
        BigDecimal minimumOrderValue,
        BigDecimal maximumDiscountAmount,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime startDate,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime endDate,

        Integer usageLimit,
        Integer usedCount,
        Boolean isActive
) {}