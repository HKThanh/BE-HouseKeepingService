package iuh.house_keeping_service_be.mappers;

import iuh.house_keeping_service_be.dtos.Booking.request.BookingCreateRequest;
import iuh.house_keeping_service_be.dtos.Booking.response.*;
import iuh.house_keeping_service_be.dtos.Booking.internal.BookingValidationResult;
import iuh.house_keeping_service_be.models.*;
import iuh.house_keeping_service_be.repositories.RuleConditionRepository;
import iuh.house_keeping_service_be.repositories.ServiceOptionChoiceRepository;
import iuh.house_keeping_service_be.utils.BookingDTOFormatter;

import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BookingMapper {
    private final ServiceOptionChoiceRepository serviceOptionChoiceRepository;
    private final RuleConditionRepository ruleConditionRepository;

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
        return new BookingData(
            booking.getBookingId(),
            booking.getBookingCode(),
            booking.getCustomer().getCustomerId(),
            booking.getCustomer().getFullName(),
            addressInfo,
            booking.getBookingTime(),
            booking.getNote(),
            booking.getTotalAmount(),
            BookingDTOFormatter.formatPrice(booking.getTotalAmount()),
            booking.getStatus().toString(),
            promotionInfo,
            detailInfos,
            paymentInfo,
            booking.getCreatedAt()
        );
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

    public CustomerAddressInfo toCustomerAddressInfo(Address address) {
        return new CustomerAddressInfo(
            address.getAddressId(),
            address.getFullAddress(),
            address.getWard(),
            address.getDistrict(),
            address.getCity(),
            address.getLatitude().doubleValue(),
            address.getLongitude().doubleValue(),
            address.getIsDefault()
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
            assignment.getCreatedAt(),
            assignment.getUpdatedAt()
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
        return new PaymentInfo(
            payment.getId(),
            payment.getAmount(),
            payment.getPaymentMethod(),
            payment.getPaymentStatus(),
            payment.getTransactionCode(),
            payment.getCreatedAt(),
            payment.getPaidAt()
        );
    }
}