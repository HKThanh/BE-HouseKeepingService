package iuh.house_keeping_service_be.dtos.Booking.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SelectedChoiceInfo {
    private Integer choiceId;
    private String choiceName;
    private String optionName;
    private BigDecimal priceAdjustment;
    private String formattedPriceAdjustment;
    private String adjustmentType; // FIXED, PERCENTAGE
    private String description;
}
