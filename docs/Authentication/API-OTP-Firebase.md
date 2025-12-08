# API OTP - Firebase Phone Authentication

## Tổng quan

API OTP sử dụng Firebase Authentication để gửi và xác thực mã OTP qua số điện thoại. Hỗ trợ các use case: đăng ký tài khoản, quên mật khẩu, xác thực số điện thoại, thay đổi số điện thoại.

**Base URL:** `/api/v1/otp`

---

## Endpoints

### 1. Gửi OTP

**Endpoint:** `POST /api/v1/otp/send`

**Mô tả:** Gửi mã OTP đến số điện thoại người dùng thông qua Firebase.

#### Request Body

```json
{
  "phoneNumber": "0912345678",
  "otpType": "REGISTER",
  "recaptchaToken": "optional_recaptcha_token"
}
```

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| `phoneNumber` | string | ✅ | Regex: `^(\+84|84|0)?[0-9]{9,10}$` | Số điện thoại Việt Nam |
| `otpType` | string | ✅ | Enum: `REGISTER`, `FORGOT_PASSWORD`, `VERIFY_PHONE`, `CHANGE_PHONE` | Loại OTP |
| `recaptchaToken` | string | ❌ | - | Token reCAPTCHA (nếu có) |

#### Response

**Success (200 OK)**
```json
{
  "success": true,
  "message": "Mã OTP đã được gửi đến số điện thoại của bạn",
  "sessionInfo": "firebase_session_info_string",
  "expirationSeconds": 300,
  "resendAfterSeconds": 60
}
```

**Error - Too Many Requests (429)**
```json
{
  "success": false,
  "message": "Vui lòng chờ 45 giây trước khi gửi lại OTP",
  "resendAfterSeconds": 45
}
```

**Error - Bad Request (400)**
```json
{
  "success": false,
  "message": "Số điện thoại đã được sử dụng"
}
```

#### Lưu ý cho FE

1. **Lưu `sessionInfo`**: Giá trị này cần được lưu lại để sử dụng khi verify OTP.
2. **Xử lý cooldown**: Hiển thị countdown timer dựa trên `resendAfterSeconds`.
3. **Validate số điện thoại**: Validate trước khi gửi request để tránh lỗi không cần thiết.
4. **OTP Type validation**:
   - `REGISTER`: Số điện thoại **chưa** được đăng ký
   - `FORGOT_PASSWORD`: Số điện thoại **đã** được đăng ký
   - `VERIFY_PHONE`: Dùng để xác thực số điện thoại hiện tại
   - `CHANGE_PHONE`: Dùng khi thay đổi số điện thoại

---

### 2. Xác thực OTP

**Endpoint:** `POST /api/v1/otp/verify`

**Mô tả:** Xác thực mã OTP người dùng nhập vào.

#### Request Body

```json
{
  "phoneNumber": "0912345678",
  "otp": "123456",
  "otpType": "REGISTER",
  "sessionInfo": "firebase_session_info_from_send"
}
```

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| `phoneNumber` | string | ✅ | Regex: `^(\+84|84|0)?[0-9]{9,10}$` | Số điện thoại |
| `otp` | string | ✅ | Đúng 6 ký tự | Mã OTP |
| `otpType` | string | ✅ | Enum values | Loại OTP |
| `sessionInfo` | string | ❌ | - | Session info từ bước gửi OTP |

#### Response

**Success (200 OK)**
```json
{
  "success": true,
  "message": "Xác thực OTP thành công",
  "verificationToken": "jwt_verification_token"
}
```

**Error - Bad Request (400)**
```json
{
  "success": false,
  "message": "Mã OTP không chính xác hoặc đã hết hạn"
}
```

**Error - Too Many Requests (429)**
```json
{
  "success": false,
  "message": "Bạn đã nhập sai OTP quá nhiều lần. Vui lòng yêu cầu mã mới"
}
```

#### Lưu ý cho FE

1. **Lưu `verificationToken`**: Token này cần để reset password hoặc hoàn tất đăng ký.
2. **Xử lý lỗi quá nhiều lần**: Sau nhiều lần nhập sai, cần yêu cầu OTP mới.
3. **Auto-submit**: Có thể tự động submit khi người dùng nhập đủ 6 số.

---

### 3. Đặt lại mật khẩu

