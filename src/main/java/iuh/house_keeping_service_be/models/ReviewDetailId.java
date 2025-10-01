package iuh.house_keeping_service_be.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ReviewDetailId implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "review_id")
    private Integer reviewId;

    @Column(name = "criteria_id")
    private Integer criteriaId;
}