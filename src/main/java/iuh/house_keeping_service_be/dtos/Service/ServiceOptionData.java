package iuh.house_keeping_service_be.dtos.Service;

import java.util.List;

public record ServiceOptionData(
    Integer optionId,
    String optionName,
    String optionType,
    Integer displayOrder,
    Boolean isRequired,
    List<ServiceOptionChoiceData> choices
) {}