package iuh.house_keeping_service_be.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "address")
public class Address {
    @Id
    @Column(name = "address_id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "street_text", nullable = false, length = Integer.MAX_VALUE)
    private String streetText;

    @NotNull
    @Column(name = "city", nullable = false, length = Integer.MAX_VALUE)
    private String city;

    @Size(max = 20)
    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at")
    private Instant updatedAt;

}