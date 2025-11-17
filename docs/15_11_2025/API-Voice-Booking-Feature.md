# Voice Booking API Documentation

## Overview

Tính năng **Voice Booking** cho phép khách hàng đặt lịch dịch vụ bằng giọng nói thông qua API. Hệ thống sử dụng OpenAI Whisper để chuyển đổi giọng nói thành văn bản, sau đó phân tích để tạo booking tự động.

**Ngày tạo**: 15/11/2025  
**Phiên bản**: 1.0  
**Base URL**: `/api/v1/customer/bookings/voice`

---

## Features

- ✅ Chuyển đổi giọng nói thành văn bản (Whisper API)
- ✅ Phân tích tự động thông tin booking từ transcript
- ✅ Hỗ trợ tiếng Việt
- ✅ Xử lý đồng bộ với timeout 30s
- ✅ Lưu trữ transcript và metadata
- ✅ Xử lý partial results (thiếu thông tin)
- ✅ JWT authentication (ROLE_CUSTOMER, ROLE_ADMIN)

---

## Endpoints

### 1. Create Voice Booking

Tạo booking từ file audio.

**Endpoint**: `POST /api/v1/customer/bookings/voice`

**Authentication**: Required (JWT Bearer Token)

**Roles**: `ROLE_CUSTOMER`, `ROLE_ADMIN`

**Content-Type**: `multipart/form-data`

**Request Parameters**:

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `audio` | File | Yes | Audio file (mp3, wav, m4a, webm, ogg, flac) |
| `hints` | String (JSON) | No | Context hints để cải thiện parsing |

**Audio Requirements**:
- **Max size**: 5MB
- **Max duration**: 120 seconds (2 minutes)
- **Formats**: mp3, wav, m4a, webm, ogg, flac
- **Sample rate**: Được tự động chuyển về 16kHz mono (nếu cần)

**Request Body Example**:
```json
{
  "audio": "<file: booking_request.mp3>",
  "hints": {
    "serviceId": 1,
    "bookingTime": "2025-11-20T14:00:00",
    "address": "123 Nguyễn Văn Linh, Quận 7"
  }
}
```

**Success Response** (200 OK - Completed):
```json
{
  "success": true,
  "message": "Booking created successfully from voice input",
  "requestId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "COMPLETED",
  "bookingId": "BKG-20251115-001",
  "transcript": "Tôi muốn đặt dịch vụ vệ sinh nhà cửa vào lúc 2 giờ chiều ngày mai tại địa chỉ 123 Nguyễn Văn Linh Quận 7",
  "confidenceScore": null,
  "processingTimeMs": 2340
}
```

**Partial Response** (206 Partial Content - Service Not Found):
```json
{
  "success": false,
  "message": "Could not extract all required information from voice input",
  "requestId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "PARTIAL",
  "transcript": "Tôi muốn đặt dịch vụ dọn dẹp vào 2 giờ chiều ngày mai",
  "missingFields": ["service"],
  "clarificationMessage": "Xin lỗi, tôi không thể xác định được dịch vụ bạn yêu cầu.\n\nCác dịch vụ hiện có:\n• Dọn dẹp theo giờ\n• Tổng vệ sinh\n• Vệ sinh Sofa - Nệm - Rèm\n• Vệ sinh máy lạnh\n• Giặt sấy theo kg\n• Giặt hấp cao cấp\n• Nấu ăn gia đình\n• Đi chợ hộ\n\nVui lòng nói lại và chọn một trong các dịch vụ trên.\n\nThông tin đã hiểu:\n- Thời gian: 2025-11-17T14:00\n- Địa chỉ: 123 Nguyễn Văn Linh, Quận 7",
  "confidenceScore": null,
  "processingTimeMs": 1850
}
```

**Error Response** (400 Bad Request):
```json
{
  "success": false,
  "message": "Không thể chuyển đổi giọng nói thành văn bản",
  "requestId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "FAILED",
  "errorDetails": "Audio quality too low or format not supported"
}
```

**Error Response** (401 Unauthorized):
```json
{
  "success": false,
  "message": "Invalid or expired token"
}
```

**Error Response** (503 Service Unavailable):
```json
{
  "success": false,
  "message": "Voice booking service is currently unavailable"
}
```

---

### 2. Get Voice Booking Request Status

Lấy thông tin chi tiết về một voice booking request.

**Endpoint**: `GET /api/v1/customer/bookings/voice/{requestId}`

**Authentication**: Required (JWT Bearer Token)

**Roles**: `ROLE_CUSTOMER`, `ROLE_ADMIN`

**Path Parameters**:

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `requestId` | String | Yes | UUID của voice booking request |

**Request Example**:
```
GET /api/v1/customer/bookings/voice/550e8400-e29b-41d4-a716-446655440000
Authorization: Bearer YOUR_JWT_TOKEN
```

