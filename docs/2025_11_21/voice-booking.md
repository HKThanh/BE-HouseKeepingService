# Voice Booking API & Realtime Events (21/11/2025)

Tài liệu tóm tắt các endpoint REST và WebSocket event dành cho luồng đặt lịch/dịch vụ bằng giọng nói. Không kèm hướng dẫn code mẫu hay thông tin DB, chỉ tập trung vào contract để FE tích hợp.

## 1) REST Endpoints (customer)

Tất cả endpoint đều yêu cầu `Authorization: Bearer <jwt>` và prefix `/api/v1/customer/bookings/voice`.

### POST `/` — Tạo yêu cầu mới từ audio
- **Body**: multipart/form-data
  - `audio` (file, **required**): audio cần nhận diện.
  - `hints` (string JSON, **optional**): gợi ý thêm cho parser (vd: `"city":"HCM"`).
- **Response body (VoiceBookingResponse)**:
  - `success` (bool), `message` (string), `status` one of `PROCESSING|PARTIAL|AWAITING_CONFIRMATION|COMPLETED|FAILED`.
  - `requestId` (string).
  - `transcript` (string, **có thể rỗng** nếu không có intent).
  - `confidenceScore` (number|null, **optional**), `processingTimeMs` (int|null, **optional**).
  - `missingFields` (array<string>, **optional**) khi `PARTIAL`.
  - `clarificationMessage` (string, **optional**) khi cần bổ sung.
  - `preview` (object, **present khi** `AWAITING_CONFIRMATION`) gồm:
    - `address`, `ward`, `city` (string|nullable),
    - `bookingTime` (ISO datetime|nullable),
    - `note` (string|nullable), `promoCode` (string|nullable), `paymentMethodId` (number|nullable),
    - `totalAmount` (number), `totalAmountFormatted` (string),
    - `services` (array): mỗi item gồm `serviceId` (int), `serviceName` (string|nullable), `quantity` (number), `unitPrice` (number), `unitPriceFormatted` (string), `subtotal` (number), `subtotalFormatted` (string), `selectedChoiceIds` (array<int>|nullable),
    - `employees` (array, **optional**): `employeeId` (string), `fullName` (string), `avatarUrl` (string|nullable), `phone` (string|nullable),
    - `autoAssignedEmployees` (bool).
  - `bookingId` (string, **present khi** `COMPLETED`).
  - `extractedInfo` (map, **optional**) chứa thông tin parser hiểu được/thiếu sót.
  - `speech` (object, **optional**) chứa URL audio TTS:
    - `message` (optional): `text`, `audioUrl`, `provider`, `processingTimeMs`
    - `clarification` (optional): cùng cấu trúc.
  - `errorDetails` (string|nullable), `failureHints` (array<string>|nullable), `retryAfterMs` (int|nullable), `isFinal` (bool).

### POST `/continue` — Bổ sung thông tin cho request PARTIAL / AWAITING_CONFIRMATION
- **Body**: multipart/form-data
  - `requestId` (string, **required**).
  - Một trong (ít nhất 1): `audio` (file, optional), `additionalText` (string, optional), `explicitFields` (string JSON — optional: map các field cụ thể đã điền).
- **Response**: `VoiceBookingResponse` (như trên). `transcript` sẽ phản ánh transcript sau khi gộp thêm thông tin hợp lệ.

### POST `/confirm` — Xác nhận draft
- **Body**: JSON `{ "requestId": "<id>" }`
- **Response**: `VoiceBookingResponse` với `status=COMPLETED`, `bookingId`.

### POST `/cancel` — Hủy draft/partial
- **Body**: JSON `{ "requestId": "<id>" }`
- **Response**: `VoiceBookingResponse` với `status=CANCELLED`.

### GET `/{requestId}` — Lấy chi tiết voice booking
- **Response**: entity VoiceBookingRequest (đầy đủ transcript, status, hints, preview, timestamps…; dùng để hiển thị lại). Một số trường có thể null/optional: `bookingId`, `previewPayload`, `draftBookingRequest`, `missingFields`, `confidenceScore`, `processingTimeMs`, `hints`.

### GET `/status` — Kiểm tra dịch vụ voice booking
- **Response**: `{ "enabled": boolean, ... }` (metadata tùy cấu hình).

