package iuh.house_keeping_service_be.utils;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public class BookingDTOFormatter {
    
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    public static String formatPrice(BigDecimal price) {
        if (price == null) return "0đ";
        return String.format("%,.0fđ", price);
    }
    
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DATE_TIME_FORMAT);
    }
    
    public static String formatDuration(BigDecimal hours) {
        if (hours == null) return "";
        
        int totalMinutes = (int) (hours.doubleValue() * 60);
        int hoursPart = totalMinutes / 60;
        int minutesPart = totalMinutes % 60;
        
        if (hoursPart > 0 && minutesPart > 0) {
            return hoursPart + " giờ " + minutesPart + " phút";
        } else if (hoursPart > 0) {
            return hoursPart + " giờ";
        } else {
            return minutesPart + " phút";
        }
    }
    
    public static String formatDuration(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return "";
        
        long minutes = ChronoUnit.MINUTES.between(start, end);
        int hours = (int) (minutes / 60);
        int remainingMinutes = (int) (minutes % 60);
        
        if (hours > 0 && remainingMinutes > 0) {
            return hours + " giờ " + remainingMinutes + " phút";
        } else if (hours > 0) {
            return hours + " giờ";
        } else {
            return remainingMinutes + " phút";
        }
    }
    
    public static String formatPhoneNumber(String phone) {
        if (phone == null || phone.length() < 10) return phone;
        
        // Format: 0xxx xxx xxx
        return phone.substring(0, 4) + " " + 
               phone.substring(4, 7) + " " + 
               phone.substring(7);
    }
}