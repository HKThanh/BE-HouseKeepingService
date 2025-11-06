package iuh.house_keeping_service_be.services.BookingService;

import iuh.house_keeping_service_be.dtos.Booking.request.BookingCreateRequest;
import iuh.house_keeping_service_be.dtos.Booking.request.ConvertBookingToPostRequest;
import iuh.house_keeping_service_be.dtos.Booking.request.BookingVerificationRequest;
import iuh.house_keeping_service_be.dtos.Booking.request.UpdateBookingStatusRequest;
import iuh.house_keeping_service_be.dtos.Booking.response.BookingHistoryResponse;
import iuh.house_keeping_service_be.dtos.Booking.response.BookingResponse;
import iuh.house_keeping_service_be.dtos.Booking.summary.BookingCreationSummary;
import iuh.house_keeping_service_be.dtos.Booking.internal.BookingValidationResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface BookingService {
    BookingCreationSummary createBooking(BookingCreateRequest request);
    BookingResponse getBookingDetails(String bookingId);
    BookingValidationResult validateBooking(BookingCreateRequest request);
    Page<BookingHistoryResponse> getBookingsByCustomerId(String customerId, Pageable pageable);
    Page<BookingHistoryResponse> getBookingsByCustomerId(String customerId, LocalDateTime fromDate, Pageable pageable);
    
    // Convert booking to post (when no employee is selected)
    BookingResponse convertBookingToPost(String bookingId, ConvertBookingToPostRequest request);
    
    // Get unverified bookings for admin review
    Page<BookingResponse> getUnverifiedBookings(Pageable pageable);
    Page<BookingResponse> getUnverifiedBookings(LocalDateTime fromDate, Pageable pageable);
    
    // Admin verify/reject booking post
    BookingResponse verifyBooking(String bookingId, BookingVerificationRequest request);
    
    // Admin update booking status
    BookingResponse updateBookingStatus(String bookingId, UpdateBookingStatusRequest request);
    
    // Customer cancel booking
    BookingResponse cancelBooking(String bookingId, String customerId, String reason);
    
    // Get verified bookings that are still awaiting employee (for Employee and Admin)
    Page<BookingResponse> getVerifiedAwaitingEmployeeBookings(String employeeId, boolean matchEmployeeZones, Pageable pageable);
    Page<BookingResponse> getVerifiedAwaitingEmployeeBookings(String employeeId, boolean matchEmployeeZones, LocalDateTime fromDate, Pageable pageable);
    
    // Get all bookings sorted by booking time descending (for Admin only)
    Page<BookingResponse> getAllBookingsSortedByBookingTime(Pageable pageable);
    Page<BookingResponse> getAllBookingsSortedByBookingTime(LocalDateTime fromDate, Pageable pageable);
    
    // Get bookings where employee has assignment, sorted by booking time
    Page<BookingResponse> getBookingsByEmployeeId(String employeeId, Pageable pageable);
    Page<BookingResponse> getBookingsByEmployeeId(String employeeId, LocalDateTime fromDate, Pageable pageable);
}
