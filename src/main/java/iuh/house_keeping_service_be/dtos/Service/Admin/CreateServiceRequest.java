package iuh.house_keeping_service_be.dtos.Service.Admin;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateServiceRequest {
    
    @NotBlank(message = "Tên dịch vụ không được để trống")
    @Size(max = 100, message = "Tên dịch vụ không được vượt quá 100 ký tự")
    private String name;
    
    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
    private String description;
    
    @NotNull(message = "Giá cơ bản không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá cơ bản phải lớn hơn 0")
    private BigDecimal basePrice;
    
    @NotBlank(message = "Đơn vị không được để trống")
    @Size(max = 20, message = "Đơn vị không được vượt quá 20 ký tự")
    private String unit;
    
    @DecimalMin(value = "0.0", message = "Thời gian ước tính phải không âm")
    private BigDecimal estimatedDurationHours;
    
    @NotNull(message = "Số nhân viên khuyến nghị không được để trống")
    @Min(value = 1, message = "Số nhân viên khuyến nghị phải ít nhất là 1")
    private Integer recommendedStaff;
    
    @Size(max = 255, message = "URL icon không được vượt quá 255 ký tự")
    private String iconUrl;
    
    @NotNull(message = "Category ID không được để trống")
    private Integer categoryId;
}
