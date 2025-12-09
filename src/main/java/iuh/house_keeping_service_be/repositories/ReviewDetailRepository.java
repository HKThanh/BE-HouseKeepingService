package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.models.ReviewDetail;
import iuh.house_keeping_service_be.models.ReviewDetailId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewDetailRepository extends JpaRepository<ReviewDetail, ReviewDetailId> {

    @Query("SELECT AVG(rd.rating) FROM ReviewDetail rd WHERE rd.review.employee.employeeId = :employeeId")
    Double findAverageRatingByEmployeeId(@Param("employeeId") String employeeId);

    @Query("SELECT CAST(ROUND(rd.rating) AS int) as star, COUNT(rd) as count " +
           "FROM ReviewDetail rd " +
           "WHERE rd.review.employee.employeeId = :employeeId " +
           "GROUP BY CAST(ROUND(rd.rating) AS int) " +
           "ORDER BY star")
    List<Object[]> findRatingDistributionByEmployeeId(@Param("employeeId") String employeeId);
}