package iuh.house_keeping_service_be.dtos.Booking.response;

import java.math.BigDecimal;

/**
 * DTO representing a selected choice option for booking preview display.
 */
public record ChoicePreviewItem(
        Integer choiceId,
        String choiceName,
        String optionName,
        BigDecimal priceAdjustment,
        String formattedPriceAdjustment
) {}