**Success Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "status": "COMPLETED",
    "transcript": "Tôi muốn đặt dịch vụ vệ sinh nhà cửa vào lúc 2 giờ chiều ngày mai tại địa chỉ 123 Nguyễn Văn Linh Quận 7",
    "confidenceScore": null,
    "processingTimeMs": 2340,
    "bookingId": "BKG-20251115-001",
    "missingFields": null,
    "errorMessage": null,
    "createdAt": "2025-11-15T10:30:00"
  }
}
```

**Error Response** (404 Not Found):
```json
{
  "success": false,
  "message": "Voice booking request not found: 550e8400-e29b-41d4-a716-446655440000"
}
```

---

### 3. Continue Voice Booking

Tiếp tục một voice booking request đã trả về PARTIAL để bổ sung thông tin thiếu.

**Endpoint**: `POST /api/v1/customer/bookings/voice/continue`

**Authentication**: Required (JWT Bearer Token)

**Roles**: `ROLE_CUSTOMER`, `ROLE_ADMIN`

**Content-Type**: `multipart/form-data`

**Request Parameters** (form-data):

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `requestId` | String | Yes | UUID của voice booking request cần tiếp tục |
| `audio` | File | No* | File audio bổ sung chứa thông tin thiếu |
| `additionalText` | String | No* | Text bổ sung chứa thông tin thiếu |
| `explicitFields` | String (JSON) | No* | JSON string chứa các field cụ thể cần bổ sung |

*Ít nhất một trong ba parameters (`audio`, `additionalText`, hoặc `explicitFields`) phải được cung cấp.

**Request Example 1** (Explicit fields):
```
POST /api/v1/customer/bookings/voice/continue
Content-Type: multipart/form-data
Authorization: Bearer YOUR_JWT_TOKEN

requestId: "155331d8-d8a6-42a2-bec8-08c89206a5f2"
explicitFields: '{"address":"123 Nguyễn Văn A","district":"Quận 1","city":"TP.HCM"}'
```

**Request Example 2** (Additional text):
```
POST /api/v1/customer/bookings/voice/continue
Content-Type: multipart/form-data
Authorization: Bearer YOUR_JWT_TOKEN

requestId: "155331d8-d8a6-42a2-bec8-08c89206a5f2"
additionalText: "Địa chỉ là 123 Nguyễn Văn A, Quận 1, TP.HCM"
```

**Request Example 3** (Audio file):
```
POST /api/v1/customer/bookings/voice/continue
Content-Type: multipart/form-data
Authorization: Bearer YOUR_JWT_TOKEN

