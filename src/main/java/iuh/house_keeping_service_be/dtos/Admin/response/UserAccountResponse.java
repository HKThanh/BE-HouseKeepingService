package iuh.house_keeping_service_be.dtos.Admin.response;

import iuh.house_keeping_service_be.enums.AccountStatus;
import iuh.house_keeping_service_be.enums.EmployeeStatus;
import iuh.house_keeping_service_be.enums.Rating;
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
public class UserAccountResponse {
    private String userType; // "CUSTOMER" or "EMPLOYEE"
    private AccountInfo account;
    private UserProfileInfo profile;

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
    public static class UserProfileInfo {
        private String id; // customerId or employeeId
        private String avatar;
        private String fullName;
        private Boolean isMale;
        private String email;
        private Boolean isEmailVerified;
        private LocalDate birthdate;
        private Rating rating;
        
        // Employee specific fields
        private LocalDate hiredDate;
        private List<String> skills;
        private String bio;
        private EmployeeStatus employeeStatus;
        private Integer vipLevel; // Customer specific field
    }
}
