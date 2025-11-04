package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.models.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, String> {

    Optional<Conversation> findByCustomer_CustomerIdAndEmployee_EmployeeId(String customerId, String employeeId);

    Optional<Conversation> findByBooking_BookingId(String bookingId);

    @Query("SELECT c FROM Conversation c WHERE c.customer.customerId = :customerId OR c.employee.employeeId = :employeeId ORDER BY c.lastMessageTime DESC")
    List<Conversation> findByCustomerOrEmployee(@Param("customerId") String customerId, @Param("employeeId") String employeeId);

    Page<Conversation> findByCustomer_CustomerIdOrderByLastMessageTimeDesc(String customerId, Pageable pageable);

    Page<Conversation> findByEmployee_EmployeeIdOrderByLastMessageTimeDesc(String employeeId, Pageable pageable);

    @Query("SELECT c FROM Conversation c WHERE (c.customer.customerId = :accountId OR c.employee.employeeId = :accountId) AND c.isActive = true ORDER BY c.lastMessageTime DESC")
    Page<Conversation> findActiveConversationsByAccount(@Param("accountId") String accountId, Pageable pageable);
}
