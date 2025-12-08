package iuh.house_keeping_service_be.enums;

/**
 * Các loại OTP được hỗ trợ
 */
public enum OtpType {
    REGISTER("Đăng ký tài khoản"),
    FORGOT_PASSWORD("Quên mật khẩu"),
    VERIFY_PHONE("Xác thực số điện thoại"),
    CHANGE_PHONE("Thay đổi số điện thoại"),
    VERIFY_EMAIL("Xác thực email");

    private final String description;

    OtpType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
