package iuh.house_keeping_service_be.dtos.VoiceBooking;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service level preview information surfaced to the customer before confirmation.
 */
public record VoiceBookingPreviewServiceItem(
        Integer serviceId,
        String serviceName,
        Integer quantity,
        BigDecimal pricePerUnit,
        String formattedPricePerUnit,
        BigDecimal subTotal,
        String formattedSubTotal,
        List<Integer> selectedChoiceIds
) {
}
