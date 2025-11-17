# API Spec – Voice Booking Confirmation Flow (17/11/2025)

## 1. Bối cảnh
Luồng đặt lịch bằng giọng nói giờ đây không tạo booking ngay sau khi parse transcript. Hệ thống dựng một **đơn nháp** (draft) kèm preview để khách hàng kiểm tra. Người dùng phải chủ động xác nhận hoặc huỷ/chỉnh sửa trước khi booking được lưu chính thức.

Các thay đổi chính:
- Lưu trữ `draft_booking_request` và `preview_payload` trong `voice_booking_request`.
- Phản hồi API & WebSocket bổ sung trạng thái `AWAITING_CONFIRMATION`, `CANCELLED`.
- Các endpoint mới: `/confirm`, `/cancel`.
- `continue` có thể cập nhật draft hiện tại hoặc yêu cầu thiếu thông tin.

## 2. Luồng tổng quát
1. **Upload voice** (`POST /bookings/voice`):
   - Whisper → Parser → `BookingCreateRequest`.
   - Validate & auto-assign nhân viên (nếu thiếu).
   - Trả về preview (`status=AWAITING_CONFIRMATION`) qua REST + WebSocket.
2. **Người dùng lựa chọn:**
   - `POST /bookings/voice/confirm`: lưu booking, trả bookingId.
   - `POST /bookings/voice/cancel`: xoá draft.
   - `POST /bookings/voice/continue`: gửi text/audio bổ sung; nếu đủ dữ liệu → preview mới, nếu thiếu → trạng thái `PARTIAL`.

## 3. REST Endpoints

### 3.1 Tạo yêu cầu voice (`POST /api/v1/customer/bookings/voice`)
| Field | Type | Mô tả |
| --- | --- | --- |
| `audio` | multipart | Bắt buộc (<=5MB) |
| `hints` | JSON string | Tuỳ chọn |

**Responses**
- `202 ACCEPTED` – `status=AWAITING_CONFIRMATION`, trả preview.
- `206 PARTIAL_CONTENT` – thiếu thông tin (`missingFields`, `clarificationMessage`).
- `400/503` – lỗi dịch vụ.

### 3.2 Xem chi tiết yêu cầu (`GET /api/v1/customer/bookings/voice/{id}`)
- Trả metadata hiện có, bao gồm preview nếu draft đang chờ xác nhận.

### 3.3 Bổ sung thông tin (`POST /api/v1/customer/bookings/voice/continue`)
| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `requestId` | string | ✔ | ID draft |
| `audio` | multipart | ✖ | Audio bổ sung |
| `additionalText` | string | ✖ | Text miêu tả thay đổi |
| `explicitFields` | JSON string | ✖ | Map field-value cụ thể |

**Responses**
- `202 ACCEPTED` – preview mới (`status=AWAITING_CONFIRMATION`).
- `206 PARTIAL_CONTENT` – vẫn thiếu dữ liệu.
- `400` – không hợp lệ.

### 3.4 Xác nhận draft (`POST /api/v1/customer/bookings/voice/confirm`)
Request body:
```json
{
  "requestId": "UUID"
}
```
Responses:
- `200 OK` – `status=COMPLETED`, chứa `bookingId`.
- `400` – draft không còn hợp lệ.
- `401/500` – lỗi bảo mật/hệ thống.

### 3.5 Huỷ draft (`POST /api/v1/customer/bookings/voice/cancel`)
Body giống `/confirm`. Trả `status=CANCELLED` sau khi xoá bản ghi.

## 4. Preview Payload

```json
{
  "addressId": "addr-123",
  "fullAddress": "123 Nguyễn Văn Linh, Q7",
  "bookingTime": "2025-11-18T14:00:00",
  "note": "Dọn phòng khách",
  "promoCode": null,
  "paymentMethodId": 1,
  "totalAmount": 450000,
  "formattedTotalAmount": "450.000 đ",
  "services": [
    {
      "serviceId": 101,
      "serviceName": "Vệ sinh nhà",
      "quantity": 1,
      "pricePerUnit": 450000,
      "formattedPricePerUnit": "450.000 đ",
      "subTotal": 450000,
      "selectedChoiceIds": [5,9]
    }
  ],
  "employees": [
    {
      "employeeId": "emp-1",
      "fullName": "Nguyễn A",
      "avatar": "https://.../a.jpg",
      "rating": "FOUR_STAR",
      "hasWorkedWithCustomer": true,
      "serviceIds": [101],
      "autoAssigned": true
    }
  ],
  "autoAssignedEmployees": true
}
```

## 5. WebSocket Events (`/topic/voice-booking/{requestId}`)
| Event | Trạng thái | Payload chính |
| --- | --- | --- |
| `RECEIVED/TRANSCRIBING` | PROCESSING | progress |
| `PARTIAL` | PARTIAL | `missingFields`, `clarificationMessage` |
| `AWAITING_CONFIRMATION` | AWAITING_CONFIRMATION | `preview`, `processingTimeMs` |
| `COMPLETED` | COMPLETED | `bookingId`, `transcript` |
| `FAILED` | FAILED | `errorMessage` |
| `CANCELLED` | CANCELLED | – |

## 6. Database Changes
- **Migration** `postgres_data/init_sql/14_voice_booking_confirmation.sql`
  - Thêm cột `draft_booking_request JSONB`, `preview_payload JSONB` cho bảng `voice_booking_request`.
  - Giữ nguyên FK/Indexes trước đó.

## 7. DTO / Model Updates
- `VoiceBookingResponse`: thêm `preview`, factory `awaitingConfirmation`, `cancelled`.
- `VoiceBookingEventPayload`: thêm `preview`.
- `VoiceBookingEventType`, `VoiceBookingStatus`: thêm `AWAITING_CONFIRMATION`, `CANCELLED`.
- `VoiceBookingRequest` entity: trạng thái mới + các field lưu draft/preview.
- DTO preview mới: `VoiceBookingPreview`, `VoiceBookingPreviewServiceItem`, `VoiceBookingEmployeePreview`.
- Request chung cho confirm/cancel: `VoiceBookingActionRequest`.

## 8. Xử lý nghiệp vụ
1. **prepareDraft**:
   - Validate `BookingCreateRequest`.
   - Auto-assign nhân viên nếu chưa có assignments (gọi `EmployeeScheduleService.findSuitableEmployees`).
   - Build preview & lưu vào record.
2. **confirmVoiceBooking**:
   - Lấy `draft_booking_request`, gọi `BookingService.createBooking`.
   - Cập nhật status `COMPLETED`, emit event `COMPLETED`.
3. **cancelVoiceBooking**:
   - Xoá record (chỉ khi `PARTIAL` hoặc `AWAITING_CONFIRMATION`), emit `CANCELLED`.

## 9. Kiểm thử đề xuất
- `./gradlew test` (hiện không có test cụ thể, nhưng đảm bảo compile).
- Manual:
  1. Upload audio hoàn chỉnh → nhận preview.
  2. Gọi `/confirm` → booking được tạo.
  3. Lặp lại nhưng dùng `/cancel`.
  4. Gọi `/continue` với audio/text mới để kiểm tra preview update.

---
**Phụ trách**: Backend Team  
**Ngày cập nhật**: 17/11/2025
