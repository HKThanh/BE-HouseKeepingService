package iuh.house_keeping_service_be.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "review_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDetail {

    @EmbeddedId
    private ReviewDetailId id = new ReviewDetailId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("reviewId")
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("criteriaId")
    @JoinColumn(name = "criteria_id", nullable = false)
    private ReviewCriteria criteria;

    @Column(name = "rating", nullable = false, precision = 2, scale = 1)
    private BigDecimal rating;
}