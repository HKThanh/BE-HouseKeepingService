package iuh.house_keeping_service_be.exceptions;

import java.util.List;

public class RecurringBookingValidationException extends RuntimeException {
    private final List<String> errors;
    private final String errorCode;

    public RecurringBookingValidationException(String message, String errorCode, List<String> errors) {
        super(message);
        this.errorCode = errorCode;
        this.errors = errors != null ? List.copyOf(errors) : List.of();
    }

    public List<String> getErrors() {
        return errors;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public static RecurringBookingValidationException timeRuleViolation(List<String> errors) {
        return new RecurringBookingValidationException(
                "Vi phạm quy tắc thời gian của lịch định kỳ",
                "RECURRING_TIME_INVALID",
                errors
        );
    }
}
