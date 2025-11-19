package iuh.house_keeping_service_be.dtos.Employee.response;

import iuh.house_keeping_service_be.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeBookingStatisticsByStatusResponse {
    private String timeUnit; // DAY, WEEK, MONTH, YEAR
    private String startDate;
    private String endDate;
    private long totalBookings;
    private Map<BookingStatus, Long> countByStatus;
}
