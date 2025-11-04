package iuh.house_keeping_service_be.services.BookingMediaService;

import iuh.house_keeping_service_be.dtos.BookingMedia.response.BookingMediaResponse;
import iuh.house_keeping_service_be.enums.MediaType;
import iuh.house_keeping_service_be.models.Assignment;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BookingMediaService {

    /**
     * Upload and save a booking media
     */
    BookingMediaResponse uploadMedia(Assignment assignment, MultipartFile file, MediaType mediaType, String description);

    /**
     * Get all media for an assignment
     */
    List<BookingMediaResponse> getMediaByAssignmentId(String assignmentId);

    /**
     * Get media by assignment and type
     */
    List<BookingMediaResponse> getMediaByAssignmentAndType(String assignmentId, MediaType mediaType);

    /**
     * Get all media for a booking
     */
    List<BookingMediaResponse> getMediaByBookingId(String bookingId);

    /**
     * Delete a media
     */
    void deleteMedia(String mediaId);
}
