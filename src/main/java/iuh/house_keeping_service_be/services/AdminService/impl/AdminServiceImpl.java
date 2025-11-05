package iuh.house_keeping_service_be.services.AdminService.impl;

import iuh.house_keeping_service_be.dtos.Statistics.RevenueStatisticsResponse;
import iuh.house_keeping_service_be.dtos.Statistics.ServiceBookingStatisticsResponse;
import iuh.house_keeping_service_be.enums.RoleName;
import iuh.house_keeping_service_be.models.AdminProfile;
import iuh.house_keeping_service_be.repositories.AdminProfileRepository;
import iuh.house_keeping_service_be.repositories.BookingRepository;
import iuh.house_keeping_service_be.services.AdminService.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminProfileRepository adminProfileRepository;

    @Autowired
    private BookingRepository bookingRepository;

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
}