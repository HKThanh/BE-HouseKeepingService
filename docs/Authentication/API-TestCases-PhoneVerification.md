# API Test Cases - Phone Verification

## Overview
Tài liệu này mô tả các endpoint xác thực số điện thoại qua OTP (One-Time Password) sử dụng Firebase Authentication.

## Base URL
```
/api/v1/auth
```

---

## 1. Send OTP to Phone Number

### Endpoint
```
POST /api/v1/auth/phone/send-otp
```

### Description
Gửi mã OTP đến số điện thoại của người dùng thông qua Firebase Authentication.

### Request Headers
```
Content-Type: application/json
```

### Request Body
```json
{
  "phoneNumber": "string",
  "recaptchaToken": "string (optional)"
}
```

#### Field Descriptions
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| phoneNumber | string | Yes | Số điện thoại với mã quốc gia (e.g., +84987654321) |
| recaptchaToken | string | No | Token từ reCAPTCHA (nếu được cấu hình) |

### Response

#### Success Response (200 OK)
```json
{
  "success": true,
  "message": "OTP đã được gửi tới số điện thoại bạn cung cấp",
  "data": {
    "sessionInfo": "string",
    "expiresIn": 300
  }
}
```

#### Field Descriptions
| Field | Type | Description |
|-------|------|-------------|
| sessionInfo | string | Session identifier để sử dụng khi verify OTP |
| expiresIn | integer | Thời gian hết hạn của OTP (giây) |

#### Error Responses

**400 Bad Request - Missing Phone Number**
```json
{
  "success": false,
  "message": "Số điện thoại không được để trống"
}
```

**400 Bad Request - Invalid Phone Format**
```json
{
  "success": false,
  "message": "Số điện thoại không hợp lệ"
}
```

**503 Service Unavailable - Firebase Not Configured**
```json
{
  "success": false,
  "message": "Dịch vụ xác thực số điện thoại chưa được cấu hình"
}
```

**502 Bad Gateway - Firebase Error**
```json
{
  "success": false,
  "message": "Lỗi kết nối với dịch vụ Firebase"
}
```

### Test Cases

#### TC-PHONE-SEND-001: Valid Phone Number
**Input:**
```json
{
  "phoneNumber": "+84987654321"
}
```
**Expected Output:** 200 OK with sessionInfo

#### TC-PHONE-SEND-002: Missing Phone Number
**Input:**
```json
{
  "phoneNumber": ""
}
```
**Expected Output:** 400 Bad Request

#### TC-PHONE-SEND-003: Invalid Phone Format (No Country Code)
**Input:**
```json
{
  "phoneNumber": "0987654321"
}
```
**Expected Output:** 400 Bad Request

#### TC-PHONE-SEND-004: Invalid Phone Format (Wrong Country Code)
**Input:**
```json
{
  "phoneNumber": "+999987654321"
}
```
**Expected Output:** 400 Bad Request

#### TC-PHONE-SEND-005: Phone Number with Spaces
**Input:**
```json
{
  "phoneNumber": "+84 987 654 321"
}
```
**Expected Output:** Depends on implementation (may accept or reject)

---

## 2. Verify OTP

### Endpoint
```
POST /api/v1/auth/phone/verify-otp
```

### Description
Xác thực mã OTP đã được gửi đến số điện thoại người dùng.

### Request Headers
```
Content-Type: application/json
```

### Request Body
```json
{
  "sessionInfo": "string",
  "otpCode": "string"
}
```

#### Field Descriptions
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| sessionInfo | string | Yes | Session identifier nhận được từ endpoint send-otp |
| otpCode | string | Yes | Mã OTP 6 chữ số |

### Response

#### Success Response (200 OK)
```json
{
  "success": true,
  "message": "Xác thực OTP thành công",
  "data": {
    "firebaseUid": "string",
    "phoneNumber": "string",
    "accountId": "string (nullable)",
    "phoneVerified": true,
    "firebaseIdToken": "string",
    "firebaseRefreshToken": "string",
    "expiresIn": 3600,
    "newFirebaseUser": true
  }
}
```

#### Field Descriptions
| Field | Type | Description |
|-------|------|-------------|
| firebaseUid | string | Firebase User ID |
| phoneNumber | string | Số điện thoại đã được xác thực |
| accountId | string | Account ID trong hệ thống (null nếu chưa đăng ký) |
| phoneVerified | boolean | Trạng thái xác thực số điện thoại |
| firebaseIdToken | string | Firebase ID token để xác thực |
| firebaseRefreshToken | string | Firebase refresh token |
| expiresIn | integer | Thời gian hết hạn của token (giây) |
| newFirebaseUser | boolean | True nếu là người dùng mới trong Firebase |

