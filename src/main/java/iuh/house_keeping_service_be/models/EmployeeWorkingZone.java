package iuh.house_keeping_service_be.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "employee_working_zones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(EmployeeWorkingZoneId.class)
public class EmployeeWorkingZone {
    @Id
    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Id
    @Column(name = "district", length = 100)
    private String district;

    @Id
    @Column(name = "city", length = 100)
    private String city;
}