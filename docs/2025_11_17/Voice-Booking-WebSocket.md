# WebSocket đặt lịch bằng giọng nói (17/11/2025)

## 1. Endpoint & Transport
- URL chính: `wss://<host>/ws/voice-booking` (có SockJS fallback: `https://<host>/ws/voice-booking/info`)
- Giao thức: STOMP over WebSocket
- Heartbeat: 10s (broker `/topic` – server/client), SockJS heartbeat 25s
- Broker prefix: `/topic` (broadcast), `/queue` (hàng đợi), `/user/queue` cho kênh riêng
- Application prefix: `/app` (client gửi message nếu có action trong tương lai)

## 2. Handshake HTTP
| Thành phần | Giá trị |
| --- | --- |
| Header bắt buộc | `Authorization: Bearer <JWT>` |
| JWT hợp lệ | Thuộc người dùng `ROLE_CUSTOMER`; role khác bị trả `403` |
| Thuộc tính session ghi nhận | `voiceBookingPrincipal`, `voiceBookingUser`, `voiceBookingRole`, `voiceBookingSession=true` |
| Lỗi thường gặp | Thiếu header → `401`, token hết hạn → `401`, role sai → `403` |

**Ví dụ request bắt tay**
```
GET /ws/voice-booking HTTP/1.1
Host: api.example.com
Upgrade: websocket
Connection: Upgrade
Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==
Sec-WebSocket-Version: 13
Authorization: Bearer <JWT>
Origin: https://app.example.com
```

## 3. STOMP CONNECT
| Thành phần | Chi tiết |
| --- | --- |
| Native header | `Authorization: Bearer <JWT>` (cả `authorization` viết thường đều được) phòng trường hợp HTTP header không tự chuyển |
| Kiểm tra | Token sẽ được xác thực lại; server gán `Principal` mới (`VoiceBookingPrincipal`) cho session |
| Lỗi | Thiếu header + không có principal từ handshake → `MessagingException: Authorization header is required...` |

**Frame CONNECT ví dụ**
```
CONNECT
accept-version:1.2
heart-beat:10000,10000
Authorization:Bearer <JWT>

^@
```

## 4. SUBSCRIBE Channels
| Destination | Dữ liệu path | Quy tắc truy cập |
| --- | --- | --- |
| `/topic/voice-booking/{requestId}` | `requestId` = ID voice booking | Chỉ được subscribe nếu request thuộc về user hiện tại (`existsByIdAndCustomer_Account_Username`). Nếu vi phạm: server reject, đồng thời gửi payload lỗi tới `/user/queue/voice-booking/errors`. |
| `/user/queue/voice-booking/errors` | Không có | Nhận lỗi kết nối/ủy quyền; mọi user hợp lệ đều subscribe được. |

**Frame SUBSCRIBE ví dụ**
```
SUBSCRIBE
id:voice-topic-1
destination:/topic/voice-booking/c867282d-650b-4b39-bb1c-9a893bfaab39
ack:auto

^@
```

**Lắng nghe lỗi cá nhân**
```
SUBSCRIBE
id:voice-errors
destination:/user/queue/voice-booking/errors
ack:auto

^@
```

## 5. Event Payload (`VoiceBookingEventPayload`)

| Trường | Kiểu | Bắt buộc | Mô tả |
| --- | --- | --- | --- |
| `eventType` | enum | ✔ | `RECEIVED`, `TRANSCRIBING`, `AWAITING_CONFIRMATION`, `PARTIAL`, `CANCELLED`, `COMPLETED`, `FAILED` |
| `requestId` | string | ✔ | ID của voice booking |
| `status` | string | ✔ | `PROCESSING`, `AWAITING_CONFIRMATION`, `PARTIAL`, `COMPLETED`, `CANCELLED`, `FAILED` |
| `transcript` | string | ✖ | Phiên âm gần nhất |
| `missingFields` | array\<string\> | ✖ | Danh sách field thiếu (chỉ `PARTIAL`) |
| `clarificationMessage` | string | ✖ | Hướng dẫn bổ sung |
| `bookingId` | string | ✖ | Có khi `COMPLETED` |
| `processingTimeMs` | integer | ✖ | Miligiây xử lý tại thời điểm phát sự kiện |
| `errorMessage` | string | ✖ | Lỗi đọc được (chỉ `FAILED`) |
| `preview` | object | ✖ | `VoiceBookingPreview` cho sự kiện `AWAITING_CONFIRMATION` |
| `timestamp` | string (ISO-8601) | ✔ | `Instant.now()` tại server |
| `progress` | double | ✖ | 0–1, hiện dùng cho `TRANSCRIBING` |

`VoiceBookingPreview` chi tiết:

