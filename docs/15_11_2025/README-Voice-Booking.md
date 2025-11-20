# Voice Booking Feature - Implementation Summary

## 📋 Tổng Quan

Tính năng **Voice Booking** cho phép khách hàng đặt lịch dịch vụ bằng giọng nói sử dụng OpenAI Whisper API.

**Ngày triển khai**: 15/11/2025  
**Branch**: `features/voice`  
**Spring Boot**: 3.5.4  
**Java**: 17

---

## 🎯 Mục Tiêu Đã Đạt Được

✅ **Endpoint mới**: `POST /api/v1/customer/bookings/voice`  
✅ **Whisper Integration**: OpenAI Whisper API cho voice-to-text  
✅ **Rule-based Parser**: Phân tích tiếng Việt từ transcript  
✅ **Database Schema**: Bảng `voice_booking_request` để lưu metadata  
✅ **Authentication**: JWT với ROLE_CUSTOMER, ROLE_ADMIN  
✅ **Error Handling**: Xử lý partial results và missing fields  
✅ **Configuration**: Linh hoạt qua `application.yml`  
✅ **Documentation**: Tài liệu API đầy đủ  

---

## 📁 Cấu Trúc File Mới

### Database
```
postgres_data/init_sql/
└── 13_voice_booking.sql          # Schema cho voice_booking_request table
```

### Models
```
src/main/java/iuh/house_keeping_service_be/
├── models/
│   └── VoiceBookingRequest.java  # Entity cho voice booking requests
├── enums/
│   └── VoiceBookingStatus.java   # Enum cho status values
└── repositories/
    └── VoiceBookingRequestRepository.java
```

### DTOs
```
src/main/java/iuh/house_keeping_service_be/dtos/VoiceBooking/
├── VoiceBookingRequest.java      # Request DTO (audio + hints)
├── VoiceBookingResponse.java     # Response DTO với factory methods
├── VoiceToTextResult.java        # Kết quả từ Whisper
└── ParsedBookingInfo.java        # Kết quả parsing transcript
```

### Services
```
src/main/java/iuh/house_keeping_service_be/services/VoiceBookingService/
├── VoiceToTextService.java           # Interface cho voice-to-text
├── WhisperVoiceToTextService.java    # Whisper API implementation
├── VoiceBookingParserService.java    # Parse transcript → BookingCreateRequest
└── VoiceBookingService.java          # Orchestrator service
```

### Controllers
```
src/main/java/iuh/house_keeping_service_be/controllers/
└── VoiceBookingController.java   # REST endpoint cho voice booking
```

### Documentation
```
docs/15_11_2025/
└── API-Voice-Booking-Feature.md  # Tài liệu API chi tiết
```

---

## 🔧 Cấu Hình

### Dependencies (build.gradle)

```gradle
// OpenAI API for Whisper voice-to-text
implementation 'com.theokanning.openai-gpt3-java:service:0.18.2'

// Audio processing library
implementation 'com.googlecode.soundlibs:jlayer:1.0.1.4'
implementation 'com.googlecode.soundlibs:mp3spi:1.9.5.4'
```

### Application Configuration (application.yml)

```yaml
whisper:
  enabled: ${WHISPER_ENABLED:true}
  api-key: ${OPENAI_API_KEY:}
  model: ${WHISPER_MODEL:whisper-1}
  timeout-seconds: ${WHISPER_TIMEOUT:30}
  max-retries: ${WHISPER_MAX_RETRIES:2}
  audio:
    max-size-mb: 5
    max-duration-seconds: 120
    target-sample-rate: 16000
    target-channels: 1
  processing:
    async-enabled: ${WHISPER_ASYNC:true}
    thread-pool-size: ${WHISPER_THREAD_POOL:3}
```

### Environment Variables

**Required**:
- `OPENAI_API_KEY` - OpenAI API key cho Whisper

**Optional**:
- `WHISPER_ENABLED` (default: true)
- `WHISPER_MODEL` (default: whisper-1)
- `WHISPER_TIMEOUT` (default: 30)
- `WHISPER_MAX_RETRIES` (default: 2)

---

## 🚀 Triển Khai

### 1. Database Migration

```bash
# Chạy migration script
psql -U postgres -d house_keeping -f postgres_data/init_sql/13_voice_booking.sql
```

### 2. Cấu Hình Environment

```bash
# Thêm vào .env hoặc system environment
export OPENAI_API_KEY="sk-..."
export WHISPER_ENABLED=true
```

### 3. Build & Run

```bash
# Build project
./gradlew clean build

# Run application
./gradlew bootRun
```

