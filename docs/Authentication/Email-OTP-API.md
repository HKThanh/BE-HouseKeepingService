# Email OTP Verification & Password Reset API

**Email OTP Verification & Password Recovery API**

- **Purpose**: Xác thực email người dùng (tùy chọn) và lấy lại mật khẩu. Gửi mã OTP 6 chữ số qua email, OTP có thời hạn 3 phút, cooldown giữa các lần gửi là 60 giây.
- **Redis keys used**: 
  - **Email Verification**: `email_otp:{email}` (OTP, TTL 3 minutes), `email_otp_cooldown:{email}` (cooldown flag, TTL 60s), `email_otp_attempts:{email}` (attempts counter)
  - **Forgot Password**: `forgot_password_otp:{email}` (OTP, TTL 3 minutes), `forgot_password_cooldown:{email}` (cooldown flag, TTL 60s), `forgot_password_attempts:{email}` (attempts counter)
- **DB columns** (added in migration `19_add_email_verification.sql`): `customer.is_email_verified`, `employee.is_email_verified` (BOOLEAN DEFAULT false)

---

## Usage Flows

### Email Verification Flow (Optional)
`POST /api/v1/otp/email/send` → user receives OTP via email → `POST /api/v1/otp/email/verify` → `is_email_verified` updated in DB

### Password Recovery Flow
`POST /api/v1/otp/email/forgot-password` → user receives OTP via email → `POST /api/v1/otp/email/reset-password` → password updated in account

### Rate Limits & Rules
- OTP length: 6 digits
- OTP TTL: 180 seconds (3 minutes) — server returns `expirationSeconds` in send response
- Resend cooldown: 60 seconds — server returns `cooldownSeconds` flag
- Max verification attempts: 5 (after which the user must request a new OTP)

---

## Endpoints

### Email Verification Endpoints

**1. Send Email OTP**
- **Method**: `POST`
- **URL**: `/api/v1/otp/email/send`
- **Auth**: Public
- **Request**:

```json
{
  "email": "user@example.com",
  "otpType": "VERIFY_EMAIL"
}
```

- **Response (200)**: 

```json
{
  "success": true,
  "message": "Mã OTP đã được gửi đến email của bạn",
  "expirationSeconds": 180,
  "cooldownSeconds": 60
}
```

- **Response (400)**: 

```json
{
  "success": false,
  "message": "Email không hợp lệ"
}
```

- **Response (429)**: 

```json
{
  "success": false,
  "message": "Vui lòng chờ 45 giây trước khi gửi lại OTP"
}
```

---

**2. Verify Email OTP**
- **Method**: `POST`
- **URL**: `/api/v1/otp/email/verify`
- **Auth**: Public
- **Request**:

```json
{
  "email": "user@example.com",
  "otp": "123456"
}
```

- **Response (200)**:

```json
{
  "success": true,
  "message": "Email của bạn đã được xác thực thành công"
}
```

- **Response (400)**:

```json
{
  "success": false,
  "message": "OTP không chính xác (4 lần thử còn lại)"
}
```

- **Response (429)**:

```json
{
  "success": false,
  "message": "Bạn đã nhập sai OTP quá nhiều lần, vui lòng yêu cầu OTP mới"
}
```

---

**3. Check Resend Cooldown**
- **Method**: `GET`
- **URL**: `/api/v1/otp/email/resend-cooldown?email={email}`
- **Auth**: Public
- **Response (200)**:

```json
{
  "success": true,
  "cooldownSeconds": 0,
  "canResend": true
}
```

---

### Password Recovery Endpoints

**4. Send Forgot Password OTP**
- **Method**: `POST`
- **URL**: `/api/v1/otp/email/forgot-password-request`
- **Auth**: Public
- **Request**:

```json
{
  "email": "user@example.com"
}
```

- **Response (200)**:

```json
{
  "success": true,
  "message": "Mã OTP đã được gửi đến email của bạn",
  "expirationSeconds": 180,
  "cooldownSeconds": 60
}
```

- **Response (400)**:

```json
{
  "success": false,
  "message": "Email không tồn tại trong hệ thống"
}
```

---

**5. Reset Password with Email OTP**
- **Method**: `POST`
- **URL**: `/api/v1/otp/email/reset-password`
- **Auth**: Public
- **Request**:

```json
{
  "email": "user@example.com",
  "otp": "123456",
  "newPassword": "NewSecurePassword123"
}
```

- **Response (200)**:

```json
{
  "success": true,
  "message": "Mật khẩu của bạn đã được đặt lại thành công"
}
```

- **Response (400)**:

```json
{
  "success": false,
  "message": "OTP không chính xác (3 lần thử còn lại)"
}
```

- **Response (400)** (email not found):

```json
{
  "success": false,
  "message": "Không tìm thấy tài khoản liên kết với email này"
}
```

---

## Database Reference

- `customer.email` (varchar, unique)
- `customer.is_email_verified` (boolean, default false)
- `employee.email` (varchar, unique)
- `employee.is_email_verified` (boolean, default false)

---

## Frontend Implementation Checklist

- [ ] On send OTP: start 180s countdown + 60s cooldown timer
- [ ] Disable resend button while `cooldownSeconds > 0`
- [ ] On OTP verify failure: show remaining attempts from message
- [ ] On max attempts: show "request new OTP" option
- [ ] For password reset: combine OTP + password input in single form
- [ ] After password reset success: redirect to login page after 2-3 seconds
- [ ] Show error messages in user-friendly Vietnamese text
- [ ] Handle 429 errors gracefully (inform user to wait and retry)

---

## Security Notes

- OTP generated using `SecureRandom`
- Passwords encoded with BCrypt
- Cooldown and attempt limits prevent brute force
- Separate Redis keys for password recovery isolate from email verification flow

---