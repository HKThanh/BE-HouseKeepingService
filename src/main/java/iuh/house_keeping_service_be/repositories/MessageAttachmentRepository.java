package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.models.MessageAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageAttachmentRepository extends JpaRepository<MessageAttachment, String> {
}
