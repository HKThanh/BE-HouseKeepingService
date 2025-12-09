package iuh.house_keeping_service_be.dtos.Booking.internal;

import iuh.house_keeping_service_be.dtos.Booking.response.PromotionInfo;
import iuh.house_keeping_service_be.models.Promotion;
import java.math.BigDecimal;

/**
 * Result of applying a promotion to a booking amount.
 * Contains the final amount after discount, the discount amount, and promotion details.
 */
public record PromotionApplicationResult(
        BigDecimal finalAmount,
        BigDecimal discountAmount,
        Promotion promotion,
        PromotionInfo promotionInfo,
        String errorMessage
) {
    /**
     * Create a successful promotion application result.
     */
    public static PromotionApplicationResult success(BigDecimal finalAmount, BigDecimal discountAmount, 
                                                     Promotion promotion, PromotionInfo promotionInfo) {
        return new PromotionApplicationResult(finalAmount, discountAmount, promotion, promotionInfo, null);
    }
    
    /**
     * Create a result when no promotion is applied.
     */
    public static PromotionApplicationResult noPromotion(BigDecimal amount) {
        return new PromotionApplicationResult(amount, BigDecimal.ZERO, null, null, null);
    }
    
    /**
     * Create a result when promotion validation fails.
     */
    public static PromotionApplicationResult error(BigDecimal amount, String errorMessage) {
        return new PromotionApplicationResult(amount, BigDecimal.ZERO, null, null, errorMessage);
    }
    
    /**
     * Check if this result has a valid promotion applied.
     */
    public boolean hasPromotion() {
        return promotion != null && discountAmount.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * Check if there was an error applying the promotion.
     */
    public boolean hasError() {
        return errorMessage != null && !errorMessage.isEmpty();
    }
}
