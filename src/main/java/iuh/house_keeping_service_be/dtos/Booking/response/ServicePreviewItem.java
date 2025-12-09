package iuh.house_keeping_service_be.dtos.Booking.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO representing a service item in booking preview with pricing details.
 */
public record ServicePreviewItem(
        Integer serviceId,
        String serviceName,
        String serviceDescription,
        String iconUrl,
        Integer quantity,
        String unit,
        BigDecimal unitPrice,
        String formattedUnitPrice,
        BigDecimal subTotal,
        String formattedSubTotal,
        List<ChoicePreviewItem> selectedChoices,
        String estimatedDuration,
        Integer recommendedStaff
) {}
