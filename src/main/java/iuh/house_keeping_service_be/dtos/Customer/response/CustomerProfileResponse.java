package iuh.house_keeping_service_be.dtos.Customer.response;

import iuh.house_keeping_service_be.enums.AccountStatus;
import iuh.house_keeping_service_be.enums.Rating;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerProfileResponse {
    private String customerId;
    private String fullName;
    private String avatar;
    private Boolean isMale;
    private String email;
    private LocalDate birthdate;
    private Rating rating;
    private Integer vipLevel;
    
    // Account information (without username/password)
    private AccountInfo account;
    
    // Address information
    private List<AddressInfo> addresses;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountInfo {
        private String accountId;
        private String phoneNumber;
        private AccountStatus status;
        private Boolean isPhoneVerified;
        private LocalDateTime lastLogin;
        private List<String> roles;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressInfo {
        private String addressId;
        private String fullAddress;
        private String ward;
        private String city;
        private BigDecimal latitude;
        private BigDecimal longitude;
        private Boolean isDefault;
    }
}
