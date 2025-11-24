package iuh.house_keeping_service_be.events;

import iuh.house_keeping_service_be.dtos.RecurringBooking.request.RecurringBookingDetailRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class RecurringBookingCreatedEvent {
    private final String recurringBookingId;
    private final List<RecurringBookingDetailRequest> detailRequests;
}
