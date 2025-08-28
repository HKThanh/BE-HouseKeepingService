package iuh.house_keeping_service_be.enums;

public enum AssignmentStatus {
    ASSIGNED,
    IN_PROGRESS,  // Instead of CHECKED_IN
    COMPLETED,    // Instead of CHECKED_OUT
    CANCELLED,    // For cancelled assignments
    NO_SHOW       // For when employee doesn't show up
}