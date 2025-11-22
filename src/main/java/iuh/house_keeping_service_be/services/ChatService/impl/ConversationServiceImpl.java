package iuh.house_keeping_service_be.services.ChatService.impl;

import iuh.house_keeping_service_be.dtos.Chat.ConversationRequest;
import iuh.house_keeping_service_be.dtos.Chat.ConversationResponse;
import iuh.house_keeping_service_be.enums.BookingStatus;
import iuh.house_keeping_service_be.enums.AssignmentStatus;
import iuh.house_keeping_service_be.enums.MessageType;
import iuh.house_keeping_service_be.enums.RecurrenceType;
import iuh.house_keeping_service_be.mappers.RecurringBookingMapper;
import iuh.house_keeping_service_be.models.*;
import iuh.house_keeping_service_be.repositories.*;
import iuh.house_keeping_service_be.services.ChatService.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private final ConversationRepository conversationRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final BookingRepository bookingRepository;
    private final RecurringBookingRepository recurringBookingRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final RecurringBookingMapper recurringBookingMapper;

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
        if (request.getBookingId() == null && request.getRecurringBookingId() == null) {
            throw new IllegalArgumentException("Booking ID hoặc Recurring Booking ID is required");
        }

        // Fetch entities
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        Booking booking = null;
        RecurringBooking recurringBooking = null;

        if (request.getBookingId() != null) {
            booking = bookingRepository.findById(request.getBookingId())
                    .orElseThrow(() -> new RuntimeException("Booking not found"));

            // Reuse conversation if one already exists for this booking
            Optional<Conversation> existingByBooking = conversationRepository.findByBooking_BookingId(booking.getBookingId());
            if (existingByBooking.isPresent()) {
                return mapToResponse(existingByBooking.get());
            }

            if (booking.getRecurringBooking() != null) {
                recurringBooking = booking.getRecurringBooking();
            }
        }

        if (request.getRecurringBookingId() != null) {
            RecurringBooking requestedRecurring = recurringBookingRepository.findById(request.getRecurringBookingId())
                    .orElseThrow(() -> new RuntimeException("Recurring booking not found"));

            if (recurringBooking != null
                    && !recurringBooking.getRecurringBookingId().equals(requestedRecurring.getRecurringBookingId())) {
                throw new IllegalArgumentException("Booking và recurring booking không khớp");
            }
            recurringBooking = requestedRecurring;
        }

        if (recurringBooking != null) {
            Optional<Conversation> existing = conversationRepository.findByRecurringBooking_RecurringBookingId(
                    recurringBooking.getRecurringBookingId());
            if (existing.isPresent()) {
                return mapToResponse(existing.get());
            }

            // Không tạo conversation riêng cho từng booking được sinh từ recurring booking
            return createConversationEntity(customer, employee, null, recurringBooking);
        }

        if (booking != null) {
            return createConversationEntity(customer, employee, booking, null);
        }

        // Fallback create when no booking/recurring context is provided
        return createConversationEntity(customer, employee, null, null);
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
    @Transactional
    public ConversationResponse getConversationByBooking(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // 1. Direct conversation by booking
        Optional<Conversation> conversationOpt = conversationRepository.findByBooking_BookingId(bookingId);
        if (conversationOpt.isPresent()) {
            return mapToResponse(conversationOpt.get());
        }

        RecurringBooking recurringBooking = booking.getRecurringBooking();

        // 2. If booking belongs to recurring, reuse conversation of that recurring booking
        if (recurringBooking != null) {
            conversationOpt = conversationRepository.findByRecurringBooking_RecurringBookingId(
                    recurringBooking.getRecurringBookingId());
            if (conversationOpt.isEmpty()) {
                conversationOpt = conversationRepository.findByBooking_RecurringBooking_RecurringBookingId(
                        recurringBooking.getRecurringBookingId());
            }
            if (conversationOpt.isPresent()) {
                return mapToResponse(conversationOpt.get());
            }

            // Không tạo conversation riêng cho từng booking được sinh từ recurring booking
            Employee resolvedEmployee = recurringBooking.getAssignedEmployee();
            if (resolvedEmployee == null) {
                resolvedEmployee = resolveEmployeeFromBooking(booking);
            }
            if (resolvedEmployee == null) {
                throw new RuntimeException("Không tìm thấy nhân viên được phân công cho lịch định kỳ này");
            }

            ConversationRequest request = new ConversationRequest(
                    booking.getCustomer().getCustomerId(),
                    resolvedEmployee.getEmployeeId(),
                    null,
                    recurringBooking.getRecurringBookingId()
            );

            return createConversation(request);
        }

        // 3. Booking without recurring booking -> create conversation for this booking
        Employee resolvedEmployee = resolveEmployeeFromBooking(booking);
        if (resolvedEmployee == null) {
            throw new RuntimeException("Không tìm thấy nhân viên được phân công cho booking này");
        }

        ConversationRequest request = new ConversationRequest(
                booking.getCustomer().getCustomerId(),
                resolvedEmployee.getEmployeeId(),
                bookingId,
                null
        );

        return createConversation(request);
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

    private ConversationResponse createConversationEntity(
            Customer customer,
            Employee employee,
            Booking booking,
            RecurringBooking recurringBooking
    ) {
        if (employee == null) {
            throw new IllegalArgumentException("Employee is required to create conversation");
        }

        Conversation conversation = new Conversation();
        conversation.setCustomer(customer);
        conversation.setEmployee(employee);
        conversation.setBooking(booking);
        conversation.setRecurringBooking(recurringBooking != null ? recurringBooking : (booking != null ? booking.getRecurringBooking() : null));
        conversation.setIsActive(true);

        Conversation savedConversation = conversationRepository.save(conversation);

        String welcomeMessage = buildWelcomeMessage(employee, booking, recurringBooking);

        ChatMessage welcomeChatMessage = new ChatMessage();
        welcomeChatMessage.setConversation(savedConversation);
        welcomeChatMessage.setSender(employee.getAccount());
        welcomeChatMessage.setMessageType(MessageType.TEXT);
        welcomeChatMessage.setContent(welcomeMessage);
        welcomeChatMessage.setIsRead(false);

        chatMessageRepository.save(welcomeChatMessage);

        savedConversation.setLastMessage(welcomeMessage);
        savedConversation.setLastMessageTime(LocalDateTime.now());
        conversationRepository.save(savedConversation);

        return mapToResponse(savedConversation);
    }

    private String buildWelcomeMessage(Employee employee, Booking booking, RecurringBooking recurringBooking) {
        String employeeName = employee.getFullName() != null ? employee.getFullName() : "nhân viên";

        if (booking != null && booking.getBookingDetails() != null && !booking.getBookingDetails().isEmpty()) {
            String serviceName = booking.getBookingDetails().get(0).getService() != null
                    ? booking.getBookingDetails().get(0).getService().getName()
                    : "dịch vụ";
            String address = booking.getAddress() != null ? booking.getAddress().getFullAddress() : "địa chỉ đã đặt";
            return "Xin chào. Tôi là " + employeeName + " sẽ làm việc cho bạn trong đơn dịch vụ "
                    + serviceName + " vào " + booking.getBookingTime() + " tại " + address
                    + ". Nếu bạn có câu hỏi, hãy nhắn tin tại đây.";
        }

        if (recurringBooking != null) {
            String daysDisplay = recurringBookingMapper.getRecurrenceDaysDisplay(RecurrenceType.WEEKLY, recurringBookingMapper.parseRecurrenceDays(recurringBooking.getRecurrenceDays()));

            String address = recurringBooking.getAddress() != null ? recurringBooking.getAddress().getFullAddress() : "địa chỉ đã đặt";
            return "Xin chào. Tôi là " + employeeName + " sẽ đồng hành cùng lịch sử dụng dịch vụ " + recurringBooking.getRecurringBookingDetails().get(0).getService().getName() + " định kỳ của bạn tại "
                    + address + " vào lúc " + recurringBooking.getBookingTime() + " " + daysDisplay + " mỗi tuần" + ". Nếu bạn có câu hỏi, hãy nhắn tin tại đây.";
        }

        return "Xin chào. Tôi là " + employeeName + ". Hãy nhắn nếu bạn cần hỗ trợ.";
    }

    private Employee resolveEmployeeFromBooking(Booking booking) {
        if (booking == null || booking.getBookingDetails() == null) {
            return null;
        }

        return booking.getBookingDetails().stream()
                .filter(detail -> detail.getAssignments() != null)
                .flatMap(detail -> detail.getAssignments().stream())
                .filter(assign -> assign.getStatus() == null || assign.getStatus() != AssignmentStatus.CANCELLED)
                .map(Assignment::getEmployee)
                .filter(emp -> emp != null && emp.getEmployeeId() != null)
                .findFirst()
                .orElse(null);
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
        if (conversation.getRecurringBooking() != null
                && conversation.getRecurringBooking().getStatus() != null
                && conversation.getRecurringBooking().getStatus() == iuh.house_keeping_service_be.enums.RecurringBookingStatus.CANCELLED) {
            canChat = false;
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
                .recurringBookingId(conversation.getRecurringBooking() != null ? conversation.getRecurringBooking().getRecurringBookingId() : null)
                .lastMessage(conversation.getLastMessage())
                .lastMessageTime(conversation.getLastMessageTime())
                .isActive(conversation.getIsActive())
                .canChat(canChat)
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .build();
    }
}
