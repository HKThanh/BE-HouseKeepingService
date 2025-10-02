package iuh.house_keeping_service_be.dtos.ChatMessage.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatMessageSendRequest(
        @NotBlank(message = "Sender account id is required")
        String senderAccountId,

        @Size(max = 5000, message = "Message text must be less than 5000 characters")
        String messageText,

        @Size(max = 255, message = "Payload type must be less than 255 characters")
        String payloadType,

        String payloadData,

        String parentMessageId
) {
    @AssertTrue(message = "Message text or payload data must be provided")
    public boolean hasContent() {
        return (messageText != null && !messageText.isBlank())
                || (payloadData != null && !payloadData.isBlank());
    }
}