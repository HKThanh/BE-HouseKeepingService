package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.models.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {

    @EntityGraph(attributePaths = {"sender", "replyTo"})
    Page<ChatMessage> findByConversation_ConversationIdOrderBySentAtDesc(String conversationId, Pageable pageable);

    @EntityGraph(attributePaths = {"sender", "conversation", "conversation.employee", "conversation.customer", "replyTo"})
    Optional<ChatMessage> findWithConversationByMessageId(String messageId);

    @EntityGraph(attributePaths = {"sender"})
    Optional<ChatMessage> findTop1ByConversation_ConversationIdOrderBySentAtDesc(String conversationId);
}