package iuh.house_keeping_service_be.dtos.Booking.summary;

import iuh.house_keeping_service_be.dtos.Booking.response.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingCreationSummary {
    private String bookingId;
    private String bookingCode;
    private String status;
    private BigDecimal totalAmount;
    private String formattedTotalAmount;
    private LocalDateTime bookingTime;
    private LocalDateTime createdAt;
    
    // Booking post fields
    private String title;
    private List<String> imageUrls;
    private Boolean isVerified;
    private String adminComment;
    
    // Related information
    private CustomerAddressInfo customerInfo;
    private List<BookingDetailInfo> serviceDetails;
    private PaymentInfo paymentInfo;
    private PromotionInfo promotionApplied;
    private List<EmployeeInfo> assignedEmployees;
    
    // Additional summary information
    private int totalServices;
    private int totalEmployees;
    private String estimatedDuration;
    private boolean hasPromotion;
    private boolean hasAutoAssignedEmployees;
    
    // Factory method
    public static BookingCreationSummary from(String bookingId, String bookingCode, String status,
                                             BigDecimal totalAmount, String formattedTotalAmount,
                                             LocalDateTime bookingTime, LocalDateTime createdAt) {
        return BookingCreationSummary.builder()
            .bookingId(bookingId)
            .bookingCode(bookingCode)
            .status(status)
            .totalAmount(totalAmount)
            .formattedTotalAmount(formattedTotalAmount)
            .bookingTime(bookingTime)
            .createdAt(createdAt)
            .build();
    }
    
    public void calculateSummaryFields() {
        this.totalServices = serviceDetails != null ? serviceDetails.size() : 0;
        this.totalEmployees = assignedEmployees != null ? assignedEmployees.size() : 0;
        this.hasPromotion = promotionApplied != null;
        
        // Calculate estimated duration if service details are available
        if (serviceDetails != null) {
            int totalMinutes = serviceDetails.stream()
                .mapToInt(detail -> {
                    // Extract hours from duration and convert to minutes
                    try {
                        return Integer.parseInt(detail.getDuration().split(" ")[0]) * 60;
                    } catch (Exception e) {
                        return 0;
                    }
                })
                .sum();
            this.estimatedDuration = (totalMinutes / 60) + " giờ " + (totalMinutes % 60) + " phút";
        }
    }
}