package iuh.house_keeping_service_be.dtos.RecurringBooking.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import iuh.house_keeping_service_be.dtos.Booking.response.BookingDetailInfo;
import iuh.house_keeping_service_be.dtos.Booking.response.CustomerAddressInfo;
import iuh.house_keeping_service_be.dtos.Booking.response.CustomerInfo;
import iuh.house_keeping_service_be.dtos.Booking.response.PromotionInfo;
import iuh.house_keeping_service_be.enums.RecurrenceType;
import iuh.house_keeping_service_be.enums.RecurringBookingStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Response DTO for recurring booking
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecurringBookingResponse {
    private String recurringBookingId;
    private String customerId;
    private String customerName;
    private CustomerInfo customer;
    private CustomerAddressInfo address;

    private RecurrenceType recurrenceType;
    private String recurrenceTypeDisplay; // "Hàng tuần" or "Hàng tháng"
    private List<Integer> recurrenceDays;
    private String recurrenceDaysDisplay; // "Thứ 2, Thứ 4, Thứ 6" or "Ngày 1, 15, 30"

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime bookingTime;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private String note;
    private String title;
    private PromotionInfo promotion;
    private List<BookingDetailInfo> recurringBookingDetails;
    private String assignedEmployeeId;
    private String assignedEmployeeName;

    private RecurringBookingStatus status;
    private String statusDisplay; // Vietnamese display

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime cancelledAt;
    private String cancellationReason;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    // Statistics
    private Integer totalGeneratedBookings;
    private Integer upcomingBookings;
}
