package iuh.house_keeping_service_be.services.BookingService;

import iuh.house_keeping_service_be.dtos.Booking.request.BookingCreateRequest;
import iuh.house_keeping_service_be.dtos.Booking.request.ConvertBookingToPostRequest;
import iuh.house_keeping_service_be.dtos.Booking.request.BookingVerificationRequest;
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
    
    // Convert booking to post (when no employee is selected)
    BookingResponse convertBookingToPost(String bookingId, ConvertBookingToPostRequest request);
    
    // Get unverified bookings for admin review
    Page<BookingResponse> getUnverifiedBookings(Pageable pageable);
    
    // Admin verify/reject booking post
    BookingResponse verifyBooking(String bookingId, BookingVerificationRequest request);
    
    // Customer cancel booking
    BookingResponse cancelBooking(String bookingId, String customerId, String reason);
    
    // Get verified bookings that are still awaiting employee (for Employee and Admin)
    Page<BookingResponse> getVerifiedAwaitingEmployeeBookings(Pageable pageable);
}