| Trường | Kiểu | Mô tả |
| --- | --- | --- |
| `addressId` / `fullAddress` / `ward` / `city` | string | Thông tin địa điểm |
| `bookingTime` | string ISO datetime | Thời gian dự kiến |
| `note`, `promoCode` | string | Lưu ý + mã giảm giá |
| `paymentMethodId` | integer | ID phương thức thanh toán |
| `totalAmount` | number | Tổng tiền (đồng) |
| `formattedTotalAmount` | string | Chuỗi hiển thị |
| `services[]` | array\<VoiceBookingPreviewServiceItem\> | Chi tiết dịch vụ |
| `employees[]` | array\<VoiceBookingEmployeePreview\> | Nhân viên sẽ phục vụ |
| `autoAssignedEmployees` | boolean | true nếu hệ thống tự phân công |

`VoiceBookingPreviewServiceItem`:
- `serviceId` (int), `serviceName` (string), `quantity` (int),
  `pricePerUnit` (number), `formattedPricePerUnit` (string),
  `subTotal` (number), `formattedSubTotal` (string), `selectedChoiceIds[]` (int).

`VoiceBookingEmployeePreview`:
- `employeeId`, `fullName`, `avatar`, `rating` (string),
  `hasWorkedWithCustomer` (boolean), `serviceIds[]` (int), `autoAssigned` (boolean).

### 5.1 Ví dụ sự kiện
- **RECEIVED**
```json
{
  "eventType": "RECEIVED",
  "requestId": "c867282d-650b-4b39-bb1c-9a893bfaab39",
  "status": "PROCESSING",
  "timestamp": "2025-11-17T09:12:02.115Z"
}
```
- **TRANSCRIBING**
```json
{
  "eventType": "TRANSCRIBING",
  "requestId": "c867282d-650b-4b39-bb1c-9a893bfaab39",
  "status": "PROCESSING",
  "progress": 0.55,
  "timestamp": "2025-11-17T09:12:04.900Z"
}
```
- **AWAITING_CONFIRMATION**
*(cấu trúc giống response REST, FE có thể tái sử dụng)*.
- **PARTIAL**
```json
{
  "eventType": "PARTIAL",
  "requestId": "c867282d-650b-4b39-bb1c-9a893bfaab39",
  "status": "PARTIAL",
  "transcript": "Đặt giúp tôi dịch vụ 2 tiếng vào sáng thứ 6...",
  "missingFields": ["addressId"],
  "clarificationMessage": "Thiếu địa chỉ",
  "processingTimeMs": 5200,
  "timestamp": "2025-11-17T09:12:07.102Z"
}
```
- **COMPLETED**
```json
{
  "eventType": "COMPLETED",
  "requestId": "c867282d-650b-4b39-bb1c-9a893bfaab39",
  "status": "COMPLETED",
  "bookingId": "BK20251117001",
  "transcript": "Đặt dịch vụ vệ sinh căn hộ lúc 14h ngày 18-11",
  "processingTimeMs": 9120,
  "timestamp": "2025-11-17T09:12:12.330Z"
}
```
- **FAILED**
```json
{
  "eventType": "FAILED",
  "requestId": "c867282d-650b-4b39-bb1c-9a893bfaab39",
  "status": "FAILED",
  "errorMessage": "Không thể kết nối dịch vụ nhận diện giọng nói",
  "transcript": null,
  "processingTimeMs": 2500,
  "timestamp": "2025-11-17T09:12:03.771Z"
}
```
- **CANCELLED**
```json
{
  "eventType": "CANCELLED",
  "requestId": "c867282d-650b-4b39-bb1c-9a893bfaab39",
  "status": "CANCELLED",
  "timestamp": "2025-11-17T09:18:00.000Z"
}
```

## 6. Error Payload (`VoiceBookingErrorPayload`)
| Trường | Kiểu | Bắt buộc | Mô tả |
| --- | --- | --- | --- |
| `errorCode` | string | ✔ | `VOICE_BOOKING_FORBIDDEN`, `VOICE_BOOKING_CONNECTION_ERROR`, ... |
| `errorMessage` | string | ✔ | Thông điệp tiếng Việt |
| `requestId` | string | ✖ | Có thể null nếu lỗi chung |
| `timestamp` | string ISO-8601 | ✔ | Thời điểm phát sinh |

**Ví dụ subscribe sai request**
```json
{
  "errorCode": "VOICE_BOOKING_FORBIDDEN",
  "errorMessage": "Bạn không có quyền theo dõi trạng thái voice booking này.",
  "requestId": "041a3cc5-6d36-48e6-9ea0-33b5560d7ce2",
  "timestamp": "2025-11-17T09:15:33.981Z"
}
```

## 7. Giới hạn & Timeout
| Tham số | Giá trị |
| --- | --- |
| `messageSizeLimit` | 128 KB |
| `sendBufferSizeLimit` | 512 KB |
| `sendTimeLimit` | 20 giây |
| `timeToFirstMessage` | 30 giây |

Vì SockJS heartbeat là 25 giây, client nên giữ kết nối (ping/pong) < 25 giây để tránh timeout. FE nên xử lý reconnect/backoff nếu nhận lỗi từ `/user/queue/voice-booking/errors`.
