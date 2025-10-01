package iuh.house_keeping_service_be.services.AssignmentService.impl;

import iuh.house_keeping_service_be.dtos.Assignment.request.AssignmentActionRequest;
import iuh.house_keeping_service_be.dtos.Assignment.response.AssignmentDetailResponse;
import iuh.house_keeping_service_be.enums.AssignmentStatus;
import iuh.house_keeping_service_be.enums.BookingStatus;
import iuh.house_keeping_service_be.models.*;
import iuh.house_keeping_service_be.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignmentServiceImplTest {

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingDetailRepository bookingDetailRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeUnavailabilityRepository employeeUnavailabilityRepository;

    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private AssignmentServiceImpl assignmentService;

    private Assignment buildAssignment(LocalDateTime bookingTime, AssignmentStatus status) {
        Account customerAccount = new Account();
        customerAccount.setPhoneNumber("0123456789");

        Customer customer = new Customer();
        customer.setFullName("Nguyễn Văn A");
        customer.setAccount(customerAccount);

        Address address = new Address();
        address.setFullAddress("123 Đường ABC, Quận 1");

        Service service = new Service();
        service.setName("Dọn nhà");
        service.setEstimatedDurationHours(BigDecimal.valueOf(2));

        Booking booking = new Booking();
        booking.setBookingId("booking-1");
        booking.setBookingCode("BK001");
        booking.setBookingTime(bookingTime);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setCustomer(customer);
        booking.setAddress(address);

        BookingDetail bookingDetail = new BookingDetail();
        bookingDetail.setId("detail-1");
        bookingDetail.setBooking(booking);
        bookingDetail.setService(service);
        bookingDetail.setQuantity(1);
        bookingDetail.setPricePerUnit(BigDecimal.valueOf(100_000));
        bookingDetail.setSubTotal(BigDecimal.valueOf(100_000));

        Employee employee = new Employee();
        employee.setEmployeeId("emp-1");

        Assignment assignment = new Assignment();
        assignment.setAssignmentId("assignment-1");
        assignment.setEmployee(employee);
        assignment.setBookingDetail(bookingDetail);
        assignment.setStatus(status);
        assignment.setCreatedAt(LocalDateTime.now().minusDays(1));

        booking.setBookingDetails(List.of(bookingDetail));
        bookingDetail.setAssignments(List.of(assignment));

        return assignment;
    }

    @BeforeEach
    void resetMocks() {
        clearInvocations(assignmentRepository, bookingRepository);
    }

    @Test
    void checkInWithinAllowedWindowUpdatesAssignmentAndBookingStatus() {
        LocalDateTime bookingTime = LocalDateTime.now();
        Assignment assignment = buildAssignment(bookingTime, AssignmentStatus.ASSIGNED);

        when(assignmentRepository.findByIdWithDetails("assignment-1"))
                .thenReturn(Optional.of(assignment));
        when(assignmentRepository.save(any(Assignment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(assignmentRepository.findByBookingIdWithStatus("booking-1"))
                .thenReturn(List.of(assignment));
        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AssignmentActionRequest request = new AssignmentActionRequest("emp-1");

        AssignmentDetailResponse response = assignmentService.checkIn("assignment-1", request);

        assertThat(assignment.getStatus()).isEqualTo(AssignmentStatus.IN_PROGRESS);
        assertThat(assignment.getCheckInTime()).isNotNull();
        assertThat(response.status()).isEqualTo(AssignmentStatus.IN_PROGRESS);
        assertThat(response.checkInTime()).isNotNull();
        assertThat(assignment.getBookingDetail().getBooking().getStatus())
                .isEqualTo(BookingStatus.IN_PROGRESS);

        verify(assignmentRepository).save(assignment);
        verify(bookingRepository).save(assignment.getBookingDetail().getBooking());
    }

    @Test
    void checkInOutsideAllowedWindowThrowsException() {
        LocalDateTime bookingTime = LocalDateTime.now().plusMinutes(30);
        Assignment assignment = buildAssignment(bookingTime, AssignmentStatus.ASSIGNED);

        when(assignmentRepository.findByIdWithDetails("assignment-1"))
                .thenReturn(Optional.of(assignment));

        AssignmentActionRequest request = new AssignmentActionRequest("emp-1");

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> assignmentService.checkIn("assignment-1", request));

        assertThat(exception.getMessage()).contains("Chỉ được điểm danh");
        verify(assignmentRepository, never()).save(any(Assignment.class));
        verify(bookingRepository, never()).save(any(Booking.class));
    }
}