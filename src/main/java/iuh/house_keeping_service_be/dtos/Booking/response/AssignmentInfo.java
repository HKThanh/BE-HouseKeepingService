package iuh.house_keeping_service_be.dtos.Booking.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import iuh.house_keeping_service_be.dtos.BookingMedia.response.BookingMediaResponse;
import iuh.house_keeping_service_be.enums.AssignmentStatus;

import java.time.LocalDateTime;
import java.util.List;

public record AssignmentInfo(
        String assignmentId,
        EmployeeInfo employee,
        AssignmentStatus status,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime checkInTime,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime checkOutTime,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updatedAt,
        
        Double checkInLatitude,
        Double checkInLongitude,
        Double checkOutLatitude,
        Double checkOutLongitude,
        
        List<BookingMediaResponse> media
) {}