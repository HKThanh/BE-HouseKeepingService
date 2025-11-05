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

    @Query("SELECT COUNT(m) FROM ChatMessage m " +
           "JOIN m.conversation c " +
           "WHERE m.conversation.conversationId = :conversationId " +
           "AND m.isRead = false " +
           "AND ((c.customer.customerId = :receiverId AND m.sender.accountId != c.customer.account.accountId) " +
           "OR (c.employee.employeeId = :receiverId AND m.sender.accountId != c.employee.account.accountId))")
    Long countUnreadMessages(@Param("conversationId") String conversationId, @Param("receiverId") String receiverId);

    @Modifying
    @Query("UPDATE ChatMessage m SET m.isRead = true " +
           "WHERE m.conversation.conversationId = :conversationId " +
           "AND m.isRead = false " +
           "AND ((m.conversation.customer.customerId = :receiverId AND m.sender.accountId != m.conversation.customer.account.accountId) " +
           "OR (m.conversation.employee.employeeId = :receiverId AND m.sender.accountId != m.conversation.employee.account.accountId))")
    void markAllAsRead(@Param("conversationId") String conversationId, @Param("receiverId") String receiverId);

    // Count unread messages for a sender (customerId or employeeId)
    // Messages where sender is NOT the given senderId and isRead = false
    @Query("SELECT COUNT(m) FROM ChatMessage m " +
           "JOIN m.conversation c " +
           "WHERE (c.customer.customerId = :senderId OR c.employee.employeeId = :senderId) " +
           "AND m.isRead = false " +
           "AND ((c.customer.customerId = :senderId AND m.sender.accountId != c.customer.account.accountId) " +
           "OR (c.employee.employeeId = :senderId AND m.sender.accountId != c.employee.account.accountId))")
    Long countUnreadMessagesBySenderId(@Param("senderId") String senderId);

    // Mark all unread messages as read for a sender in a specific conversation
    @Modifying
    @Query("UPDATE ChatMessage m SET m.isRead = true " +
           "WHERE m.conversation.conversationId = :conversationId " +
           "AND m.isRead = false " +
           "AND ((m.conversation.customer.customerId = :senderId AND m.sender.accountId != m.conversation.customer.account.accountId) " +
           "OR (m.conversation.employee.employeeId = :senderId AND m.sender.accountId != m.conversation.employee.account.accountId))")
    int markAsReadBySenderIdAndConversation(@Param("senderId") String senderId, @Param("conversationId") String conversationId);

    // Mark all unread messages as read for a sender (across all conversations)
    @Modifying
    @Query("UPDATE ChatMessage m SET m.isRead = true " +
           "WHERE m.conversation.conversationId IN " +
           "(SELECT c.conversationId FROM Conversation c WHERE c.customer.customerId = :senderId OR c.employee.employeeId = :senderId) " +
           "AND m.isRead = false " +
           "AND ((m.conversation.customer.customerId = :senderId AND m.sender.accountId != m.conversation.customer.account.accountId) " +
           "OR (m.conversation.employee.employeeId = :senderId AND m.sender.accountId != m.conversation.employee.account.accountId))")
    int markAllAsReadBySenderId(@Param("senderId") String senderId);
}
