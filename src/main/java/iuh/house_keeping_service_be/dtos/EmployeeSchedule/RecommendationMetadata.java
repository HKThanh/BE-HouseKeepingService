package iuh.house_keeping_service_be.dtos.EmployeeSchedule;

/**
 * Metadata describing how the machine learning model scored a candidate.
 * Simplified to only return the score.
 */
public record RecommendationMetadata(
        Double score
) {
}
