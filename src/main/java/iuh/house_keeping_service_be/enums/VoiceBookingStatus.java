package iuh.house_keeping_service_be.enums;

/**
 * Status values for voice booking request processing
 */
public enum VoiceBookingStatus {
    PENDING("Đang chờ xử lý"),
    PROCESSING("Đang xử lý"),
    COMPLETED("Hoàn thành"),
    PARTIAL("Thiếu thông tin"),
    FAILED("Thất bại");

    private final String description;

    VoiceBookingStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static VoiceBookingStatus fromString(String status) {
        if (status == null) {
            return PENDING;
        }
        
        try {
            return VoiceBookingStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return PENDING;
        }
    }
}
