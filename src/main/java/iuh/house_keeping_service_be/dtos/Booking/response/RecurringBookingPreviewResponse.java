package iuh.house_keeping_service_be.dtos.Booking.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import iuh.house_keeping_service_be.enums.RecurrenceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Response DTO for recurring booking preview.
 * Shows pricing breakdown and planned booking times for a recurring schedule.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecurringBookingPreviewResponse {
    
    // Overall validation status
    private boolean valid;
    private List<String> errors;
    
    // ===== SHARED SERVICE INFO (same for all occurrences) =====
    // Service items with pricing details (shared across all occurrences)
    private List<ServicePreviewItem> serviceItems;
    private int totalServices;
    private int totalQuantityPerOccurrence;
    
    // Subtotal per occurrence (before discount and fees)
    private BigDecimal subtotalPerOccurrence;
    private String formattedSubtotalPerOccurrence;
    
    // Fees breakdown (shared, applied per occurrence)
    private List<FeeBreakdownResponse> feeBreakdowns;
    private BigDecimal totalFeesPerOccurrence;
    private String formattedTotalFeesPerOccurrence;
    
    // Discount per occurrence
    private BigDecimal discountPerOccurrence;
    private String formattedDiscountPerOccurrence;
    
    // Recurrence info
    private RecurrenceType recurrenceType;
    private List<Integer> recurrenceDays;
    private String recurrenceDescription; // e.g., "Every Monday, Wednesday, Friday at 09:00"
    
    @JsonFormat(pattern = "HH:mm")
    private LocalTime bookingTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    
    // Planned booking times (limited by maxPreviewOccurrences)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private List<LocalDateTime> plannedBookingTimes;
    private int occurrenceCount;
    private int maxPreviewOccurrences;
    private boolean hasMoreOccurrences; // true if total occurrences exceed maxPreviewOccurrences
    
    // Single booking preview (detailed pricing for one occurrence) - kept for backward compatibility
    private BookingPreviewResponse singleBookingPreview;
    
    // Per-occurrence pricing (same for all occurrences since same services)
    private BigDecimal pricePerOccurrence;
    private String formattedPricePerOccurrence;
    
    // Total estimated pricing (pricePerOccurrence * occurrenceCount)
    private BigDecimal totalEstimatedPrice;
    private String formattedTotalEstimatedPrice;
    
    // Duration per occurrence
    private String estimatedDurationPerOccurrence;
    private int recommendedStaff;
    
    // Customer and address info (from single booking preview)
    private String customerId;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private CustomerAddressInfo addressInfo;
    private boolean usingNewAddress;
    
    // Payment method info
    private Integer paymentMethodId;
    private String paymentMethodName;
    
    // Promo code info
    private String promoCode;
    private boolean promoAppliedToAll;
    private PromotionInfo promotionInfo;
    
    /**
     * Create an error response with validation errors.
     */
    public static RecurringBookingPreviewResponse error(List<String> errors) {
        return RecurringBookingPreviewResponse.builder()
                .valid(false)
                .errors(errors)
                .occurrenceCount(0)
                .plannedBookingTimes(List.of())
                .build();
    }
    
    /**
     * Generate recurrence description text.
     */
    public static String generateRecurrenceDescription(RecurrenceType type, List<Integer> days, LocalTime time) {
        if (type == null || days == null || days.isEmpty()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        if (type == RecurrenceType.WEEKLY) {
            sb.append("Hàng tuần vào ");
            List<String> dayNames = days.stream()
                    .sorted()
                    .map(RecurringBookingPreviewResponse::getDayOfWeekName)
                    .toList();
            sb.append(String.join(", ", dayNames));
        } else if (type == RecurrenceType.MONTHLY) {
            sb.append("Hàng tháng vào ngày ");
            List<String> dayStrings = days.stream()
                    .sorted()
                    .map(String::valueOf)
                    .toList();
            sb.append(String.join(", ", dayStrings));
        }
        
        if (time != null) {
            sb.append(" lúc ").append(time.toString());
        }
        
        return sb.toString();
    }
    
    private static String getDayOfWeekName(int dayOfWeek) {
        return switch (dayOfWeek) {
            case 1 -> "Thứ Hai";
            case 2 -> "Thứ Ba";
            case 3 -> "Thứ Tư";
            case 4 -> "Thứ Năm";
            case 5 -> "Thứ Sáu";
            case 6 -> "Thứ Bảy";
            case 7 -> "Chủ Nhật";
            default -> "Ngày " + dayOfWeek;
        };
    }
}
