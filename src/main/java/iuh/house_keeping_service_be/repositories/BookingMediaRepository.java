package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.enums.MediaType;
import iuh.house_keeping_service_be.models.BookingMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingMediaRepository extends JpaRepository<BookingMedia, String> {

    /**
     * Find all media for a specific assignment
     */
    List<BookingMedia> findByAssignment_AssignmentId(String assignmentId);

    /**
     * Find media by assignment and type
     */
    List<BookingMedia> findByAssignment_AssignmentIdAndMediaType(String assignmentId, MediaType mediaType);

    /**
     * Find all media for a booking
     */
    @Query("SELECT bm FROM BookingMedia bm " +
           "WHERE bm.assignment.bookingDetail.booking.bookingId = :bookingId " +
           "ORDER BY bm.uploadedAt DESC")
    List<BookingMedia> findByBookingId(@Param("bookingId") String bookingId);

    /**
     * Count media by assignment
     */
    long countByAssignment_AssignmentId(String assignmentId);

    /**
     * Delete all media for an assignment
     */
    void deleteByAssignment_AssignmentId(String assignmentId);
}
