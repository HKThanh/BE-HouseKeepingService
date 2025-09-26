package iuh.house_keeping_service_be.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "employee_working_zones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeWorkingZone {

    @EmbeddedId
    private EmployeeWorkingZoneId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("employeeId")
    @JoinColumn(name = "employee_id")
    @JsonIgnore
    private Employee employee;

    @Column(name = "ward", insertable = false, updatable = false)
    private String ward;

    @Column(name = "city", insertable = false, updatable = false)
    private String city;
}