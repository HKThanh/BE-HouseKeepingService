# Notifications - Luồng hoàn tất & thanh toán (21-11-2025)

Tài liệu cho FE về push notification sau khi nhân viên checkout (hoàn tất công việc) và sau khi thanh toán thành công (nhắc đánh giá nhân viên).

## 1) Sự kiện: Nhân viên check-out xong, booking hoàn tất
- Service: AssignmentServiceImpl.checkOut
- Khi tất cả assignment của booking `COMPLETED`, hệ thống gửi notification cho customer.
- Notification:
  - Type: `BOOKING_COMPLETED`
  - Title: `Công việc đã hoàn tất`
  - Message: `Nhân viên đã hoàn tất công việc cho booking {bookingCode}. Vui lòng kiểm tra và tiến hành thanh toán.`
  - Action URL: `/bookings/{bookingId}`
  - Related: `relatedId = bookingId`, `relatedType = BOOKING`
  - Priority: `HIGH`
  - Target role: `CUSTOMER`
- Cách nhận realtime: subscribe socket `/topic/notifications/{accountId}` (đã dùng chung notification WS trước đó).

### API phía employee (điểm danh kết thúc)
- `POST /api/v1/employee/assignments/{assignmentId}/check-out`
- Form data:
  - `request` (JSON string AssignmentCheckOutRequest): `{ "employeeId": "e...", "imageDescription": "optional" }`
  - `images` (optional, max 10, content-type image/*)
- Response 200:
  ```json
  {
    "success": true,
    "message": "Chấm công kết thúc công việc thành công",
    "data": {
      "assignmentId": "as000001-...",
      "bookingCode": "BK000123",
      "serviceName": "...",
      "customerName": "...",
      "customerPhone": "...",
      "address": "...",
      "bookingTime": "2025-11-21T10:00:00",
      "estimatedDurationHours": 2.0,
      "pricePerUnit": 120000,
      "quantity": 1,
      "subTotal": 120000,
      "status": "COMPLETED",
      "checkInTime": "2025-11-21T10:02:00",
      "checkOutTime": "2025-11-21T12:05:00",
      "note": null
    }
  }
  ```
- Lưu ý: FE không cần xử lý gì thêm để bắn notification, BE tự gửi khi booking đủ điều kiện hoàn tất.

## 2) Sự kiện: Thanh toán thành công → nhắc đánh giá nhân viên
- Service: PaymentServiceImpl.updatePaymentStatus -> dispatchPaymentSuccessNotification
- Khi status cập nhật lên `PAID` lần đầu:
  - Gửi notification thanh toán (đã có): `PAYMENT_SUCCESS`.
  - Gửi thêm notification nhắc đánh giá:
    - Type: `REVIEW_REQUEST`
    - Title: `Đánh giá nhân viên`
    - Message: `Thanh toán thành công. Vui lòng đánh giá nhân viên ({employeeNames}) cho booking {bookingCode}.`
      - `employeeNames` ghép từ danh sách assignment của booking (bỏ trạng thái CANCELLED).
    - Action URL: `/bookings/{bookingId}/review`
    - Related: `relatedId = bookingId`, `relatedType = BOOKING`
    - Priority: `NORMAL`
    - Target role: `CUSTOMER`
- Cách nhận realtime: subscribe `/topic/notifications/{accountId}`.

### API cập nhật trạng thái thanh toán
- Webhook/payment update: `POST /api/v1/payments/status` (tuỳ integration), body (ví dụ):
  ```json
  {
    "transactionCode": "TXN123",
    "status": "PAID"
  }
  ```
- Nếu chuyển `PAID` lần đầu, BE tự gửi 2 notification:
  1) `PAYMENT_SUCCESS` (đã có).
  2) `REVIEW_REQUEST` (mới thêm).

## WebSocket endpoint
- Notifications: `/ws/notifications` (SockJS + STOMP), broker `/topic`.
- Subscribe pattern: `/topic/notifications/{accountId}`.
- Payload mẫu (NotificationResponse):
  ```json
  {
    "notificationId": "ntf0001-...",
    "accountId": "a1000001-...",
    "targetRole": "CUSTOMER",
    "type": "BOOKING_COMPLETED",
    "title": "Công việc đã hoàn tất",
    "message": "Nhân viên đã hoàn tất công việc cho booking BK000123. Vui lòng kiểm tra và tiến hành thanh toán.",
    "relatedId": "bk000123-...",
    "relatedType": "BOOKING",
    "priority": "HIGH",
    "actionUrl": "/bookings/bk000123-...",
    "isRead": false,
    "createdAt": "2025-11-21T12:05:10"
  }
  ```

## Data mẫu (khớp init_sql)
- Customer accountId: `a1000001-0000-0000-0000-000000000001`
- Booking code: `BK000123` (ví dụ), bookingId `bk000123-...`
- Employee names: lấy từ assignment theo booking, bỏ các assignment `CANCELLED`.

## Tóm tắt tham số FE cần
- Subscribe WS: `/topic/notifications/{accountId}`.
- Đọc `type`:
  - `BOOKING_COMPLETED`: hiển thị banner hoàn tất + CTA thanh toán (`actionUrl`).
  - `REVIEW_REQUEST`: hiển thị banner nhắc đánh giá + CTA review.
- REST không đổi; BE tự đẩy notification khi đủ điều kiện (checkout đủ -> completed, payment chuyển PAID). 
