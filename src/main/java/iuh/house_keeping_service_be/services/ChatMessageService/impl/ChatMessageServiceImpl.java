package iuh.house_keeping_service_be.services.ChatMessageService.impl;

import iuh.house_keeping_service_be.dtos.ChatMessage.request.ChatMessageDeleteRequest;
import iuh.house_keeping_service_be.dtos.ChatMessage.request.ChatMessageRecallRequest;
import iuh.house_keeping_service_be.dtos.ChatMessage.request.ChatMessageReplyRequest;
import iuh.house_keeping_service_be.dtos.ChatMessage.request.ChatMessageSendRequest;
import iuh.house_keeping_service_be.dtos.ChatMessage.response.ChatMessageResponse;
import iuh.house_keeping_service_be.dtos.ChatMessage.response.ChatMessageSocketPayload;
import iuh.house_keeping_service_be.exceptions.ChatMessageOperationException;
import iuh.house_keeping_service_be.exceptions.ResourceNotFoundException;
import iuh.house_keeping_service_be.models.Account;
import iuh.house_keeping_service_be.models.ChatMessage;
import iuh.house_keeping_service_be.models.ChatRoom;
import iuh.house_keeping_service_be.repositories.AccountRepository;
import iuh.house_keeping_service_be.repositories.ChatMessageRepository;
import iuh.house_keeping_service_be.repositories.ChatRoomRepository;
import iuh.house_keeping_service_be.services.ChatMessageService.ChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessageServiceImpl implements ChatMessageService {

    private static final Duration DEFAULT_RECALL_WINDOW = Duration.ofMinutes(10);

    private final ChatMessageRepository chatMessageRepository;
    private final AccountRepository accountRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public ChatMessageResponse sendMessage(String chatRoomId, ChatMessageSendRequest request) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> ResourceNotFoundException.withCustomMessage(
                        "Không tìm thấy phòng chat: " + chatRoomId
                ));
        Account sender = accountRepository.findById(request.senderAccountId())
                .orElseThrow(() -> ResourceNotFoundException.withCustomMessage(
                        "Không tìm thấy tài khoản gửi: " + request.senderAccountId()
                ));

        validateParticipant(chatRoom, sender.getAccountId());

        ChatMessage parentMessage = resolveParentMessage(chatRoom, request.parentMessageId());

        ChatMessage message = new ChatMessage();
        message.setChatRoom(chatRoom);
        message.setSender(sender);
        message.setMessageText(request.messageText());
        message.setPayloadType(request.payloadType());
        message.setPayloadData(request.payloadData());
        message.setParentMessage(parentMessage);

        ChatMessage saved = chatMessageRepository.save(message);
        chatRoom.setLastMessageAt(saved.getSentAt());
        chatRoomRepository.save(chatRoom);

        ChatMessageResponse response = toResponse(saved);
        publishSocketEvent(chatRoom.getChatRoomId(), ChatMessageSocketPayload.EventType.CREATED, response);
        log.debug("Created message {} in chat room {}", saved.getChatMessageId(), chatRoomId);
        return response;
    }

    @Override
    @Transactional
    public ChatMessageResponse replyMessage(String parentMessageId, ChatMessageReplyRequest request) {
        ChatMessage parentMessage = chatMessageRepository.findById(parentMessageId)
                .orElseThrow(() -> ResourceNotFoundException.withCustomMessage(
                        "Không tìm thấy tin nhắn để trả lời: " + parentMessageId
                ));

        ChatMessageSendRequest sendRequest = new ChatMessageSendRequest(
                request.senderAccountId(),
                request.messageText(),
                request.payloadType(),
                request.payloadData(),
                parentMessageId
        );

        ChatMessageResponse response = sendMessage(parentMessage.getChatRoom().getChatRoomId(), sendRequest);
        log.debug("Created reply message for parent {}", parentMessageId);
        return response;
    }

    @Override
    @Transactional
    public ChatMessageResponse deleteMessage(String messageId, ChatMessageDeleteRequest request) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> ResourceNotFoundException.withCustomMessage(
                        "Không tìm thấy tin nhắn: " + messageId
                ));

        Account actor = accountRepository.findById(request.accountId())
                .orElseThrow(() -> ResourceNotFoundException.withCustomMessage(
                        "Không tìm thấy tài khoản: " + request.accountId()
                ));

        validateParticipant(message.getChatRoom(), actor.getAccountId());

        if (!message.isDeleted()) {
            message.markDeleted(actor.getAccountId());
            chatMessageRepository.save(message);
            log.debug("Message {} deleted by {}", messageId, actor.getAccountId());
        }

        ChatMessageResponse response = toResponse(message);
        publishSocketEvent(message.getChatRoom().getChatRoomId(), ChatMessageSocketPayload.EventType.DELETED, response);
        return response;
    }

    @Override
    @Transactional
    public ChatMessageResponse recallMessage(String messageId, ChatMessageRecallRequest request) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> ResourceNotFoundException.withCustomMessage(
                        "Không tìm thấy tin nhắn: " + messageId
                ));

        if (!message.getSender().getAccountId().equals(request.accountId())) {
            throw new ChatMessageOperationException("Chỉ người gửi mới có thể thu hồi tin nhắn");
        }

        if (message.isRecalled()) {
            return toResponse(message);
        }

        if (message.isDeleted()) {
            throw new ChatMessageOperationException("Tin nhắn đã bị xóa và không thể thu hồi");
        }

        if (!message.canBeRecalled(DEFAULT_RECALL_WINDOW)) {
            throw new ChatMessageOperationException("Tin nhắn đã quá thời gian cho phép để thu hồi");
        }

        message.markRecalled(request.accountId());
        chatMessageRepository.save(message);
        log.debug("Message {} recalled by {}", messageId, request.accountId());
        ChatMessageResponse response = toResponse(message);
        publishSocketEvent(message.getChatRoom().getChatRoomId(), ChatMessageSocketPayload.EventType.RECALLED, response);
        return response;
    }

    private ChatMessage resolveParentMessage(ChatRoom chatRoom, String parentMessageId) {
        if (parentMessageId == null || parentMessageId.isBlank()) {
            return null;
        }

        ChatMessage parentMessage = chatMessageRepository.findById(parentMessageId)
                .orElseThrow(() -> ResourceNotFoundException.withCustomMessage(
                        "Không tìm thấy tin nhắn để trả lời: " + parentMessageId
                ));

        if (!parentMessage.getChatRoom().getChatRoomId().equals(chatRoom.getChatRoomId())) {
            throw new ChatMessageOperationException("Tin nhắn cha không thuộc phòng chat này");
        }

        if (parentMessage.isDeleted()) {
            throw new ChatMessageOperationException("Không thể trả lời tin nhắn đã bị xóa");
        }

        return parentMessage;
    }

    @Override
    @Transactional
    public boolean addParticipantToChatRoom(String chatRoomId, String accountId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElse(null);
        if (chatRoom == null) {
            log.warn("Cannot add participant to non-existent chat room: {}", chatRoomId);
            return false;
        }
        
        Account account = accountRepository.findById(accountId).orElse(null);
        if (account == null) {
            log.warn("Cannot add non-existent account to chat room: {}", accountId);
            return false;
        }
        
        // Check if user is already a participant
        boolean isCustomer = chatRoom.getCustomerAccount() != null 
            && chatRoom.getCustomerAccount().getAccountId().equals(accountId);
        boolean isEmployee = chatRoom.getEmployeeAccount() != null 
            && chatRoom.getEmployeeAccount().getAccountId().equals(accountId);
            
        if (isCustomer || isEmployee) {
            log.debug("Account {} is already a participant in chat room {}", accountId, chatRoomId);
            return true;
        }
        
        // Add as customer if no customer exists, otherwise as employee if no employee exists
        if (chatRoom.getCustomerAccount() == null) {
            chatRoom.setCustomerAccount(account);
            chatRoomRepository.save(chatRoom);
            log.info("Added account {} as customer to chat room {}", accountId, chatRoomId);
            return true;
        } else if (chatRoom.getEmployeeAccount() == null) {
            chatRoom.setEmployeeAccount(account);
            chatRoomRepository.save(chatRoom);
            log.info("Added account {} as employee to chat room {}", accountId, chatRoomId);
            return true;
        }
        
        log.warn("Chat room {} already has both customer and employee, cannot add account {}", 
                chatRoomId, accountId);
        return false;
    }

    private void validateParticipant(ChatRoom chatRoom, String accountId) {
        if (chatRoom == null) {
            throw new ResourceNotFoundException("Phòng chat không hợp lệ");
        }

        log.debug("Validating participant - AccountId: {}", accountId);
        log.debug("ChatRoom - CustomerAccount: {}, EmployeeAccount: {}", 
                 chatRoom.getCustomerAccount() != null ? chatRoom.getCustomerAccount().getAccountId() : "null",
                 chatRoom.getEmployeeAccount() != null ? chatRoom.getEmployeeAccount().getAccountId() : "null");

        boolean isCustomer = chatRoom.getCustomerAccount() != null
                && chatRoom.getCustomerAccount().getAccountId().equals(accountId);
        boolean isEmployee = chatRoom.getEmployeeAccount() != null
                && chatRoom.getEmployeeAccount().getAccountId().equals(accountId);

        log.debug("Validation results - isCustomer: {}, isEmployee: {}", isCustomer, isEmployee);

        if (!isCustomer && !isEmployee) {
            // Try to add the participant to the chat room automatically
            log.info("Account {} not found in chat room {}, attempting to add dynamically", accountId, chatRoom.getChatRoomId());
            
            boolean added = addParticipantToChatRoom(chatRoom.getChatRoomId(), accountId);
            if (!added) {
                log.error("Account {} is not a participant in chat room {} and could not be added. Customer: {}, Employee: {}", 
                         accountId, chatRoom.getChatRoomId(),
                         chatRoom.getCustomerAccount() != null ? chatRoom.getCustomerAccount().getAccountId() : "null",
                         chatRoom.getEmployeeAccount() != null ? chatRoom.getEmployeeAccount().getAccountId() : "null");
                throw new ChatMessageOperationException("Tài khoản không thuộc phòng chat này");
            }
            
            // Refresh the chat room data after adding participant
            ChatRoom updatedChatRoom = chatRoomRepository.findById(chatRoom.getChatRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Phòng chat không hợp lệ"));
            
            // Update the reference to the updated chat room
            chatRoom.setCustomerAccount(updatedChatRoom.getCustomerAccount());
            chatRoom.setEmployeeAccount(updatedChatRoom.getEmployeeAccount());
            
            log.info("Successfully added account {} to chat room {}", accountId, chatRoom.getChatRoomId());
        }
        
        log.debug("Participant validation successful for account: {}", accountId);
    }

    private void publishSocketEvent(String chatRoomId, ChatMessageSocketPayload.EventType eventType, ChatMessageResponse response) {
        if (chatRoomId == null) {
            log.warn("Không thể phát sự kiện WebSocket vì chatRoomId null");
            return;
        }

        messagingTemplate.convertAndSend(
                "/topic/chatrooms/" + chatRoomId,
                new ChatMessageSocketPayload(eventType, response)
        );
    }

    private ChatMessageResponse toResponse(ChatMessage message) {
        ChatMessageResponse.ReplyInfo replyInfo = null;
        if (message.getParentMessage() != null) {
            ChatMessage parent = message.getParentMessage();
            replyInfo = new ChatMessageResponse.ReplyInfo(
                    parent.getChatMessageId(),
                    parent.getSender() != null ? parent.getSender().getAccountId() : null,
                    parent.getMessageText()
            );
        }

        ChatMessageResponse.DeletionInfo deletionInfo = message.isDeleted()
                ? new ChatMessageResponse.DeletionInfo(message.getDeletedAt(), message.getDeletedByAccountId())
                : null;

        ChatMessageResponse.RecallInfo recallInfo = message.isRecalled()
                ? new ChatMessageResponse.RecallInfo(message.getRecalledAt())
                : null;

        return new ChatMessageResponse(
                message.getChatMessageId(),
                message.getChatRoom() != null ? message.getChatRoom().getChatRoomId() : null,
                message.getSender() != null ? message.getSender().getAccountId() : null,
                message.getMessageText(),
                message.getPayloadType(),
                message.getPayloadData(),
                message.getSentAt(),
                message.getReadAt(),
                message.getReadByAccountId(),
                replyInfo,
                deletionInfo,
                recallInfo
        );
    }
}