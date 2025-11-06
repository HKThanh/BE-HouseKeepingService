package iuh.house_keeping_service_be.mappers;

import iuh.house_keeping_service_be.dtos.Booking.request.BookingCreateRequest;
import iuh.house_keeping_service_be.dtos.Booking.response.*;
import iuh.house_keeping_service_be.dtos.Booking.internal.BookingValidationResult;
import iuh.house_keeping_service_be.models.*;
import iuh.house_keeping_service_be.repositories.PaymentMethodRepository;
import iuh.house_keeping_service_be.repositories.RuleConditionRepository;
import iuh.house_keeping_service_be.repositories.ServiceOptionChoiceRepository;
import iuh.house_keeping_service_be.utils.BookingDTOFormatter;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BookingMapper {
    private final ServiceOptionChoiceRepository serviceOptionChoiceRepository;
    private final RuleConditionRepository ruleConditionRepository;
    private final PaymentMethodRepository paymentMethodRepository;

    public Booking toEntity(BookingCreateRequest request, BookingValidationResult validation) {
        Booking booking = new Booking();
        booking.setBookingTime(request.bookingTime());
        booking.setNote(request.note());
        booking.setTotalAmount(validation.getCalculatedTotalAmount());
        return booking;
    }

    public BookingData mapToBookingData(Booking booking, 
                                    CustomerAddressInfo addressInfo,
                                    List<BookingDetailInfo> detailInfos,
                                    PaymentInfo paymentInfo,
                                    PromotionInfo promotionInfo) {
        // Calculate isPost: true if title OR imageUrls is not null/empty
        boolean isPost = (booking.getTitle() != null && !booking.getTitle().trim().isEmpty()) 
                      || (booking.getImageUrls() != null && !booking.getImageUrls().isEmpty());
        
        BookingData data = new BookingData(
            booking.getBookingId(),
            booking.getBookingCode(),
            booking.getCustomer().getCustomerId(),
            booking.getCustomer().getFullName(),
            toCustomerInfo(booking.getCustomer()),
            addressInfo,
            booking.getBookingTime(),
            booking.getNote(),
            booking.getTotalAmount(),
            BookingDTOFormatter.formatPrice(booking.getTotalAmount()),
            booking.getStatus().toString(),
            booking.getTitle(),
            booking.getImageUrls(),
            isPost,
            booking.getIsVerified(),
            booking.getAdminComment(),
            promotionInfo,
            detailInfos,
            paymentInfo,
            booking.getCreatedAt()
        );
        return data;
    }

    public BookingResponse toResponse(Booking booking) {
        BookingData bookingData = mapToBookingData(
            booking,
            toCustomerAddressInfo(booking.getAddress()),
            booking.getBookingDetails().stream()
                .map(this::toBookingDetailInfo)
                .collect(Collectors.toList()),
            booking.getPayments().isEmpty() ? null : toPaymentInfo(booking.getPayments().get(0)),
            booking.getPromotion() != null ? toPromotionInfo(booking.getPromotion()) : null
        );

        return BookingResponse.success(bookingData);
    }

    public BookingResponse toBookingResponse(Booking booking) {
        return toResponse(booking);
    }

    public CustomerAddressInfo toCustomerAddressInfo(Address address) {
        return new CustomerAddressInfo(
            address.getAddressId(),
            address.getFullAddress(),
            address.getWard(),
            address.getCity(),
            address.getLatitude() != null ? address.getLatitude().doubleValue() : null,
            address.getLongitude() != null ? address.getLongitude().doubleValue() : null,
            address.getIsDefault()
        );
    }

    public CustomerInfo toCustomerInfo(Customer customer) {
        return new CustomerInfo(
            customer.getCustomerId(),
            customer.getFullName(),
            customer.getAvatar(),
            customer.getEmail(),
            customer.getAccount() != null ? customer.getAccount().getPhoneNumber() : null,
            customer.getIsMale(),
            customer.getBirthdate(),
            customer.getRating() != null ? customer.getRating().toString() : null,
            customer.getVipLevel()
        );
    }

    public PromotionInfo toPromotionInfo(Promotion promotion) {
        return new PromotionInfo(
            promotion.getPromotionId(),
            promotion.getPromoCode(),
            promotion.getDescription(),
            promotion.getDiscountType(),
            promotion.getDiscountValue(),
            promotion.getMaxDiscountAmount()
        );
    }

    public BookingDetailInfo toBookingDetailInfo(BookingDetail detail) {
        return new BookingDetailInfo(
            detail.getId(),
            toServiceInfo(detail.getService()),
            detail.getQuantity(),
            detail.getPricePerUnit(),
            BookingDTOFormatter.formatPrice(detail.getPricePerUnit()),
            detail.getSubTotal(),
            BookingDTOFormatter.formatPrice(detail.getSubTotal()),
            mapSelectedChoices(detail), // Add selected choices mapping
            detail.getAssignments().stream()
                .map(this::toAssignmentInfo)
                .collect(Collectors.toList()),
            BookingDTOFormatter.formatDuration(detail.getService().getEstimatedDurationHours()),
            BookingDTOFormatter.formatDuration(detail.getService().getEstimatedDurationHours())
        );
    }

    private List<SelectedChoiceInfo> mapSelectedChoices(BookingDetail detail) {
        // This method would need to be implemented based on how you store selected choices
        // For now, return empty list if selectedChoiceIds is null or empty
        if (detail.getSelectedChoiceIds() == null || detail.getSelectedChoiceIds().isEmpty()) {
            return List.of();
        }
        
        // TODO: Implement logic to fetch choice details from selectedChoiceIds
        // You might need to inject ServiceOptionChoiceRepository here

        String[] choiceIdStrs = detail.getSelectedChoiceIds().split(",");

        List<Integer> choiceIds =
            java.util.Arrays.stream(choiceIdStrs)
                .map(String::trim)
                .map(Integer::parseInt)
                .collect(Collectors.toList());

        List<ServiceOptionChoice> choices = serviceOptionChoiceRepository.findByIdIn(choiceIds);

        return choices.stream()
                .map(choice -> new SelectedChoiceInfo(
                    choice.getId(),
                        choice.getLabel(),
                        choice.getOption().getLabel(),
                        ruleConditionRepository.findByChoice_Id(choice.getId()).getRule().getPriceAdjustment(),
                        BookingDTOFormatter.formatPrice(ruleConditionRepository.findByChoice_Id(choice.getId()).getRule().getPriceAdjustment())
                ))
                .collect(Collectors.toList());
    }

    public ServiceInfo toServiceInfo(Service service) {
        return new ServiceInfo(
            service.getServiceId(),
            service.getName(),
            service.getDescription(),
            service.getBasePrice(),
            service.getUnit(),
            service.getEstimatedDurationHours().doubleValue(),
            service.getIconUrl(),
            service.getCategory() != null ? service.getCategory().getCategoryName() : null,
            service.getIsActive()
        );
    }

    public AssignmentInfo toAssignmentInfo(Assignment assignment) {
        return new AssignmentInfo(
            assignment.getAssignmentId(),
            toEmployeeInfo(assignment.getEmployee()),
            assignment.getStatus(),
            assignment.getCheckInTime(), // Use calculated start time
            assignment.getCheckOutTime(),   // Use calculated end time
            null, // createdAt - timestamp field removed from Assignment
            null  // updatedAt - timestamp field removed from Assignment
        );
    }

    public EmployeeInfo toEmployeeInfo(Employee employee) {
        return new EmployeeInfo(
            employee.getEmployeeId(),
            employee.getFullName(),
            employee.getEmail(),
            employee.getAccount().getPhoneNumber(),
            employee.getAvatar(),
            employee.getRating(),
            employee.getEmployeeStatus(),
            employee.getSkills() != null ? employee.getSkills() : List.of(),
            employee.getBio()
        );
    }

    public PaymentInfo toPaymentInfo(Payment payment) {
        String paymentMethodName = paymentMethodRepository.findPaymentMethodNameByPaymentId(payment.getId());

        return new PaymentInfo(
            payment.getId(),
            payment.getAmount(),
            paymentMethodName,
            payment.getPaymentStatus(),
            payment.getTransactionCode(),
            payment.getCreatedAt(),
            payment.getPaidAt()
        );
    }
}