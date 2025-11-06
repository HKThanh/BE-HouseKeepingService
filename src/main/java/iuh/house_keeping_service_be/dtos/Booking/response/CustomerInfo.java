package iuh.house_keeping_service_be.dtos.Booking.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerInfo {
    private String customerId;
    private String fullName;
    private String avatar;
    private String email;
    private String phoneNumber;
    private Boolean isMale;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthdate;
    
    private String rating;
    private Integer vipLevel;
}
