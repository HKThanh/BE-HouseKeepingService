package iuh.house_keeping_service_be.dtos.Admin.response;

import iuh.house_keeping_service_be.enums.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminProfileResponse {
    private String adminProfileId;
    private String fullName;
    private Boolean isMale;
    private String department;
    private String contactInfo;
    private LocalDate birthdate;
    private LocalDate hireDate;
    
    // Account information (without username/password)
    private AccountInfo account;
    
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
}
