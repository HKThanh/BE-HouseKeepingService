package iuh.house_keeping_service_be.dtos.Booking.response;

import iuh.house_keeping_service_be.enums.EmployeeStatus;
import iuh.house_keeping_service_be.enums.Rating;

import java.util.List;

/**
 * Public employee information DTO without sensitive data (phone, email)
 * Used for displaying employee info to customers in booking responses
 */
public record EmployeeInfoPublic(
        String employeeId,
        String fullName,
        String avatar,
        Rating rating,
        EmployeeStatus employeeStatus,
        List<String> skills,
        String bio
) {}
