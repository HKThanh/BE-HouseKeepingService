package iuh.house_keeping_service_be.services.ChatService.impl;

import iuh.house_keeping_service_be.dtos.Chat.request.ChatMessagePayload;
import iuh.house_keeping_service_be.dtos.Chat.response.ChatMessageResponse;
import iuh.house_keeping_service_be.dtos.Chat.response.ChatParticipantResponse;
import iuh.house_keeping_service_be.dtos.Chat.response.ChatRoomResponse;
import iuh.house_keeping_service_be.enums.AssignmentStatus;
import iuh.house_keeping_service_be.enums.ChatMessageType;
import iuh.house_keeping_service_be.enums.ChatParticipantRole;
import iuh.house_keeping_service_be.models.*;
import iuh.house_keeping_service_be.repositories.*;
import iuh.house_keeping_service_be.services.ChatService.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    private static final EnumSet<AssignmentStatus> ACTIVE_ASSIGNMENT_STATUSES = EnumSet.of(
            AssignmentStatus.ASSIGNED,
            AssignmentStatus.IN_PROGRESS,
            AssignmentStatus.COMPLETED
    );

    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final BookingRepository bookingRepository;
    private final AssignmentRepository assignmentRepository;
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional
    public ChatRoomResponse createOrGetChatRoom(String bookingId, String accountId) {
        log.info("Ensuring chat room for booking {} by account {}", bookingId, accountId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy booking: " + bookingId));
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản: " + accountId));

        ensureParticipationPermission(booking, account);

        ChatRoom chatRoom = chatRoomRepository.findByBooking_BookingId(bookingId)
                .orElseGet(() -> {
                    ChatRoom created = chatRoomRepository.save(ChatRoom.builder()
                            .booking(booking)
                            .build());
                    log.info("Created new chat room {} for booking {}", created.getChatRoomId(), bookingId);
                    return created;
                });

        syncParticipants(chatRoom);

        ChatRoom persisted = chatRoomRepository.findById(chatRoom.getChatRoomId())
                .orElse(chatRoom);
        return toChatRoomResponse(persisted);
    }

    @Override
    @Transactional
    public ChatRoomResponse getChatRoomByBooking(String bookingId, String accountId) {
        log.info("Fetching chat room for booking {} by account {}", bookingId, accountId);
        return createOrGetChatRoom(bookingId, accountId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChatMessageResponse> getMessages(String chatRoomId, Pageable pageable, String accountId) {
        log.info("Retrieving messages for chat room {} by account {}", chatRoomId, accountId);

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng chat: " + chatRoomId));

        if (!chatParticipantRepository.existsByChatRoom_ChatRoomIdAndAccount_AccountId(chatRoomId, accountId)) {
            throw new AccessDeniedException("Bạn không có quyền xem cuộc trò chuyện này");
        }

        return chatMessageRepository.findByChatRoom_ChatRoomIdOrderByCreatedAtDesc(chatRoom.getChatRoomId(), pageable)
                .map(this::toChatMessageResponse);
    }

    @Override
    @Transactional
    public ChatMessageResponse sendMessage(ChatMessagePayload payload, String accountId) {
        log.info("Account {} sending message to chat room {}", accountId, payload.chatRoomId());

        if (!chatParticipantRepository.existsByChatRoom_ChatRoomIdAndAccount_AccountId(payload.chatRoomId(), accountId)) {
            throw new AccessDeniedException("Bạn không có quyền gửi tin nhắn trong cuộc trò chuyện này");
        }

        ChatRoom chatRoom = chatRoomRepository.findById(payload.chatRoomId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng chat: " + payload.chatRoomId()));
        Account sender = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản: " + accountId));

        validatePayload(payload);

        ChatMessage replyTo = null;
        if (StringUtils.hasText(payload.replyToMessageId())) {
            replyTo = chatMessageRepository.findById(payload.replyToMessageId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tin nhắn trả lời"));
            if (!replyTo.getChatRoom().getChatRoomId().equals(chatRoom.getChatRoomId())) {
                throw new IllegalArgumentException("Tin nhắn trả lời phải thuộc cùng cuộc trò chuyện");
            }
        }

        ChatMessage message = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .messageType(payload.messageType())
                .content(normalizeContent(payload))
                .attachmentUrl(resolveAttachment(payload))
                .replyTo(replyTo)
                .revoke(false)
                .build();

        ChatMessage saved = chatMessageRepository.save(message);
        log.debug("Saved chat message {} in room {}", saved.getChatMessageId(), chatRoom.getChatRoomId());

        return toChatMessageResponse(saved);
    }

    @Override
    @Transactional
    public ChatMessageResponse revokeMessage(String messageId, String accountId) {
        log.info("Account {} revoking message {}", accountId, messageId);

        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tin nhắn: " + messageId));

        if (!chatParticipantRepository.existsByChatRoom_ChatRoomIdAndAccount_AccountId(
                message.getChatRoom().getChatRoomId(), accountId)) {
            throw new AccessDeniedException("Bạn không tham gia cuộc trò chuyện này");
        }

        if (!message.getSender().getAccountId().equals(accountId)) {
            throw new AccessDeniedException("Chỉ người gửi mới có thể thu hồi tin nhắn");
        }

        if (!message.isRevoke()) {
            message.setRevoke(true);
            message.setUpdatedAt(LocalDateTime.now());
            chatMessageRepository.save(message);
            log.debug("Message {} marked as revoked", messageId);
        }

        return toChatMessageResponse(message);
    }

    private void ensureParticipationPermission(Booking booking, Account account) {
        String accountId = account.getAccountId();

        boolean isCustomer = Optional.ofNullable(booking.getCustomer())
                .map(Customer::getAccount)
                .map(Account::getAccountId)
                .map(accountId::equals)
                .orElse(false);

        String employeeId = employeeRepository.findByAccount_AccountId(accountId)
                .map(Employee::getEmployeeId)
                .orElse(null);

        boolean isEmployee = employeeId != null && assignmentRepository
                .existsByBookingDetail_Booking_BookingIdAndEmployee_EmployeeIdAndStatusIn(
                        booking.getBookingId(),
                        employeeId,
                        ACTIVE_ASSIGNMENT_STATUSES);

        if (!isCustomer && !isEmployee) {
            throw new AccessDeniedException("Tài khoản không có quyền truy cập cuộc trò chuyện này");
        }
    }

    private void syncParticipants(ChatRoom chatRoom) {
        Booking booking = chatRoom.getBooking();

        if (booking.getCustomer() != null) {
            addParticipantIfMissing(chatRoom, booking.getCustomer().getAccount(), ChatParticipantRole.CUSTOMER);
        }

        List<Account> employeeAccounts = assignmentRepository.findByBookingIdWithStatus(booking.getBookingId()).stream()
                .filter(assignment -> assignment.getEmployee() != null)
                .filter(assignment -> ACTIVE_ASSIGNMENT_STATUSES.contains(assignment.getStatus()))
                .map(assignment -> assignment.getEmployee().getAccount())
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(Account::getAccountId, account -> account, (first, second) -> first),
                        map -> map.values().stream().toList()
                ));

        employeeAccounts.forEach(account -> addParticipantIfMissing(chatRoom, account, ChatParticipantRole.EMPLOYEE));
    }

    private void addParticipantIfMissing(ChatRoom chatRoom, Account account, ChatParticipantRole role) {
        if (account == null) {
            return;
        }

        if (chatParticipantRepository.existsByChatRoom_ChatRoomIdAndAccount_AccountId(
                chatRoom.getChatRoomId(), account.getAccountId())) {
            return;
        }

        ChatParticipant participant = ChatParticipant.builder()
                .chatRoom(chatRoom)
                .account(account)
                .participantRole(role)
                .build();

        chatParticipantRepository.save(participant);
        log.debug("Added participant {} with role {} to chat room {}", account.getAccountId(), role, chatRoom.getChatRoomId());
    }

    private void validatePayload(ChatMessagePayload payload) {
        if (payload.messageType() == ChatMessageType.TEXT) {
            if (!StringUtils.hasText(payload.content())) {
                throw new IllegalArgumentException("Nội dung tin nhắn không được để trống");
            }
        } else if (payload.messageType() == ChatMessageType.IMAGE) {
            if (!StringUtils.hasText(payload.attachmentUrl())) {
                throw new IllegalArgumentException("Ảnh gửi phải có đường dẫn hợp lệ");
            }
        } else {
            throw new IllegalArgumentException("Loại tin nhắn không được hỗ trợ");
        }
    }

    private String normalizeContent(ChatMessagePayload payload) {
        if (payload.messageType() == ChatMessageType.TEXT) {
            return payload.content().trim();
        }
        return StringUtils.hasText(payload.content()) ? payload.content().trim() : null;
    }

    private String resolveAttachment(ChatMessagePayload payload) {
        if (payload.messageType() == ChatMessageType.IMAGE) {
            return StringUtils.trimWhitespace(payload.attachmentUrl());
        }
        return null;
    }

    private ChatRoomResponse toChatRoomResponse(ChatRoom chatRoom) {
        List<ChatParticipantResponse> participants = chatParticipantRepository
                .findByChatRoom_ChatRoomId(chatRoom.getChatRoomId()).stream()
                .map(this::toParticipantResponse)
                .sorted(Comparator.comparing(ChatParticipantResponse::role).thenComparing(ChatParticipantResponse::displayName))
                .toList();

        return new ChatRoomResponse(
                chatRoom.getChatRoomId(),
                chatRoom.getBooking().getBookingId(),
                chatRoom.getBooking().getBookingCode(),
                participants,
                chatRoom.getCreatedAt(),
                chatRoom.getUpdatedAt()
        );
    }

    private ChatParticipantResponse toParticipantResponse(ChatParticipant participant) {
        Account account = participant.getAccount();
        return new ChatParticipantResponse(
                account.getAccountId(),
                participant.getParticipantRole(),
                resolveDisplayName(account),
                resolveAvatar(account)
        );
    }

    private ChatMessageResponse toChatMessageResponse(ChatMessage message) {
        Account sender = message.getSender();
        ChatParticipantRole role = resolveRole(sender);

        ChatMessageResponse.ReplyMessageSummary replySummary = null;
        if (message.getReplyTo() != null) {
            ChatMessage replied = message.getReplyTo();
            replySummary = new ChatMessageResponse.ReplyMessageSummary(
                    replied.getChatMessageId(),
                    replied.getMessageType(),
                    buildReplyPreview(replied),
                    replied.isRevoke()
            );
        }

        return new ChatMessageResponse(
                message.getChatMessageId(),
                message.getChatRoom().getChatRoomId(),
                sender.getAccountId(),
                role,
                resolveDisplayName(sender),
                message.getMessageType(),
                message.isRevoke() ? null : message.getContent(),
                message.isRevoke() ? null : message.getAttachmentUrl(),
                message.isRevoke(),
                replySummary,
                message.getCreatedAt(),
                message.getUpdatedAt()
        );
    }

    private String resolveDisplayName(Account account) {
        return customerRepository.findByAccount_AccountId(account.getAccountId())
                .map(Customer::getFullName)
                .or(() -> employeeRepository.findByAccount_AccountId(account.getAccountId())
                        .map(Employee::getFullName))
                .orElse(account.getUsername());
    }

    private String resolveAvatar(Account account) {
        return customerRepository.findByAccount_AccountId(account.getAccountId())
                .map(Customer::getAvatar)
                .or(() -> employeeRepository.findByAccount_AccountId(account.getAccountId())
                        .map(Employee::getAvatar))
                .orElse(null);
    }

    private ChatParticipantRole resolveRole(Account account) {
        if (customerRepository.findByAccount_AccountId(account.getAccountId()).isPresent()) {
            return ChatParticipantRole.CUSTOMER;
        }
        if (employeeRepository.findByAccount_AccountId(account.getAccountId()).isPresent()) {
            return ChatParticipantRole.EMPLOYEE;
        }
        return ChatParticipantRole.ADMIN;
    }

    private String buildReplyPreview(ChatMessage message) {
        if (message.isRevoke()) {
            return "Tin nhắn đã bị thu hồi";
        }
        if (message.getMessageType() == ChatMessageType.IMAGE) {
            return StringUtils.hasText(message.getContent()) ? message.getContent() : "Đã gửi một hình ảnh";
        }
        return Optional.ofNullable(message.getContent()).map(content -> {
            String normalized = content.trim();
            return normalized.length() > 120 ? normalized.substring(0, 120) + "..." : normalized;
        }).orElse("");
    }
}