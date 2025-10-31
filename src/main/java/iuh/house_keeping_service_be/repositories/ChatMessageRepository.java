package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.models.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {

    Page<ChatMessage> findByConversation_ConversationIdOrderByCreatedAtDesc(String conversationId, Pageable pageable);

    List<ChatMessage> findByConversation_ConversationIdOrderByCreatedAtAsc(String conversationId);

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.conversation.conversationId = :conversationId AND m.isRead = false AND m.sender.accountId != :accountId")
    Long countUnreadMessages(@Param("conversationId") String conversationId, @Param("accountId") String accountId);

    @Modifying
    @Query("UPDATE ChatMessage m SET m.isRead = true WHERE m.conversation.conversationId = :conversationId AND m.sender.accountId != :accountId AND m.isRead = false")
    void markAllAsRead(@Param("conversationId") String conversationId, @Param("accountId") String accountId);
}
