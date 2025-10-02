package iuh.house_keeping_service_be.dtos.ChatMessage.response;

public record ChatMessageSocketPayload(
        EventType eventType,
        ChatMessageResponse message
) {
    public enum EventType {
        CREATED,
        DELETED,
        RECALLED
    }
}