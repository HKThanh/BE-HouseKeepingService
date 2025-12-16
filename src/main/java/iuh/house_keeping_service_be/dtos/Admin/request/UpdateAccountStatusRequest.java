package iuh.house_keeping_service_be.dtos.Admin.request;

import iuh.house_keeping_service_be.enums.AccountStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAccountStatusRequest {
    
    @NotNull(message = "Trạng thái tài khoản không được để trống")
    private AccountStatus status;
    
    private String reason; // Optional reason for status change
}
