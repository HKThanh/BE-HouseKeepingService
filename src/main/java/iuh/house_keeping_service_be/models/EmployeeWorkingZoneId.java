package iuh.house_keeping_service_be.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeWorkingZoneId implements Serializable {

    @Column(name = "employee_id")
    private String employeeId;

    @Column(name = "ward")
    private String ward;

    @Column(name = "city")
    private String city;
}