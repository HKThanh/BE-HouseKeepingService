package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.models.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {

    List<ChatMessage> findByChatRoomChatRoomIdAndDeletedAtIsNullOrderBySentAtAsc(String chatRoomId);

    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatRoom.assignment.assignmentId = :assignmentId " +
            "AND cm.deletedAt IS NULL ORDER BY cm.sentAt ASC")
    List<ChatMessage> findByAssignmentId(@Param("assignmentId") String assignmentId);

    @Query("SELECT cm FROM ChatMessage cm " +
            "WHERE cm.chatRoom.assignment.assignmentId = :assignmentId " +
            "AND (cm.chatRoom.customerAccount.accountId = :accountId OR cm.chatRoom.employeeAccount.accountId = :accountId) " +
            "AND cm.deletedAt IS NULL " +
            "ORDER BY cm.sentAt ASC")
    List<ChatMessage> findByAssignmentIdAndParticipant(@Param("assignmentId") String assignmentId,
                                                       @Param("accountId") String accountId);

    List<ChatMessage> findByParentMessageChatMessageId(String parentMessageId);
}