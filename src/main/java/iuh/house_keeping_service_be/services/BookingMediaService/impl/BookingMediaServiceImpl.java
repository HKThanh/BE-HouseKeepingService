package iuh.house_keeping_service_be.services.BookingMediaService.impl;

import iuh.house_keeping_service_be.dtos.BookingMedia.response.BookingMediaResponse;
import iuh.house_keeping_service_be.dtos.Cloudinary.CloudinaryUploadResult;
import iuh.house_keeping_service_be.enums.MediaType;
import iuh.house_keeping_service_be.models.Assignment;
import iuh.house_keeping_service_be.models.BookingMedia;
import iuh.house_keeping_service_be.repositories.BookingMediaRepository;
import iuh.house_keeping_service_be.services.BookingMediaService.BookingMediaService;
import iuh.house_keeping_service_be.services.CloudinaryService.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingMediaServiceImpl implements BookingMediaService {

    private final BookingMediaRepository bookingMediaRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public BookingMediaResponse uploadMedia(Assignment assignment, MultipartFile file, MediaType mediaType, String description) {
        log.info("Uploading media for assignment {} with type {}", assignment.getAssignmentId(), mediaType);

        // Validate file
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống");
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File phải là định dạng ảnh");
        }

        // Validate file size (max 10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("Kích thước file không được vượt quá 10MB");
        }

        try {
            // Upload to Cloudinary
            CloudinaryUploadResult uploadResult = cloudinaryService.uploadBookingImage(file);

            // Create and save BookingMedia entity
            BookingMedia media = new BookingMedia();
            media.setAssignment(assignment);
            media.setMediaUrl(uploadResult.secureUrl());
            media.setPublicId(uploadResult.publicId());
            media.setMediaType(mediaType);
            media.setDescription(description);

            BookingMedia savedMedia = bookingMediaRepository.save(media);

            log.info("Successfully uploaded media {} for assignment {}", savedMedia.getMediaId(), assignment.getAssignmentId());

            return toResponse(savedMedia);

        } catch (Exception e) {
            log.error("Error uploading media for assignment {}: {}", assignment.getAssignmentId(), e.getMessage());
            throw new RuntimeException("Lỗi khi tải ảnh lên: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingMediaResponse> getMediaByAssignmentId(String assignmentId) {
        log.info("Getting all media for assignment {}", assignmentId);
        List<BookingMedia> mediaList = bookingMediaRepository.findByAssignment_AssignmentId(assignmentId);
        return mediaList.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingMediaResponse> getMediaByAssignmentAndType(String assignmentId, MediaType mediaType) {
        log.info("Getting media for assignment {} with type {}", assignmentId, mediaType);
        List<BookingMedia> mediaList = bookingMediaRepository.findByAssignment_AssignmentIdAndMediaType(assignmentId, mediaType);
        return mediaList.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingMediaResponse> getMediaByBookingId(String bookingId) {
        log.info("Getting all media for booking {}", bookingId);
        List<BookingMedia> mediaList = bookingMediaRepository.findByBookingId(bookingId);
        return mediaList.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteMedia(String mediaId) {
        log.info("Deleting media {}", mediaId);
        BookingMedia media = bookingMediaRepository.findById(mediaId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy media với ID: " + mediaId));

        // Delete from Cloudinary if publicId exists
        if (media.getPublicId() != null && !media.getPublicId().isEmpty()) {
            try {
                cloudinaryService.deleteImage(media.getPublicId());
            } catch (Exception e) {
                log.warn("Failed to delete image from Cloudinary: {}", e.getMessage());
            }
        }

        bookingMediaRepository.delete(media);
        log.info("Successfully deleted media {}", mediaId);
    }

    private BookingMediaResponse toResponse(BookingMedia media) {
        return new BookingMediaResponse(
                media.getMediaId(),
                media.getAssignment().getAssignmentId(),
                media.getMediaUrl(),
                media.getPublicId(),
                media.getMediaType(),
                media.getDescription(),
                media.getUploadedAt()
        );
    }
}
