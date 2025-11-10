package iuh.house_keeping_service_be.services.RecommendationService.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import iuh.house_keeping_service_be.dtos.EmployeeSchedule.RecommendationMetadata;
import iuh.house_keeping_service_be.dtos.EmployeeSchedule.SuitableEmployeeRequest;
import iuh.house_keeping_service_be.dtos.EmployeeSchedule.SuitableEmployeeResponse;
import iuh.house_keeping_service_be.services.RecommendationService.EmployeeRecommendationService;
import iuh.house_keeping_service_be.services.RecommendationService.model.RecommendationModelDefinition;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MachineLearningEmployeeRecommendationService implements EmployeeRecommendationService {

    private static final Map<String, Double> RATING_LOOKUP = Map.of(
            "HIGHEST", 1.0,
            "HIGH", 0.85,
            "MEDIUM", 0.65,
            "LOW", 0.4,
            "LOWEST", 0.2
    );

    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;

    @Value("${ml.recommendation.employee.enabled:true}")
    private boolean recommendationEnabled;

    @Value("${ml.recommendation.employee.model-path:classpath:ml/employee_recommendation_model.json}")
    private String modelPath;

    private RecommendationModelDefinition modelDefinition;

    @PostConstruct
    public void init() {
        this.modelDefinition = loadModelDefinition();
    }

    @Override
    public List<SuitableEmployeeResponse> recommend(SuitableEmployeeRequest request,
                                                    List<SuitableEmployeeResponse> candidates) {
        if (!recommendationEnabled || candidates == null || candidates.isEmpty()) {
            return candidates;
        }

        RecommendationModelDefinition activeModel =
                modelDefinition != null ? modelDefinition : RecommendationModelDefinition.fallback();

        return candidates.stream()
                .map(candidate -> scoreCandidate(candidate, request, activeModel))
                .sorted(Comparator.comparingDouble(this::extractScore).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public String getModelVersion() {
        if (!recommendationEnabled) {
            return "disabled";
        }
        return modelDefinition != null ? modelDefinition.version() : RecommendationModelDefinition.fallback().version();
    }

    private RecommendationModelDefinition loadModelDefinition() {
        Resource resource = resourceLoader.getResource(modelPath);
        if (!resource.exists()) {
            log.warn("Recommendation model resource {} not found, using fallback definition", modelPath);
            return RecommendationModelDefinition.fallback();
        }

        try (InputStream inputStream = resource.getInputStream()) {
            RecommendationModelDefinition parsed =
                    objectMapper.readValue(inputStream, RecommendationModelDefinition.class);
            log.info("Loaded employee recommendation model {} from {}", parsed.version(), modelPath);
            return parsed;
        } catch (IOException ex) {
            log.error("Could not load recommendation model from {}: {}", modelPath, ex.getMessage());
            return RecommendationModelDefinition.fallback();
        }
    }

    private SuitableEmployeeResponse scoreCandidate(SuitableEmployeeResponse candidate,
                                                    SuitableEmployeeRequest request,
                                                    RecommendationModelDefinition model) {

        Map<String, Double> featureVector = buildFeatureVector(candidate, request);
        double rawScore = model.bias();

        for (Map.Entry<String, Double> weightEntry : model.weights().entrySet()) {
            double featureValue = featureVector.getOrDefault(weightEntry.getKey(), 0.0d);
            rawScore += featureValue * weightEntry.getValue();
        }

        double normalizedScore = roundToThreeDecimals(sigmoid(rawScore));
        RecommendationMetadata metadata = new RecommendationMetadata(
                normalizedScore,
                model.version(),
                Collections.unmodifiableMap(new LinkedHashMap<>(featureVector))
        );

        return new SuitableEmployeeResponse(
                candidate.employeeId(),
                candidate.fullName(),
                candidate.avatar(),
                candidate.skills(),
                candidate.rating(),
                candidate.status(),
                candidate.workingWards(),
                candidate.workingCity(),
                candidate.completedJobs(),
                metadata
        );
    }

    private Map<String, Double> buildFeatureVector(SuitableEmployeeResponse candidate,
                                                   SuitableEmployeeRequest request) {
        Map<String, Double> features = new LinkedHashMap<>();
        features.put("rating", normalizeRating(candidate.rating()));
        features.put("completedJobs", normalizeCompletedJobs(candidate.completedJobs()));
        features.put("locationAffinity", computeLocationAffinity(request, candidate));
        features.put("skillVersatility", normalizeSkillVersatility(candidate.skills()));
        features.put("bookingTimeFit", computeBookingTimeFit(request.bookingTime()));
        return features;
    }

    private double normalizeRating(String rating) {
        if (rating == null || rating.isBlank() || "N/A".equalsIgnoreCase(rating)) {
            return 0.4;
        }

        try {
            double numericRating = Double.parseDouble(rating);
            return clamp(numericRating / 5.0);
        } catch (NumberFormatException ignored) {
            return RATING_LOOKUP.getOrDefault(rating.toUpperCase(Locale.ROOT), 0.4);
        }
    }

    private double normalizeCompletedJobs(Integer completedJobs) {
        if (completedJobs == null || completedJobs <= 0) {
            return 0.2;
        }
        return clamp(Math.tanh(completedJobs / 40.0));
    }

    private double computeLocationAffinity(SuitableEmployeeRequest request,
                                           SuitableEmployeeResponse candidate) {
        if (request == null) {
            return 0.5;
        }

        double cityScore = locationMatchScore(request.city(), candidate.workingCity());

        double wardScore = 0.0;
        if (request.ward() != null && candidate.workingWards() != null) {
            String normalizedWard = normalizeLocation(request.ward());
            if (normalizedWard != null) {
                wardScore = Arrays.stream(candidate.workingWards())
                        .filter(Objects::nonNull)
                        .map(this::normalizeLocation)
                        .filter(Objects::nonNull)
                        .mapToDouble(zoneWard -> tokenSimilarity(normalizedWard, zoneWard))
                        .max()
                        .orElse(0.0);
            }
        }

        return clamp(0.6 * cityScore + 0.4 * wardScore);
    }

    private double locationMatchScore(String requestLocation, String candidateLocation) {
        String normalizedRequest = normalizeLocation(requestLocation);
        String normalizedCandidate = normalizeLocation(candidateLocation);

        if (normalizedRequest == null || normalizedCandidate == null) {
            return 0.0;
        }
        if (normalizedCandidate.equals(normalizedRequest)) {
            return 1.0;
        }
        if (normalizedCandidate.contains(normalizedRequest) || normalizedRequest.contains(normalizedCandidate)) {
            return 0.85;
        }

        return tokenSimilarity(normalizedRequest, normalizedCandidate);
    }

    private double tokenSimilarity(String left, String right) {
        if (left == null || right == null) {
            return 0.0;
        }

        Set<String> leftTokens = new HashSet<>(Arrays.asList(left.split("\\s+")));
        Set<String> rightTokens = new HashSet<>(Arrays.asList(right.split("\\s+")));
        leftTokens.removeIf(String::isBlank);
        rightTokens.removeIf(String::isBlank);

        if (leftTokens.isEmpty() || rightTokens.isEmpty()) {
            return 0.0;
        }

        Set<String> intersection = new HashSet<>(leftTokens);
        intersection.retainAll(rightTokens);

        Set<String> union = new HashSet<>(leftTokens);
        union.addAll(rightTokens);

        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }

    private String normalizeLocation(String location) {
        if (location == null) {
            return null;
        }

        String normalized = location.trim();
        if (normalized.isEmpty()) {
            return null;
        }

        normalized = normalized.replaceFirst("(?i)^P\\.\\s*", "");
        normalized = normalized.replaceFirst("(?i)^Phường\\s+", "");
        normalized = normalized.replaceFirst("(?i)^phường\\s+", "");
        normalized = normalized.replaceFirst("(?i)^TP\\.\\s*", "");
        normalized = normalized.replaceFirst("(?i)^Tp\\.\\s*", "");
        normalized = normalized.replaceFirst("(?i)^Thành phố\\s+", "");
        normalized = normalized.replaceFirst("(?i)^thành phố\\s+", "");

        return normalized.trim().toLowerCase(Locale.ROOT);
    }

    private double normalizeSkillVersatility(List<String> skills) {
        if (skills == null || skills.isEmpty()) {
            return 0.3;
        }
        return clamp((double) skills.size() / 8.0);
    }

    private double computeBookingTimeFit(LocalDateTime bookingTime) {
        if (bookingTime == null) {
            return 0.5;
        }

        int hour = bookingTime.getHour();
        // prefer bookings within 8AM - 8PM and closer to early afternoon
        if (hour < 8 || hour > 20) {
            return 0.25;
        }

        double distanceFromIdeal = Math.abs(13 - hour); // 1 PM as ideal
        return clamp(1 - (distanceFromIdeal / 10.0));
    }

    private double sigmoid(double value) {
        return 1.0 / (1.0 + Math.exp(-value));
    }

    private double extractScore(SuitableEmployeeResponse response) {
        if (response == null || response.recommendation() == null || response.recommendation().score() == null) {
            return 0.0;
        }
        return response.recommendation().score();
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private double roundToThreeDecimals(double value) {
        return Math.round(value * 1000.0d) / 1000.0d;
    }
}
