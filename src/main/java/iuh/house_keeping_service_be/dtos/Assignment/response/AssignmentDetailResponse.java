package iuh.house_keeping_service_be.dtos.Assignment.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import iuh.house_keeping_service_be.dtos.BookingMedia.response.BookingMediaResponse;
import iuh.house_keeping_service_be.enums.AssignmentStatus;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

public record AssignmentDetailResponse(
        String assignmentId,
        String bookingCode,
        String serviceName,
        String customerName,
        String customerPhone,
        String serviceAddress,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime bookingTime,

        BigDecimal estimatedDurationHours,
        BigDecimal pricePerUnit,
        Integer quantity,
        BigDecimal totalAmount,
        AssignmentStatus status,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime assignedAt,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime checkInTime,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime checkOutTime,

        String note,
        
        Double checkInLatitude,
        Double checkInLongitude,
        Double checkOutLatitude,
        Double checkOutLongitude,
        
        List<BookingMediaResponse> media
) {}