### 4. Kiểm Tra

```bash
# Check service status
curl http://localhost:8080/api/v1/customer/bookings/voice/status
```

---

## 📊 Database Schema

### Table: `voice_booking_request`

| Column | Type | Description |
|--------|------|-------------|
| id | VARCHAR(36) | Primary key (UUID) |
| customer_id | VARCHAR(36) | Foreign key → customer |
| audio_file_name | VARCHAR(255) | Tên file audio |
| audio_duration_seconds | DECIMAL(10,2) | Thời lượng audio |
| audio_size_bytes | BIGINT | Kích thước file |
| transcript | TEXT | Transcript từ Whisper |
| confidence_score | DECIMAL(5,4) | Độ tin cậy (0-1) |
| processing_time_ms | INTEGER | Thời gian xử lý (ms) |
| hints | JSONB | Context hints từ user |
| booking_id | VARCHAR(36) | Foreign key → booking (nếu success) |
| status | VARCHAR(50) | PENDING/PROCESSING/COMPLETED/PARTIAL/FAILED |
| error_message | TEXT | Thông báo lỗi (nếu có) |
| missing_fields | JSONB | Danh sách field còn thiếu |
| created_at | TIMESTAMP | Thời gian tạo |
| updated_at | TIMESTAMP | Thời gian cập nhật |

**Indexes**:
- `idx_voice_booking_customer` (customer_id)
- `idx_voice_booking_status` (status)
- `idx_voice_booking_booking` (booking_id)
- `idx_voice_booking_created` (created_at DESC)

---

## 🔄 Workflow

### 1. Voice to Text (Whisper)
```
Audio File → WhisperVoiceToTextService → Transcript
- Validate: size ≤ 5MB, duration ≤ 120s
- Call OpenAI Whisper API
- Retry logic: max 2 retries với exponential backoff
- Timeout: 30s
```

### 2. Transcript Parsing
```
Transcript → VoiceBookingParserService → ParsedBookingInfo
- Extract service (từ keywords)
- Extract time (từ patterns: "2 giờ", "ngày mai")
- Extract address (từ keywords: "tại", "ở", "địa chỉ")
- Apply hints nếu có
- Validate completeness
```

### 3. Booking Creation
```
ParsedBookingInfo → BookingService → Booking
- Convert to BookingCreateRequest
- Validate booking rules
- Create booking record
- Update voice_booking_request status
```

### Status Flow
```
PENDING → PROCESSING → [COMPLETED | PARTIAL | FAILED]
```

---

## 🔊 WebSocket Voice Booking Channel

### Kiến trúc & luồng
```
Client (SockJS + STOMP)
        │  Authorization: Bearer <JWT>
        ▼
/ws/voice-booking (JwtHandshakeInterceptor + VoiceBookingHandshakeHandler)
        │
VoiceBookingChannelInterceptor (JWT re-check + ownership guard)
        │
VoiceBookingEventPublisher (SimpMessagingTemplate)
        │
/topic/voice-booking/{requestId} & /user/queue/voice-booking/errors
```
- Handshake bắt buộc gửi header `Authorization: Bearer <token>` và token phải thuộc `ROLE_CUSTOMER`.
- `VoiceBookingChannelInterceptor` chặn CONNECT/SUBSCRIBE nếu JWT hết hạn hoặc user subscribe sai `requestId`, đồng thời push lỗi vào `/user/queue/voice-booking/errors`.
- Heart-beat: 10s (server ↔ client); SockJS fallback giữ `setHeartbeatTime(25s)`.
- Khách hàng có thể reconnect và subscribe lại cùng `requestId` để tiếp tục nhận sự kiện.

### Endpoints & channels
| Purpose | Path |
|---------|------|
| WebSocket endpoint | `ws://<host>/ws/voice-booking` |
| Topic per request | `/topic/voice-booking/{requestId}` |
| Error queue | `/user/queue/voice-booking/errors` |

### Event types
| Event | Khi nào | Payload chính |
|-------|---------|---------------|
| `RECEIVED` | Server nhận file audio/continue request | `requestId`, `status=PROCESSING`, `timestamp` |
| `TRANSCRIBING` | Đang gửi tới Whisper / ghép transcript | `progress` (0→1), `status=PROCESSING` |
| `PARTIAL` | Thiếu field | `missingFields[]`, `clarificationMessage`, `transcript` |
| `COMPLETED` | Booking tạo thành công | `bookingId`, `transcript`, `processingTimeMs` |
| `FAILED` | Mọi lỗi xử lý | `errorMessage`, `transcript` (nếu có) |

