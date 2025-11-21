package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.models.AdditionalFee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AdditionalFeeRepository extends JpaRepository<AdditionalFee, String> {

    List<AdditionalFee> findByActiveTrue();

    Optional<AdditionalFee> findFirstBySystemSurchargeTrueAndActiveTrue();

    List<AdditionalFee> findAllByOrderByPriorityAscCreatedAtDesc();

    @Modifying
    @Query("update AdditionalFee f set f.active = false where f.systemSurcharge = true and f.active = true and f.id <> :id")
    void deactivateOtherSystemSurcharge(@Param("id") String id);
}
