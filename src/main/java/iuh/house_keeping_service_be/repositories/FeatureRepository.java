package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.models.Feature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeatureRepository extends JpaRepository<Feature, Integer> {

    @Query("SELECT f FROM Feature f ORDER BY f.module, f.featureName")
    List<Feature> findAllOrderByModuleAndName();

    @Query("SELECT f FROM Feature f WHERE f.module = :module ORDER BY f.featureName")
    List<Feature> findByModuleOrderByFeatureName(@Param("module") String module);

    @Query("SELECT DISTINCT f.module FROM Feature f ORDER BY f.module")
    List<String> findAllModules();
}