package iuh.house_keeping_service_be.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "review_criteria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCriteria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "criteria_id")
    private Integer criteriaId;

    @Column(name = "criteria_name", nullable = false, unique = true, length = 100)
    private String criteriaName;
}