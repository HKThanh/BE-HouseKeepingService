package iuh.house_keeping_service_be.services.ChatService.impl;

import iuh.house_keeping_service_be.dtos.Chat.request.SendMessageRequest;
import iuh.house_keeping_service_be.dtos.Chat.response.ChatMessageResponse;
import iuh.house_keeping_service_be.dtos.Chat.response.ConversationParticipantResponse;
import iuh.house_keeping_service_be.dtos.Chat.response.ConversationSummaryResponse;
import iuh.house_keeping_service_be.dtos.Chat.response.MessagePageResponse;
import iuh.house_keeping_service_be.dtos.Chat.response.ReplyMessageResponse;
import iuh.house_keeping_service_be.enums.ChatMessageType;
import iuh.house_keeping_service_be.models.Account;
import iuh.house_keeping_service_be.models.ChatMessage;
import iuh.house_keeping_service_be.models.Conversation;
import iuh.house_keeping_service_be.models.Customer;
import iuh.house_keeping_service_be.models.Employee;
import iuh.house_keeping_service_be.repositories.ChatMessageRepository;
import iuh.house_keeping_service_be.repositories.ConversationRepository;
import iuh.house_keeping_service_be.repositories.CustomerRepository;
import iuh.house_keeping_service_be.repositories.EmployeeRepository;
import iuh.house_keeping_service_be.services.ChatService.ChatService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ChatServiceImpl implements ChatService {

    private static final int MAX_PAGE_SIZE = 100;

    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final EmployeeRepository employeeRepository;
    private final CustomerRepository customerRepository;

    @Override
    public Conversation ensureConversation(String employeeId, String customerId) {
        Optional<Conversation> existing = conversationRepository
                .findByEmployee_EmployeeIdAndCustomer_CustomerId(employeeId, customerId);
        if (existing.isPresent()) {
            return existing.get();
        }

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên"));
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khách hàng"));

        Conversation conversation = new Conversation();
        conversation.setEmployee(employee);
        conversation.setCustomer(customer);
        conversation.setLastMessageAt(LocalDateTime.now());

        Conversation saved = conversationRepository.save(conversation);
        log.debug("Created new conversation {} between employee {} and customer {}",
                saved.getConversationId(), employeeId, customerId);
        return saved;
    }

    @Override
    public ChatMessageResponse sendMessage(String accountId, SendMessageRequest request) {
        Conversation conversation = getConversationWithParticipantCheck(request.conversationId(), accountId);

        validateMessagePayload(request);

        Account sender = resolveSenderAccount(conversation, accountId);

        ChatMessage message = new ChatMessage();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setMessageType(request.messageType());
        message.setContent(normalizeContent(request));
        message.setFileName(request.fileName());
        message.setFileUrl(request.fileUrl());
        message.setFileSize(request.fileSize());

        if (StringUtils.hasText(request.replyToMessageId())) {
            ChatMessage replyTo = chatMessageRepository.findById(request.replyToMessageId())
                    .orElseThrow(() -> new IllegalArgumentException("Tin nhắn phản hồi không tồn tại"));
            if (!replyTo.getConversation().getConversationId().equals(conversation.getConversationId())) {
                throw new IllegalStateException("Tin nhắn phản hồi không thuộc hội thoại hiện tại");
            }
            message.setReplyTo(replyTo);
        }

        ChatMessage savedMessage = chatMessageRepository.save(message);
        conversation.touchLastMessage(savedMessage.getSentAt());

        ChatMessage persisted = chatMessageRepository.findWithConversationByMessageId(savedMessage.getMessageId())
                .orElseThrow(() -> new IllegalStateException("Không thể tải tin nhắn vừa gửi"));

        return toMessageResponse(persisted, persisted.getConversation());
    }

    @Override
    public ChatMessageResponse recallMessage(String accountId, String messageId) {
        ChatMessage message = chatMessageRepository.findWithConversationByMessageId(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tin nhắn"));

        if (!message.getSender().getAccountId().equals(accountId)) {
            throw new IllegalStateException("Chỉ có thể thu hồi tin nhắn của chính bạn");
        }

        message.markRevoked();
        ChatMessage saved = chatMessageRepository.save(message);

        return toMessageResponse(saved, saved.getConversation());
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<ConversationSummaryResponse> getUserConversations(String accountId) {
        List<Conversation> conversations = conversationRepository
                .findByEmployee_Account_AccountIdOrCustomer_Account_AccountId(accountId, accountId);

        return conversations.stream()
                .map(conversation -> {
                    Optional<ChatMessage> lastMessageOpt = chatMessageRepository
                            .findTop1ByConversation_ConversationIdOrderBySentAtDesc(conversation.getConversationId());

                    ChatMessageResponse lastMessageResponse = lastMessageOpt
                            .map(message -> {
                                message.setConversation(conversation);
                                return toMessageResponse(message, conversation);
                            })
                            .orElse(null);

                    return new ConversationSummaryResponse(
                            conversation.getConversationId(),
                            toParticipant(conversation, accountId),
                            lastMessageResponse,
                            conversation.getLastMessageAt()
                    );
                })
                .sorted(Comparator.comparing(ConversationSummaryResponse::lastMessageAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public MessagePageResponse getConversationMessages(String accountId, String conversationId, int page, int size) {
        Conversation conversation = getConversationWithParticipantCheck(conversationId, accountId);

        int pageSize = Math.max(1, Math.min(size, MAX_PAGE_SIZE));
        Pageable pageable = PageRequest.of(Math.max(page, 0), pageSize);

        Page<ChatMessage> messagePage = chatMessageRepository
                .findByConversation_ConversationIdOrderBySentAtDesc(conversationId, pageable);

        List<ChatMessageResponse> responses = messagePage.getContent().stream()
                .map(message -> {
                    message.setConversation(conversation);
                    return toMessageResponse(message, conversation);
                })
                .collect(Collectors.toList());

        return new MessagePageResponse(
                responses,
                messagePage.getNumber(),
                messagePage.getSize(),
                messagePage.getTotalElements(),
                messagePage.getTotalPages()
        );
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public boolean isParticipant(String conversationId, String accountId) {
        return conversationRepository.findWithParticipantsByConversationId(conversationId)
                .map(conversation -> isParticipant(conversation, accountId))
                .orElse(false);
    }

    private Conversation getConversationWithParticipantCheck(String conversationId, String accountId) {
        Conversation conversation = conversationRepository.findWithParticipantsByConversationId(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy cuộc hội thoại"));

        if (!isParticipant(conversation, accountId)) {
            throw new IllegalStateException("Bạn không có quyền truy cập cuộc hội thoại này");
        }

        return conversation;
    }

    private boolean isParticipant(Conversation conversation, String accountId) {
        return conversation.getEmployee().getAccount().getAccountId().equals(accountId)
                || conversation.getCustomer().getAccount().getAccountId().equals(accountId);
    }

    private Account resolveSenderAccount(Conversation conversation, String accountId) {
        if (conversation.getEmployee().getAccount().getAccountId().equals(accountId)) {
            return conversation.getEmployee().getAccount();
        }
        if (conversation.getCustomer().getAccount().getAccountId().equals(accountId)) {
            return conversation.getCustomer().getAccount();
        }
        throw new IllegalStateException("Không xác định được thông tin người gửi");
    }

    private void validateMessagePayload(SendMessageRequest request) {
        if (request.messageType() == ChatMessageType.TEXT) {
            if (!StringUtils.hasText(request.content())) {
                throw new IllegalArgumentException("Nội dung tin nhắn không được để trống");
            }
        } else if (request.messageType() == ChatMessageType.FILE) {
            if (!StringUtils.hasText(request.fileUrl())) {
                throw new IllegalArgumentException("Tệp đính kèm phải có đường dẫn");
            }
        } else {
            throw new IllegalArgumentException("Loại tin nhắn không hợp lệ");
        }
    }

    private String normalizeContent(SendMessageRequest request) {
        if (request.messageType() == ChatMessageType.TEXT) {
            return StringUtils.hasText(request.content()) ? request.content().trim() : null;
        }
        return request.content();
    }

    private ChatMessageResponse toMessageResponse(ChatMessage message, Conversation conversation) {
        ConversationParticipantResponse senderInfo = determineSenderInfo(conversation, message.getSender().getAccountId());
        ReplyMessageResponse replyResponse = null;
        if (message.getReplyTo() != null) {
            ChatMessage reply = message.getReplyTo();
            replyResponse = new ReplyMessageResponse(
                    reply.getMessageId(),
                    reply.getMessageType(),
                    reply.isRevoked() ? null : reply.getContent(),
                    reply.isRevoked() ? null : reply.getFileName(),
                    reply.isRevoked() ? null : reply.getFileUrl(),
                    reply.isRevoked(),
                    reply.getSentAt()
            );
        }

        return new ChatMessageResponse(
                message.getMessageId(),
                conversation.getConversationId(),
                senderInfo.accountId(),
                senderInfo.fullName(),
                senderInfo.avatar(),
                senderInfo.role(),
                message.getMessageType(),
                message.isRevoked() ? null : message.getContent(),
                message.isRevoked() ? null : message.getFileName(),
                message.isRevoked() ? null : message.getFileUrl(),
                message.isRevoked() ? null : message.getFileSize(),
                message.isRevoked(),
                replyResponse,
                message.getSentAt(),
                message.getUpdatedAt()
        );
    }

    private ConversationParticipantResponse determineSenderInfo(Conversation conversation, String accountId) {
        if (conversation.getEmployee().getAccount().getAccountId().equals(accountId)) {
            Employee employee = conversation.getEmployee();
            return new ConversationParticipantResponse(
                    employee.getAccount().getAccountId(),
                    employee.getEmployeeId(),
                    employee.getFullName(),
                    employee.getAvatar(),
                    "EMPLOYEE"
            );
        }

        Customer customer = conversation.getCustomer();
        return new ConversationParticipantResponse(
                customer.getAccount().getAccountId(),
                customer.getCustomerId(),
                customer.getFullName(),
                customer.getAvatar(),
                "CUSTOMER"
        );
    }

    private ConversationParticipantResponse toParticipant(Conversation conversation, String viewerAccountId) {
        boolean viewerIsEmployee = conversation.getEmployee().getAccount().getAccountId().equals(viewerAccountId);
        if (viewerIsEmployee) {
            Customer customer = conversation.getCustomer();
            return new ConversationParticipantResponse(
                    customer.getAccount().getAccountId(),
                    customer.getCustomerId(),
                    customer.getFullName(),
                    customer.getAvatar(),
                    "CUSTOMER"
            );
        }

        Employee employee = conversation.getEmployee();
        return new ConversationParticipantResponse(
                employee.getAccount().getAccountId(),
                employee.getEmployeeId(),
                employee.getFullName(),
                employee.getAvatar(),
                "EMPLOYEE"
        );
    }
}