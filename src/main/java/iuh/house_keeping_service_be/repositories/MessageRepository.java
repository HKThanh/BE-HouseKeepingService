package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.models.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, String> {
    Page<Message> findByConversation_ConversationId(String conversationId, Pageable pageable);

    Optional<Message> findFirstByConversation_ConversationId(String conversationId);
}
