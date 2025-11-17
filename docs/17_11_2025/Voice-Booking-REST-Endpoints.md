# VoiceBookingController – REST API (17/11/2025)

## 1. Thông tin chung
- Base path: `/api/v1/customer/bookings/voice`
- Các endpoint (trừ `/status`) bắt buộc header `Authorization: Bearer <JWT>` với quyền `ROLE_CUSTOMER` hoặc `ROLE_ADMIN`.
- Tất cả phản hồi REST dùng JSON UTF-8.

### 1.1 Cấu trúc `VoiceBookingResponse`
| Trường | Kiểu | Bắt buộc | Mô tả |
| --- | --- | --- | --- |
| `success` | boolean | ✔ | true nếu thao tác hợp lệ |
| `message` | string | ✔ | Thông điệp hiển thị |
| `requestId` | string | ✔ | UUID của voice booking request |
| `status` | string | ✔ | `PROCESSING`, `AWAITING_CONFIRMATION`, `COMPLETED`, `PARTIAL`, `FAILED`, `CANCELLED` |
| `transcript` | string | ✖ | Bản phiên âm audio |
| `confidenceScore` | number | ✖ | Độ tin cậy 0-1 |
| `processingTimeMs` | integer | ✖ | Miligiây xử lý |
| `bookingId` | string | ✖ | Mã đơn đã tạo |
| `preview` | object | ✖ | `VoiceBookingPreview` cho nháp |
| `missingFields` | array\<string\> | ✖ | Các field còn thiếu |
| `clarificationMessage` | string | ✖ | Nội dung yêu cầu bổ sung |
| `extractedInfo` | object | ✖ | Dữ liệu hệ thống đã bóc tách |
| `errorDetails` | string | ✖ | Chi tiết lỗi nội bộ |

`VoiceBookingPreview` gồm các trường:
- Thông tin địa chỉ (`addressId`, `fullAddress`, `ward`, `city`)
- Lịch hẹn (`bookingTime`, `note`, `promoCode`, `paymentMethodId`)
- Giá trị (`totalAmount`, `formattedTotalAmount`)
- Danh sách dịch vụ `services[]` (mỗi phần tử: `serviceId`, `serviceName`, `quantity`, `pricePerUnit`, `formattedPricePerUnit`, `subTotal`, `formattedSubTotal`, `selectedChoiceIds[]`)
- Danh sách nhân viên `employees[]` (mỗi phần tử: `employeeId`, `fullName`, `avatar`, `rating`, `hasWorkedWithCustomer`, `serviceIds[]`, `autoAssigned`)
- Cờ `autoAssignedEmployees`

---

## 2. POST `/api/v1/customer/bookings/voice`
Tạo yêu cầu đặt lịch bằng giọng nói.

### 2.1 Input
- **Headers**
  - `Authorization: Bearer <JWT>`
  - `Content-Type: multipart/form-data`
- **Body parts**

| Field | Kiểu | Bắt buộc | Mô tả |
| --- | --- | --- | --- |
| `audio` | File | ✔ | File âm thanh ≤ 5 MB, MIME `audio/*` hoặc `application/octet-stream` |
| `hints` | String (JSON) | ✖ | Chuỗi JSON ≤ 1000 ký tự (VD: `{"addressId":"addr-1","bookingDate":"2025-11-20"}`) cung cấp ngữ cảnh |

### 2.2 Response

| HTTP | Payload | Diễn giải |
| --- | --- | --- |
| 200 | `VoiceBookingResponse` (`status=COMPLETED`, có `bookingId`) | Đã dựng booking thành công |
| 202 | `VoiceBookingResponse` (`status=AWAITING_CONFIRMATION`, có `preview`) | Cần người dùng xác nhận nháp |
| 206 | `VoiceBookingResponse` (`status=PARTIAL`, có `missingFields`) | Thiếu thông tin, cần bổ sung |
| 400 | `{ "success": false, "message": "Invalid audio file: ..."} ` | Lỗi validation audio/hints |
| 401 | `{ "success": false, "message": "Invalid or expired token" }` | Sai JWT |
| 503 | `{ "success": false, "message": "Voice booking service is currently unavailable" }` | Dịch vụ tạm ngưng |
| 500 | `{ "success": false, "message": "Internal server error: ..." }` | Lỗi hệ thống |

