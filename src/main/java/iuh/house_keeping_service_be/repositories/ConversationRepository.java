package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.models.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, String> {
    Optional<Conversation> findByBooking_BookingId(String bookingId);
}
