package iuh.house_keeping_service_be.enums;

public enum AssignmentStatus {
    PENDING,      // Waiting for employee to accept
    ASSIGNED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,    // For cancelled assignments
    NO_SHOW       // For when employee doesn't show up
}