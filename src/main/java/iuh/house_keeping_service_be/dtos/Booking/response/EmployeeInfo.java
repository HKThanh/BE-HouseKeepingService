package iuh.house_keeping_service_be.dtos.Booking.response;

public record EmployeeInfo(
        String employeeId,
        String fullName,
        String email,
        String phoneNumber,
        String avatar,
        String rating,
        String employeeStatus,
        String[] skills,
        String bio
) {}