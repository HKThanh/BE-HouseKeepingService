package iuh.house_keeping_service_be.dtos.Customer;

import java.time.LocalDate;

public record CustomerUpdateRequest(
        String fullName,
        Boolean isMale,
        String avatar,
        String email,
        LocalDate birthdate
) {
}