**Ví dụ phản hồi `AWAITING_CONFIRMATION`**
```json
{
  "success": true,
  "message": "Đã dựng đơn nháp, vui lòng xác nhận để hoàn tất đặt lịch",
  "requestId": "c867282d-650b-4b39-bb1c-9a893bfaab39",
  "status": "AWAITING_CONFIRMATION",
  "transcript": "Tôi muốn đặt dịch vụ vệ sinh căn hộ lúc 14h chiều mai...",
  "confidenceScore": 0.92,
  "processingTimeMs": 6430,
  "preview": {
    "addressId": "addr-01",
    "fullAddress": "12 Nguyễn Văn Bảo, Gò Vấp, TP.HCM",
    "bookingTime": "2025-11-18T14:00:00",
    "note": "Mang thêm máy hút bụi",
    "promoCode": null,
    "paymentMethodId": 2,
    "totalAmount": 450000,
    "formattedTotalAmount": "450.000đ",
    "services": [
      {
        "serviceId": 101,
        "serviceName": "Vệ sinh căn hộ 2 phòng ngủ",
        "quantity": 1,
        "pricePerUnit": 450000,
        "formattedPricePerUnit": "450.000đ",
        "subTotal": 450000,
        "formattedSubTotal": "450.000đ",
        "selectedChoiceIds": [3, 5]
      }
    ],
    "employees": [],
    "autoAssignedEmployees": true
  }
}
```

---

## 3. GET `/api/v1/customer/bookings/voice/{requestId}`
Truy vấn trạng thái chi tiết.

### 3.1 Input
- Headers: `Authorization: Bearer <JWT>`
- Path: `requestId` (UUID thuộc chính user)

### 3.2 Response
| HTTP | Payload | Diễn giải |
| --- | --- | --- |
| 200 | `{ "success": true, "data": { ... } }` | Chi tiết yêu cầu |
| 401 | `{ "success": false, "message": "Invalid or expired token" }` | Sai JWT |
| 404 | `{ "success": false, "message": "<lý do requestId không tồn tại>" }` | Không tìm thấy hoặc không thuộc người dùng |
| 500 | `{ "success": false, "message": "Internal server error: ..." }` | Lỗi hệ thống |

`data` gồm: `id`, `status`, `transcript`, `confidenceScore`, `processingTimeMs`, `bookingId`, `missingFields`, `errorMessage`, `createdAt`.

**Ví dụ**
```json
{
  "success": true,
  "data": {
    "id": "c867282d-650b-4b39-bb1c-9a893bfaab39",
    "status": "PARTIAL",
    "transcript": "Đặt giúp tôi lễ tân 2 tiếng sáng mai...",
    "confidenceScore": 0.64,
    "processingTimeMs": 5170,
    "bookingId": null,
    "missingFields": ["bookingTime", "serviceId"],
    "errorMessage": null,
    "createdAt": "2025-11-17T09:12:03.214Z"
  }
}
```

---

## 4. POST `/api/v1/customer/bookings/voice/continue`
Gửi bổ sung dữ liệu cho request ở trạng thái `PARTIAL`.

### 4.1 Input
- **Headers**: `Authorization: Bearer <JWT>`
- **Content-Type**: `multipart/form-data`
- **Body parts**

| Field | Kiểu | Bắt buộc | Mô tả |
| --- | --- | --- | --- |
| `requestId` | String | ✔ | ID của voice booking cần tiếp tục |
| `audio` | File | ✖ | File audio bổ sung |
| `additionalText` | String | ✖ | Text mô tả thông tin còn thiếu |
| `explicitFields` | String (JSON) | ✖ | Map JSON các cặp key/value (VD: `{"bookingTime":"2025-11-20T08:00:00","addressId":"addr-02"}`) |

**Điều kiện**: ít nhất một trong các trường `audio`, `additionalText`, `explicitFields` phải được gửi.

### 4.2 Response
| HTTP | Payload | Diễn giải |
| --- | --- | --- |
| 200 | `VoiceBookingResponse` (`success=true`) | Đã đủ dữ liệu, có thể hoàn tất |
| 202 | `VoiceBookingResponse` (`status=AWAITING_CONFIRMATION`) | Đã dựng lại nháp |
| 206 | `VoiceBookingResponse` (`status=PARTIAL`) | Vẫn thiếu thông tin |
| 400 | `{ "success": false, "message": "At least one of audio..." }` hoặc lỗi parse JSON | Request không hợp lệ |
| 401 | `{ "success": false, "message": "Invalid or expired token" }` | Sai JWT |
| 500 | `{ "success": false, "message": "Internal server error: ..." }` | Lỗi hệ thống |

