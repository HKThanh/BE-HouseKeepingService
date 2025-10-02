package iuh.house_keeping_service_be.services.AssignmentService.impl;

import iuh.house_keeping_service_be.dtos.Assignment.request.AssignmentActionRequest;
import iuh.house_keeping_service_be.dtos.Assignment.request.AssignmentCancelRequest;
import iuh.house_keeping_service_be.dtos.Assignment.response.AssignmentDetailResponse;
import iuh.house_keeping_service_be.dtos.Assignment.response.BookingSummary;
import iuh.house_keeping_service_be.enums.AssignmentStatus;
import iuh.house_keeping_service_be.enums.BookingStatus;
import iuh.house_keeping_service_be.models.*;
import iuh.house_keeping_service_be.repositories.*;
import iuh.house_keeping_service_be.repositories.projections.ZoneCoordinate;
import iuh.house_keeping_service_be.services.AssignmentService.AssignmentService;
//import iuh.house_keeping_service_be.services.NotificationService.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssignmentServiceImpl implements AssignmentService {

    private static final DateTimeFormatter CHECK_WINDOW_FORMATTER = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    private final AssignmentRepository assignmentRepository;
    private final BookingRepository bookingRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeUnavailabilityRepository employeeUnavailabilityRepository;
    private final AddressRepository addressRepository;
    private final ChatRoomRepository chatRoomRepository;
//    private final NotificationService notificationService;

    @Override
    public List<AssignmentDetailResponse> getEmployeeAssignments(String employeeId, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        List<Assignment> assignments;

        if (status != null && !status.isEmpty()) {
            try {
                AssignmentStatus assignmentStatus = AssignmentStatus.valueOf(status.toUpperCase());
                assignments = assignmentRepository.findByEmployeeIdAndStatusWithDetails(employeeId, assignmentStatus, pageable);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid status provided: {}", status);
                assignments = assignmentRepository.findByEmployeeIdWithDetails(employeeId, pageable);
            }
        } else {
            assignments = assignmentRepository.findByEmployeeIdWithDetails(employeeId, pageable);
        }

        return assignments.stream()
                .map(this::mapToAssignmentDetailResponse)
                .collect(Collectors.toList());
    }

