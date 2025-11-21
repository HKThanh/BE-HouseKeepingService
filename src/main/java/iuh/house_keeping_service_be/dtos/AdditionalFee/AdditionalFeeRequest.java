package iuh.house_keeping_service_be.dtos.AdditionalFee;

import iuh.house_keeping_service_be.enums.AdditionalFeeType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdditionalFeeRequest {
    @NotBlank(message = "Tên phụ phí không được để trống")
    private String name;
    private String description;

    @NotNull(message = "Loại phụ phí bắt buộc")
    private AdditionalFeeType feeType;

    @NotNull(message = "Giá trị phụ phí bắt buộc")
    @DecimalMin(value = "0", message = "Giá trị phải lớn hơn hoặc bằng 0")
    private BigDecimal value;

    private Boolean systemSurcharge = false;
    private Boolean active = true;
    private Integer priority = 0;
}
