package iuh.house_keeping_service_be.services.RecommendationService;

import iuh.house_keeping_service_be.dtos.EmployeeSchedule.SuitableEmployeeRequest;
import iuh.house_keeping_service_be.dtos.EmployeeSchedule.SuitableEmployeeResponse;

import java.util.List;

public interface EmployeeRecommendationService {

    /**
     * Apply the machine learning model to the provided candidates list.
     *
     * @param request   the original request coming from the customer
     * @param candidates list of employees that are already available for the slot
     * @return the same candidates ranked and enriched with recommendation metadata
     */
    List<SuitableEmployeeResponse> recommend(SuitableEmployeeRequest request,
                                             List<SuitableEmployeeResponse> candidates);

    /**
     * @return current model version used for scoring, or "n/a" when the engine is disabled.
     */
    String getModelVersion();
}