**Ví dụ request multipart (pseudo cURL)**
```
curl -X POST https://api.example.com/api/v1/customer/bookings/voice/continue \
  -H "Authorization: Bearer <JWT>" \
  -F "requestId=c867282d-650b-4b39-bb1c-9a893bfaab39" \
  -F "additionalText=Thời gian mong muốn: 8h ngày 20/11" \
  -F "explicitFields={\"serviceId\":105}"
```

**Ví dụ response `PARTIAL`**
```json
{
  "success": false,
  "message": "Could not extract all required information from voice input",
  "requestId": "c867282d-650b-4b39-bb1c-9a893bfaab39",
  "status": "PARTIAL",
  "transcript": "Thời gian mong muốn 8h sáng 20/11",
  "missingFields": ["addressId"],
  "clarificationMessage": "Vui lòng cung cấp địa chỉ chính xác để tiếp tục",
  "extractedInfo": {
    "bookingTime": "2025-11-20T08:00:00",
    "serviceId": 105
  },
  "confidenceScore": 0.71,
  "processingTimeMs": 2100
}
```

---

## 5. POST `/api/v1/customer/bookings/voice/confirm`
Xác nhận nháp để tạo booking chính thức.

### 5.1 Input
- Headers: `Authorization: Bearer <JWT>`
- Body JSON:

| Field | Kiểu | Bắt buộc | Mô tả |
| --- | --- | --- | --- |
| `requestId` | string | ✔ | ID của nháp cần xác nhận |

**Ví dụ**
```json
{ "requestId": "c867282d-650b-4b39-bb1c-9a893bfaab39" }
```

### 5.2 Response
| HTTP | Payload | Diễn giải |
| --- | --- | --- |
| 200 | `VoiceBookingResponse` (`status=COMPLETED`, `bookingId` != null) | Đặt lịch thành công |
| 400 | `{ "success": false, "message": "<validation/state error>" }` | RequestId sai trạng thái |
| 401 | `{ "success": false, "message": "Invalid or expired token" }` | Sai JWT |
| 500 | `{ "success": false, "message": "Internal server error: ..." }` | Lỗi hệ thống |

**Ví dụ phản hồi**
```json
{
  "success": true,
  "message": "Booking created successfully from voice input",
  "requestId": "c867282d-650b-4b39-bb1c-9a893bfaab39",
  "status": "COMPLETED",
  "bookingId": "BK20251117001",
  "transcript": "Đặt giúp tôi dịch vụ vệ sinh 3 phòng ngủ...",
  "confidenceScore": 0.94,
  "processingTimeMs": 8120
}
```

---

## 6. POST `/api/v1/customer/bookings/voice/cancel`
Hủy nháp voice booking.

### 6.1 Input
- Headers: `Authorization: Bearer <JWT>`
- Body JSON: giống request confirm (`requestId` bắt buộc).

### 6.2 Response
| HTTP | Payload | Diễn giải |
| --- | --- | --- |
| 200 | `VoiceBookingResponse` (`status=CANCELLED`) | Đã hủy nháp |
| 400 | `{ "success": false, "message": "<validation/state error>" }` | Không thể hủy |
| 401 | `{ "success": false, "message": "Invalid or expired token" }` | Sai JWT |
| 500 | `{ "success": false, "message": "Internal server error: ..." }` | Lỗi hệ thống |

**Ví dụ phản hồi**
```json
{
  "success": true,
  "message": "Đã huỷ yêu cầu đặt lịch bằng giọng nói",
  "requestId": "c867282d-650b-4b39-bb1c-9a893bfaab39",
  "status": "CANCELLED"
}
```

---

## 7. GET `/api/v1/customer/bookings/voice/status`
Kiểm tra khả dụng dịch vụ.

### 7.1 Input
- Không cần Authorization.

### 7.2 Response
| HTTP | Payload |
| --- | --- |
| 200 | `{ "success": true, "voiceBookingEnabled": true, "message": "Voice booking service is available" }` |

Khi dịch vụ tắt, `voiceBookingEnabled=false` và `message="Voice booking service is currently unavailable"`.
