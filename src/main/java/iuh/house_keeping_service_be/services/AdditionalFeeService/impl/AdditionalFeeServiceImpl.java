package iuh.house_keeping_service_be.services.AdditionalFeeService.impl;

import iuh.house_keeping_service_be.dtos.AdditionalFee.AdditionalFeeRequest;
import iuh.house_keeping_service_be.dtos.AdditionalFee.AdditionalFeeResponse;
import iuh.house_keeping_service_be.enums.AdditionalFeeType;
import iuh.house_keeping_service_be.models.AdditionalFee;
import iuh.house_keeping_service_be.repositories.AdditionalFeeRepository;
import iuh.house_keeping_service_be.services.AdditionalFeeService.AdditionalFeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdditionalFeeServiceImpl implements AdditionalFeeService {

    private final AdditionalFeeRepository additionalFeeRepository;

    @Override
    @Transactional
    public AdditionalFeeResponse create(AdditionalFeeRequest request) {
        validateRequest(request);
        AdditionalFee fee = mapToEntity(new AdditionalFee(), request);
        AdditionalFee saved = additionalFeeRepository.save(fee);
        if (saved.isSystemSurcharge() && saved.isActive()) {
            additionalFeeRepository.deactivateOtherSystemSurcharge(saved.getId());
        }
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public AdditionalFeeResponse update(String id, AdditionalFeeRequest request) {
        validateRequest(request);
        AdditionalFee existing = additionalFeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phụ phí: " + id));
        mapToEntity(existing, request);
        AdditionalFee saved = additionalFeeRepository.save(existing);
        if (saved.isSystemSurcharge() && saved.isActive()) {
            additionalFeeRepository.deactivateOtherSystemSurcharge(saved.getId());
        }
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public AdditionalFeeResponse activate(String id, boolean active) {
        AdditionalFee existing = additionalFeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phụ phí: " + id));
        existing.setActive(active);
        if (existing.isSystemSurcharge() && active) {
            additionalFeeRepository.deactivateOtherSystemSurcharge(existing.getId());
        }
        AdditionalFee saved = additionalFeeRepository.save(existing);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public AdditionalFeeResponse markAsSystemSurcharge(String id) {
        AdditionalFee existing = additionalFeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phụ phí: " + id));
        
        // QUAN TRỌNG: Tắt phí hệ thống cũ TRƯỚC khi đặt phí mới
        // để tránh vi phạm unique constraint (chỉ cho phép 1 phí hệ thống active)
        additionalFeeRepository.deactivateOtherSystemSurcharge(id);
        
        existing.setSystemSurcharge(true);
        existing.setActive(true);
        AdditionalFee saved = additionalFeeRepository.save(existing);
        return mapToResponse(saved);
    }

    @Override
    public Page<AdditionalFeeResponse> list(Pageable pageable) {
        return additionalFeeRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Override
    public AdditionalFeeResponse getById(String id) {
        return additionalFeeRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phụ phí: " + id));
    }

    @Override
    public AdditionalFee getActiveSystemSurcharge() {
        return additionalFeeRepository.findFirstBySystemSurchargeTrueAndActiveTrue().orElse(null);
    }

    @Override
    public List<AdditionalFee> getActiveNonSystemFees() {
        return additionalFeeRepository.findByActiveTrue().stream()
                .filter(f -> !f.isSystemSurcharge())
                .collect(Collectors.toList());
    }

    @Override
    public List<AdditionalFee> getActiveFeesByIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return additionalFeeRepository.findAllById(ids).stream()
                .filter(f -> f.isActive() && !f.isSystemSurcharge())
                .collect(Collectors.toList());
    }

    private void validateRequest(AdditionalFeeRequest request) {
        if (request.getFeeType() == AdditionalFeeType.FLAT && request.getValue().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Giá trị phụ phí không hợp lệ");
        }
        if (request.getFeeType() == AdditionalFeeType.PERCENT &&
                (request.getValue().compareTo(BigDecimal.ZERO) < 0 || request.getValue().compareTo(new BigDecimal("5")) > 0)) {
            // 500% upper guard
            throw new IllegalArgumentException("Giá trị phần trăm không hợp lệ");
        }
    }

    private AdditionalFee mapToEntity(AdditionalFee target, AdditionalFeeRequest request) {
        target.setName(request.getName());
        target.setDescription(request.getDescription());
        target.setFeeType(request.getFeeType());
        target.setValue(request.getValue());
        target.setSystemSurcharge(Boolean.TRUE.equals(request.getSystemSurcharge()));
        target.setActive(Boolean.TRUE.equals(request.getActive()));
        target.setPriority(request.getPriority() != null ? request.getPriority() : 0);
        return target;
    }

    private AdditionalFeeResponse mapToResponse(AdditionalFee fee) {
        return AdditionalFeeResponse.builder()
                .id(fee.getId())
                .name(fee.getName())
                .description(fee.getDescription())
                .feeType(fee.getFeeType())
                .value(fee.getValue())
                .systemSurcharge(fee.isSystemSurcharge())
                .active(fee.isActive())
                .priority(fee.getPriority())
                .createdAt(fee.getCreatedAt())
                .updatedAt(fee.getUpdatedAt())
                .build();
    }
}
