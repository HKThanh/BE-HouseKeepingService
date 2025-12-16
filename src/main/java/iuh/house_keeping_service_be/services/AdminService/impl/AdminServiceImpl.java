package iuh.house_keeping_service_be.services.AdminService.impl;

import iuh.house_keeping_service_be.dtos.Admin.response.AdminProfileResponse;
import iuh.house_keeping_service_be.dtos.Admin.response.UserAccountResponse;
import iuh.house_keeping_service_be.dtos.Statistics.RevenueStatisticsResponse;
import iuh.house_keeping_service_be.dtos.Statistics.ServiceBookingStatisticsResponse;
import iuh.house_keeping_service_be.enums.AccountStatus;
import iuh.house_keeping_service_be.enums.RoleName;
import iuh.house_keeping_service_be.enums.UserType;
import iuh.house_keeping_service_be.models.AdminProfile;
import iuh.house_keeping_service_be.models.Customer;
import iuh.house_keeping_service_be.models.Employee;
import iuh.house_keeping_service_be.models.Role;
import iuh.house_keeping_service_be.repositories.AdminProfileRepository;
import iuh.house_keeping_service_be.repositories.BookingRepository;
import iuh.house_keeping_service_be.repositories.CustomerRepository;
import iuh.house_keeping_service_be.repositories.EmployeeRepository;
import iuh.house_keeping_service_be.services.AdminService.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminProfileRepository adminProfileRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public AdminProfile findByAccountId(String accountId) {
        return adminProfileRepository.findByAccount_AccountId(accountId)
                .orElseThrow(() -> new RuntimeException("Không thể tìm thấy admin với id account này: " + accountId));
    }

    @Override
    public AdminProfile findByContactInfoAndAccountRole(String contactInfo, RoleName roleName) {
        return adminProfileRepository.findByContactInfoAndAccountRole(contactInfo, roleName)
                .orElseThrow(() -> new RuntimeException("Không thể tìm thấy thông tin này: " + contactInfo));
    }

    @Override
    public AdminProfile findById(String id) {
        return adminProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin"));
    }

    @Override
    public AdminProfileResponse getAdminProfile(String adminProfileId) {
        AdminProfile admin = findById(adminProfileId);
        
        return AdminProfileResponse.builder()
                .adminProfileId(admin.getAdminProfileId())
                .fullName(admin.getFullName())
                .isMale(admin.getIsMale())
                .department(admin.getDepartment())
                .contactInfo(admin.getContactInfo())
                .birthdate(admin.getBirthdate())
                .hireDate(admin.getHireDate())
                .account(AdminProfileResponse.AccountInfo.builder()
                        .accountId(admin.getAccount().getAccountId())
                        .phoneNumber(admin.getAccount().getPhoneNumber())
                        .status(admin.getAccount().getStatus())
                        .isPhoneVerified(admin.getAccount().getIsPhoneVerified())
                        .lastLogin(admin.getAccount().getLastLogin())
                        .roles(admin.getAccount().getRoles().stream()
                                .map(Role::getRoleName)
                                .map(Enum::name)
                                .collect(Collectors.toList()))
                        .build())
                .build();
    }

    @Override
    public boolean isAdminByUsername(String username) {
        return !adminProfileRepository.existsByAccount_Username(username);
    }

    @Override
    public ServiceBookingStatisticsResponse getServiceBookingStatistics(String period, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime;
        LocalDateTime endDateTime;
        String periodLabel;

        // Calculate date range based on period
        if (startDate != null && endDate != null) {
            // Custom date range
            startDateTime = startDate.atStartOfDay();
            endDateTime = endDate.atTime(LocalTime.MAX);
            periodLabel = period != null ? period.toUpperCase() : "CUSTOM";
        } else {
            LocalDate now = LocalDate.now();
            
            switch (period.toUpperCase()) {
                case "DAY":
                    startDateTime = now.atStartOfDay();
                    endDateTime = now.atTime(LocalTime.MAX);
                    periodLabel = "DAY";
                    break;
                    
                case "WEEK":
                    LocalDate startOfWeek = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                    LocalDate endOfWeek = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
                    startDateTime = startOfWeek.atStartOfDay();
                    endDateTime = endOfWeek.atTime(LocalTime.MAX);
                    periodLabel = "WEEK";
                    break;
                    
                case "MONTH":
                    startDateTime = now.with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay();
                    endDateTime = now.with(TemporalAdjusters.lastDayOfMonth()).atTime(LocalTime.MAX);
                    periodLabel = "MONTH";
                    break;
                    
                case "QUARTER":
                    int currentMonth = now.getMonthValue();
                    int quarterStartMonth = ((currentMonth - 1) / 3) * 3 + 1;
                    LocalDate quarterStart = LocalDate.of(now.getYear(), quarterStartMonth, 1);
                    LocalDate quarterEnd = quarterStart.plusMonths(2).with(TemporalAdjusters.lastDayOfMonth());
                    startDateTime = quarterStart.atStartOfDay();
                    endDateTime = quarterEnd.atTime(LocalTime.MAX);
                    periodLabel = "QUARTER";
                    break;
                    
                case "YEAR":
                    startDateTime = now.with(TemporalAdjusters.firstDayOfYear()).atStartOfDay();
                    endDateTime = now.with(TemporalAdjusters.lastDayOfYear()).atTime(LocalTime.MAX);
                    periodLabel = "YEAR";
                    break;
                    
                default:
                    throw new IllegalArgumentException("Invalid period: " + period + ". Must be DAY, WEEK, MONTH, QUARTER, or YEAR");
            }
        }

        log.info("Getting service booking statistics for period: {} from {} to {}", periodLabel, startDateTime, endDateTime);

        // Get statistics from repository
        List<Object[]> rawStats = bookingRepository.getServiceBookingStatistics(startDateTime, endDateTime);
        
        // Calculate total bookings
        long totalBookings = rawStats.stream()
                .mapToLong(row -> ((Number) row[2]).longValue())
                .sum();

        // Transform to response DTOs
        List<ServiceBookingStatisticsResponse.ServiceStatistic> serviceStats = new ArrayList<>();
        int rank = 1;
        
        for (Object[] row : rawStats) {
            Integer serviceIdInt = (Integer) row[0];
            String serviceId = serviceIdInt != null ? serviceIdInt.toString() : null;
            String serviceName = (String) row[1];
            Long bookingCount = ((Number) row[2]).longValue();
            Double percentage = totalBookings > 0 ? (bookingCount * 100.0 / totalBookings) : 0.0;
            
            serviceStats.add(ServiceBookingStatisticsResponse.ServiceStatistic.builder()
                    .serviceId(serviceId)
                    .serviceName(serviceName)
                    .bookingCount(bookingCount)
                    .percentage(Math.round(percentage * 100.0) / 100.0) // Round to 2 decimal places
                    .rank(rank++)
                    .build());
        }

        return ServiceBookingStatisticsResponse.builder()
                .period(periodLabel)
                .startDate(startDateTime.toLocalDate().toString())
                .endDate(endDateTime.toLocalDate().toString())
                .totalBookings(totalBookings)
                .serviceStatistics(serviceStats)
                .build();
    }

    @Override
    public RevenueStatisticsResponse getRevenueStatistics(String period, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime;
        LocalDateTime endDateTime;
        String periodLabel;

        // Calculate date range based on period
        if (startDate != null && endDate != null) {
            // Custom date range
            startDateTime = startDate.atStartOfDay();
            endDateTime = endDate.atTime(LocalTime.MAX);
            periodLabel = period != null ? period.toUpperCase() : "CUSTOM";
        } else {
            LocalDate now = LocalDate.now();
            
            switch (period.toUpperCase()) {
                case "DAY":
                    startDateTime = now.atStartOfDay();
                    endDateTime = now.atTime(LocalTime.MAX);
                    periodLabel = "DAY";
                    break;
                    
                case "WEEK":
                    LocalDate startOfWeek = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                    LocalDate endOfWeek = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
                    startDateTime = startOfWeek.atStartOfDay();
                    endDateTime = endOfWeek.atTime(LocalTime.MAX);
                    periodLabel = "WEEK";
                    break;
                    
                case "MONTH":
                    startDateTime = now.with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay();
                    endDateTime = now.with(TemporalAdjusters.lastDayOfMonth()).atTime(LocalTime.MAX);
                    periodLabel = "MONTH";
                    break;
                    
                case "QUARTER":
                    int currentMonth = now.getMonthValue();
                    int quarterStartMonth = ((currentMonth - 1) / 3) * 3 + 1;
                    LocalDate quarterStart = LocalDate.of(now.getYear(), quarterStartMonth, 1);
                    LocalDate quarterEnd = quarterStart.plusMonths(2).with(TemporalAdjusters.lastDayOfMonth());
                    startDateTime = quarterStart.atStartOfDay();
                    endDateTime = quarterEnd.atTime(LocalTime.MAX);
                    periodLabel = "QUARTER";
                    break;
                    
                case "YEAR":
                    startDateTime = now.with(TemporalAdjusters.firstDayOfYear()).atStartOfDay();
                    endDateTime = now.with(TemporalAdjusters.lastDayOfYear()).atTime(LocalTime.MAX);
                    periodLabel = "YEAR";
                    break;
                    
                default:
                    throw new IllegalArgumentException("Invalid period: " + period + ". Must be DAY, WEEK, MONTH, QUARTER, or YEAR");
            }
        }

        log.info("Getting revenue statistics for period: {} from {} to {}", periodLabel, startDateTime, endDateTime);

        // Get statistics from repository
        Object[] rawStats = bookingRepository.getRevenueStatistics(startDateTime, endDateTime);
        
        log.debug("Raw stats from repository: length={}, content={}", rawStats != null ? rawStats.length : 0, 
                  rawStats != null ? java.util.Arrays.toString(rawStats) : "null");
        
        // Handle case when query returns a single array containing all values
        Object[] statsArray = rawStats;
        if (rawStats != null && rawStats.length == 1 && rawStats[0] instanceof Object[]) {
            statsArray = (Object[]) rawStats[0];
        }
        
        // Extract values from query result and convert to proper types
        BigDecimal totalRevenue = statsArray != null && statsArray.length > 0 ? convertToBigDecimal(statsArray[0]) : BigDecimal.ZERO;
        Long totalBookings = statsArray != null && statsArray.length > 1 && statsArray[1] != null ? ((Number) statsArray[1]).longValue() : 0L;
        BigDecimal averageRevenue = statsArray != null && statsArray.length > 2 ? convertToBigDecimal(statsArray[2]) : BigDecimal.ZERO;
        BigDecimal maxAmount = statsArray != null && statsArray.length > 3 ? convertToBigDecimal(statsArray[3]) : BigDecimal.ZERO;
        BigDecimal minAmount = statsArray != null && statsArray.length > 4 ? convertToBigDecimal(statsArray[4]) : BigDecimal.ZERO;

        // Handle case when no bookings exist
        if (totalBookings == 0) {
            totalRevenue = BigDecimal.ZERO;
            averageRevenue = BigDecimal.ZERO;
            maxAmount = BigDecimal.ZERO;
            minAmount = BigDecimal.ZERO;
        }

        return RevenueStatisticsResponse.builder()
                .period(periodLabel)
                .startDate(startDateTime.toLocalDate().toString())
                .endDate(endDateTime.toLocalDate().toString())
                .totalRevenue(totalRevenue)
                .totalBookings(totalBookings)
                .averageRevenuePerBooking(averageRevenue)
                .maxBookingAmount(maxAmount)
                .minBookingAmount(minAmount)
                .build();
    }

    /**
     * Helper method to safely convert Object to BigDecimal
     * Handles various Number types from database queries
     */
    private BigDecimal convertToBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            log.warn("Failed to convert value to BigDecimal: {}", value);
            return BigDecimal.ZERO;
        }
    }

    @Override
    public Page<UserAccountResponse> getUserAccounts(UserType userType, AccountStatus status, Pageable pageable) {
        log.info("Getting user accounts - userType: {}, status: {}, page: {}, size: {}", 
                 userType, status, pageable.getPageNumber(), pageable.getPageSize());

        List<UserAccountResponse> allUsers = new ArrayList<>();
        long totalElements = 0;

        if (userType == null || userType == UserType.ALL || userType == UserType.CUSTOMER) {
            Page<Customer> customers = customerRepository.findAllWithAccountByStatus(status, pageable);
            List<UserAccountResponse> customerResponses = customers.getContent().stream()
                    .map(this::mapCustomerToUserAccountResponse)
                    .collect(Collectors.toList());
            allUsers.addAll(customerResponses);
            totalElements += customers.getTotalElements();
        }

        if (userType == null || userType == UserType.ALL || userType == UserType.EMPLOYEE) {
            Page<Employee> employees = employeeRepository.findAllWithAccountByStatus(status, pageable);
            List<UserAccountResponse> employeeResponses = employees.getContent().stream()
                    .map(this::mapEmployeeToUserAccountResponse)
                    .collect(Collectors.toList());
            allUsers.addAll(employeeResponses);
            totalElements += employees.getTotalElements();
        }

        return new PageImpl<>(allUsers, pageable, totalElements);
    }

    private UserAccountResponse mapCustomerToUserAccountResponse(Customer customer) {
        return UserAccountResponse.builder()
                .userType("CUSTOMER")
                .account(UserAccountResponse.AccountInfo.builder()
                        .accountId(customer.getAccount().getAccountId())
                        .phoneNumber(customer.getAccount().getPhoneNumber())
                        .status(customer.getAccount().getStatus())
                        .isPhoneVerified(customer.getAccount().getIsPhoneVerified())
                        .lastLogin(customer.getAccount().getLastLogin())
                        .roles(customer.getAccount().getRoles() != null
                                ? customer.getAccount().getRoles().stream()
                                        .map(Role::getRoleName)
                                        .map(Enum::name)
                                        .collect(Collectors.toList())
                                : new ArrayList<>())
                        .build())
                .profile(UserAccountResponse.UserProfileInfo.builder()
                        .id(customer.getCustomerId())
                        .avatar(customer.getAvatar())
                        .fullName(customer.getFullName())
                        .isMale(customer.getIsMale())
                        .email(customer.getEmail())
                        .isEmailVerified(customer.getIsEmailVerified())
                        .birthdate(customer.getBirthdate())
                        .rating(customer.getRating())
                        .vipLevel(customer.getVipLevel())
                        .build())
                .build();
    }

    private UserAccountResponse mapEmployeeToUserAccountResponse(Employee employee) {
        return UserAccountResponse.builder()
                .userType("EMPLOYEE")
                .account(UserAccountResponse.AccountInfo.builder()
                        .accountId(employee.getAccount().getAccountId())
                        .phoneNumber(employee.getAccount().getPhoneNumber())
                        .status(employee.getAccount().getStatus())
                        .isPhoneVerified(employee.getAccount().getIsPhoneVerified())
                        .lastLogin(employee.getAccount().getLastLogin())
                        .roles(employee.getAccount().getRoles() != null
                                ? employee.getAccount().getRoles().stream()
                                        .map(Role::getRoleName)
                                        .map(Enum::name)
                                        .collect(Collectors.toList())
                                : new ArrayList<>())
                        .build())
                .profile(UserAccountResponse.UserProfileInfo.builder()
                        .id(employee.getEmployeeId())
                        .avatar(employee.getAvatar())
                        .fullName(employee.getFullName())
                        .isMale(employee.getIsMale())
                        .email(employee.getEmail())
                        .isEmailVerified(employee.getIsEmailVerified())
                        .birthdate(employee.getBirthdate())
                        .rating(employee.getRating())
                        .hiredDate(employee.getHiredDate())
                        .skills(employee.getSkills())
                        .bio(employee.getBio())
                        .employeeStatus(employee.getEmployeeStatus())
                        .build())
                .build();
    }
}