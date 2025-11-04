package iuh.house_keeping_service_be.dtos.BookingMedia.response;

import iuh.house_keeping_service_be.enums.MediaType;

import java.time.LocalDateTime;

public record BookingMediaResponse(
        String mediaId,
        String assignmentId,
        String mediaUrl,
        String publicId,
        MediaType mediaType,
        String description,
        LocalDateTime uploadedAt
) {
}
