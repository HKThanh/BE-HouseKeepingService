package iuh.house_keeping_service_be.dtos.Chat.response;

public record AttachmentUploadResponse(
        String url,
        String publicId,
        String contentType,
        long size
) {
}
