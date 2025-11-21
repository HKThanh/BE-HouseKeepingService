# Recurring Booking Conversation (21-11-2025)

Sau khi tạo recurring booking, hệ thống sẽ tự động tạo (hoặc tái sử dụng) một conversation duy nhất gắn với recurring booking. FE nhận conversation ngay trong response.

## API
### Create recurring booking + conversation
- `POST /api/v1/customer/recurring-bookings/{customerId}`
  - Role: CUSTOMER (Bearer token)
  - Body (rút gọn ví dụ):
    ```json
    {
      "addressId": "addr0001-...",
      "recurrenceType": "WEEKLY",
      "recurrenceDays": [2,5],
      "bookingTime": "09:00:00",
      "startDate": "2025-11-25",
      "endDate": "2025-12-31",
      "note": "Vệ sinh định kỳ",
      "title": "Recurring clean",
      "promoCode": null,
      "bookingDetails": [
        { "serviceId": 1, "quantity": 1, "expectedPricePerUnit": 120000, "selectedChoiceIds": [1,2] }
      ]
    }
    ```
  - Response 201 (đã rút gọn):
    ```json
    {
      "success": true,
      "message": "Đặt lịch định kỳ thành công",
      "data": {
        "success": true,
        "message": "Đặt lịch định kỳ thành công",
        "recurringBooking": {
          "recurringBookingId": "rb000001-...",
          "customerId": "c1000001-...",
          "title": "Recurring clean",
          "status": "ACTIVE",
          "bookingTime": "09:00:00",
          "startDate": "2025-11-25",
          "endDate": "2025-12-31",
          "recurringBookingDetails": [
            { "serviceId": 1, "serviceName": "Vệ sinh nhà cửa", "quantity": 1, "pricePerUnit": 120000 }
          ],
          "upcomingBookings": 2,
          "totalGeneratedBookings": 2
        },
        "generatedBookingIds": [
          "b0000001-0000-0000-0000-000000000010",
          "b0000001-0000-0000-0000-000000000011"
        ],
        "totalBookingsToBeCreated": 2,
        "conversation": {
          "conversationId": "conv-rec-0001",
          "customerId": "c1000001-0000-0000-0000-000000000001",
          "employeeId": "e1000001-0000-0000-0000-000000000003",
          "bookingId": "b0000001-0000-0000-0000-000000000010",
          "recurringBookingId": "rb000001-...",
          "lastMessage": "Xin chào. Tôi là ...",
          "lastMessageTime": "2025-11-21T09:00:00",
          "isActive": true,
          "canChat": true
        }
      }
    }
    ```
- Lưu ý:
  - BE sẽ cố gắng tìm nhân viên từ assignment của các booking vừa tạo (bỏ assignment CANCELLED). Nếu tìm được, conversation được tạo và trả về.
  - Nếu chưa có assignment (chưa tìm thấy nhân viên), trường `conversation` có thể null; conversation sẽ được tạo khi có đủ thông tin nhân viên (qua booking thuộc recurring).

### Lấy conversation theo booking trong recurring
- `GET /api/v1/conversations/booking/{bookingId}`
  - Nếu booking thuộc recurring, BE trả conversation của recurring (tái sử dụng, không tạo mới mỗi booking).

## WebSocket
- Không thay đổi endpoint: `/ws/chat` (SockJS + STOMP).
- Topic chat per conversation: `/topic/conversation/{conversationId}` (đã dùng chung).
- Conversation của recurring booking chỉ có 1 conversationId dùng cho toàn bộ chuỗi.

## Data mẫu (init_sql)
- Customer: `c1000001-0000-0000-0000-000000000001`
- Employee: `e1000001-0000-0000-0000-000000000003`
- Booking id mẫu sinh từ recurring: `b0000001-0000-0000-0000-000000000010`, `...011`
- Conversation mẫu: `conv-rec-0001` (ví dụ)

## Tích hợp FE
1) Sau khi gọi create recurring booking, dùng `data.conversation` để:
   - Prefetch chat thread (`conversationId`).
   - Subscribe `/topic/conversation/{conversationId}` để nhận tin nhắn realtime.
2) Các booking con thuộc recurring (booking list) vẫn nên hiển thị entry “Chat” trỏ tới conversationId chung từ recurring.
3) Nếu response chưa có conversation (null), FE có thể gọi lại `GET /api/v1/conversations/booking/{bookingId}` khi booking đầu tiên có assignment để lấy conversationId, hoặc poll pending và cập nhật UI khi có. 
