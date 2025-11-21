package iuh.house_keeping_service_be.dtos.Service;

import java.math.BigDecimal;

public record ServiceOptionChoiceData(
    Integer choiceId,
    String choiceName,
    Integer displayOrder,
    Boolean isDefault,
    BigDecimal priceAdjustment,
    Integer staffAdjustment,
    BigDecimal durationAdjustmentHours,
    String formattedPriceAdjustment
) {}