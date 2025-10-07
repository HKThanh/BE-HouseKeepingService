package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.models.Conversation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, String> {

    Optional<Conversation> findByEmployee_EmployeeIdAndCustomer_CustomerId(String employeeId, String customerId);

    @EntityGraph(attributePaths = {"employee", "employee.account", "customer", "customer.account"})
    Optional<Conversation> findWithParticipantsByConversationId(String conversationId);

    @EntityGraph(attributePaths = {"employee", "employee.account", "customer", "customer.account"})
    List<Conversation> findByEmployee_Account_AccountIdOrCustomer_Account_AccountId(String employeeAccountId, String customerAccountId);
}