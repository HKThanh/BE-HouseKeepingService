package iuh.house_keeping_service_be.services.ChatService.impl;

import iuh.house_keeping_service_be.dtos.Chat.ConversationRequest;
import iuh.house_keeping_service_be.dtos.Chat.ConversationResponse;
import iuh.house_keeping_service_be.models.*;
import iuh.house_keeping_service_be.repositories.*;
import iuh.house_keeping_service_be.services.ChatService.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private final ConversationRepository conversationRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional
    public ConversationResponse createConversation(ConversationRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Conversation conversation = new Conversation();
        conversation.setCustomer(customer);
        conversation.setIsActive(true);

        if (request.getEmployeeId() != null) {
            Employee employee = employeeRepository.findById(request.getEmployeeId())
                    .orElseThrow(() -> new RuntimeException("Employee not found"));
            conversation.setEmployee(employee);
        }

        if (request.getBookingId() != null) {
            Booking booking = bookingRepository.findById(request.getBookingId())
                    .orElseThrow(() -> new RuntimeException("Booking not found"));
            conversation.setBooking(booking);
        }

        Conversation savedConversation = conversationRepository.save(conversation);
        return mapToResponse(savedConversation);
    }

    @Override
    @Transactional(readOnly = true)
    public ConversationResponse getConversationById(String conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        return mapToResponse(conversation);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConversationResponse> getConversationsByAccount(String accountId, Pageable pageable) {
        Page<Conversation> conversations = conversationRepository.findActiveConversationsByAccount(accountId, pageable);
        return conversations.map(this::mapToResponse);
    }

    @Override
    @Transactional
    public ConversationResponse getOrCreateConversation(String customerId, String employeeId) {
        return conversationRepository.findByCustomer_CustomerIdAndEmployee_EmployeeId(customerId, employeeId)
                .map(this::mapToResponse)
                .orElseGet(() -> {
                    ConversationRequest request = new ConversationRequest();
                    request.setCustomerId(customerId);
                    request.setEmployeeId(employeeId);
                    return createConversation(request);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public ConversationResponse getConversationByBooking(String bookingId) {
        Conversation conversation = conversationRepository.findByBooking_BookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Conversation not found for booking"));
        return mapToResponse(conversation);
    }

    @Override
    @Transactional
    public void updateLastMessage(String conversationId, String lastMessage) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        conversation.setLastMessage(lastMessage);
        conversation.setLastMessageTime(LocalDateTime.now());
        conversationRepository.save(conversation);
    }

    @Override
    @Transactional
    public void deleteConversation(String conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        conversation.setIsActive(false);
        conversationRepository.save(conversation);
    }

    private ConversationResponse mapToResponse(Conversation conversation) {
        return ConversationResponse.builder()
                .conversationId(conversation.getConversationId())
                .customerId(conversation.getCustomer().getCustomerId())
                .customerName(conversation.getCustomer().getFullName())
                .employeeId(conversation.getEmployee() != null ? conversation.getEmployee().getEmployeeId() : null)
                .employeeName(conversation.getEmployee() != null ? conversation.getEmployee().getFullName() : null)
                .bookingId(conversation.getBooking() != null ? conversation.getBooking().getBookingId() : null)
                .lastMessage(conversation.getLastMessage())
                .lastMessageTime(conversation.getLastMessageTime())
                .isActive(conversation.getIsActive())
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .build();
    }
}
