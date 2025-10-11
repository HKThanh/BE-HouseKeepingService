package iuh.house_keeping_service_be.services.ConversationService.impl;

import iuh.house_keeping_service_be.dtos.Chat.response.AttachmentUploadResponse;
import iuh.house_keeping_service_be.dtos.Chat.response.ChatParticipantDto;
import iuh.house_keeping_service_be.dtos.Chat.response.ConversationResponse;
import iuh.house_keeping_service_be.enums.ChatParticipantType;
import iuh.house_keeping_service_be.enums.AssignmentStatus;
import iuh.house_keeping_service_be.exceptions.ChatAccessDeniedException;
import iuh.house_keeping_service_be.exceptions.ChatValidationException;
import iuh.house_keeping_service_be.exceptions.ResourceNotFoundException;
import iuh.house_keeping_service_be.models.*;
import iuh.house_keeping_service_be.repositories.AssignmentRepository;
import iuh.house_keeping_service_be.repositories.ConversationRepository;
import iuh.house_keeping_service_be.repositories.AccountRepository;
import iuh.house_keeping_service_be.repositories.BookingRepository;
import iuh.house_keeping_service_be.services.CloudinaryService.CloudinaryService;
import iuh.house_keeping_service_be.services.ConversationService.ConversationService;
import iuh.house_keeping_service_be.dtos.Cloudinary.CloudinaryUploadResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationServiceImpl implements ConversationService {

    private static final Set<String> IMAGE_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final Set<String> VIDEO_CONTENT_TYPES = Set.of("video/mp4", "video/webm");
    private static final long IMAGE_MAX_SIZE = 10 * 1024 * 1024L;
    private static final long VIDEO_MAX_SIZE = 100 * 1024 * 1024L;

    private final ConversationRepository conversationRepository;
    private final BookingRepository bookingRepository;
    private final AssignmentRepository assignmentRepository;
    private final AccountRepository accountRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public ConversationResponse getOrCreateConversation(String bookingId, String username) {
        Account account = getAccountByUsername(username);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch đặt: " + bookingId));

        if (!isAccountParticipant(account.getAccountId(), booking)) {
            throw new ChatAccessDeniedException("Bạn không có quyền truy cập cuộc hội thoại này");
        }

        Conversation conversation = conversationRepository.findByBooking_BookingId(bookingId)
                .orElseGet(() -> createConversation(booking));

        List<ChatParticipantDto> participants = buildParticipants(booking);
        return new ConversationResponse(conversation.getConversationId(), bookingId, participants);
    }

    @Override
    @Transactional
    public AttachmentUploadResponse uploadAttachment(String conversationId, MultipartFile file, String username) {
        Account account = getAccountByUsername(username);
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cuộc hội thoại: " + conversationId));

        if (!isAccountParticipant(account.getAccountId(), conversation.getBooking())) {
            throw new ChatAccessDeniedException("Bạn không có quyền tải tệp lên cuộc hội thoại này");
        }

        String contentType = validateFile(file);
        boolean isVideo = VIDEO_CONTENT_TYPES.contains(contentType);

        CloudinaryUploadResult uploadResult = cloudinaryService.uploadChatMedia(file, isVideo);
        log.debug("Uploaded chat media to Cloudinary with publicId={} and url={}", uploadResult.publicId(), uploadResult.secureUrl());

        return new AttachmentUploadResponse(uploadResult.secureUrl(), uploadResult.publicId(), contentType, file.getSize());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isParticipant(String conversationId, String accountId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cuộc hội thoại: " + conversationId));
        return isAccountParticipant(accountId, conversation.getBooking());
    }

    private Account getAccountByUsername(String username) {
        return accountRepository.findByUsername(username)
                .orElseThrow(() -> new ChatAccessDeniedException("Tài khoản không hợp lệ"));
    }

    private Conversation createConversation(Booking booking) {
        Conversation conversation = new Conversation();
        conversation.setBooking(booking);
        return conversationRepository.save(conversation);
    }

    private List<ChatParticipantDto> buildParticipants(Booking booking) {
        List<ChatParticipantDto> participants = new ArrayList<>();
        Set<String> addedAccountIds = new HashSet<>();

        Customer customer = booking.getCustomer();
        if (customer != null && customer.getAccount() != null) {
            Account customerAccount = customer.getAccount();
            participants.add(new ChatParticipantDto(
                    customer.getCustomerId(),
                    customerAccount.getAccountId(),
                    customerAccount.getUsername(),
                    ChatParticipantType.CUSTOMER,
                    customer.getFullName(),
                    customer.getAvatar()
            ));
            addedAccountIds.add(customerAccount.getAccountId());
        }

        List<Assignment> assignments = assignmentRepository.findAssignmentsWithEmployeesByBookingId(booking.getBookingId());
        for (Assignment assignment : assignments) {
            if (assignment.getStatus() == AssignmentStatus.CANCELLED) {
                continue;
            }
            Employee employee = assignment.getEmployee();
            if (employee == null || employee.getAccount() == null) {
                continue;
            }
            Account employeeAccount = employee.getAccount();
            if (addedAccountIds.add(employeeAccount.getAccountId())) {
                participants.add(new ChatParticipantDto(
                        employee.getEmployeeId(),
                        employeeAccount.getAccountId(),
                        employeeAccount.getUsername(),
                        ChatParticipantType.EMPLOYEE,
                        employee.getFullName(),
                        employee.getAvatar()
                ));
            }
        }

        return participants;
    }

    private boolean isAccountParticipant(String accountId, Booking booking) {
        if (booking == null) {
            return false;
        }
        Customer customer = booking.getCustomer();
        if (customer != null && customer.getAccount() != null
                && Objects.equals(customer.getAccount().getAccountId(), accountId)) {
            return true;
        }

        Set<String> employeeAccountIds = assignmentRepository.findAssignmentsWithEmployeesByBookingId(booking.getBookingId()).stream()
                .filter(assignment -> assignment.getStatus() != AssignmentStatus.CANCELLED)
                .map(Assignment::getEmployee)
                .filter(Objects::nonNull)
                .map(Employee::getAccount)
                .filter(Objects::nonNull)
                .map(Account::getAccountId)
                .collect(Collectors.toSet());

        return employeeAccountIds.contains(accountId);
    }

    private String validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ChatValidationException("Tệp tải lên không hợp lệ");
        }

        String contentType = file.getContentType();
        if (contentType == null || (!IMAGE_CONTENT_TYPES.contains(contentType) && !VIDEO_CONTENT_TYPES.contains(contentType))) {
            throw new ChatValidationException("Định dạng tệp không được hỗ trợ");
        }

        long size = file.getSize();
        if (IMAGE_CONTENT_TYPES.contains(contentType) && size > IMAGE_MAX_SIZE) {
            throw new ChatValidationException("Kích thước ảnh không được vượt quá 10MB");
        }
        if (VIDEO_CONTENT_TYPES.contains(contentType) && size > VIDEO_MAX_SIZE) {
            throw new ChatValidationException("Kích thước video không được vượt quá 100MB");
        }
        return contentType;
    }
}