# Payload thông báo booking realtime

Tài liệu này mô tả chi tiết payload `NotificationWebSocketDTO` được đẩy qua WebSocket cho cả ứng dụng nhân viên lẫn khách hàng khi có sự kiện liên quan tới booking (tạo đơn, gán nhân viên, hủy đơn, duyệt bài, thanh toán, đánh giá). Ví dụ bên dưới tập trung nhiều vào flow nhân viên, nhưng cấu trúc payload giống hệt cho mọi role.

## Luồng & endpoint WebSocket
- Gateway STOMP: `/ws` (theo cấu hình `WebSocketConfig`).
- Nhân viên đăng ký hàng đợi cá nhân:  
  `SUBSCRIBE /user/{accountId}/EMPLOYEE/queue/notifications`
- Sau khi `BookingServiceImpl` lưu thành công assignment, từng nhân viên được gán sẽ nhận payload dưới đây. Thông tin được đồng thời lưu DB (bảng `notifications`) để app có thể đồng bộ theo REST nếu cần.

## Cấu trúc payload
| Trường | Kiểu | Ví dụ | Mô tả |
| --- | --- | --- | --- |
| `notificationId` | `string` | `"ntf12345-..."` | ID thông báo trong DB. Có thể dùng để đánh dấu đã đọc. |
| `accountId` | `string` | `"acc-2c8..."` | Account ID của nhân viên nhận thông báo. |
| `targetRole` | `string` | `"EMPLOYEE"` | Vai trò mục tiêu, dùng cho routing UI. |
| `type` | `string` | `"ASSIGNMENT_CREATED"` | Phân loại thông báo. Các giá trị nằm trong `Notification.NotificationType`. |
| `title` | `string` | `"Bạn có công việc mới"` | Tiêu đề hiển thị. |
| `message` | `string` | `"Bạn đã được phân công làm việc cho booking BK12345. Vui lòng xem chi tiết."` | Nội dung chi tiết, có thể hiển thị ngay cho người dùng. |
| `relatedId` | `string` | `"asg-09f..."` | ID liên quan. Với booking thì là `assignmentId`. |
| `relatedType` | `string` | `"ASSIGNMENT"` | Kiểu entity liên quan (`BOOKING`, `ASSIGNMENT`, `PAYMENT`, …). |
| `priority` | `string` | `"HIGH"` | Độ ưu tiên (LOW/NORMAL/HIGH/URGENT). Nhân viên có thể lọc/sắp xếp từ trường này. |
| `actionUrl` | `string` | `"/assignments/asg-09f..."` | Đường dẫn deep-link để mở màn hình chi tiết. |
| `createdAt` | `string (ISO-8601)` | `"2025-11-18T10:45:12.123"` | Thời gian hệ thống tạo thông báo. Dùng để sắp xếp và hiển thị “time ago”. |

## Sample payload
```json
{
  "notificationId": "ntf12345-0000-0000-0000-000000987654",
  "accountId": "acc-a1b2c3",
  "targetRole": "EMPLOYEE",
  "type": "ASSIGNMENT_CREATED",
  "title": "Bạn có công việc mới",
  "message": "Bạn đã được phân công làm việc cho booking BK45291. Vui lòng xem chi tiết.",
  "relatedId": "asg-79f0e1",
  "relatedType": "ASSIGNMENT",
  "priority": "HIGH",
  "actionUrl": "/assignments/asg-79f0e1",
  "createdAt": "2025-11-18T10:45:12.123"
}
```

### Mapping sự kiện -> Notification

| Sự kiện | Service phát sinh | `targetRole` | `type` | Nội dung |
| --- | --- | --- | --- | --- |
| Booking được tạo (khách chọn nhân viên hoặc hệ thống auto-assign) | `BookingServiceImpl.createBooking` | `CUSTOMER` | `BOOKING_CREATED` | Báo cho khách biết mã booking và trạng thái “đang chờ xác minh”. |
| Booking khách hủy | `BookingServiceImpl.cancelBooking` | `CUSTOMER` | `BOOKING_CANCELLED` | Thông báo đã hủy + lý do mà khách nhập. |
| Bài post được admin duyệt / từ chối | `BookingServiceImpl.verifyBooking` | `CUSTOMER` | `BOOKING_VERIFIED` / `BOOKING_REJECTED` | Báo kết quả duyệt và nhắc khách kiểm tra chi tiết. |
| Nhân viên được phân công | `BookingServiceImpl.createBooking` | `EMPLOYEE` | `ASSIGNMENT_CREATED` | WebSocket tới nhân viên về job mới. |
| Nhân viên tự hủy assignment (khủng hoảng) | `AssignmentServiceImpl.cancelAssignment` | `CUSTOMER` | `ASSIGNMENT_CRISIS` | Báo khách rằng nhân viên vừa hủy cùng lý do để khách chủ động xử lý. |
| Thanh toán thành công | `PaymentServiceImpl.updatePaymentStatus` | `CUSTOMER` | `PAYMENT_SUCCESS` | Hiển thị số tiền đã thanh toán và mã booking. |
| Khách gửi đánh giá nhân viên | `ReviewServiceImpl.createReview` | `EMPLOYEE` | `REVIEW_RECEIVED` | Báo số sao (làm tròn) để nhân viên biết có đánh giá mới. |

### Sample payload bổ sung

**Booking được tạo (CUSTOMER)**
```json
{
  "notificationId": "ntfBOOK-001",
  "accountId": "acc-customer-01",
  "targetRole": "CUSTOMER",
  "type": "BOOKING_CREATED",
  "title": "Đặt lịch thành công",
  "message": "Booking BK78901 của bạn đã được tạo thành công và đang chờ xác minh.",
  "relatedId": "booking-78901",
  "relatedType": "BOOKING",
  "priority": "NORMAL",
  "actionUrl": "/bookings/booking-78901",
  "createdAt": "2025-11-18T11:30:00.000"
}
```

**Nhân viên hủy assignment (CUSTOMER)**
```json
{
  "notificationId": "ntfCRISIS-001",
  "accountId": "acc-customer-02",
  "targetRole": "CUSTOMER",
  "type": "ASSIGNMENT_CRISIS",
  "title": "KHẨN CẤP: Nhân viên hủy công việc",
  "message": "Nhân viên đã hủy công việc cho booking BK32100. Lý do: Nghỉ ốm đột xuất.",
  "relatedId": "booking-32100",
  "relatedType": "BOOKING",
  "priority": "URGENT",
  "actionUrl": "/bookings/booking-32100",
  "createdAt": "2025-11-18T13:05:45.000"
}
```

## Lưu ý triển khai phía client
1. **Xác thực session**: Client (cả customer/employee) cần gửi header Authorization (Bearer token) ngay từ khung CONNECT để server ánh xạ được `accountId`.  
2. **Đăng ký đúng hàng đợi**: Customer sử dụng `/user/{accountId}/CUSTOMER/queue/notifications`, nhân viên dùng `/user/{accountId}/EMPLOYEE/queue/notifications`.  
3. **Phân trang đồng bộ**: Nếu người dùng offline, vẫn có thể gọi REST `GET /notifications?role=...` để đồng bộ lại vì payload WebSocket tương đồng với response.  
4. **Đánh dấu đã đọc**: Khi người dùng mở màn hình detail, sử dụng `PATCH /notifications/{notificationId}/read` để cập nhật trạng thái.  
5. **Backup channel**: Trong trường hợp mất kết nối WebSocket, client nên chủ động refetch định kỳ bằng REST để tránh bỏ lỡ sự kiện quan trọng (hủy assignment, thanh toán...).  
