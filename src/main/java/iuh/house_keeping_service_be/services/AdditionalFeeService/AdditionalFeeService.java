package iuh.house_keeping_service_be.services.AdditionalFeeService;

import iuh.house_keeping_service_be.dtos.AdditionalFee.AdditionalFeeRequest;
import iuh.house_keeping_service_be.dtos.AdditionalFee.AdditionalFeeResponse;
import iuh.house_keeping_service_be.models.AdditionalFee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdditionalFeeService {
    AdditionalFeeResponse create(AdditionalFeeRequest request);
    AdditionalFeeResponse update(String id, AdditionalFeeRequest request);
    AdditionalFeeResponse activate(String id, boolean active);
    AdditionalFeeResponse markAsSystemSurcharge(String id);
    Page<AdditionalFeeResponse> list(Pageable pageable);
    AdditionalFeeResponse getById(String id);

    AdditionalFee getActiveSystemSurcharge();
    List<AdditionalFee> getActiveNonSystemFees();
    List<AdditionalFee> getActiveFeesByIds(List<String> ids);
}