**Payload chuẩn**:
```json
{
  "eventType": "PARTIAL",
  "requestId": "b3f1...",
  "status": "PARTIAL",
  "transcript": "Tôi muốn đặt ...",
  "missingFields": ["service"],
  "clarificationMessage": "Vui lòng chọn dịch vụ",
  "bookingId": null,
  "processingTimeMs": 1850,
  "errorMessage": null,
  "timestamp": "2025-11-16T09:10:33.170Z",
  "progress": null
}
```

**Error queue payload**:
```json
{
  "errorCode": "VOICE_BOOKING_FORBIDDEN",
  "errorMessage": "Bạn không có quyền theo dõi trạng thái voice booking này.",
  "requestId": "b3f1...",
  "timestamp": "2025-11-16T09:11:00.218Z"
}
```

### Ví dụ client (SockJS + STOMP)
```javascript
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

const accessToken = 'Bearer xxx';
const requestId = 'b3f1-...';

const sock = new SockJS('http://localhost:8080/ws/voice-booking', null, {
  transports: ['xhr-streaming', 'xhr-polling'], // cho phép set header
  transportOptions: {
    'xhr-streaming': { headers: { Authorization: accessToken } },
    'xhr-polling': { headers: { Authorization: accessToken } }
  }
});

const client = Stomp.over(sock);
client.heartbeat.outgoing = 10000;
client.heartbeat.incoming = 10000;

client.connect(
  { Authorization: accessToken },
  () => {
    client.subscribe(`/topic/voice-booking/${requestId}`, (frame) => {
      console.log('Voice event', JSON.parse(frame.body));
    });
    client.subscribe('/user/queue/voice-booking/errors', (frame) => {
      console.error('Voice WS error', JSON.parse(frame.body));
    });
  },
  (error) => console.error('WebSocket disconnected', error)
);
```

### Logging & quan sát
- `VoiceBookingEventPublisher` log `user`, `requestId`, `eventType`, `processingTimeMs`.
- `VoiceBookingChannelInterceptor` log cảnh báo khi bị từ chối subscribe/connect.
- Có thể thêm metric Prometheus qua `Counter`/`Timer` tại publisher nếu cần dashboards.

### Kiểm thử gợi ý
1. **Handshake**: kết nối với JWT hợp lệ/không hợp lệ/không phải ROLE_CUSTOMER → expect 101 vs 401/403.
2. **Authorization leak**: user A thử subscribe `/topic/voice-booking/{requestIdB}` → nhận lỗi ở `/user/queue/voice-booking/errors` và server throw `MessagingException`.
3. **Event flow**: POST `/voice` với audio hợp lệ → nhận chuỗi `RECEIVED → TRANSCRIBING → COMPLETED`.
4. **Partial loop**: ép parser trả `PARTIAL`, sau đó POST `/voice/continue` → kiểm tra topic cũ nhận thêm `RECEIVED → PARTIAL` hoặc `COMPLETED`.
5. **JWT Expired**: connect thành công, revoke token rồi gửi CONNECT mới → server chặn, log và không gửi event.
6. **Reconnect**: kill socket rồi subscribe lại cùng `requestId` → nhận tiếp các event kế (verify no duplicate rows).

---

## 🎨 API Usage Examples

### Example 1: Complete Request

**Request**:
```bash
curl -X POST "http://localhost:8080/api/v1/customer/bookings/voice" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "audio=@booking.mp3"
```

**Audio**: "Tôi muốn đặt dịch vụ vệ sinh nhà cửa vào 2 giờ chiều mai tại 123 Nguyễn Văn Linh Quận 7"

**Response** (200 OK):
```json
{
  "success": true,
  "status": "COMPLETED",
  "bookingId": "BKG-20251115-001",
  "transcript": "...",
  "processingTimeMs": 2340
}
```

---

### Example 2: Partial Request (Missing Info)

**Audio**: "Đặt dịch vụ giặt ủi vào 9 giờ sáng"

**Response** (206 Partial Content):
```json
{
  "success": false,
  "status": "PARTIAL",
  "missingFields": ["address"],
  "clarificationMessage": "Tôi đã hiểu được...\nTuy nhiên, tôi cần thêm thông tin về:\n- Địa chỉ",
  "transcript": "...",
  "processingTimeMs": 1850
}
```

---

### Example 3: With Hints

**Request**:
```bash
curl -X POST "http://localhost:8080/api/v1/customer/bookings/voice" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "audio=@booking.mp3" \
  -F 'hints={"serviceId": 1, "address": "456 Lê Văn Việt Q9"}'
```

