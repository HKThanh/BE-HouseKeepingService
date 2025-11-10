package iuh.house_keeping_service_be.services.RecommendationService.model;

import java.util.Collections;
import java.util.Map;

public record RecommendationModelDefinition(
        String version,
        double bias,
        Map<String, Double> weights
) {

    public RecommendationModelDefinition {
        version = (version == null || version.isBlank()) ? "fallback-v1" : version;
        weights = weights == null ? Map.of() : Collections.unmodifiableMap(weights);
    }

    public static RecommendationModelDefinition fallback() {
        return new RecommendationModelDefinition(
                "fallback-v1",
                0.25,
                Map.of(
                        "rating", 0.45,
                        "completedJobs", 0.25,
                        "locationAffinity", 0.2,
                        "skillVersatility", 0.1,
                        "bookingTimeFit", 0.05
                )
        );
    }
}
