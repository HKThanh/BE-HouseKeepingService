package iuh.house_keeping_service_be.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    @Id
    @Column(name = "role_id")
    private Integer roleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_name", length = 20, unique = true, nullable = false)
    private RoleName roleName;

    public enum RoleName {
        ADMIN, EMPLOYEE, CUSTOMER
    }
}