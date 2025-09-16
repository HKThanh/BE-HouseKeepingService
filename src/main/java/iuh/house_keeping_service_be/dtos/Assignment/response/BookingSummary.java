package iuh.house_keeping_service_be.dtos.Assignment.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BookingSummary(
        String bookingDetailId,
        String bookingCode,
        String serviceName,
        String serviceAddress,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime bookingTime,
        BigDecimal estimatedDurationHours,
        Integer requiredEmployees
) {}