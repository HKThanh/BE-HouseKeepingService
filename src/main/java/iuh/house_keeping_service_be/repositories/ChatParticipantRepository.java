package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.models.ChatParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, String> {
    boolean existsByChatRoom_ChatRoomIdAndAccount_AccountId(String chatRoomId, String accountId);

    List<ChatParticipant> findByChatRoom_ChatRoomId(String chatRoomId);
}