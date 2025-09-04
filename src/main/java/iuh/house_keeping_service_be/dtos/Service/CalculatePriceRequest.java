package iuh.house_keeping_service_be.dtos.Service;

import java.util.List;

public record CalculatePriceRequest(
    Integer serviceId,
    List<Integer> selectedChoiceIds,
    Integer quantity
) {}