requestId: "155331d8-d8a6-42a2-bec8-08c89206a5f2"
audio: <file: additional_info.mp3>
```

**Success Response** (200 OK):
```json
{
  "success": true,
  "message": "Booking created successfully from voice input",
  "requestId": "155331d8-d8a6-42a2-bec8-08c89206a5f2",
  "status": "COMPLETED",
  "bookingId": "BKG-20251116-002",
  "transcript": "Tôi muốn đặt dịch vụ vào chiều mai lúc 3 giờ chiều. Địa chỉ là 123 Nguyễn Văn A, Quận 1, TP.HCM",
  "confidenceScore": null,
  "processingTimeMs": 4973
}
```

**Partial Response** (206 Partial Content):
```json
{
  "success": false,
  "message": "Could not extract all required information from voice input",
  "requestId": "155331d8-d8a6-42a2-bec8-08c89206a5f2",
  "status": "PARTIAL",
  "transcript": "Tôi muốn đặt dịch vụ vào chiều mai lúc 3 giờ chiều. 123 Nguyễn Văn A",
  "missingFields": ["district", "city"],
  "clarificationMessage": "✓ Tôi đã hiểu được:\n  • Thời gian: 2025-11-17T15:00\n  • Dịch vụ: Dọn dẹp theo giờ\n  • Địa chỉ: 123 Nguyễn Văn A\n\n⚠️ Tôi cần thêm thông tin về:\n  • Quận/Huyện\n  • Thành phố",
  "extractedInfo": {
    "bookingTime": "2025-11-17T15:00",
    "services": "Dọn dẹp theo giờ",
    "address": "123 Nguyễn Văn A"
  },
  "confidenceScore": null,
  "processingTimeMs": 5230
}
```

**Error Response** (400 Bad Request):
```json
{
  "success": false,
  "message": "Không thể tiếp tục yêu cầu này",
  "requestId": "155331d8-d8a6-42a2-bec8-08c89206a5f2",
  "status": "FAILED",
  "errorDetails": "Request status is not PARTIAL, current status: COMPLETED"
}
```

**Error Response** (404 Not Found):
```json
{
  "success": false,
  "message": "Voice booking request not found: 155331d8-d8a6-42a2-bec8-08c89206a5f2"
}
```

---

### 4. Check Service Status

Kiểm tra trạng thái của voice booking service.

**Endpoint**: `GET /api/v1/customer/bookings/voice/status`

**Authentication**: Not Required

**Request Example**:
```
GET /api/v1/customer/bookings/voice/status
```

**Success Response** (200 OK):
```json
{
  "success": true,
  "voiceBookingEnabled": true,
  "message": "Voice booking service is available"
}
```

---

## Voice Transcript Examples

### Example 1: Complete Request (Exact Match)
**Audio Transcript**:
> "Tôi muốn đặt dịch vụ **Tổng vệ sinh** vào lúc 2 giờ chiều ngày mai tại địa chỉ 123 Nguyễn Văn Linh Phường Thủ Dầu 1 TP. Hồ Chí Minh"

**Extracted Information**:
- Service: Tổng vệ sinh (exact match with DB)
- Time: 14:00 (ngày mai)
- Address: 123 Nguyễn Văn Linh Quận 7

**Result**: ✅ COMPLETED

---

### Example 2: Service Not Matched
**Audio Transcript**:
> "Đặt dịch vụ **dọn dẹp nhà** vào 9 giờ sáng ngày mai tại 456 Lê Văn Việt"

**Extracted Information**:
- Time: 09:00 (ngày mai)
- Address: 456 Lê Văn Việt

**Missing**: Service (không khớp với tên trong DB)

**Clarification**: Hiển thị danh sách 8 dịch vụ có sẵn, yêu cầu chọn lại

**Result**: ⚠️ PARTIAL (service clarification needed)

---

### Example 3: With Hints
**Audio Transcript**:
> "Tôi muốn đặt lịch vào 3 giờ chiều mai"

**Hints**:
```json
{
  "serviceId": 2,
  "address": "456 Lê Văn Việt, Quận 9"
}
```

**Result**: ✅ COMPLETED (service and address from hints, time from audio)

---

**Status Values**:
- `PENDING`: Mới tạo, chưa xử lý
- `PROCESSING`: Đang xử lý
- `COMPLETED`: Hoàn thành, booking đã được tạo
- `PARTIAL`: Thiếu thông tin, cần clarification
- `FAILED`: Xử lý thất bại

---

## Performance & Cost Optimization

**Timeouts**:
- API timeout: 30 seconds
- Retry delay: 1s, 2s (exponential backoff)
- Max retries: 2

---

## Error Handling

### Common Errors

| Error | Description | Solution |
|-------|-------------|----------|
| `Audio file too large` | File > 5MB | Compress or trim audio |
| `Audio duration too long` | Duration > 120s | Split into shorter clips |
| `Invalid audio format` | Unsupported format | Use mp3, wav, m4a, etc. |
| `Whisper API timeout` | Request > 30s | Retry with better network |
| `Whisper API key invalid` | Missing/wrong API key | Configure OPENAI_API_KEY |
| `Service unavailable` | Whisper disabled | Enable in config |
| `Missing required fields` | Incomplete transcript | Provide hints or retry |

---

## Testing

### Manual Testing with cURL

```bash
# 1. Test service status
curl -X GET "http://localhost:8080/api/v1/customer/bookings/voice/status"

# 2. Create voice booking
curl -X POST "http://localhost:8080/api/v1/customer/bookings/voice" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "audio=@test_booking.mp3" \
  -F 'hints={"serviceId": 1, "address": "123 Test St"}'

# 3. Check request status
curl -X GET "http://localhost:8080/api/v1/customer/bookings/voice/REQUEST_ID" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Sample Audio Scripts (Vietnamese)

**Script 1** (Complete):
```
Tôi muốn đặt dịch vụ vệ sinh nhà cửa vào lúc 2 giờ chiều ngày mai 
tại địa chỉ 123 Nguyễn Văn Linh, Quận 7, Thành phố Hồ Chí Minh.
```

**Script 2** (With note):
```
Đặt lịch giặt ủi lúc 9 giờ sáng thứ 7 này tại 456 Lê Văn Việt, Quận 9.
Lưu ý: Có nhiều quần áo cần giặt, khoảng 5kg.
```

**Script 3** (Partial):
```
Tôi cần dịch vụ nấu ăn vào buổi trưa.
```

---

## Service Matching Algorithm

### Matching Strategy

1. **Exact Match** (Priority 1):
   - So khớp chính xác tên dịch vụ trong DB (case-insensitive)
   - Ví dụ: "Tổng vệ sinh" → ✅ Match "Tổng vệ sinh"

2. **Fuzzy Match** (Priority 2):
   - Sử dụng Levenshtein Distance algorithm
   - Threshold: 70% similarity minimum
   - High confidence: > 80% similarity
   - Ví dụ: "tổng vê sinh" → ✅ Match "Tổng vệ sinh" (85%)

