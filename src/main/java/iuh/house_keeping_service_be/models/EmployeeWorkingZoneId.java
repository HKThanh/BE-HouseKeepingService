package iuh.house_keeping_service_be.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeWorkingZoneId implements Serializable {
    private String employee;
    private String district;
    private String city;
}