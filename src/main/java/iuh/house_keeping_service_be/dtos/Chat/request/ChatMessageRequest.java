package iuh.house_keeping_service_be.dtos.Chat.request;

import iuh.house_keeping_service_be.enums.MessageType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

public record ChatMessageRequest(
        @NotBlank(message = "Conversation id is required")
        String conversationId,
        @NotNull(message = "Message type is required")
        MessageType messageType,
        String content,
        String replyToMessageId,
        @Valid
        List<AttachmentPayload> attachments
) {
    public List<AttachmentPayload> attachments() {
        return attachments == null ? new ArrayList<>() : attachments;
    }
}