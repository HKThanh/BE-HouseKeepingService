package iuh.house_keeping_service_be.mappers;

import iuh.house_keeping_service_be.dtos.Booking.response.BookingDetailInfo;
import iuh.house_keeping_service_be.dtos.Booking.response.CustomerAddressInfo;
import iuh.house_keeping_service_be.dtos.Booking.response.CustomerInfo;
import iuh.house_keeping_service_be.dtos.Booking.response.PromotionInfo;
import iuh.house_keeping_service_be.dtos.Booking.response.SelectedChoiceInfo;
import iuh.house_keeping_service_be.dtos.Booking.response.ServiceInfo;
import iuh.house_keeping_service_be.dtos.RecurringBooking.response.RecurringBookingResponse;
import iuh.house_keeping_service_be.enums.RecurrenceType;
import iuh.house_keeping_service_be.enums.RecurringBookingStatus;
import iuh.house_keeping_service_be.models.*;
import iuh.house_keeping_service_be.repositories.RuleConditionRepository;
import iuh.house_keeping_service_be.repositories.ServiceOptionChoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RecurringBookingMapper {

    private final ServiceOptionChoiceRepository serviceOptionChoiceRepository;
    private final RuleConditionRepository ruleConditionRepository;
    private final BookingMapper bookingMapper;

    public RecurringBookingResponse toResponse(RecurringBooking recurringBooking) {
        RecurringBookingResponse response = new RecurringBookingResponse();
        
        response.setRecurringBookingId(recurringBooking.getRecurringBookingId());
        response.setCustomerId(recurringBooking.getCustomer().getCustomerId());
        response.setCustomerName(recurringBooking.getCustomer().getFullName());
        response.setCustomer(bookingMapper.toCustomerInfo(recurringBooking.getCustomer()));
        response.setAddress(bookingMapper.toCustomerAddressInfo(recurringBooking.getAddress()));

        response.setRecurrenceType(recurringBooking.getRecurrenceType());
        response.setRecurrenceTypeDisplay(getRecurrenceTypeDisplay(recurringBooking.getRecurrenceType()));

        List<Integer> days = parseRecurrenceDays(recurringBooking.getRecurrenceDays());
        response.setRecurrenceDays(days);
        response.setRecurrenceDaysDisplay(getRecurrenceDaysDisplay(recurringBooking.getRecurrenceType(), days));

        response.setBookingTime(recurringBooking.getBookingTime());
        response.setStartDate(recurringBooking.getStartDate());
        response.setEndDate(recurringBooking.getEndDate());
        response.setNote(recurringBooking.getNote());
        response.setTitle(recurringBooking.getTitle());

        if (recurringBooking.getPromotion() != null) {
            response.setPromotion(bookingMapper.toPromotionInfo(recurringBooking.getPromotion()));
        }

        List<BookingDetailInfo> detailInfos = recurringBooking.getRecurringBookingDetails().stream()
                .map(this::toBookingDetailInfo)
                .collect(Collectors.toList());
        response.setRecurringBookingDetails(detailInfos);

        response.setStatus(recurringBooking.getStatus());
        response.setStatusDisplay(getStatusDisplay(recurringBooking.getStatus()));
        response.setCancelledAt(recurringBooking.getCancelledAt());
        response.setCancellationReason(recurringBooking.getCancellationReason());
        response.setCreatedAt(recurringBooking.getCreatedAt());
        response.setUpdatedAt(recurringBooking.getUpdatedAt());
        if (recurringBooking.getAssignedEmployee() != null) {
            response.setAssignedEmployee(
                bookingMapper.toEmployeeInfoPublic(recurringBooking.getAssignedEmployee())
            );
        }

        // Statistics - safely handle lazy loading
        try {
            response.setTotalGeneratedBookings(
                recurringBooking.getGeneratedBookings() != null 
                    ? recurringBooking.getGeneratedBookings().size() 
                    : 0
            );
        } catch (Exception e) {
            response.setTotalGeneratedBookings(0);
        }

        return response;
    }

    private BookingDetailInfo toBookingDetailInfo(RecurringBookingDetail detail) {
        Service service = detail.getService();
        
        List<Integer> choiceIds = detail.getSelectedChoiceIds() != null && !detail.getSelectedChoiceIds().isEmpty()
                ? Arrays.stream(detail.getSelectedChoiceIds().split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Integer::parseInt)
                        .collect(Collectors.toList())
                : List.of();

        List<SelectedChoiceInfo> selectedChoices = choiceIds.isEmpty() ? List.of()
                : serviceOptionChoiceRepository.findAllById(choiceIds).stream()
                        .map(choice -> {
                            try {
                                var ruleCondition = ruleConditionRepository.findByChoice_Id(choice.getId());
                                BigDecimal priceAdjustment = ruleCondition != null && ruleCondition.getRule() != null 
                                        ? ruleCondition.getRule().getPriceAdjustment() 
                                        : BigDecimal.ZERO;
                                return new SelectedChoiceInfo(
                                        choice.getId(),
                                        choice.getLabel(),
                                        choice.getOption().getLabel(),
                                        priceAdjustment,
                                        String.format("%,.0f đ", priceAdjustment)
                                );
                            } catch (Exception e) {
                                return new SelectedChoiceInfo(
                                        choice.getId(),
                                        choice.getLabel(),
                                        choice.getOption().getLabel(),
                                        BigDecimal.ZERO,
                                        "0 đ"
                                );
                            }
                        })
                        .collect(Collectors.toList());

        ServiceInfo serviceInfo = new ServiceInfo(
                service.getServiceId(),
                service.getName(),
                service.getDescription(),
                service.getBasePrice(),
                service.getUnit(),
                service.getEstimatedDurationHours() != null ? service.getEstimatedDurationHours().doubleValue() : 0.0,
                service.getIconUrl(),
                service.getCategory() != null ? service.getCategory().getCategoryName() : null,
                service.getIsActive()
        );

        BigDecimal subTotal = detail.getPricePerUnit().multiply(new BigDecimal(detail.getQuantity()));

        return new BookingDetailInfo(
                detail.getRecurringBookingDetailId(),
                serviceInfo,
                detail.getQuantity(),
                detail.getPricePerUnit(),
                String.format("%,.0f đ", detail.getPricePerUnit()),
                subTotal,
                String.format("%,.0f đ", subTotal),
                selectedChoices,
                List.of(),
                String.format("%.1fh", service.getEstimatedDurationHours() != null ? service.getEstimatedDurationHours().doubleValue() : 0.0),
                String.format("%.1fh", service.getEstimatedDurationHours() != null ? service.getEstimatedDurationHours().doubleValue() : 0.0)
        );
    }

    public List<Integer> parseRecurrenceDays(String recurrenceDays) {
        if (recurrenceDays == null || recurrenceDays.isEmpty()) {
            return List.of();
        }
        return Arrays.stream(recurrenceDays.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .sorted()
                .collect(Collectors.toList());
    }

    private String getRecurrenceTypeDisplay(RecurrenceType type) {
        return switch (type) {
            case WEEKLY -> "Hàng tuần";
            case MONTHLY -> "Hàng tháng";
        };
    }

    public String getRecurrenceDaysDisplay(RecurrenceType type, List<Integer> days) {
        if (days.isEmpty()) {
            return "";
        }

        if (type == RecurrenceType.WEEKLY) {
            return days.stream()
                    .map(this::getDayOfWeekDisplay)
                    .collect(Collectors.joining(", "));
        } else {
            return days.stream()
                    .map(day -> "Ngày " + day)
                    .collect(Collectors.joining(", "));
        }
    }

    private String getDayOfWeekDisplay(int dayOfWeek) {
        return switch (dayOfWeek) {
            case 1 -> "Thứ 2";
            case 2 -> "Thứ 3";
            case 3 -> "Thứ 4";
            case 4 -> "Thứ 5";
            case 5 -> "Thứ 6";
            case 6 -> "Thứ 7";
            case 7 -> "Chủ nhật";
            default -> "Không xác định";
        };
    }

    private String getStatusDisplay(RecurringBookingStatus status) {
        return switch (status) {
            case ACTIVE -> "Đang hoạt động";
            case CANCELLED -> "Đã hủy";
            case COMPLETED -> "Đã hoàn thành";
        };
    }
}
