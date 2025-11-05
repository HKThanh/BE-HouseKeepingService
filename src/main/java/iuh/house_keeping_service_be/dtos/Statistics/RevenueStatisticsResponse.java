package iuh.house_keeping_service_be.dtos.Statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueStatisticsResponse {
    private String period; // "DAY", "WEEK", "MONTH", "QUARTER", "YEAR", "CUSTOM"
    private String startDate;
    private String endDate;
    private BigDecimal totalRevenue;
    private Long totalBookings;
    private BigDecimal averageRevenuePerBooking;
    private BigDecimal maxBookingAmount;
    private BigDecimal minBookingAmount;
}