**Endpoint:** `POST /api/v1/otp/reset-password`

**Mô tả:** Đặt lại mật khẩu sau khi xác thực OTP thành công.

#### Request Body

```json
{
  "phoneNumber": "0912345678",
  "verificationToken": "token_from_verify_step",
  "newPassword": "newpassword123",
  "role": "CUSTOMER"
}
```

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| `phoneNumber` | string | ✅ | Regex: `^(\+84|84|0)?[0-9]{9,10}$` | Số điện thoại |
| `verificationToken` | string | ✅ | - | Token từ bước verify OTP |
| `newPassword` | string | ✅ | 6-100 ký tự | Mật khẩu mới |
| `role` | string | ✅ | - | Vai trò của tài khoản: `CUSTOMER`, `EMPLOYEE` |

#### Response

**Success (200 OK)**
```json
{
  "success": true,
  "message": "Đặt lại mật khẩu thành công"
}
```

**Error - Unauthorized (401)**
```json
{
  "success": false,
  "message": "Token xác thực không hợp lệ hoặc đã hết hạn"
}
```

**Error - Not Found (404)**
```json
{
  "success": false,
  "message": "Không tìm thấy tài khoản với số điện thoại này"
}
```

#### Lưu ý cho FE

1. **Token có thời hạn**: Token từ bước verify chỉ có hiệu lực trong thời gian ngắn (thường 5-10 phút).
2. **Yêu cầu role**: Cần biết role của user để reset đúng tài khoản.
3. **Redirect sau thành công**: Chuyển hướng về trang đăng nhập sau khi reset thành công.

---

### 4. Kiểm tra trạng thái gửi lại OTP

**Endpoint:** `GET /api/v1/otp/resend-status`

**Mô tả:** Kiểm tra xem có thể gửi lại OTP chưa (cooldown check).

#### Query Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `phoneNumber` | string | ✅ | Số điện thoại |
| `otpType` | string | ✅ | Loại OTP |

#### Request Example

```
GET /api/v1/otp/resend-status?phoneNumber=0912345678&otpType=REGISTER
```

#### Response

**Success (200 OK) - Có thể gửi lại**
```json
{
  "success": true,
  "canResend": true,
  "resendAfterSeconds": 0
}
```

**Success (200 OK) - Đang cooldown**
```json
{
  "success": true,
  "canResend": false,
  "resendAfterSeconds": 45
}
```

#### Lưu ý cho FE

1. **Polling**: Có thể poll endpoint này mỗi giây để update countdown.
2. **Local timer**: Nên dùng local countdown timer và chỉ call API khi cần xác nhận.

---

### 5. Kiểm tra trạng thái dịch vụ OTP

**Endpoint:** `GET /api/v1/otp/status`

**Mô tả:** Kiểm tra trạng thái hoạt động của dịch vụ OTP.

#### Response

**Success (200 OK)**
```json
{
  "success": true,
  "enabled": true,
  "provider": "firebase"
}
```

#### Lưu ý cho FE

1. **Health check**: Dùng để kiểm tra service có hoạt động không trước khi show UI OTP.
2. **Fallback**: Nếu `enabled: false`, có thể hiển thị thông báo "Dịch vụ tạm thời không khả dụng".

---

## Flow hoàn chỉnh

### Flow Đăng ký (REGISTER)

```
┌─────────────────┐    ┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│  Nhập SĐT       │───▶│  Gửi OTP     │───▶│  Nhập OTP    │───▶│  Verify OTP  │
│  (REGISTER)     │    │  POST /send  │    │  6 chữ số    │    │  POST /verify│
└─────────────────┘    └──────────────┘    └──────────────┘    └──────────────┘
                              │                                        │
                              ▼                                        ▼
                       Lưu sessionInfo                         Lưu verificationToken
                                                                       │
                                                                       ▼
                                                               ┌──────────────┐
                                                               │  Hoàn tất    │
                                                               │  Đăng ký     │
                                                               └──────────────┘
```

### Flow Quên mật khẩu (FORGOT_PASSWORD)

