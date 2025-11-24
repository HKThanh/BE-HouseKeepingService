package iuh.house_keeping_service_be.exceptions;

import iuh.house_keeping_service_be.dtos.Booking.response.BookingErrorResponse;
import iuh.house_keeping_service_be.dtos.EmployeeSchedule.ApiResponse;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // Bắt tất cả các lỗi Exception chung
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGlobalException(Exception e) {
//         log.error("Unhandled exception occurred: ", e); // Ghi log lỗi
        ApiResponse<?> errorResponse = new ApiResponse<>(false, "An internal server error occurred: " + e.getMessage(), null);
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleResourceNotFoundException(ResourceNotFoundException e) {
        ApiResponse<?> errorResponse = new ApiResponse<>(false, e.getMessage(), null);
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND); // Trả về mã 404
    }

    @ExceptionHandler(BookingValidationException.class)
    public ResponseEntity<BookingErrorResponse> handleBookingValidation(BookingValidationException ex) {
        log.warn("Booking validation failed: {}", ex.getMessage());
        
        var response = BookingErrorResponse.validationError(
            "Booking validation failed", 
            ex.getErrors()
        );
        
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(EmployeeConflictException.class)
    public ResponseEntity<BookingErrorResponse> handleEmployeeConflict(EmployeeConflictException ex) {
        log.warn("Employee conflict detected: {}", ex.getMessage());
        
        var response = BookingErrorResponse.conflictError(
            "Employee scheduling conflict detected", 
            ex.getConflicts()
        );
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(BookingCreationException.class)
    public ResponseEntity<BookingErrorResponse> handleBookingCreation(BookingCreationException ex) {
        log.error("Booking creation failed: {}", ex.getMessage(), ex);
        
        var response = BookingErrorResponse.businessError(
            ex.getMessage(),
            "BOOKING_CREATION_FAILED"
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(RecurringBookingValidationException.class)
    public ResponseEntity<BookingErrorResponse> handleRecurringValidation(RecurringBookingValidationException ex) {
        log.warn("Recurring booking validation failed: {}", ex.getErrors());

        var response = BookingErrorResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .errorCode(ex.getErrorCode())
                .validationErrors(ex.getErrors())
                .conflicts(List.of())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(PriceCalculationException.class)
    public ResponseEntity<BookingErrorResponse> handlePriceCalculation(PriceCalculationException ex) {
        log.error("Price calculation failed: {}", ex.getMessage(), ex);
        
        String message;
        if (ex.getContext() == null) {
            message = ex.getMessage();
        } else {
            var context = ex.getContext();
            message = String.format("Price calculation failed for service '%s' (ID: %d): %s",
                context.serviceName(), context.serviceId(), context.details());
        }
        
        var response = BookingErrorResponse.businessError(message, "PRICE_CALCULATION_ERROR");
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(BookingTimeException.class)
    public ResponseEntity<BookingErrorResponse> handleBookingTime(BookingTimeException ex) {
        log.warn("Booking time constraint violation: {}", ex.getMessage());
        
        String errorCode;
        if (ex.getMessage().contains("Invalid booking time range")) {
            errorCode = "INVALID_TIME_RANGE";
        } else if (ex.getMessage().contains("outside business hours")) {
            errorCode = "OUTSIDE_BUSINESS_HOURS";
        } else if (ex.getMessage().contains("in the past")) {
            errorCode = "TIME_IN_PAST";
        } else {
            errorCode = "BOOKING_TIME_ERROR";
        }
        
        var response = BookingErrorResponse.businessError(ex.getMessage(), errorCode);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler({
        BookingNotFoundException.class,
        AddressNotFoundException.class,
        ServiceNotFoundException.class,
        EmployeeNotFoundException.class,
        CustomerNotFoundException.class
    })
    public ResponseEntity<BookingErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        
        String errorCode;
        if (ex instanceof BookingNotFoundException) {
            errorCode = "BOOKING_NOT_FOUND";
        } else if (ex instanceof AddressNotFoundException) {
            errorCode = "ADDRESS_NOT_FOUND";
        } else if (ex instanceof ServiceNotFoundException) {
            errorCode = "SERVICE_NOT_FOUND";
        } else if (ex instanceof EmployeeNotFoundException) {
            errorCode = "EMPLOYEE_NOT_FOUND";
        } else if (ex instanceof CustomerNotFoundException) {
            errorCode = "CUSTOMER_NOT_FOUND";
        } else {
            errorCode = "RESOURCE_NOT_FOUND";
        }
        
        var response = BookingErrorResponse.businessError(ex.getMessage(), errorCode);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BookingErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        log.warn("Request validation failed: {}", ex.getMessage());
        
        List<String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.toList());
        
        var response = BookingErrorResponse.validationError("Request validation failed", errors);
        return ResponseEntity.badRequest().body(response);
    }


    @ExceptionHandler(ReviewPermissionException.class)
    public ResponseEntity<ApiResponse<?>> handleReviewPermission(ReviewPermissionException ex) {
        log.warn("Review permission denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse<>(false, ex.getMessage(), null));
    }

    @ExceptionHandler(ReviewAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<?>> handleDuplicateReview(ReviewAlreadyExistsException ex) {
        log.warn("Duplicate review detected: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiResponse<>(false, ex.getMessage(), null));
    }

    @ExceptionHandler({ReviewBookingStateException.class, ReviewAssignmentException.class})
    public ResponseEntity<ApiResponse<?>> handleReviewBusinessExceptions(RuntimeException ex) {
        log.warn("Review validation failed: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, ex.getMessage(), null));
    }
}
