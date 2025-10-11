package iuh.house_keeping_service_be.services.MessageService.impl;

import iuh.house_keeping_service_be.dtos.Chat.request.AttachmentPayload;
import iuh.house_keeping_service_be.dtos.Chat.request.ChatMessageRequest;
import iuh.house_keeping_service_be.dtos.Chat.response.AttachmentResponse;
import iuh.house_keeping_service_be.dtos.Chat.response.ChatParticipantDto;
import iuh.house_keeping_service_be.dtos.Chat.response.MessageResponse;
import iuh.house_keeping_service_be.dtos.Chat.response.ReplyToMessageResponse;
import iuh.house_keeping_service_be.enums.ChatParticipantType;
import iuh.house_keeping_service_be.enums.MessageType;
import iuh.house_keeping_service_be.exceptions.ChatAccessDeniedException;
import iuh.house_keeping_service_be.exceptions.ChatValidationException;
import iuh.house_keeping_service_be.exceptions.ResourceNotFoundException;
import iuh.house_keeping_service_be.models.*;
import iuh.house_keeping_service_be.repositories.*;
import iuh.house_keeping_service_be.services.ConversationService.ConversationService;
import iuh.house_keeping_service_be.services.MessageService.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageServiceImpl implements MessageService {

    private static final Set<String> IMAGE_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final Set<String> VIDEO_CONTENT_TYPES = Set.of("video/mp4", "video/webm");

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final ConversationService conversationService;

    @Override
    @Transactional
    public MessageResponse sendMessage(ChatMessageRequest request, String username) {
        Account sender = getAccountByUsername(username);
        Conversation conversation = conversationRepository.findById(request.conversationId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cuộc hội thoại: " + request.conversationId()));

        if (!conversationService.isParticipant(conversation.getConversationId(), sender.getAccountId())) {
            throw new ChatAccessDeniedException("Bạn không có quyền gửi tin nhắn trong cuộc hội thoại này");
        }

        validateMessageRequest(request);

        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setMessageType(request.messageType());
        message.setContent(trimContent(request.content()));

        if (request.replyToMessageId() != null && !request.replyToMessageId().isBlank()) {
            Message replyTo = messageRepository.findById(request.replyToMessageId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tin nhắn trả lời không tồn tại"));
            if (!Objects.equals(replyTo.getConversation().getConversationId(), conversation.getConversationId())) {
                throw new ChatValidationException("Tin nhắn trả lời phải thuộc cùng cuộc hội thoại");
            }
            message.setReplyTo(replyTo);
        }

        List<MessageAttachment> attachments = request.attachments().stream()
                .map(payload -> toAttachment(payload, message, request.messageType()))
                .collect(Collectors.toList());
        message.setAttachments(attachments);

        Message saved = messageRepository.save(message);
        log.debug("Persisted message {} in conversation {}", saved.getMessageId(), conversation.getConversationId());

        return mapToResponse(saved, new HashMap<>());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MessageResponse> getConversationMessages(String conversationId, Pageable pageable, String username) {
        Account account = getAccountByUsername(username);
        if (!conversationService.isParticipant(conversationId, account.getAccountId())) {
            throw new ChatAccessDeniedException("Bạn không có quyền xem tin nhắn của cuộc hội thoại này");
        }

        Page<Message> messages = messageRepository.findByConversation_ConversationId(conversationId, pageable);
        Map<String, ChatParticipantDto> senderCache = new HashMap<>();
        List<MessageResponse> responses = messages.getContent().stream()
                .map(message -> mapToResponse(message, senderCache))
                .toList();

        return new PageImpl<>(responses, pageable, messages.getTotalElements());
    }

    @Override
    @Transactional
    public MessageResponse recallMessage(String messageId, String username) {
        Account account = getAccountByUsername(username);
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tin nhắn: " + messageId));

        if (!conversationService.isParticipant(message.getConversation().getConversationId(), account.getAccountId())) {
            throw new ChatAccessDeniedException("Bạn không có quyền thao tác trên tin nhắn này");
        }
        if (!Objects.equals(message.getSender().getAccountId(), account.getAccountId())) {
            throw new ChatAccessDeniedException("Chỉ người gửi mới có thể thu hồi tin nhắn");
        }
        if (message.isRevoked()) {
            return mapToResponse(message, new HashMap<>());
        }

        message.setRevoked(true);
        message.setContent(null);
        message.getAttachments().clear();

        Message saved = messageRepository.save(message);
        log.debug("Message {} has been revoked by account {}", saved.getMessageId(), account.getAccountId());
        return mapToResponse(saved, new HashMap<>());
    }

    private Account getAccountByUsername(String username) {
        return accountRepository.findByUsername(username)
                .orElseThrow(() -> new ChatAccessDeniedException("Tài khoản không hợp lệ"));
    }

    private void validateMessageRequest(ChatMessageRequest request) {
        if (request.messageType() == MessageType.TEXT) {
            if (request.content() == null || request.content().isBlank()) {
                throw new ChatValidationException("Nội dung tin nhắn không được để trống");
            }
            if (!request.attachments().isEmpty()) {
                throw new ChatValidationException("Tin nhắn văn bản không được đính kèm tệp");
            }
        } else if (request.messageType() == MessageType.IMAGE || request.messageType() == MessageType.VIDEO) {
            if (request.attachments().isEmpty()) {
                throw new ChatValidationException("Tin nhắn đa phương tiện phải có tệp đính kèm");
            }
            for (AttachmentPayload payload : request.attachments()) {
                validateAttachmentPayload(payload, request.messageType());
            }
        } else {
            throw new ChatValidationException("Loại tin nhắn không hợp lệ");
        }
    }

    private void validateAttachmentPayload(AttachmentPayload payload, MessageType messageType) {
        String contentType = payload.contentType();
        if (messageType == MessageType.IMAGE && !IMAGE_CONTENT_TYPES.contains(contentType)) {
            throw new ChatValidationException("Tệp đính kèm phải là hình ảnh hợp lệ");
        }
        if (messageType == MessageType.VIDEO && !VIDEO_CONTENT_TYPES.contains(contentType)) {
            throw new ChatValidationException("Tệp đính kèm phải là video hợp lệ");
        }
    }

    private MessageAttachment toAttachment(AttachmentPayload payload, Message message, MessageType messageType) {
        validateAttachmentPayload(payload, messageType);
        MessageAttachment attachment = new MessageAttachment();
        attachment.setMessage(message);
        attachment.setUrl(payload.url());
        attachment.setPublicId(payload.publicId());
        attachment.setContentType(payload.contentType());
        attachment.setFileSize(payload.size());
        return attachment;
    }

    private String trimContent(String content) {
        if (content == null) {
            return null;
        }
        String trimmed = content.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private MessageResponse mapToResponse(Message message, Map<String, ChatParticipantDto> senderCache) {
        ChatParticipantDto sender = resolveParticipant(message.getSender(), senderCache);
        ReplyToMessageResponse replyTo = null;
        if (message.getReplyTo() != null) {
            Message replied = message.getReplyTo();
            replyTo = new ReplyToMessageResponse(
                    replied.getMessageId(),
                    replied.getMessageType(),
                    replied.isRevoked() ? null : replied.getContent(),
                    replied.isRevoked()
            );
        }

        List<AttachmentResponse> attachments = message.isRevoked()
                ? List.of()
                : message.getAttachments().stream()
                .map(attachment -> new AttachmentResponse(
                        attachment.getAttachmentId(),
                        attachment.getUrl(),
                        attachment.getContentType(),
                        attachment.getFileSize(),
                        attachment.getPublicId(),
                        attachment.getCreatedAt()
                ))
                .toList();

        String content = message.isRevoked() ? null : message.getContent();

        return new MessageResponse(
                message.getMessageId(),
                message.getConversation().getConversationId(),
                sender,
                message.getMessageType(),
                content,
                message.isRevoked(),
                replyTo,
                attachments,
                message.getCreatedAt(),
                message.getUpdatedAt()
        );
    }

    private ChatParticipantDto resolveParticipant(Account account, Map<String, ChatParticipantDto> cache) {
        return cache.computeIfAbsent(account.getAccountId(), id -> {
            return customerRepository.findByAccount_AccountId(id)
                    .map(customer -> new ChatParticipantDto(
                            customer.getCustomerId(),
                            account.getAccountId(),
                            account.getUsername(),
                            ChatParticipantType.CUSTOMER,
                            customer.getFullName(),
                            customer.getAvatar()
                    ))
                    .or(() -> employeeRepository.findByAccount_AccountId(id)
                            .map(employee -> new ChatParticipantDto(
                                    employee.getEmployeeId(),
                                    account.getAccountId(),
                                    account.getUsername(),
                                    ChatParticipantType.EMPLOYEE,
                                    employee.getFullName(),
                                    employee.getAvatar()
                            )))
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin người gửi"));
        });
    }
}