## 2) WebSocket (STOMP over SockJS)

- Kết nối tới endpoint cấu hình, ví dụ `http://<host>/ws/voice-booking`.
- Subcribe topic theo request: `/topic/voice-booking/{requestId}`.
- Mỗi event gửi payload `VoiceBookingEventPayload`:
  - `requestId` (string), `status` (PROCESSING|PARTIAL|AWAITING_CONFIRMATION|COMPLETED|FAILED|CANCELLED).
  - `event` (RECEIVED|TRANSCRIBING|PARTIAL|AWAITING_CONFIRMATION|COMPLETED|FAILED|CANCELLED).
  - `transcript` (string|null), `processingTimeMs` (int|null), `confidenceScore` (number|null).
  - `missingFields` (array<string>|null), `clarificationMessage` (string|null).
  - `preview` (object|null) như phần REST.
  - `bookingId` (string|null) khi COMPLETED.
  - `message` (string), `speech` (object TTS), `hints` (array<string>|null).
  - `aiPrompt` (string|null) nếu có.
  - `errorDetails` (string|null).

## 3) Trạng thái & hành vi chính

- `PROCESSING`: đang nhận audio, chuyển STT và parse.
- `PARTIAL`: thiếu thông tin; FE hiển thị `clarificationMessage`, `missingFields`, `extractedInfo`, phát `speech` nếu có; dùng `/continue` để bổ sung.
- `AWAITING_CONFIRMATION`: đã dựng preview; FE cho phép `/confirm` hoặc `/cancel`.
- `COMPLETED`: booking đã tạo thành công; hiển thị `bookingId`.
- `FAILED`: lỗi; xem `errorDetails`, `failureHints`.
- `CANCELLED`: người dùng hủy draft/partial.

## 4) Lưu ý frontend

- Transcript có thể rỗng nếu audio không chứa intent đặt lịch; không nên chặn UI.
- Luôn đọc `isFinal` để biết luồng có kết thúc chưa.
- Khi `speech` có dữ liệu, ưu tiên phát `speech.message.audioUrl`; `clarification` là audio phụ.
- Với `missingFields`, FE nên hiển thị danh sách và cho phép người dùng nhập nhanh (text) hoặc ghi âm.
- `explicitFields` trong `/continue` cho phép FE gửi map các field đã biết (vd: `{ "address": "...", "bookingTime": "..." }`) để parser bỏ qua việc tự trích xuất.

## 5) Ví dụ request/response (JSON)

> Các ví dụ mang tính minh họa; có thể thay id/payload theo dữ liệu thực tế (service names: “Dọn dẹp theo giờ”, “Tổng vệ sinh”, ...).

### 5.1 Tạo mới (POST `/`)
Multipart (form-data): `audio=<file.wav>`, `hints={"city":"HCM","preferredService":"don_dep"}`  
**Response (PARTIAL)**:
```json
{
  "success": false,
  "message": "Chào bạn! ...",
  "requestId": "186f88e9-889a-4d0e-9a08-2da38048c02b",
  "status": "PARTIAL",
  "isFinal": false,
  "transcript": "",
  "processingTimeMs": 2573,
  "missingFields": ["service"],
  "clarificationMessage": "Bạn vui lòng cung cấp thêm thông tin chi tiết...",
  "extractedInfo": {
    "availableServices": "• Dọn dẹp theo giờ\n• Tổng vệ sinh\n• Vệ sinh Sofa - Nệm - Rèm\n• Vệ sinh máy lạnh\n• Giặt sấy theo kg\n• Giặt hấp cao cấp\n• Nấu ăn gia đình\n• Đi chợ hộ",
    "aiPrompt": "Chào bạn! ... (gợi ý dịch vụ)"
  },
  "speech": {
    "message": {
      "text": "Chào bạn!...",
      "audioUrl": "https://.../tts/message.mp3",
      "provider": "fpt-ai",
      "processingTimeMs": 4200
    },
    "clarification": {
      "text": "Bạn vui lòng cung cấp thêm thông tin chi tiết...",
      "audioUrl": "https://.../tts/clarify.mp3",
      "provider": "fpt-ai",
      "processingTimeMs": 4800
    }
  }
}
```

