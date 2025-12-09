package iuh.house_keeping_service_be.dtos.Booking.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for multiple booking preview.
 * Contains individual preview for each booking time plus aggregated totals.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MultipleBookingPreviewResponse {
    
    // Overall validation status (true only if ALL bookings are valid)
    private boolean valid;
    private List<String> errors;
    
    // Number of bookings
    private int bookingCount;
    
    // ===== SHARED SERVICE INFO (same for all bookings) =====
    // Service items with pricing details (shared across all booking times)
    private List<ServicePreviewItem> serviceItems;
    private int totalServices;
    private int totalQuantityPerBooking;
    
    // Subtotal per booking (before discount and fees)
    private BigDecimal subtotalPerBooking;
    private String formattedSubtotalPerBooking;
    
    // Customer and address info (shared)
    private String customerId;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private CustomerAddressInfo addressInfo;
    private boolean usingNewAddress;
    
    // Payment method info (shared)
    private Integer paymentMethodId;
    private String paymentMethodName;
    
    // Fees breakdown (shared, applied per booking)
    private List<FeeBreakdownResponse> feeBreakdowns;
    private BigDecimal totalFeesPerBooking;
    private String formattedTotalFeesPerBooking;
    
    // Promotion info (shared, applied to all bookings)
    private PromotionInfo promotionInfo;
    private BigDecimal discountPerBooking;
    private String formattedDiscountPerBooking;
    
    // Price per booking after discount and fees
    private BigDecimal pricePerBooking;
    private String formattedPricePerBooking;
    
    // Estimated duration per booking
    private String estimatedDurationPerBooking;
    private int recommendedStaff;
    
    // ===== INDIVIDUAL BOOKING TIMES =====
    // Individual booking previews for each time slot (simplified, mainly for time-specific info)
    private List<BookingPreviewResponse> bookingPreviews;
    
    // ===== AGGREGATED TOTALS =====
    // Aggregated totals across all bookings
    private BigDecimal totalEstimatedPrice;
    private String formattedTotalEstimatedPrice;
    
    // Total duration across all bookings
    private String totalEstimatedDuration;
    
    // Promo code applied (same for all bookings)
    private String promoCode;
    private boolean promoAppliedToAll;
    
    // Summary of valid/invalid bookings
    private int validBookingsCount;
    private int invalidBookingsCount;
    
    // Booking times that passed validation
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private List<LocalDateTime> validBookingTimes;
    
    // Booking times that failed validation
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private List<LocalDateTime> invalidBookingTimes;
    
    /**
     * Create an error response with validation errors.
     */
    public static MultipleBookingPreviewResponse error(List<String> errors) {
        return MultipleBookingPreviewResponse.builder()
                .valid(false)
                .errors(errors)
                .bookingCount(0)
                .bookingPreviews(List.of())
                .validBookingsCount(0)
                .invalidBookingsCount(0)
                .validBookingTimes(List.of())
                .invalidBookingTimes(List.of())
                .build();
    }
}
