package iuh.house_keeping_service_be.enums;

public enum AssignmentStatus {
    ASSIGNED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,    // For cancelled assignments
    NO_SHOW       // For when employee doesn't show up
}