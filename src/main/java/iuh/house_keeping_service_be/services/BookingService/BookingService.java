package iuh.house_keeping_service_be.services.BookingService;

import iuh.house_keeping_service_be.dtos.Booking.request.BookingCreateRequest;
import iuh.house_keeping_service_be.dtos.Booking.response.BookingHistoryResponse;
import iuh.house_keeping_service_be.dtos.Booking.response.BookingResponse;
import iuh.house_keeping_service_be.dtos.Booking.summary.BookingCreationSummary;
import iuh.house_keeping_service_be.dtos.Booking.internal.BookingValidationResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookingService {
    BookingCreationSummary createBooking(BookingCreateRequest request);
    BookingResponse getBookingDetails(String bookingId);
    BookingValidationResult validateBooking(BookingCreateRequest request);
    Page<BookingHistoryResponse> getBookingsByCustomerId(String customerId, Pageable pageable);
}