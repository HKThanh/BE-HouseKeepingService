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
            "recurringBookingId": "dd1a0a33-8cb3-4e11-9a82-f9cffdeb65ee",
            "customerId": "c1000001-0000-0000-0000-000000000004",
            "customerName": "Nguyễn Văn An",
            "customer": {
                "customerId": "c1000001-0000-0000-0000-000000000004",
                "fullName": "Nguyễn Văn An",
                "avatar": "https://i.pravatar.cc/150?img=11",
                "email": "nguyenvanan@gmail.com",
                "phoneNumber": "0987654321",
                "isMale": true,
                "birthdate": "1995-03-15",
                "rating": null,
                "vipLevel": null
            },
            "address": {
                "addressId": "adrs0001-0000-0000-0000-000000000009",
                "fullAddress": "45 Nguyễn Huệ, Phường Phú An, Thành phố Hồ Chí Minh",
                "ward": "Phường Phú An",
                "city": "Thành phố Hồ Chí Minh",
                "latitude": 10.7743,
                "longitude": 106.7043,
                "isDefault": true
            },
            "recurrenceType": "WEEKLY",
            "recurrenceTypeDisplay": "Hàng tuần",
            "recurrenceDays": [
                1,
                2,
                4
            ],
            "recurrenceDaysDisplay": "Thứ 2, Thứ 3, Thứ 5",
            "bookingTime": "08:00:00",
            "startDate": "2025-11-27",
            "endDate": "2026-01-01",
            "note": "Vệ sinh định kỳ căn hộ 2 phòng ngủ",
            "title": "Dọn dẹp hàng tuần",
            "promotion": null,
            "recurringBookingDetails": [
                {
                    "bookingDetailId": "abd5fa1d-046e-4fac-a96d-9bac273c395c",
                    "service": {
                        "serviceId": 1,
                        "name": "Dọn dẹp theo giờ",
                        "description": "Lau dọn, hút bụi, làm sạch các bề mặt cơ bản trong nhà. Phù hợp cho nhu cầu duy trì vệ sinh hàng tuần.",
                        "basePrice": 50000.00,
                        "unit": "Giờ",
                        "estimatedDurationHours": 2.0,
                        "iconUrl": "https://res.cloudinary.com/dkzemgit8/image/upload/v1757599899/Cleaning_Clock_z29juh.png",
                        "categoryName": "Dọn dẹp nhà",
                        "isActive": true
                    },
                    "quantity": 1,
                    "pricePerUnit": 50000.00,
                    "formattedPricePerUnit": "50,000 đ",
                    "subTotal": 50000.00,
                    "formattedSubTotal": "50,000 đ",
                    "selectedChoices": [],
                    "assignments": [],
                    "duration": "2.0h",
                    "formattedDuration": "2.0h"
                }
            ],
            "assignedEmployeeId": "e1000001-0000-0000-0000-000000000020",
            "assignedEmployeeName": "Phạm Thị Dung Em",
            "status": "ACTIVE",
            "statusDisplay": "Đang hoạt động",
            "cancelledAt": null,
            "cancellationReason": null,
            "createdAt": "2025-11-26T21:43:06",
            "updatedAt": "2025-11-26T21:43:06",
            "totalGeneratedBookings": 3,
            "upcomingBookings": 3,
            "expectedBookingsInWindow": 4,
            "generatedBookingsInWindow": 3,
            "generationWindowDays": 7,
            "generationProgressPercent": 75.0
        },
        "generatedBookingIds": [
            "bf2f7c6a-6608-4fad-9417-b486105be6e9",
            "b6f25b01-2806-4246-8c33-3bd234e0ae5f",
            "7a294acf-f251-46d6-a0c8-eefda5ea8932"
        ],
        "totalBookingsToBeCreated": 4,
        "expectedBookingsInWindow": 4,
        "generatedBookingsInWindow": 3,
        "generationWindowDays": 7,
        "generationProgressPercent": 75.0,
        "conversation": {
            "conversationId": "190ed24d-cf48-426d-a79e-3188965775fa",
            "customerId": "c1000001-0000-0000-0000-000000000004",
            "customerName": "Nguyễn Văn An",
            "customerAvatar": "https://i.pravatar.cc/150?img=11",
            "employeeId": "e1000001-0000-0000-0000-000000000020",
            "employeeName": "Phạm Thị Dung Em",
            "employeeAvatar": "https://i.pravatar.cc/150?img=45",
            "bookingId": null,
            "recurringBookingId": "dd1a0a33-8cb3-4e11-9a82-f9cffdeb65ee",
            "lastMessage": "Xin chào. Tôi là Phạm Thị Dung Em sẽ đồng hành cùng lịch sử dụng dịch vụ Dọn dẹp theo giờ định kỳ của bạn tại 45 Nguyễn Huệ, Phường Phú An, Thành phố Hồ Chí Minh vào lúc 08:00 Thứ 2, Thứ 3, Thứ 5 mỗi tuần. Nếu bạn có câu hỏi, hãy nhắn tin tại đây.",
            "lastMessageTime": "2025-11-26T21:43:31.317952955",
            "isActive": true,
            "canChat": true,
            "createdAt": null,
            "updatedAt": null
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