```
┌─────────────────┐    ┌──────────────┐    ┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│  Nhập SĐT       │───▶│  Gửi OTP     │───▶│  Nhập OTP    │───▶│  Verify OTP  │───▶│  Reset Pass  │
│  (FORGOT_PASS)  │    │  POST /send  │    │  6 chữ số    │    │  POST /verify│    │  POST /reset │
└─────────────────┘    └──────────────┘    └──────────────┘    └──────────────┘    └──────────────┘
                              │                                        │
                              ▼                                        ▼
                       Lưu sessionInfo                         Lưu verificationToken
```

---

## Enum OtpType

| Value | Description | Business Logic |
|-------|-------------|----------------|
| `REGISTER` | Đăng ký tài khoản | Số điện thoại chưa tồn tại trong hệ thống |
| `FORGOT_PASSWORD` | Quên mật khẩu | Số điện thoại phải tồn tại trong hệ thống |
| `VERIFY_PHONE` | Xác thực số điện thoại | Không validate, dùng cho xác thực |
| `CHANGE_PHONE` | Thay đổi số điện thoại | Dùng khi user muốn đổi SĐT |

---

## Định dạng số điện thoại

Backend tự động chuẩn hóa số điện thoại. FE có thể gửi các format sau:

| Input | Normalized |
|-------|------------|
| `0912345678` | `+84912345678` |
| `84912345678` | `+84912345678` |
| `+84912345678` | `+84912345678` |
| `912345678` | `+84912345678` |

**Regex validation:** `^(\+84|84|0)?[0-9]{9,10}$`

---

## Error Codes

| HTTP Status | Meaning | Hành động FE |
|-------------|---------|--------------|
| 200 | Thành công | Xử lý data |
| 400 | Request không hợp lệ | Hiển thị message lỗi |
| 401 | Token không hợp lệ | Yêu cầu gửi lại OTP |
| 404 | Không tìm thấy tài khoản | Hiển thị message lỗi |
| 429 | Quá nhiều request | Hiển thị cooldown timer |
| 500 | Lỗi server | Hiển thị message lỗi chung |

---

## Lưu ý quan trọng cho Frontend

### 1. Security
- Không lưu `verificationToken` vào localStorage (dùng sessionStorage hoặc state).
- Token có thời hạn ngắn, cần xử lý case token hết hạn.

### 2. UX
- Hiển thị countdown timer khi đang cooldown.
- Auto-focus vào input OTP sau khi gửi thành công.
- Cho phép paste OTP từ clipboard.
- Mask số điện thoại khi hiển thị (ví dụ: `+84****678`).

### 3. Validation
- Validate số điện thoại trước khi call API.
- OTP chỉ chấp nhận 6 chữ số.
- Mật khẩu tối thiểu 6 ký tự.

### 4. Retry Logic
- Khi nhận 429, chờ theo `resendAfterSeconds`.
- Khi nhận 500, cho phép retry sau vài giây.

### 5. Testing
- Trong môi trường DEV, OTP sẽ được log ra console backend.
- Có thể dùng số điện thoại test được cấu hình trong Firebase Console.

---

## Ví dụ tích hợp (JavaScript/TypeScript)

```typescript
// Service class
class OtpService {
  private baseUrl = '/api/v1/otp';
  private sessionInfo: string | null = null;
  private verificationToken: string | null = null;

  async sendOtp(phoneNumber: string, otpType: string): Promise<SendOtpResponse> {
    const response = await fetch(`${this.baseUrl}/send`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ phoneNumber, otpType })
    });
    
    const data = await response.json();
    if (data.success) {
      this.sessionInfo = data.sessionInfo;
    }
    return data;
  }

  async verifyOtp(phoneNumber: string, otp: string, otpType: string): Promise<VerifyOtpResponse> {
    const response = await fetch(`${this.baseUrl}/verify`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ 
        phoneNumber, 
        otp, 
        otpType, 
        sessionInfo: this.sessionInfo 
      })
    });
    
    const data = await response.json();
    if (data.success) {
      this.verificationToken = data.verificationToken;
    }
    return data;
  }

  async resetPassword(phoneNumber: string, newPassword: string, role: string): Promise<ResetPasswordResponse> {
    const response = await fetch(`${this.baseUrl}/reset-password`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        phoneNumber,
        verificationToken: this.verificationToken,
        newPassword,
        role
      })
    });
    
    return response.json();
  }
}
```

---

## Changelog

| Version | Date | Changes |
|---------|------|---------|
| 1.0.0 | 2025-12-05 | Initial release với Firebase OTP |