3. **No Match** (Clarification Required):
   - Không tìm thấy dịch vụ phù hợp
   - Hiển thị danh sách tất cả dịch vụ active
   - Yêu cầu người dùng chọn lại từ danh sách
   - Ví dụ: "dọn dẹp" → ❌ Không match → Hiển thị 8 dịch vụ

### Available Services (As of Nov 2025)

1. Dọn dẹp theo giờ
2. Tổng vệ sinh
3. Vệ sinh Sofa - Nệm - Rèm
4. Vệ sinh máy lạnh
5. Giặt sấy theo kg
6. Giặt hấp cao cấp
7. Nấu ăn gia đình
8. Đi chợ hộ

---

## WebSocket Voice Booking Channel

### Endpoint & Auth
- **Endpoint**: `ws://<host>/ws/voice-booking`
- **Protocol**: STOMP + SockJS fallback, heartbeat 10s.
- **JWT**: header `Authorization: Bearer <token>` ngay trên handshake + trong `connectHeaders`.
- **Role**: chỉ `ROLE_CUSTOMER` được phép kết nối (ChannelInterceptor re-check).

### Topics
| Destination | Mô tả |
|-------------|-------|
| `/topic/voice-booking/{requestId}` | Push trạng thái xử lý theo request |
| `/user/queue/voice-booking/errors` | Push lỗi xác thực/kết nối cho user hiện tại |

### Event reference
| eventType | status | Khi nào | Fields chính |
|-----------|--------|---------|--------------|
| `RECEIVED` | `PROCESSING` | Server lưu audio/new chunk | `timestamp` |
| `TRANSCRIBING` | `PROCESSING` | Đang gọi Whisper | `progress` 0→1 |
| `PARTIAL` | `PARTIAL` | Thiếu dữ liệu | `missingFields`, `clarificationMessage`, `transcript` |
| `COMPLETED` | `COMPLETED` | Booking tạo thành công | `bookingId`, `processingTimeMs`, `transcript` |
| `FAILED` | `FAILED` | Lỗi bất kỳ (JWT timeout, audio fail) | `errorMessage`, `transcript` nếu có |

```json
{
  "eventType": "FAILED",
  "requestId": "b3f1-4a21",
  "status": "FAILED",
  "transcript": null,
  "missingFields": null,
  "clarificationMessage": null,
  "bookingId": null,
  "processingTimeMs": null,
  "errorMessage": "Không thể chuyển đổi giọng nói thành văn bản",
  "timestamp": "2025-11-16T09:05:33.120Z",
  "progress": null
}
```

**Error queue**:
```json
{
  "errorCode": "VOICE_BOOKING_FORBIDDEN",
  "errorMessage": "Bạn không có quyền theo dõi trạng thái voice booking này.",
  "requestId": "b3f1-4a21",
  "timestamp": "2025-11-16T09:06:02.511Z"
}
```

### Client sample
```javascript
const sock = new SockJS('https://api.example.com/ws/voice-booking', null, {
  transports: ['xhr-streaming', 'xhr-polling'],
  transportOptions: {
    'xhr-streaming': { headers: { Authorization: `Bearer ${token}` } },
    'xhr-polling': { headers: { Authorization: `Bearer ${token}` } }
  }
});

const stomp = Stomp.over(sock);
stomp.connect(
  { Authorization: `Bearer ${token}` },
  () => {
    stomp.subscribe(`/topic/voice-booking/${requestId}`, (msg) => handleVoiceEvent(JSON.parse(msg.body)));
    stomp.subscribe('/user/queue/voice-booking/errors', (msg) => console.error(JSON.parse(msg.body)));
  },
  (err) => console.error('Voice WS error', err)
);
```

### Test checklist
1. Handshake 401 khi thiếu JWT / sai role, 200 khi hợp lệ.
2. User A subscribe request của user B → server deny + push lỗi.
3. Luồng thành công: `RECEIVED → TRANSCRIBING → COMPLETED`.
4. Luồng partial + `/voice/continue`: `RECEIVED → PARTIAL → COMPLETED`.
5. Reconnect sau khi mất kết nối → `SUBSCRIBE` lại và tiếp tục nhận event.

---

## Future Enhancements

### Planned Features

- [ ] Support for offline Whisper.cpp (cost reduction)
- [ ] Multi-language support (English, etc.)
- [ ] Speaker diarization (multiple speakers)
- [ ] Confidence-based auto-retry
- [ ] Audio quality pre-validation
- [ ] WebSocket notifications for async processing
- [ ] Voice response synthesis (TTS)
- [x] **Strict service name matching** (v1.1 - Nov 16, 2025)
- [x] **Fuzzy matching with Levenshtein distance** (v1.1)
- [x] **Continue partial booking endpoint** (v1.2 - Nov 16, 2025)
