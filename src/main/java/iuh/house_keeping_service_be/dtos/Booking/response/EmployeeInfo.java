package iuh.house_keeping_service_be.dtos.Booking.response;

import iuh.house_keeping_service_be.enums.EmployeeStatus;
import iuh.house_keeping_service_be.enums.Rating;

import java.util.List;

public record EmployeeInfo(
        String employeeId,
        String fullName,
        String email,
        String phoneNumber,
        String avatar,
        Rating rating,
        EmployeeStatus employeeStatus,
        List<String> skills,
        String bio
) {}