**Audio**: "Đặt lịch vào 3 giờ chiều mai"

**Response**: ✅ COMPLETED (serviceId và address từ hints, time từ audio)

---

## ⚙️ Chi Phí & Hiệu Năng

### Whisper API Costs

| Duration | Cost (USD) | Monthly (1000 reqs) |
|----------|-----------|---------------------|
| 30s | ~$0.003 | $3.00 |
| 60s | ~$0.006 | $6.00 |
| 90s | ~$0.009 | $9.00 |
| 120s | ~$0.012 | $12.00 |

**Pricing**: $0.006 per minute

### Performance

- **Audio upload**: 100-500ms
- **Whisper transcription**: 1-3s
- **Parsing**: 100-300ms
- **Booking creation**: 200-500ms
- **Total**: ~2-4s (sync mode)

---

## 🔒 Security

### Authentication
- JWT Bearer Token required
- Roles: `ROLE_CUSTOMER`, `ROLE_ADMIN`

### Validation
- Audio size: max 5MB
- Audio duration: max 120s
- Supported formats: mp3, wav, m4a, webm, ogg, flac

### Rate Limiting
- Whisper API timeout: 30s
- Max retries: 2
- Exponential backoff: 1s, 2s

---

## 🐛 Error Handling

### Common Errors

| Error | HTTP | Description |
|-------|------|-------------|
| Audio too large | 400 | File size > 5MB |
| Audio too long | 400 | Duration > 120s |
| Invalid format | 400 | Unsupported audio format |
| Service unavailable | 503 | Whisper disabled or API key missing |
| Whisper timeout | 500 | API call exceeded 30s |
| Invalid token | 401 | JWT expired or invalid |

---

## 🧪 Testing

### Manual Testing

```bash
# 1. Check service status
curl http://localhost:8080/api/v1/customer/bookings/voice/status

# 2. Create voice booking
curl -X POST "http://localhost:8080/api/v1/customer/bookings/voice" \
  -H "Authorization: Bearer TOKEN" \
  -F "audio=@test.mp3"

# 3. Get request status
curl -X GET "http://localhost:8080/api/v1/customer/bookings/voice/REQUEST_ID" \
  -H "Authorization: Bearer TOKEN"
```

### Sample Audio Scripts (Vietnamese)

**Complete**:
```
Tôi muốn đặt dịch vụ vệ sinh nhà cửa vào lúc 2 giờ chiều ngày mai 
tại địa chỉ 123 Nguyễn Văn Linh, Quận 7.
```

**Partial**:
```
Đặt dịch vụ giặt ủi vào 9 giờ sáng thứ 7.
```

**With Note**:
```
Tôi cần dịch vụ nấu ăn vào buổi trưa mai tại 456 Lê Văn Việt. 
Lưu ý: Có 5 người ăn.
```

---

## 📚 Next Steps

### Future Enhancements

1. **Offline Whisper.cpp** - Giảm chi phí bằng local processing
2. **Async Processing** - Sử dụng WebSocket cho kết quả real-time
3. **Multi-language** - Hỗ trợ tiếng Anh và các ngôn ngữ khác
4. **Confidence Scoring** - Tự động retry khi confidence thấp
5. **Voice Response** - TTS để phản hồi bằng giọng nói
6. **Advanced NLP** - Sử dụng LLM để parsing tốt hơn

### Integration Points

- [ ] Mobile app integration
- [ ] WebSocket notifications cho async processing
- [ ] Admin dashboard cho voice booking analytics
- [ ] A/B testing cho parser improvements

---

## 📖 Documentation

- **API Docs**: `docs/15_11_2025/API-Voice-Booking-Feature.md`
- **Database Schema**: `postgres_data/init_sql/13_voice_booking.sql`
- **Config Reference**: `src/main/resources/application.yml`

---

## 👥 Contacts

**Developer**: Backend Team  
**Date**: 15/11/2025  
**Support**: support@housekeeping.com

---

## ✅ Checklist

- [x] Database migration created
- [x] Entity and Repository implemented
- [x] DTOs created
- [x] Services implemented (VoiceToTextService, ParserService, VoiceBookingService)
- [x] Controller with REST endpoints
- [x] Configuration in application.yml
- [x] Dependencies added to build.gradle
- [x] API documentation
- [x] Error handling
- [x] JWT authentication
- [x] Vietnamese language support
- [x] Audio validation
- [x] Retry logic
- [x] Timeout handling
- [ ] Unit tests (TODO)
- [ ] Integration tests (TODO)
- [ ] Performance tests (TODO)

---

**Status**: ✅ **READY FOR TESTING**
