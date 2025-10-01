package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.models.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {

    boolean existsByBooking_BookingIdAndEmployee_EmployeeId(String bookingId, String employeeId);

    @EntityGraph(attributePaths = {"details", "details.criteria"})
    List<Review> findByEmployee_EmployeeIdOrderByCreatedAtDesc(String employeeId);

    @EntityGraph(attributePaths = {"details", "details.criteria"})
    Page<Review> findByEmployee_EmployeeId(String employeeId, Pageable pageable);

    long countByEmployee_EmployeeId(String employeeId);
}