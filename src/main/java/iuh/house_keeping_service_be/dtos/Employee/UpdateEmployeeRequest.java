package iuh.house_keeping_service_be.dtos.Employee;

import iuh.house_keeping_service_be.enums.EmployeeStatus;
import iuh.house_keeping_service_be.enums.Rating;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class UpdateEmployeeRequest {
    private String fullName;
    private Boolean isMale;
    private String email;
    private String phoneNumber;
    private LocalDate birthdate;
    private LocalDate hiredDate;
    private List<String> skills;
    private String bio;
    private String avatar;
    private EmployeeStatus employeeStatus;
    private Rating rating;
}