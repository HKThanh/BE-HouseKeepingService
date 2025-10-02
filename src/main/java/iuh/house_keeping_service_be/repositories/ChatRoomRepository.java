package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.models.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, String> {

    Optional<ChatRoom> findByAssignmentAssignmentId(String assignmentId);

    @Query("SELECT cr FROM ChatRoom cr " +
            "WHERE cr.assignment.assignmentId = :assignmentId " +
            "AND (cr.customerAccount.accountId = :accountId OR cr.employeeAccount.accountId = :accountId)")
    Optional<ChatRoom> findByAssignmentIdAndParticipant(@Param("assignmentId") String assignmentId,
                                                        @Param("accountId") String accountId);
}