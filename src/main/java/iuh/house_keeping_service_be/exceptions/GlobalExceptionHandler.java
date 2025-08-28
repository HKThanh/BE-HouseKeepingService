package iuh.house_keeping_service_be.exceptions;

import iuh.house_keeping_service_be.dtos.EmployeeSchedule.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
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
}