//    @Override
//    public List<BookingSummary> getAvailableBookings(String employeeId, int page, int size) {
//        Employee employee = employeeRepository.findEmployeeWithDetails(employeeId)
//                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên"));
//
//        Set<EmployeeWorkingZone> workingZones = employee.getWorkingZones() == null
//                ? Set.of()
//                : new HashSet<>(employee.getWorkingZones());
//
//        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "bookingTime"));
//
//        if (!workingZones.isEmpty()) {
//            List<String> zoneKeys = workingZones.stream()
//                    .map(this::buildZoneKey)
//                    .filter(Objects::nonNull)
//                    .distinct()
//                    .collect(Collectors.toList());
//
//            if (!zoneKeys.isEmpty()) {
//                List<Booking> zoneBookings = bookingRepository
//                        .findAwaitingEmployeeBookingsByZones(zoneKeys, pageable)
//                        .getContent();
//
//                List<BookingSummary> zoneSummaries = mapToBookingSummaries(zoneBookings);
//                if (!zoneSummaries.isEmpty()) {
//                    return zoneSummaries;
//                }
//            }
//        }
//
//        List<Booking> awaitingBookings = bookingRepository.findAwaitingEmployeeBookings(pageable);
//        return sortBookingsByProximity(awaitingBookings, workingZones);
//    }

    @Override
    public List<BookingSummary> getAvailableBookings(String employeeId, int page, int size) {
        Employee employee = employeeRepository.findEmployeeWithDetails(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên"));

        Set<EmployeeWorkingZone> workingZones = employee.getWorkingZones() == null
                ? Set.of()
                : new HashSet<>(employee.getWorkingZones());

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "bookingTime"));
        List<BookingSummary> result = new ArrayList<>();

        // Get bookings in employee's working zones first
        if (!workingZones.isEmpty()) {
            List<String> zoneKeys = workingZones.stream()
                    .map(this::buildZoneKey)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());

            if (!zoneKeys.isEmpty()) {
                Page<Booking> zoneBookingsPage = bookingRepository
                        .findAwaitingEmployeeBookingsByZones(zoneKeys, pageable);

                List<BookingSummary> zoneSummaries = mapToBookingSummaries(zoneBookingsPage.getContent());
                result.addAll(zoneSummaries);

                // If we have enough bookings from zones, return them
                if (result.size() >= size) {
                    return result.subList(0, size);
                }
            }
        }

        // Get remaining slots to fill with bookings outside zones
        int remainingSlots = size - result.size();
        if (remainingSlots > 0) {
            List<String> zoneKeys = workingZones.stream()
                    .map(this::buildZoneKey)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());

            List<Booking> outsideZoneBookings = zoneKeys.isEmpty()
                ? bookingRepository.findAwaitingEmployeeBookings(PageRequest.of(0, remainingSlots * 2))
                : bookingRepository.findAwaitingEmployeeBookingsOutsideZones(zoneKeys);

            List<BookingSummary> sortedOutsideBookings = sortBookingsByProximity(
                outsideZoneBookings, workingZones);

            // Add sorted bookings to fill remaining slots
            int toAdd = Math.min(remainingSlots, sortedOutsideBookings.size());
            result.addAll(sortedOutsideBookings.subList(0, toAdd));
        }

        return result;
    }

    @Override
    @Transactional
    public AssignmentDetailResponse acceptBookingDetail(String detailId, String employeeId) {
        BookingDetail bookingDetail = bookingDetailRepository.findById(detailId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy dịch vụ"));

        Booking booking = bookingDetail.getBooking();
        if (booking == null) {
            throw new IllegalStateException("Không thể xác định booking của chi tiết dịch vụ này");
        }

        EnumSet<BookingStatus> allowedStatuses = EnumSet.of(BookingStatus.AWAITING_EMPLOYEE, BookingStatus.CONFIRMED);
        if (!allowedStatuses.contains(booking.getStatus())) {
            throw new IllegalStateException(String.format(
                    "Không thể nhận booking khi đang ở trạng thái %s", booking.getStatus().name()));
        }

        LocalDateTime shiftStart = bookingDetail.getBooking().getBookingTime();
        LocalDateTime shiftEnd = calculateShiftEndTime(shiftStart, bookingDetail);

        List<Assignment> conflictingAssignments = assignmentRepository.findConflictingAssignments(employeeId, shiftStart, shiftEnd);
        if (!conflictingAssignments.isEmpty()) {
            throw new IllegalStateException("Nhân viên đã được phân công công việc khác trong khung giờ này");
        }

        List<EmployeeUnavailability> unavailabilities =
                employeeUnavailabilityRepository.findByEmployeeAndPeriod(employeeId, shiftStart, shiftEnd);
        boolean hasLeaveConflict = employeeUnavailabilityRepository.hasConflict(employeeId, shiftStart, shiftEnd);
        if (!unavailabilities.isEmpty() || hasLeaveConflict) {
            throw new IllegalStateException("Nhân viên đang có lịch nghỉ được phê duyệt trong khung giờ này");
        }

        if (bookingDetail.getAssignments().size() >= bookingDetail.getQuantity()) {
            throw new IllegalStateException("Chi tiết dịch vụ đã có đủ nhân viên");
        }

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên"));

        boolean alreadyAssigned = assignmentRepository.existsByBookingDetailIdAndEmployeeEmployeeId(detailId, employeeId);
        if (alreadyAssigned) {
            throw new IllegalStateException("Nhân viên đã nhận chi tiết dịch vụ này");
        }

        Assignment assignment = new Assignment();
        assignment.setBookingDetail(bookingDetail);
        assignment.setEmployee(employee);
        assignment.setStatus(AssignmentStatus.ASSIGNED);
        Assignment savedAssignment = assignmentRepository.save(assignment);

        bookingDetail.getAssignments().add(savedAssignment);

        boolean allAssigned = booking.getBookingDetails().stream()
                .allMatch(bd -> bd.getAssignments().size() >= bd.getQuantity());
        if (allAssigned && booking.getStatus() == BookingStatus.AWAITING_EMPLOYEE) {
            booking.setStatus(BookingStatus.CONFIRMED);
            booking.setUpdatedAt(LocalDateTime.now());
            bookingRepository.save(booking);
        }

        initializeChatRoomIfAbsent(savedAssignment);

        return mapToAssignmentDetailResponse(savedAssignment);
    }

    private void initializeChatRoomIfAbsent(Assignment assignment) {
        if (assignment == null || assignment.getAssignmentId() == null) {
            return;
        }

        if (chatRoomRepository.findByAssignmentAssignmentId(assignment.getAssignmentId()).isPresent()) {
            return;
        }

        BookingDetail bookingDetail = assignment.getBookingDetail();
        Booking booking = bookingDetail != null ? bookingDetail.getBooking() : null;
        Customer customer = booking != null ? booking.getCustomer() : null;
        Account customerAccount = customer != null ? customer.getAccount() : null;
        Employee employee = assignment.getEmployee();
        Account employeeAccount = employee != null ? employee.getAccount() : null;

        if (customerAccount == null || employeeAccount == null) {
            log.warn("Cannot initialize chat room for assignment {} due to missing participant accounts", assignment.getAssignmentId());
            return;
        }

        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setAssignment(assignment);
        chatRoom.setCustomerAccount(customerAccount);
        chatRoom.setEmployeeAccount(employeeAccount);

        chatRoomRepository.save(chatRoom);
    }

    @Override
    @Transactional
    public AssignmentDetailResponse checkIn(String assignmentId, AssignmentActionRequest request) {
        Assignment assignment = assignmentRepository.findByIdWithDetails(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy công việc"));

        ensureAssignmentBelongsToEmployee(assignment, request.employeeId());

        if (assignment.getCheckInTime() != null) {
            throw new IllegalStateException("Công việc đã được điểm danh bắt đầu");
        }

        if (assignment.getStatus() != AssignmentStatus.ASSIGNED) {
            throw new IllegalStateException(String.format(
                    "Không thể điểm danh công việc đang ở trạng thái %s",
                    assignment.getStatus().name()
            ));
        }

        Booking booking = assignment.getBookingDetail().getBooking();
        if (booking == null || booking.getBookingTime() == null) {
            throw new IllegalStateException("Không xác định được thời gian bắt đầu của lịch làm");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime bookingTime = booking.getBookingTime();
        LocalDateTime windowStart = bookingTime.minusMinutes(10);
        LocalDateTime windowEnd = bookingTime.plusMinutes(5);

        if (now.isBefore(windowStart) || now.isAfter(windowEnd)) {
            throw new IllegalStateException(String.format(
                    "Chỉ được điểm danh trong khoảng từ %s đến %s",
                    windowStart.format(CHECK_WINDOW_FORMATTER),
                    windowEnd.format(CHECK_WINDOW_FORMATTER)
            ));
        }

        assignment.setCheckInTime(now);
        assignment.setStatus(AssignmentStatus.IN_PROGRESS);
        Assignment savedAssignment = assignmentRepository.save(assignment);

        updateBookingStatusToInProgressIfNeeded(booking, now);

        return mapToAssignmentDetailResponse(savedAssignment);
    }

    @Override
    @Transactional
    public AssignmentDetailResponse checkOut(String assignmentId, AssignmentActionRequest request) {
        Assignment assignment = assignmentRepository.findByIdWithDetails(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy công việc"));

        ensureAssignmentBelongsToEmployee(assignment, request.employeeId());

        if (assignment.getStatus() != AssignmentStatus.IN_PROGRESS) {
            throw new IllegalStateException("Chỉ có thể chấm công kết thúc khi công việc đang được thực hiện");
        }

        if (assignment.getCheckOutTime() != null) {
            throw new IllegalStateException("Công việc đã được chấm công kết thúc");
        }

        LocalDateTime now = LocalDateTime.now();
        assignment.setCheckOutTime(now);
        assignment.setStatus(AssignmentStatus.COMPLETED);

        Assignment savedAssignment = assignmentRepository.save(assignment);

        Booking booking = assignment.getBookingDetail().getBooking();
        updateBookingStatusToCompletedIfNeeded(booking, now);

        return mapToAssignmentDetailResponse(savedAssignment);
    }

    private LocalDateTime calculateShiftEndTime(LocalDateTime shiftStart, BookingDetail bookingDetail) {
        if (shiftStart == null) {
            throw new IllegalArgumentException("Booking không có thời gian bắt đầu hợp lệ");
        }

        if (bookingDetail.getService() == null || bookingDetail.getService().getEstimatedDurationHours() == null) {
            return shiftStart.plusHours(2);
        }

        var duration = bookingDetail.getService().getEstimatedDurationHours();
        long hours = duration.longValue();
        long minutes = duration.remainder(java.math.BigDecimal.ONE)
                .multiply(java.math.BigDecimal.valueOf(60))
                .setScale(0, java.math.RoundingMode.HALF_UP)
                .longValue();

        if (minutes >= 60) {
            hours += minutes / 60;
            minutes = minutes % 60;
        }

        return shiftStart.plusHours(hours).plusMinutes(minutes);
    }


    @Override
    @Transactional
    public boolean cancelAssignment(String assignmentId, AssignmentCancelRequest request) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy công việc"));

        // Check if assignment can be cancelled
        if (assignment.getStatus() != AssignmentStatus.ASSIGNED) {
            throw new IllegalStateException("Chỉ có thể hủy công việc đang ở trạng thái 'Đã nhận'");
        }

        BookingDetail bookingDetail = assignment.getBookingDetail();
        Booking booking = bookingDetail.getBooking();

        // Check if booking is not too close to start time (e.g., within 2 hours)
        LocalDateTime now = LocalDateTime.now();
        if (booking.getBookingTime().isBefore(now.plusHours(2))) {
            throw new IllegalStateException("Không thể hủy công việc trong vòng 2 giờ trước giờ bắt đầu");
        }

        try {
            // Update assignment status
            assignment.setStatus(AssignmentStatus.CANCELLED);
            assignment.setUpdatedAt(now);
            assignmentRepository.save(assignment);

            // Update booking status if all assignments are cancelled
            updateBookingStatusIfNeeded(booking.getBookingId());

            // Send crisis notification to customer
            sendCrisisNotification(booking, assignment, request.reason());

            log.info("Assignment {} cancelled by employee {}. Reason: {}",
                    assignmentId, assignment.getEmployee().getEmployeeId(), request.reason());

            return true;

        } catch (Exception e) {
            log.error("Failed to cancel assignment {}: {}", assignmentId, e.getMessage(), e);
            throw new RuntimeException("Lỗi khi hủy công việc: " + e.getMessage());
        }
    }

    private void updateBookingStatusIfNeeded(String bookingId) {
        List<Assignment> bookingAssignments = assignmentRepository.findByBookingIdWithStatus(bookingId);

        boolean allCancelled = bookingAssignments.stream()
                .allMatch(a -> a.getStatus() == AssignmentStatus.CANCELLED);

        if (allCancelled) {
            Booking booking = bookingRepository.findById(bookingId).orElse(null);
            if (booking != null && booking.getStatus() != BookingStatus.CANCELLED) {
                booking.setStatus(BookingStatus.CANCELLED);
                booking.setUpdatedAt(LocalDateTime.now());
                bookingRepository.save(booking);
                log.info("Booking {} status updated to CANCELLED due to all assignments being cancelled", bookingId);
            }
        }
    }

    private void sendCrisisNotification(Booking booking, Assignment assignment, String reason) {
        try {
            // Send immediate notification to customer
            String message = String.format(
                    "THÔNG BÁO KHẨN: Lịch dọn dẹp %s của bạn vào %s đã bị hủy bởi nhân viên. " +
                    "Lý do: %s. Vui lòng liên hệ 1900-xxx để được hỗ trợ đặt lại dịch vụ.",
                    booking.getBookingCode(),
                    booking.getBookingTime(),
                    reason
            );

            //TODO: Implement notification service

//            notificationService.sendCrisisNotification(
//                    booking.getCustomer().getCustomerId(),
//                    "Lịch dịch vụ bị hủy khẩn cấp",
//                    message
//            );

            // Log for admin monitoring
            log.warn("CRISIS: Assignment {} cancelled by employee {}. Booking: {}, Customer: {}, Reason: {}",
                    assignment.getAssignmentId(),
                    assignment.getEmployee().getFullName(),
                    booking.getBookingCode(),
                    booking.getCustomer().getFullName(),
                    reason
            );

        } catch (Exception e) {
            log.error("Failed to send crisis notification for cancelled assignment {}: {}",
                    assignment.getAssignmentId(), e.getMessage(), e);
        }
    }

    private List<BookingSummary> mapToBookingSummaries(List<Booking> bookings) {
        return bookings.stream()
                .flatMap(booking -> booking.getBookingDetails().stream()
                        .filter(detail -> detail.getAssignments().isEmpty())
                        .map(detail -> mapToBookingSummary(booking, detail)))
                .collect(Collectors.toList());
    }

    private List<BookingSummary> sortBookingsByProximity(List<Booking> bookings, Set<EmployeeWorkingZone> workingZones) {
        Map<String, GeoCoordinate> zoneCoordinates = resolveZoneCoordinates(workingZones);

        if (zoneCoordinates.isEmpty()) {
            return mapToBookingSummaries(bookings);
        }

        return bookings.stream()
                .flatMap(booking -> booking.getBookingDetails().stream()
                        .filter(detail -> detail.getAssignments().isEmpty())
                        .map(detail -> new BookingCandidate(
                                mapToBookingSummary(booking, detail),
                                calculateMinDistance(booking.getAddress(), zoneCoordinates)
                        )))
                .sorted(Comparator.comparingDouble(BookingCandidate::distance))
                .map(BookingCandidate::summary)
                .collect(Collectors.toList());
    }

//    private Map<String, GeoCoordinate> resolveZoneCoordinates(Set<EmployeeWorkingZone> workingZones) {
//        if (workingZones == null || workingZones.isEmpty()) {
//            return Map.of();
//        }
//
//        return workingZones.stream()
//                .map(zone -> new AbstractMap.SimpleEntry<>(buildZoneKey(zone), getZoneRepresentativeCoordinate(zone).orElse(null)))
//                .filter(entry -> entry.getKey() != null)
//                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (existing, replacement) -> existing));
//    }

    private Map<String, GeoCoordinate> resolveZoneCoordinates(Set<EmployeeWorkingZone> workingZones) {
        if (workingZones == null || workingZones.isEmpty()) {
            return Map.of();
        }

        return workingZones.stream()
                .map(zone -> new AbstractMap.SimpleEntry<>(
                    buildZoneKey(zone),
                    getZoneRepresentativeCoordinate(zone).orElse(null)
                ))
                .filter(entry -> entry.getKey() != null && entry.getValue() != null) // Filter out null values
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (existing, replacement) -> existing
                ));
    }

    private Optional<GeoCoordinate> getZoneRepresentativeCoordinate(EmployeeWorkingZone zone) {
        if (zone == null || zone.getWard() == null || zone.getCity() == null) {
            return Optional.empty();
        }

        Optional<ZoneCoordinate> coordinate = addressRepository
                .findAverageCoordinateByWardAndCity(zone.getWard(), zone.getCity());

        if (coordinate.isEmpty()) {
            return Optional.empty();
        }

        ZoneCoordinate zoneCoordinate = coordinate.get();
        if (zoneCoordinate.latitude() == null || zoneCoordinate.longitude() == null) {
            return Optional.empty();
        }

        return Optional.of(new GeoCoordinate(
                zoneCoordinate.latitude(),
                zoneCoordinate.longitude()
        ));
    }

    private String buildZoneKey(EmployeeWorkingZone zone) {
        if (zone == null || zone.getWard() == null || zone.getCity() == null) {
            return null;
        }

        return (zone.getWard().trim().toLowerCase() + "|" + zone.getCity().trim().toLowerCase());
    }

    private double calculateMinDistance(Address address, Map<String, GeoCoordinate> zoneCoordinates) {
        if (address == null || address.getLatitude() == null || address.getLongitude() == null || zoneCoordinates.isEmpty()) {
            return Double.MAX_VALUE;
        }

        GeoCoordinate bookingCoordinate = new GeoCoordinate(
                address.getLatitude().doubleValue(),
                address.getLongitude().doubleValue()
        );

        return zoneCoordinates.values().stream()
                .filter(Objects::nonNull)
                .mapToDouble(zoneCoordinate -> calculateDistance(bookingCoordinate, zoneCoordinate))
                .min()
                .orElse(Double.MAX_VALUE);
    }

    private double calculateDistance(GeoCoordinate source, GeoCoordinate target) {
        final double earthRadiusKm = 6371.0;

        double sourceLatRad = Math.toRadians(source.latitude());
        double targetLatRad = Math.toRadians(target.latitude());
        double deltaLat = Math.toRadians(target.latitude() - source.latitude());
        double deltaLon = Math.toRadians(target.longitude() - source.longitude());

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(sourceLatRad) * Math.cos(targetLatRad)
                * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadiusKm * c;
    }

    private BookingSummary mapToBookingSummary(Booking booking, BookingDetail detail) {
        return new BookingSummary(
                detail.getId(),
                booking.getBookingCode(),
                detail.getService().getName(),
                booking.getAddress() != null ? booking.getAddress().getFullAddress() : null,
                booking.getBookingTime(),
                detail.getService().getEstimatedDurationHours(),
                detail.getQuantity()
        );
    }

    private record GeoCoordinate(double latitude, double longitude) {}

    private record BookingCandidate(BookingSummary summary, double distance) {}

    private AssignmentDetailResponse mapToAssignmentDetailResponse(Assignment assignment) {
        BookingDetail bookingDetail = assignment.getBookingDetail();
        Booking booking = bookingDetail.getBooking();

        return new AssignmentDetailResponse(
                assignment.getAssignmentId(),
                booking.getBookingCode(),
                bookingDetail.getService().getName(),
                booking.getCustomer().getFullName(),
                booking.getCustomer().getAccount().getPhoneNumber(),
                booking.getAddress().getFullAddress(),
                booking.getBookingTime(),
                bookingDetail.getService().getEstimatedDurationHours(),
                bookingDetail.getPricePerUnit(),
                bookingDetail.getQuantity(),
                bookingDetail.getSubTotal(),
                assignment.getStatus(),
                assignment.getCreatedAt(),
                assignment.getCheckInTime(),
                assignment.getCheckOutTime(),
                booking.getNote()
        );
    }

    private void ensureAssignmentBelongsToEmployee(Assignment assignment, String employeeId) {
        if (assignment.getEmployee() == null || assignment.getEmployee().getEmployeeId() == null
                || !assignment.getEmployee().getEmployeeId().equals(employeeId)) {
            throw new IllegalStateException("Nhân viên không có quyền truy cập công việc này");
        }
    }

    private void updateBookingStatusToInProgressIfNeeded(Booking booking, LocalDateTime referenceTime) {
        if (booking == null) {
            return;
        }

        List<Assignment> bookingAssignments = assignmentRepository.findByBookingIdWithStatus(booking.getBookingId());

        boolean allStarted = bookingAssignments.stream()
                .filter(a -> a.getStatus() != AssignmentStatus.CANCELLED)
                .allMatch(a -> a.getStatus() == AssignmentStatus.IN_PROGRESS || a.getStatus() == AssignmentStatus.COMPLETED);

        if (allStarted && booking.getStatus() != BookingStatus.IN_PROGRESS) {
            booking.setStatus(BookingStatus.IN_PROGRESS);
            booking.setUpdatedAt(referenceTime);
            bookingRepository.save(booking);
        }
    }

    private void updateBookingStatusToCompletedIfNeeded(Booking booking, LocalDateTime referenceTime) {
        if (booking == null) {
            return;
        }

        List<Assignment> bookingAssignments = assignmentRepository.findByBookingIdWithStatus(booking.getBookingId());

        boolean allCompleted = bookingAssignments.stream()
                .filter(a -> a.getStatus() != AssignmentStatus.CANCELLED)
                .allMatch(a -> a.getStatus() == AssignmentStatus.COMPLETED);

        if (allCompleted && booking.getStatus() != BookingStatus.COMPLETED) {
            booking.setStatus(BookingStatus.COMPLETED);
            booking.setUpdatedAt(referenceTime);
            bookingRepository.save(booking);
        }
    }
}