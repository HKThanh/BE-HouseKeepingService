package iuh.house_keeping_service_be.dtos.Booking.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingDetailInfo {
    private String bookingDetailId;
    private ServiceInfo service;
    private Integer quantity;
    private BigDecimal pricePerUnit;
    private String formattedPricePerUnit;
    private BigDecimal subTotal;
    private String formattedSubTotal;
    private List<SelectedChoiceInfo> selectedChoices;
    private List<AssignmentInfo> assignments;
    private String duration;
    private String formattedDuration;
}