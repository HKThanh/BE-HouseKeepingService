package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.models.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {
    Page<ChatMessage> findByChatRoom_ChatRoomIdOrderByCreatedAtDesc(String chatRoomId, Pageable pageable);
}