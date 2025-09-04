package iuh.house_keeping_service_be.dtos.Service;

public record ServiceOptionChoiceData(
    Integer choiceId,
    String choiceName,
    Integer displayOrder,
    Boolean isDefault
) {}