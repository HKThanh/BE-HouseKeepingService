package iuh.house_keeping_service_be.dtos.Employee.response;

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
public class EmployeeProfileResponse {
    private String employeeId;
    private String fullName;
    private String avatar;
    private Boolean isMale;
    private String email;
    private LocalDate birthdate;
    private LocalDate hiredDate;
    private List<String> skills;
    private String bio;
    private Rating rating;
    private EmployeeStatus employeeStatus;
    private List<WorkingZoneInfo> workingZones;
    
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
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkingZoneInfo {
        private String ward;
        private String city;
    }
}
