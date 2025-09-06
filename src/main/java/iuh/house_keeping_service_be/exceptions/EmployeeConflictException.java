package iuh.house_keeping_service_be.exceptions;

import iuh.house_keeping_service_be.dtos.Booking.internal.ConflictInfo;

import java.time.LocalDateTime;
import java.util.List;

public class EmployeeConflictException extends RuntimeException {
    private final List<ConflictInfo> conflicts;
    
    public EmployeeConflictException(String message, List<ConflictInfo> conflicts) {
        super(message);
        this.conflicts = conflicts != null ? List.copyOf(conflicts) : List.of();
    }
    
    public EmployeeConflictException(List<ConflictInfo> conflicts) {
        super("Employee scheduling conflict detected");
        this.conflicts = conflicts != null ? List.copyOf(conflicts) : List.of();
    }
    
    public List<ConflictInfo> getConflicts() {
        return conflicts;
    }
    
    // Static factory methods
    public static EmployeeConflictException withConflicts(List<ConflictInfo> conflicts) {
        return new EmployeeConflictException("Employee scheduling conflict detected", conflicts);
    }
    
    public static EmployeeConflictException singleConflict(ConflictInfo conflict) {
        return new EmployeeConflictException("Employee conflict detected", List.of(conflict));
    }
    
    public static EmployeeConflictException unavailable(String employeeId, String employeeName) {
        var conflict = new ConflictInfo(
            "EMPLOYEE_UNAVAILABLE", 
            employeeId, 
            LocalDateTime.now(), 
            LocalDateTime.now(), 
            "Employee is not available at requested time"
        );
        return new EmployeeConflictException("Employee unavailable", List.of(conflict));
    }
    
    public static EmployeeConflictException overbooked(String employeeId, String employeeName) {
        var conflict = new ConflictInfo(
             "EMPLOYEE_OVERBOOKED", 
            employeeId,
            LocalDateTime.now(), 
            LocalDateTime.now(), 
            "Employee has too many assignments at this time"
        );
        return new EmployeeConflictException("Employee overbooked", List.of(conflict));
    }
}