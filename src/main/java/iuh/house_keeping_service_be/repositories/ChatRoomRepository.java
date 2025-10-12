package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.models.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, String> {
    Optional<ChatRoom> findByBooking_BookingId(String bookingId);
}