**Email OTP Verification API**

- **Purpose**: Xác thực email người dùng (tùy chọn). Gửi mã OTP 6 chữ số qua email, OTP có thời hạn 3 phút, cooldown giữa các lần gửi là 60 giây.
- **Redis keys used**: `email_otp:{email}` (OTP, TTL 3 minutes), `email_otp_cooldown:{email}` (cooldown flag, TTL 60s), `email_otp_attempts:{email}` (attempts counter).
- **DB columns** (added in migration `19_add_email_verification.sql`): `customer.is_email_verified`, `employee.is_email_verified` (BOOLEAN DEFAULT false)

**How FE should use these endpoints**
- Flow: `POST /api/v1/otp/email/send` → user receives OTP via email → user submits OTP to `POST /api/v1/otp/email/verify` → on success, `is_email_verified` updated in DB.
- Rate limits & rules:
  - OTP length: 6 digits
  - OTP TTL: 180 seconds (3 minutes) — server returns `expirationSeconds` in send response
  - Resend cooldown: 60 seconds — server returns `cooldownSeconds` or `canResend` flag
  - Max verification attempts: 5 (after which the user must request a new OTP)

**Endpoints**

- **Send Email OTP**
  - **Method**: `POST`
  - **URL**: `/api/v1/otp/email/send`
  - **Auth**: Public (no auth required)
  - **Request JSON (body)**:

```json
{
  "email": "user@example.com",
  "otpType": "VERIFY_EMAIL"   // Optional text, server doesn't strictly require specific enum here for email flows
}
```

  - **Response - Success (200)**:

```json
{
  "success": true,
  "message": "Mã OTP đã được gửi đến email của bạn",
  "expirationSeconds": 180,
  "cooldownSeconds": 60
}
```

  - **Response - Too Many Requests (429)** (if in cooldown):

```json
{
  "success": false,
  "message": "Vui lòng chờ <n> giây trước khi gửi lại OTP",
  "resendAfterSeconds": <n>
}
```

  - **Response - Bad Request (400)** (validation):

```json
{
  "success": false,
  "message": "Email không hợp lệ"
}
```

  - **Notes for FE**:
    - Disable the resend button while `cooldownSeconds > 0` and show the countdown.
    - Show message that OTP expires in 3 minutes.

- **Verify Email OTP**
  - **Method**: `POST`
  - **URL**: `/api/v1/otp/email/verify`
  - **Auth**: Public
  - **Request JSON (body)**:

```json
{
  "email": "user@example.com",
  "otp": "123456"
}
```

  - **Response - Success (200)**:

```json
{
  "success": true,
  "message": "Email của bạn đã được xác thực thành công"
}
```

  - **Response - Bad Request (400)** (wrong code / validation / expired):

```json
{
  "success": false,
  "message": "OTP không chính xác (N lần thử còn lại)"
}
```

  - **Response - Too Many Attempts (429)** (if exceeded max attempts):

```json
{
  "success": false,
  "message": "Bạn đã nhập sai OTP quá nhiều lần, vui lòng yêu cầu OTP mới"
}
```

  - **Response - Not Found / Invalid (400)** (if email not found in system when confirming):

```json
{
  "success": false,
  "message": "Email không tồn tại trong hệ thống"
}
```

  - **Notes for FE**:
    - On success, you can fetch user profile to see `is_email_verified` flag, or rely on the success response.
    - If verification fails, show attempts remaining returned in message (server includes remaining count in message text).

- **Check Email OTP Resend Cooldown**
  - **Method**: `GET`
  - **URL**: `/api/v1/otp/email/resend-cooldown?email={email}`
  - **Auth**: Public
  - **Response - Success (200)**:

```json
{
  "success": true,
  "cooldownSeconds": 0,
  "canResend": true
}
```

  - **If still cooling down**:

```json
{
  "success": true,
  "cooldownSeconds": 42,
  "canResend": false
}
```

**Redis keys / TTL summary (for backend engineers / FE awareness)**
- `email_otp:{email}` → stores 6-digit OTP string. TTL = 3 minutes
- `email_otp_cooldown:{email}` → exists for 60 seconds after sending (used to prevent immediate resends)
- `email_otp_attempts:{email}` → stores integer attempts; TTL aligned with OTP TTL (resets on new send)

**Error codes & messages (important ones FE should handle)**
- 200: success
- 400: bad request / validation error (invalid email format, missing fields, OTP invalid/expired, email not found)
- 429: too many requests (cooldown active or too many failed verification attempts)
- 500: internal server error (mail service down, redis down, etc.)

**Frontend implementation tips**
- After `send` success, start a 180-second countdown for OTP expiry and a separate 60-second cooldown for resend. Disable resend button for cooldown period.
- If verify fails, show remaining attempts (server message includes remaining attempts in Vietnamese; parse/translate as needed) and optionally mask the email when showing messages (e.g., `u***@example.com`).
- Use small debounce on resend button; always check `/email/resend-cooldown` prior to sending to avoid unnecessary API errors.
- If the FE allows both phone and email verification, show them separately and clearly mark email verification as optional.

**Example FE flow (pseudo)**
1. User clicks "Send verification to email" → call `POST /api/v1/otp/email/send` with `{ email }`.
2. On 200: start cooldown (60s) and expiry (180s), show input for OTP.
3. User enters OTP → call `POST /api/v1/otp/email/verify` with `{ email, otp }`.
4. On success: show success toast and optionally re-fetch profile to display `is_email_verified`.

**Contact / Debugging tips for backend**
- If emails don't arrive, check `MailProperties` config and SMTP logs. The code uses `EmailService.sendEmail`.
- Redis must be reachable; OTP storage and cooldown rely on it.
- If users report immediate resend blocked beyond 60s, check Redis clock/drift or TTL retrieval logic.

---
