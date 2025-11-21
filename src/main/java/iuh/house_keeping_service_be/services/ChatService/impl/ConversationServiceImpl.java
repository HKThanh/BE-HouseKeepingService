package iuh.house_keeping_service_be.services.ChatService.impl;

import iuh.house_keeping_service_be.dtos.Chat.ConversationRequest;
import iuh.house_keeping_service_be.dtos.Chat.ConversationResponse;
import iuh.house_keeping_service_be.enums.BookingStatus;
import iuh.house_keeping_service_be.enums.MessageType;
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
    private final ChatMessageRepository chatMessageRepository;

    @Override
    @Transactional
    public ConversationResponse createConversation(ConversationRequest request) {
        // Validate required fields
        if (request.getEmployeeId() == null) {
            throw new IllegalArgumentException("Employee ID is required");
        }
        if (request.getCustomerId() == null) {
            throw new IllegalArgumentException("Customer ID is required");
        }
        if (request.getBookingId() == null) {
            throw new IllegalArgumentException("Booking ID is required");
        }

        // Fetch entities
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Create conversation
        Conversation conversation = new Conversation();
        conversation.setCustomer(customer);
        conversation.setEmployee(employee);
        conversation.setBooking(booking);
        conversation.setIsActive(true);

        Conversation savedConversation = conversationRepository.save(conversation);

        // Create automatic welcome message from employee
        String welcomeMessage = "Xin chào. Tôi là nhân viên được chọn để làm việc cho bạn trong đơn đặt dịch vụ " + booking.getBookingDetails().get(0).getService().getName() + " vào lúc " + booking.getBookingTime() + ", tại địa chỉ: " + booking.getAddress().getFullAddress() + ". Nếu bạn có bất kỳ câu hỏi nào, xin vui lòng liên hệ với tôi qua cuộc trò chuyện này.";
        
        ChatMessage welcomeChatMessage = new ChatMessage();
        welcomeChatMessage.setConversation(savedConversation);
        welcomeChatMessage.setSender(employee.getAccount());
        welcomeChatMessage.setMessageType(MessageType.TEXT);
        welcomeChatMessage.setContent(welcomeMessage);
        welcomeChatMessage.setIsRead(false);
        
        chatMessageRepository.save(welcomeChatMessage);

        // Update conversation's last message
        savedConversation.setLastMessage(welcomeMessage);
        savedConversation.setLastMessageTime(LocalDateTime.now());
        conversationRepository.save(savedConversation);

        return mapToResponse(savedConversation);
    }

    @Override
    @Transactional
    public ConversationResponse getConversationById(String conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        
        // Check booking status and disable conversation if completed or cancelled
        if (conversation.getBooking() != null) {
            BookingStatus bookingStatus = conversation.getBooking().getStatus();
            if (bookingStatus == BookingStatus.COMPLETED || bookingStatus == BookingStatus.CANCELLED) {
                if (conversation.getIsActive()) {
                    conversation.setIsActive(false);
                    conversationRepository.save(conversation);
                }
            }
        }
        
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
    @Transactional(readOnly = true)
    public Page<ConversationResponse> getConversationsBySenderId(String senderId, Pageable pageable) {
        Page<Conversation> conversations = conversationRepository.findBySenderId(senderId, pageable);
        return conversations.map(this::mapToResponse);
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
        // Tính toán flag canChat
        Boolean canChat = true;
        
        // Nếu conversation có isActive = false thì không cho phép chat
        if (conversation.getIsActive() != null && !conversation.getIsActive()) {
            canChat = false;
        }
        
        // Kiểm tra booking của conversation
        if (conversation.getBooking() != null) {
            String bookingStatus = conversation.getBooking().getStatus().name();
            // Nếu booking có status là COMPLETED hoặc CANCELLED thì không cho phép chat
            if ("COMPLETED".equals(bookingStatus) || "CANCELLED".equals(bookingStatus)) {
                canChat = false;
            }
        }
        
        return ConversationResponse.builder()
                .conversationId(conversation.getConversationId())
                .customerId(conversation.getCustomer().getCustomerId())
                .customerName(conversation.getCustomer().getFullName())
                .customerAvatar(conversation.getCustomer().getAvatar())
                .employeeId(conversation.getEmployee() != null ? conversation.getEmployee().getEmployeeId() : null)
                .employeeName(conversation.getEmployee() != null ? conversation.getEmployee().getFullName() : null)
                .employeeAvatar(conversation.getEmployee() != null ? conversation.getEmployee().getAvatar() : null)
                .bookingId(conversation.getBooking() != null ? conversation.getBooking().getBookingId() : null)
                .lastMessage(conversation.getLastMessage())
                .lastMessageTime(conversation.getLastMessageTime())
                .isActive(conversation.getIsActive())
                .canChat(canChat)
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .build();
    }
}