### 5.2 Bổ sung (POST `/continue`)
Multipart (form-data):
- `requestId=186f88e9-889a-4d0e-9a08-2da38048c02b`
- `additionalText=Tôi muốn đặt dịch vụ dọn dẹp theo giờ vào 3h chiều mai tại 123 ABC, Quận 1`
- (optional) `explicitFields={"address":"123 ABC, Quận 1","bookingTime":"2025-11-22T15:00:00"}`

**Response (AWAITING_CONFIRMATION)**:
```json
{
  "success": true,
  "message": "Đã dựng đơn nháp, vui lòng xác nhận để hoàn tất đặt lịch",
  "requestId": "186f88e9-889a-4d0e-9a08-2da38048c02b",
  "status": "AWAITING_CONFIRMATION",
  "isFinal": true,
  "transcript": "Tôi muốn đặt dịch vụ dọn dẹp theo giờ vào 3h chiều mai tại 123 ABC, Quận 1",
  "confidenceScore": 0.82,
  "processingTimeMs": 2850,
  "preview": {
    "addressId": 1001,
    "address": "123 ABC, Phường Bến Nghé, Quận 1, TP.HCM",
    "ward": "Bến Nghé",
    "city": "TP.HCM",
    "bookingTime": "2025-11-22T15:00:00",
    "note": "Ghi chú thêm...",
    "promoCode": null,
    "paymentMethodId": 2,
    "totalAmount": 450000,
    "totalAmountFormatted": "450.000đ",
    "services": [
      {
        "serviceId": 1,
        "serviceName": "Dọn dẹp theo giờ",
        "quantity": 2,
        "unitPrice": 200000,
        "unitPriceFormatted": "200.000đ",
        "subtotal": 400000,
        "subtotalFormatted": "400.000đ",
        "selectedChoiceIds": [11, 15]
      }
    ],
    "employees": [
      { "employeeId": "e01", "fullName": "Nguyễn A", "avatarUrl": "https://.../a.jpg", "phone": "0901xxx" }
    ],
    "autoAssignedEmployees": true
  },
  "speech": {
    "message": { "text": "Đã dựng đơn nháp...", "audioUrl": "https://.../tts/preview.mp3" }
  }
}
```

### 5.3 Xác nhận (POST `/confirm`)
Body: `{"requestId":"186f88e9-889a-4d0e-9a08-2da38048c02b"}`  
**Response (COMPLETED)**:
```json
{
  "success": true,
  "message": "Booking created successfully from voice input",
  "requestId": "186f88e9-889a-4d0e-9a08-2da38048c02b",
  "status": "COMPLETED",
  "isFinal": true,
  "bookingId": "BKG-20251122-0001",
  "transcript": "Tôi muốn đặt dịch vụ dọn dẹp theo giờ vào 3h chiều mai tại 123 ABC, Quận 1",
  "processingTimeMs": 3100
}
```

### 5.4 WebSocket event (PARTIAL)
Subscribe `/topic/voice-booking/186f88e9-889a-4d0e-9a08-2da38048c02b`  
Payload:
```json
{
  "event": "PARTIAL",
  "status": "PARTIAL",
  "requestId": "186f88e9-889a-4d0e-9a08-2da38048c02b",
  "transcript": "",
  "missingFields": ["service"],
  "clarificationMessage": "Bạn vui lòng cung cấp thêm thông tin chi tiết...",
  "message": "Chào bạn!...",
  "processingTimeMs": 2573,
  "speech": {
    "message": { "audioUrl": "https://.../tts/msg.mp3" },
    "clarification": { "audioUrl": "https://.../tts/clarify.mp3" }
  }
}
```

### 5.5 WebSocket event (AWAITING_CONFIRMATION)
```json
{
  "event": "AWAITING_CONFIRMATION",
  "status": "AWAITING_CONFIRMATION",
  "requestId": "186f88e9-889a-4d0e-9a08-2da38048c02b",
  "transcript": "Tôi muốn đặt dịch vụ dọn dẹp theo giờ vào 3h chiều mai tại 123 ABC, Quận 1",
  "preview": { "...": "xem preview ở mục 5.2" },
  "message": "Đã dựng đơn nháp, vui lòng xác nhận để hoàn tất đặt lịch"
}
```
