package iuh.house_keeping_service_be.dtos.EmployeeSchedule;

import java.util.Map;

/**
 * Metadata describing how the machine learning model scored a candidate.
 */
public record RecommendationMetadata(
        Double score,
        String modelVersion,
        Map<String, Double> featureSignals
) {
}
