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
 * Response DTO for booking preview/quote endpoint.
 * Contains all pricing information formatted like an invoice for FE display.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingPreviewResponse {
    
    // Validation status
    private boolean valid;
    private List<String> errors;
    
    // Customer information
    private String customerId;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    
    // Address information
    private CustomerAddressInfo addressInfo;
    private boolean usingNewAddress;
    
    // Booking time (optional for preview)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime bookingTime;
    
    // Service items (itemized like invoice)
    private List<ServicePreviewItem> serviceItems;
    private int totalServices;
    private int totalQuantity;
    
    // Pricing breakdown
    private BigDecimal subtotal;                    // Sum of all service subtotals (before discount)
    private String formattedSubtotal;
    
    // Promotion/Discount
    private PromotionInfo promotionInfo;
    private BigDecimal discountAmount;
    private String formattedDiscountAmount;
    
    // Amount after discount
    private BigDecimal totalAfterDiscount;
    private String formattedTotalAfterDiscount;
    
    // Fees breakdown
    private List<FeeBreakdownResponse> feeBreakdowns;
    private BigDecimal totalFees;
    private String formattedTotalFees;
    
    // Grand total
    private BigDecimal grandTotal;
    private String formattedGrandTotal;
    
    // Additional info
    private String estimatedDuration;
    private int recommendedStaff;
    private String note;
    
    // Payment method
    private Integer paymentMethodId;
    private String paymentMethodName;
    
    /**
     * Create a successful preview response.
     */
    public static BookingPreviewResponse success(BookingPreviewResponseBuilder builder) {
        return builder.valid(true).errors(List.of()).build();
    }
    
    /**
     * Create an error preview response with validation errors.
     */
    public static BookingPreviewResponse error(List<String> errors) {
        return BookingPreviewResponse.builder()
                .valid(false)
                .errors(errors)
                .build();
    }
}
