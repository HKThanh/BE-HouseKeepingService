package iuh.house_keeping_service_be.dtos.Statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceBookingStatisticsResponse {
    private String period; // "DAY", "WEEK", "MONTH", "QUARTER", "YEAR"
    private String startDate;
    private String endDate;
    private Long totalBookings;
    private List<ServiceStatistic> serviceStatistics;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceStatistic {
        private String serviceId;
        private String serviceName;
        private Long bookingCount;
        private Double percentage;
        private Integer rank;
    }
}
