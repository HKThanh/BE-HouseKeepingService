package iuh.house_keeping_service_be.dtos.EmployeeSchedule;

public record ApiResponse<T>(
    boolean success,
    String message,
    T data
) {}