#### Error Responses

**400 Bad Request - Missing Session Info**
```json
{
  "success": false,
  "message": "Session info không được để trống"
}
```

**400 Bad Request - Missing OTP Code**
```json
{
  "success": false,
  "message": "Mã OTP không được để trống"
}
```

**400 Bad Request - Invalid OTP**
```json
{
  "success": false,
  "message": "Mã OTP không chính xác"
}
```

**400 Bad Request - Expired Session**
```json
{
  "success": false,
  "message": "Session đã hết hạn. Vui lòng yêu cầu gửi lại OTP"
}
```

**502 Bad Gateway - Firebase Error**
```json
{
  "success": false,
  "message": "Lỗi xác thực với Firebase"
}
```

### Test Cases

#### TC-PHONE-VERIFY-001: Valid OTP
**Input:**
```json
{
  "sessionInfo": "valid-session-id-from-send-otp",
  "otpCode": "123456"
}
```
**Expected Output:** 200 OK with Firebase tokens

#### TC-PHONE-VERIFY-002: Missing Session Info
**Input:**
```json
{
  "sessionInfo": "",
  "otpCode": "123456"
}
```
**Expected Output:** 400 Bad Request

#### TC-PHONE-VERIFY-003: Missing OTP Code
**Input:**
```json
{
  "sessionInfo": "valid-session-id",
  "otpCode": ""
}
```
**Expected Output:** 400 Bad Request

#### TC-PHONE-VERIFY-004: Invalid OTP Code
**Input:**
```json
{
  "sessionInfo": "valid-session-id",
  "otpCode": "000000"
}
```
**Expected Output:** 400 Bad Request

#### TC-PHONE-VERIFY-005: Expired Session
**Input:**
```json
{
  "sessionInfo": "expired-session-id",
  "otpCode": "123456"
}
```
**Expected Output:** 400 Bad Request

#### TC-PHONE-VERIFY-006: OTP Code with Wrong Length
**Input:**
```json
{
  "sessionInfo": "valid-session-id",
  "otpCode": "12345"
}
```
**Expected Output:** 400 Bad Request

#### TC-PHONE-VERIFY-007: OTP Code with Letters
**Input:**
```json
{
  "sessionInfo": "valid-session-id",
  "otpCode": "12ABC6"
}
```
**Expected Output:** 400 Bad Request

---

## Integration Flow

### Complete Phone Verification Flow

```
1. User enters phone number
   ↓
2. App calls POST /phone/send-otp
   ↓
3. User receives OTP via SMS
   ↓
4. User enters OTP code
   ↓
5. App calls POST /phone/verify-otp with sessionInfo + otpCode
   ↓
6. System returns Firebase tokens and account info
   ↓
7. App can use Firebase tokens for authentication
```

### Use Cases

1. **New User Registration**
   - Phone not registered in system
   - `accountId` will be null
   - `newFirebaseUser` will be true
   - Can proceed to create account with verified phone

2. **Existing User Phone Verification**
   - Phone already registered in system
   - `accountId` will contain existing account ID
   - Can link Firebase authentication to existing account

3. **Phone Number Update**
   - User wants to change phone number
   - Verify new phone before updating account

---

## Security Considerations

1. **Rate Limiting**: Implement rate limiting on send-otp endpoint to prevent SMS spam
2. **Session Expiration**: OTP sessions should expire after 5-10 minutes
3. **OTP Attempts**: Limit number of verification attempts per session
4. **reCAPTCHA**: Consider implementing reCAPTCHA for send-otp endpoint
5. **Phone Number Validation**: Validate phone format before sending OTP
6. **Firebase Security**: Ensure Firebase project has proper security rules configured

---

## Error Handling Best Practices

1. Always check response status code
2. Handle network errors gracefully
3. Implement retry logic for transient failures
4. Show user-friendly error messages
5. Log errors for debugging purposes

---

## Notes

- OTP codes are typically 6 digits
- OTP validity is usually 5-10 minutes
- Phone numbers must include country code (+84 for Vietnam)
- Firebase Authentication must be properly configured
- Consider implementing SMS provider backup for reliability
- Test with real phone numbers in staging environment
- Use test phone numbers in development environment (if supported by Firebase)

---

## Related Endpoints

- `POST /api/v1/auth/register` - Register new account after phone verification
- `POST /api/v1/auth/login` - Login with verified phone-based account
- `POST /api/v1/auth/validate-token` - Validate Firebase tokens

---

## Changelog

| Date | Version | Changes |
|------|---------|---------|
| 2025-11-10 | 1.0 | Initial documentation for phone verification endpoints |
