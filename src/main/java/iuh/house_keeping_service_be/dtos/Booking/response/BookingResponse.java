package iuh.house_keeping_service_be.dtos.Booking.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import iuh.house_keeping_service_be.enums.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record BookingResponse(
        String bookingId,
        String bookingCode,
        String customerId,
        CustomerAddressInfo address,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime bookingTime,

        String note,
        BigDecimal totalAmount,
        BookingStatus status,
        PromotionInfo promotion,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updatedAt,

        List<BookingDetailInfo> bookingDetails,
        List<PaymentInfo> payments
) {}