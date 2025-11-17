package iuh.house_keeping_service_be.dtos.VoiceBooking;

/**
 * Event types emitted on the voice booking WebSocket channel.
 */
public enum VoiceBookingEventType {
    RECEIVED,
    TRANSCRIBING,
    PARTIAL,
    COMPLETED,
    FAILED
}
