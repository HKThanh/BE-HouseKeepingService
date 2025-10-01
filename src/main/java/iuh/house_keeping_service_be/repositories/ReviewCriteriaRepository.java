package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.models.ReviewCriteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewCriteriaRepository extends JpaRepository<ReviewCriteria, Integer> {
}