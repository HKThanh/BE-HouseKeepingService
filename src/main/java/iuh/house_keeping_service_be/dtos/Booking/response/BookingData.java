package iuh.house_keeping_service_be.dtos.Booking.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import iuh.house_keeping_service_be.dtos.Booking.response.FeeBreakdownResponse;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingData {
    private String bookingId;
    private String bookingCode;
    
    // Customer information
    private String customerId;
    private String customerName;
    private CustomerInfo customer;
    
    private CustomerAddressInfo address;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime bookingTime;
    
    private String note;
    private BigDecimal totalAmount;
    private String formattedTotalAmount;
    private BigDecimal baseAmount;
    private BigDecimal totalFees;
    private List<FeeBreakdownResponse> fees;
    private String status;
    
    private String title;
    private List<String> imageUrls;
    private Boolean isPost;
    private Boolean isVerified;
    private String adminComment;
    
    private PromotionInfo promotion;
    private List<BookingDetailInfo> bookingDetails;
    private PaymentInfo payment;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}
