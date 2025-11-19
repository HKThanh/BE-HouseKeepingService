package iuh.house_keeping_service_be.services.AssignmentService.impl;

import iuh.house_keeping_service_be.dtos.Assignment.request.AssignmentCancelRequest;
import iuh.house_keeping_service_be.dtos.Assignment.response.AssignmentDetailResponse;
import iuh.house_keeping_service_be.dtos.Assignment.response.BookingSummary;
import iuh.house_keeping_service_be.enums.AssignmentStatus;
import iuh.house_keeping_service_be.enums.BookingStatus;
import iuh.house_keeping_service_be.enums.MediaType;
import iuh.house_keeping_service_be.models.*;
import iuh.house_keeping_service_be.repositories.*;
import iuh.house_keeping_service_be.repositories.projections.ZoneCoordinate;
import iuh.house_keeping_service_be.services.AssignmentService.AssignmentService;
import iuh.house_keeping_service_be.services.BookingMediaService.BookingMediaService;
import iuh.house_keeping_service_be.services.NotificationService.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    private final BookingMediaService bookingMediaService;
    private final NotificationService notificationService;

    @Override
    public List<AssignmentDetailResponse> getEmployeeAssignments(String employeeId, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "bookingDetail.booking.bookingTime"));

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
                        .findPendingBookingsByZones(zoneKeys, pageable);

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
                ? bookingRepository.findPendingBookings(PageRequest.of(0, remainingSlots * 2))
                : bookingRepository.findPendingBookingsOutsideZones(zoneKeys);

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

        EnumSet<BookingStatus> allowedStatuses = EnumSet.of(BookingStatus.PENDING, BookingStatus.AWAITING_EMPLOYEE, BookingStatus.CONFIRMED);
        if (!allowedStatuses.contains(booking.getStatus())) {
            throw new IllegalStateException(String.format(
                    "Không thể nhận booking khi đang ở trạng thái %s", booking.getStatus().name()));
        }

        LocalDateTime shiftStart = bookingDetail.getBooking().getBookingTime();
        LocalDateTime shiftEnd = calculateShiftEndTime(shiftStart, bookingDetail);

        List<Assignment> conflictingAssignments = assignmentRepository.findConflictingAssignments(
                employeeId, shiftStart, shiftEnd, null);
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
        assignmentRepository.save(assignment);

        bookingDetail.getAssignments().add(assignment);

        // Check if all booking details are fully assigned
        boolean allAssigned = booking.getBookingDetails().stream()
                .allMatch(bd -> bd.getAssignments().size() >= bd.getQuantity());
        
        boolean wasAwaitingEmployee = booking.getStatus() == BookingStatus.AWAITING_EMPLOYEE;
        
        if (allAssigned && (booking.getStatus() == BookingStatus.PENDING || booking.getStatus() == BookingStatus.AWAITING_EMPLOYEE)) {
            booking.setStatus(BookingStatus.CONFIRMED);
            booking.setUpdatedAt(LocalDateTime.now());
            bookingRepository.save(booking);
            
            // Send notification to customer when booking is confirmed
            notificationService.sendBookingConfirmedNotification(
                booking.getCustomer().getAccount().getAccountId(),
                booking.getBookingId(),
                booking.getBookingCode()
            );
        } else if (wasAwaitingEmployee) {
            // Notification when employee joins AWAITING_EMPLOYEE booking (not yet fully assigned)
            String customerAccountId = booking.getCustomer().getAccount().getAccountId();
            notificationService.createNotification(
                new iuh.house_keeping_service_be.dtos.Notification.NotificationRequest(
                    customerAccountId,
                    "CUSTOMER", // Target role - notification for customer only
                    Notification.NotificationType.ASSIGNMENT_CREATED,
                    "Nhân viên đã tham gia",
                    String.format("Nhân viên %s đã tham gia vào booking %s của bạn", 
                        employee.getFullName(), booking.getBookingCode()),
                    booking.getBookingId(),
                    Notification.RelatedEntityType.BOOKING,
                    Notification.NotificationPriority.NORMAL,
                    "/bookings/" + booking.getBookingId()
                )
            );
        }

        return mapToAssignmentDetailResponse(assignment);
    }

    @Override
    @Transactional
    public AssignmentDetailResponse checkIn(String assignmentId, String employeeId, List<MultipartFile> images, String imageDescription) {
        Assignment assignment = assignmentRepository.findByIdWithDetails(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy công việc"));

        ensureAssignmentBelongsToEmployee(assignment, employeeId);

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

        // Upload check-in images if provided
        if (images != null && !images.isEmpty()) {
            for (MultipartFile image : images) {
                if (image != null && !image.isEmpty()) {
                    try {
                        bookingMediaService.uploadMedia(savedAssignment, image, MediaType.CHECK_IN_IMAGE, imageDescription);
                        log.info("Check-in image uploaded successfully for assignment {}", assignmentId);
                    } catch (Exception e) {
                        log.error("Failed to upload check-in image for assignment {}: {}", assignmentId, e.getMessage());
                        // Don't fail the check-in if image upload fails
                    }
                }
            }
        }

        updateBookingStatusToInProgressIfNeeded(booking, now);

        return mapToAssignmentDetailResponse(savedAssignment);
    }

    @Override
    @Transactional
    public AssignmentDetailResponse checkOut(String assignmentId, String employeeId, List<MultipartFile> images, String imageDescription) {
        Assignment assignment = assignmentRepository.findByIdWithDetails(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy công việc"));

        ensureAssignmentBelongsToEmployee(assignment, employeeId);

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

        // Upload check-out images if provided
        if (images != null && !images.isEmpty()) {
            for (MultipartFile image : images) {
                if (image != null && !image.isEmpty()) {
                    try {
                        bookingMediaService.uploadMedia(savedAssignment, image, MediaType.CHECK_OUT_IMAGE, imageDescription);
                        log.info("Check-out image uploaded successfully for assignment {}", assignmentId);
                    } catch (Exception e) {
                        log.error("Failed to upload check-out image for assignment {}: {}", assignmentId, e.getMessage());
                        // Don't fail the check-out if image upload fails
                    }
                }
            }
        }

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

        // Check if assignment can be cancelled (PENDING or ASSIGNED)
        if (assignment.getStatus() != AssignmentStatus.PENDING && assignment.getStatus() != AssignmentStatus.ASSIGNED) {
            throw new IllegalStateException("Chỉ có thể hủy công việc đang ở trạng thái 'Chờ xác nhận' hoặc 'Đã nhận'");
        }

        BookingDetail bookingDetail = assignment.getBookingDetail();
        Booking booking = bookingDetail.getBooking();

        // Check if booking is not too close to start time (30 minutes before)
        LocalDateTime now = LocalDateTime.now();
        if (booking.getBookingTime().isBefore(now.plusMinutes(30))) {
            throw new IllegalStateException("Không thể hủy công việc trong vòng 30 phút trước giờ bắt đầu");
        }

        try {
            // Update assignment status
            assignment.setStatus(AssignmentStatus.CANCELLED);
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

    @Override
    @Transactional
    public AssignmentDetailResponse acceptAssignment(String assignmentId, String employeeId) {
        Assignment assignment = assignmentRepository.findByIdWithDetails(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy công việc"));

        ensureAssignmentBelongsToEmployee(assignment, employeeId);

        // Check if assignment is in PENDING status
        if (assignment.getStatus() != AssignmentStatus.PENDING) {
            throw new IllegalStateException(String.format(
                    "Không thể nhận công việc đang ở trạng thái %s. Chỉ có thể nhận công việc đang ở trạng thái PENDING.",
                    assignment.getStatus().name()
            ));
        }

        BookingDetail bookingDetail = assignment.getBookingDetail();
        Booking booking = bookingDetail.getBooking();

        // Check time conflict with other assignments (exclude current assignment)
        LocalDateTime shiftStart = booking.getBookingTime();
        LocalDateTime shiftEnd = calculateShiftEndTime(shiftStart, bookingDetail);

        List<Assignment> conflictingAssignments = assignmentRepository.findConflictingAssignments(
                employeeId, shiftStart, shiftEnd, assignmentId);
        if (!conflictingAssignments.isEmpty()) {
            throw new IllegalStateException("Nhân viên đã được phân công công việc khác trong khung giờ này");
        }

        // Check leave/unavailability conflict
        List<EmployeeUnavailability> unavailabilities =
                employeeUnavailabilityRepository.findByEmployeeAndPeriod(employeeId, shiftStart, shiftEnd);
        boolean hasLeaveConflict = employeeUnavailabilityRepository.hasConflict(employeeId, shiftStart, shiftEnd);
        if (!unavailabilities.isEmpty() || hasLeaveConflict) {
            throw new IllegalStateException("Nhân viên đang có lịch nghỉ được phê duyệt trong khung giờ này");
        }

        // Update assignment status to ASSIGNED
        assignment.setStatus(AssignmentStatus.ASSIGNED);
        Assignment savedAssignment = assignmentRepository.save(assignment);

        // Check if all assignments for the booking are now assigned
        boolean allAssigned = booking.getBookingDetails().stream()
                .allMatch(bd -> bd.getAssignments().stream()
                        .filter(a -> a.getStatus() != AssignmentStatus.CANCELLED)
                        .allMatch(a -> a.getStatus() == AssignmentStatus.ASSIGNED || 
                                      a.getStatus() == AssignmentStatus.IN_PROGRESS || 
                                      a.getStatus() == AssignmentStatus.COMPLETED)
                );

        if (allAssigned && booking.getStatus() == BookingStatus.PENDING) {
            booking.setStatus(BookingStatus.CONFIRMED);
            booking.setUpdatedAt(LocalDateTime.now());
            bookingRepository.save(booking);
        }

        log.info("Assignment {} accepted by employee {}", assignmentId, employeeId);

        return mapToAssignmentDetailResponse(savedAssignment);
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
            String normalizedReason = (reason != null && !reason.trim().isEmpty())
                    ? reason.trim()
                    : "Nhân viên hủy công việc";

            Customer customer = booking.getCustomer();
            Account account = customer != null ? customer.getAccount() : null;
            if (account != null) {
                notificationService.sendAssignmentCancelledNotification(
                        account.getAccountId(),
                        booking.getBookingId(),
                        booking.getBookingCode(),
                        normalizedReason
                );
            } else {
                log.warn("Skip crisis notification for assignment {} because booking {} is missing customer account",
                        assignment.getAssignmentId(), booking.getBookingId());
            }

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
                null, // assignedAt - timestamp field removed from Assignment
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

    @Override
    public iuh.house_keeping_service_be.dtos.Assignment.response.AssignmentStatisticsByStatusResponse 
            getAssignmentStatisticsByStatus(String employeeId, String timeUnit, LocalDateTime startDate, LocalDateTime endDate) {
        
        log.info("Getting assignment statistics for employee: {}, timeUnit: {}, from: {} to: {}", 
                 employeeId, timeUnit, startDate, endDate);
        
        // Validate employee exists
        employeeRepository.findById(employeeId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên với ID: " + employeeId));
        
        // Calculate date range based on time unit if not provided
        LocalDateTime calculatedStartDate = startDate;
        LocalDateTime calculatedEndDate = endDate;
        
        if (startDate == null || endDate == null) {
            calculatedEndDate = LocalDateTime.now();
            
            switch (timeUnit.toUpperCase()) {
                case "DAY":
                    calculatedStartDate = calculatedEndDate.minusDays(1).withHour(0).withMinute(0).withSecond(0);
                    calculatedEndDate = calculatedEndDate.withHour(23).withMinute(59).withSecond(59);
                    break;
                case "WEEK":
                    calculatedStartDate = calculatedEndDate.minusWeeks(1).withHour(0).withMinute(0).withSecond(0);
                    break;
                case "MONTH":
                    calculatedStartDate = calculatedEndDate.minusMonths(1).withHour(0).withMinute(0).withSecond(0);
                    break;
                case "YEAR":
                    calculatedStartDate = calculatedEndDate.minusYears(1).withHour(0).withMinute(0).withSecond(0);
                    break;
                default:
                    throw new IllegalArgumentException("Đơn vị thời gian không hợp lệ. Chỉ chấp nhận: DAY, WEEK, MONTH, YEAR");
            }
        }
        
        // Count assignments by each status
        Map<AssignmentStatus, Long> countByStatus = new HashMap<>();
        long totalAssignments = 0;
        
        for (AssignmentStatus status : AssignmentStatus.values()) {
            long count = assignmentRepository.countByEmployeeIdAndStatusAndDateRange(
                employeeId, status, calculatedStartDate, calculatedEndDate);
            countByStatus.put(status, count);
            totalAssignments += count;
        }
        
        log.info("Statistics retrieved: {} total assignments for employee {}", totalAssignments, employeeId);
        
        return iuh.house_keeping_service_be.dtos.Assignment.response.AssignmentStatisticsByStatusResponse.builder()
            .timeUnit(timeUnit.toUpperCase())
            .startDate(calculatedStartDate.toString())
            .endDate(calculatedEndDate.toString())
            .totalAssignments(totalAssignments)
            .countByStatus(countByStatus)
            .build();
    }

    @Override
    public iuh.house_keeping_service_be.dtos.Employee.response.EmployeeBookingStatisticsByStatusResponse 
            getEmployeeBookingStatisticsByStatus(String employeeId, String timeUnit, LocalDateTime startDate, LocalDateTime endDate) {
        
        log.info("Getting booking statistics for employee: {}, timeUnit: {}, from: {} to: {}", 
                 employeeId, timeUnit, startDate, endDate);
        
        // Validate employee exists
        employeeRepository.findById(employeeId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên với ID: " + employeeId));
        
        // Calculate date range based on time unit if not provided
        LocalDateTime calculatedStartDate = startDate;
        LocalDateTime calculatedEndDate = endDate;
        
        if (startDate == null || endDate == null) {
            calculatedEndDate = LocalDateTime.now();
            
            switch (timeUnit.toUpperCase()) {
                case "DAY":
                    calculatedStartDate = calculatedEndDate.minusDays(1).withHour(0).withMinute(0).withSecond(0);
                    calculatedEndDate = calculatedEndDate.withHour(23).withMinute(59).withSecond(59);
                    break;
                case "WEEK":
                    calculatedStartDate = calculatedEndDate.minusWeeks(1).withHour(0).withMinute(0).withSecond(0);
                    break;
                case "MONTH":
                    calculatedStartDate = calculatedEndDate.minusMonths(1).withHour(0).withMinute(0).withSecond(0);
                    break;
                case "YEAR":
                    calculatedStartDate = calculatedEndDate.minusYears(1).withHour(0).withMinute(0).withSecond(0);
                    break;
                default:
                    throw new IllegalArgumentException("Đơn vị thời gian không hợp lệ. Chỉ chấp nhận: DAY, WEEK, MONTH, YEAR");
            }
        }
        
        // Count distinct bookings by each booking status
        Map<BookingStatus, Long> countByStatus = new HashMap<>();
        long totalBookings = 0;
        
        for (BookingStatus status : BookingStatus.values()) {
            long count = assignmentRepository.countDistinctBookingsByEmployeeIdAndBookingStatusAndDateRange(
                employeeId, status, calculatedStartDate, calculatedEndDate);
            countByStatus.put(status, count);
            totalBookings += count;
        }
        
        log.info("Statistics retrieved: {} total bookings for employee {}", totalBookings, employeeId);
        
        return iuh.house_keeping_service_be.dtos.Employee.response.EmployeeBookingStatisticsByStatusResponse.builder()
            .timeUnit(timeUnit.toUpperCase())
            .startDate(calculatedStartDate.toString())
            .endDate(calculatedEndDate.toString())
            .totalBookings(totalBookings)
            .countByStatus(countByStatus)
            .build();
    }
}
