package iuh.house_keeping_service_be.controllers;

import iuh.house_keeping_service_be.dtos.BookingMedia.response.BookingMediaResponse;
import iuh.house_keeping_service_be.enums.MediaType;
import iuh.house_keeping_service_be.services.BookingMediaService.BookingMediaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/booking-media")
@RequiredArgsConstructor
@Slf4j
public class BookingMediaController {

    private final BookingMediaService bookingMediaService;

    @GetMapping("/assignment/{assignmentId}")
    @PreAuthorize("hasAnyAuthority('ROLE_EMPLOYEE', 'ROLE_CUSTOMER', 'ROLE_ADMIN')")
    public ResponseEntity<?> getMediaByAssignment(@PathVariable String assignmentId) {
        try {
            log.info("Getting all media for assignment {}", assignmentId);
            List<BookingMediaResponse> mediaList = bookingMediaService.getMediaByAssignmentId(assignmentId);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", mediaList,
                    "totalItems", mediaList.size()
            ));
        } catch (Exception e) {
            log.error("Error getting media for assignment {}: {}", assignmentId, e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Lỗi khi lấy danh sách ảnh: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/assignment/{assignmentId}/type/{mediaType}")
    @PreAuthorize("hasAnyAuthority('ROLE_EMPLOYEE', 'ROLE_CUSTOMER', 'ROLE_ADMIN')")
    public ResponseEntity<?> getMediaByAssignmentAndType(
            @PathVariable String assignmentId,
            @PathVariable MediaType mediaType) {
        try {
            log.info("Getting media for assignment {} with type {}", assignmentId, mediaType);
            List<BookingMediaResponse> mediaList = bookingMediaService.getMediaByAssignmentAndType(assignmentId, mediaType);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", mediaList,
                    "totalItems", mediaList.size()
            ));
        } catch (Exception e) {
            log.error("Error getting media for assignment {} with type {}: {}", assignmentId, mediaType, e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Lỗi khi lấy danh sách ảnh: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/booking/{bookingId}")
    @PreAuthorize("hasAnyAuthority('ROLE_EMPLOYEE', 'ROLE_CUSTOMER', 'ROLE_ADMIN')")
    public ResponseEntity<?> getMediaByBooking(@PathVariable String bookingId) {
        try {
            log.info("Getting all media for booking {}", bookingId);
            List<BookingMediaResponse> mediaList = bookingMediaService.getMediaByBookingId(bookingId);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", mediaList,
                    "totalItems", mediaList.size()
            ));
        } catch (Exception e) {
            log.error("Error getting media for booking {}: {}", bookingId, e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Lỗi khi lấy danh sách ảnh: " + e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{mediaId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> deleteMedia(@PathVariable String mediaId) {
        try {
            log.info("Deleting media {}", mediaId);
            bookingMediaService.deleteMedia(mediaId);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Xóa ảnh thành công"
            ));
        } catch (IllegalArgumentException e) {
            log.error("Media not found: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error deleting media {}: {}", mediaId, e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Lỗi khi xóa ảnh: " + e.getMessage()
            ));
        }
    }
}
