package iuh.house_keeping_service_be.controllers;

import iuh.house_keeping_service_be.dtos.AdditionalFee.AdditionalFeeRequest;
import iuh.house_keeping_service_be.dtos.AdditionalFee.AdditionalFeeResponse;
import iuh.house_keeping_service_be.services.AdditionalFeeService.AdditionalFeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/additional-fees")
@RequiredArgsConstructor
public class AdditionalFeeAdminController {

    private final AdditionalFeeService additionalFeeService;

    @PostMapping
    public ResponseEntity<AdditionalFeeResponse> create(@Valid @RequestBody AdditionalFeeRequest request) {
        return ResponseEntity.ok(additionalFeeService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdditionalFeeResponse> update(@PathVariable String id,
                                                        @Valid @RequestBody AdditionalFeeRequest request) {
        return ResponseEntity.ok(additionalFeeService.update(id, request));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<AdditionalFeeResponse> activate(@PathVariable String id,
                                                          @RequestParam(defaultValue = "true") boolean active) {
        return ResponseEntity.ok(additionalFeeService.activate(id, active));
    }

    @PostMapping("/{id}/system-surcharge")
    public ResponseEntity<AdditionalFeeResponse> markSystem(@PathVariable String id) {
        return ResponseEntity.ok(additionalFeeService.markAsSystemSurcharge(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdditionalFeeResponse> get(@PathVariable String id) {
        return ResponseEntity.ok(additionalFeeService.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<AdditionalFeeResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "priority,asc") String[] sort
    ) {
        Sort sortObj = Sort.by(Sort.Order.by(sort[0]));
        if (sort.length > 1) {
            // support direction in sort[1]
            String dir = sort[1];
            sortObj = "desc".equalsIgnoreCase(dir) ? sortObj.descending() : sortObj.ascending();
        }
        Pageable pageable = PageRequest.of(page, size, sortObj);
        return ResponseEntity.ok(additionalFeeService.list(pageable));
    }
}
