# API Hủy Booking - Phía Customer

## Tổng quan
API này cho phép khách hàng hủy booking đã đặt trước đó. Hệ thống sẽ xử lý việc hủy các assignment liên quan và hoàn tiền nếu có thanh toán.

## Endpoint

### Cancel Booking
**PUT** `/api/v1/customer/bookings/{bookingId}/cancel`

#### Request Headers
```
Authorization: Bearer {jwt_token}
Content-Type: application/json
```

#### Path Parameters
| Tham số | Kiểu | Bắt buộc | Mô tả |
|---------|------|----------|-------|
| bookingId | String | Có | ID của booking cần hủy |

#### Request Body
```json
{
  "reason": "Lý do hủy booking (tối đa 500 ký tự)"
}
```

**Validation Rules:**
- `reason`: Optional, tối đa 500 ký tự

#### Response Success (200 OK)
```json
{
  "success": true,
  "message": "Hủy booking thành công",
  "data": {
    "bookingId": "uuid-string",
    "bookingCode": "BK12345678901",
    "customerId": "uuid-string",
    "addressId": "uuid-string",
    "bookingTime": "2025-10-27T10:00:00",
    "note": "Ghi chú booking",
    "totalAmount": 500000.00,
    "status": "CANCELLED",
    "title": null,
    "imageUrl": null,
    "isVerified": true,
    "adminComment": "Khách hàng hủy: Thay đổi kế hoạch",
    "createdAt": "2025-10-26T08:00:00",
    "updatedAt": "2025-10-26T14:30:00",
    "bookingDetails": [...],
    "payments": [...]
  }
}
```

#### Response Errors

**400 Bad Request - Token không hợp lệ**
```json
{
  "success": false,
  "message": "Token không hợp lệ"
}
```

**401 Unauthorized - Token hết hạn**
```json
{
  "success": false,
  "message": "Token không hợp lệ"
}
```

**400 Bad Request - Không có quyền hủy**
```json
{
  "success": false,
  "message": "Bạn không có quyền hủy booking này"
}
```

**400 Bad Request - Booking đã bị hủy**
```json
{
  "success": false,
  "message": "Booking đã bị hủy trước đó"
}
```

**400 Bad Request - Booking đã hoàn thành**
```json
{
  "success": false,
  "message": "Không thể hủy booking đã hoàn thành"
}
```

**400 Bad Request - Booking đang thực hiện**
```json
{
  "success": false,
  "message": "Không thể hủy booking đang thực hiện"
}
```

**404 Not Found - Không tìm thấy booking**
```json
{
  "success": false,
  "message": "Booking not found with ID: {bookingId}"
}
```

**500 Internal Server Error**
```json
{
  "success": false,
  "message": "Đã xảy ra lỗi khi hủy booking"
}
```

## Quy trình xử lý

### 1. Xác thực
- Kiểm tra JWT token trong header
- Trích xuất username từ token
- Lấy thông tin customer từ username

### 2. Kiểm tra quyền
- Tìm booking theo bookingId
- Xác minh booking thuộc về customer đang request
- Từ chối nếu customer không phải chủ sở hữu

### 3. Kiểm tra trạng thái booking
Chỉ được phép hủy booking với các trạng thái sau:
- ✅ `PENDING` - Đang chờ xác nhận
- ✅ `AWAITING_EMPLOYEE` - Đang chờ nhân viên (booking post)
- ✅ `CONFIRMED` - Đã xác nhận
- ❌ `IN_PROGRESS` - Không được hủy khi đang thực hiện
- ❌ `COMPLETED` - Không được hủy khi đã hoàn thành
- ❌ `CANCELLED` - Đã hủy rồi

### 4. Cập nhật booking
- Đổi status thành `CANCELLED`
- Lưu lý do hủy vào `adminComment`
- Cập nhật `updatedAt`

### 5. Hủy assignments liên quan
Với mỗi BookingDetail trong booking:
- Tìm tất cả assignments
- Đổi status của assignment thành `CANCELLED` (nếu chưa COMPLETED)
- Lưu thông tin assignment đã hủy
- Log thông tin employee bị ảnh hưởng

### 6. Xử lý thanh toán
Với mỗi Payment của booking:
- **Nếu status = PAID**: Đổi thành `REFUNDED` (đánh dấu chờ hoàn tiền)
- **Nếu status = PENDING**: Đổi thành `CANCELED` (hủy giao dịch)
- **Các status khác**: Không thay đổi
- Log thông tin payment đã xử lý

### 7. Lưu và trả về
- Lưu tất cả thay đổi vào database (Transaction)
- Convert Booking entity thành BookingResponse
- Trả về response thành công

## Business Logic

### Điều kiện hủy booking
```java
// Chỉ được hủy nếu
currentStatus != CANCELLED 
  && currentStatus != COMPLETED 
  && currentStatus != IN_PROGRESS
```

### Xử lý lý do hủy
```java
if (reason != null && !reason.trim().isEmpty()) {
    cancelNote = "Khách hàng hủy: " + reason.trim();
} else {
    cancelNote = "Khách hàng hủy booking";
}

// Append vào adminComment hiện có (nếu có)
if (existingAdminComment != null) {
    adminComment = existingAdminComment + " | " + cancelNote;
} else {
    adminComment = cancelNote;
}
```

### Xử lý assignments
```java
for (BookingDetail detail : bookingDetails) {
    List<Assignment> assignments = findByBookingDetailId(detail.getId());
    for (Assignment assignment : assignments) {
        if (assignment.status != CANCELLED 
            && assignment.status != COMPLETED) {
            assignment.status = CANCELLED;
            save(assignment);
            // Log info
        }
    }
}
```

### Xử lý payments
```java
for (Payment payment : payments) {
    switch (payment.status) {
        case PAID:
            payment.status = REFUNDED;
            break;
        case PENDING:
            payment.status = CANCELED;
            break;
        // Các status khác không xử lý
    }
    save(payment);
}
```

## Security

### Authorization
- **Required Role**: `ROLE_CUSTOMER` hoặc `ROLE_ADMIN`
- Sử dụng `@PreAuthorize("hasAnyRole('ROLE_CUSTOMER')")`
- Customer chỉ được hủy booking của chính mình

### Validation
- Kiểm tra JWT token hợp lệ
- Kiểm tra customer ownership
- Kiểm tra booking status có thể hủy
- Validate lý do hủy (max 500 ký tự)

## Transaction Management

- Sử dụng `@Transactional` annotation
- Tất cả operations trong cùng 1 transaction
- Rollback nếu có bất kỳ lỗi nào xảy ra

## Logging

```java
log.info("Customer {} cancelling booking {}", customerId, bookingId);
log.warn("Booking {} is already cancelled", bookingId);
log.error("Customer {} tried to cancel booking {} which belongs to customer {}", ...);
log.info("Cancelled assignment {} for employee {}", assignmentId, employeeId);
log.info("Marked payment {} for refund", paymentId);
log.info("Booking {} cancelled successfully by customer {}", bookingId, customerId);
```

## TODO - Features chưa implement

### 1. Notification System
```java
// TODO: Send notification to assigned employees about cancellation
// - Email/SMS thông báo booking bị hủy
// - Thông báo trong app cho employee
// - Include thông tin booking và lý do hủy
```

### 2. Refund Processing
```java
// TODO: Process actual refund through payment gateway
// - Tích hợp với VNPay/MoMo/ZaloPay
// - Xử lý refund request
// - Tracking refund status
// - Notification khi refund thành công
```

### 3. Employee Schedule Update
```java
// TODO: Update employee schedules if needed
// - Cập nhật lịch làm việc của employee
// - Release time slots đã được assigned
// - Notification cho employee về lịch trống
```

### 4. Cancellation Policy
```java
// TODO: Implement cancellation policy
// - Phí hủy dựa trên thời gian hủy
// - Hoàn tiền 100% nếu hủy trước 24h
// - Hoàn tiền 50% nếu hủy trước 12h
// - Không hoàn tiền nếu hủy trong 12h
```

## Test Cases

### TC001: Hủy booking thành công
**Input:**
- bookingId: valid UUID
- customer: owner của booking
- status: PENDING hoặc CONFIRMED
- reason: "Thay đổi kế hoạch"

**Expected:**
- Response 200 OK
- Booking status = CANCELLED
- adminComment chứa lý do hủy
- Assignments được hủy
- Payments được xử lý

### TC002: Hủy booking không phải của mình
**Input:**
- bookingId: valid UUID
- customer: NOT owner

**Expected:**
- Response 400 Bad Request
- Message: "Bạn không có quyền hủy booking này"

### TC003: Hủy booking đã bị hủy
**Input:**
- bookingId: valid UUID
- status: CANCELLED

**Expected:**
- Response 400 Bad Request
- Message: "Booking đã bị hủy trước đó"

### TC004: Hủy booking đã hoàn thành
**Input:**
- bookingId: valid UUID
- status: COMPLETED

**Expected:**
- Response 400 Bad Request
- Message: "Không thể hủy booking đã hoàn thành"

### TC005: Hủy booking đang thực hiện
**Input:**
- bookingId: valid UUID
- status: IN_PROGRESS

**Expected:**
- Response 400 Bad Request
- Message: "Không thể hủy booking đang thực hiện"

### TC006: Hủy booking không tồn tại
**Input:**
- bookingId: invalid UUID

**Expected:**
- Response 404 Not Found
- Message: "Booking not found with ID: {bookingId}"

### TC007: Token không hợp lệ
**Input:**
- Authorization header: invalid token

**Expected:**
- Response 400/401
- Message: "Token không hợp lệ"

### TC008: Hủy booking không có lý do
**Input:**
- bookingId: valid UUID
- reason: null hoặc empty

**Expected:**
- Response 200 OK
- adminComment: "Khách hàng hủy booking"

### TC009: Hủy booking với payment PAID
**Input:**
- bookingId: valid UUID
- có payment với status = PAID

**Expected:**
- Response 200 OK
- Payment status = REFUNDED

### TC010: Hủy booking với payment PENDING
**Input:**
- bookingId: valid UUID
- có payment với status = PENDING

**Expected:**
- Response 200 OK
- Payment status = CANCELED

## Database Impact

### Tables Updated

1. **bookings**
   - status → CANCELLED
   - admin_comment → thêm lý do hủy
   - updated_at → current timestamp

2. **assignments**
   - status → CANCELLED (cho các assignment chưa COMPLETED)

3. **payments**
   - payment_status → REFUNDED (nếu PAID)
   - payment_status → CANCELED (nếu PENDING)

### No Changes To
- booking_details
- customers
- employees
- addresses
- services

## Performance Considerations

- Query optimization: Eager loading cho bookingDetails
- Batch update cho multiple assignments
- Index trên booking_id, customer_id, status
- Transaction timeout configuration

## Error Handling

Tất cả exceptions được xử lý bởi `GlobalExceptionHandler`:
- `BookingNotFoundException` → 404
- `BookingValidationException` → 400
- `RuntimeException` → 500

## API Usage Example

### cURL
```bash
curl -X PUT "http://localhost:8080/api/v1/customer/bookings/{bookingId}/cancel" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "reason": "Tôi có việc đột xuất không thể sắp xếp được"
  }'
```

### JavaScript (Axios)
```javascript
const cancelBooking = async (bookingId, reason) => {
  try {
    const response = await axios.put(
      `/api/v1/customer/bookings/${bookingId}/cancel`,
      { reason },
      {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      }
    );
    return response.data;
  } catch (error) {
    console.error('Cancel booking failed:', error.response.data);
    throw error;
  }
};
```

### Java (Spring RestTemplate)
```java
HttpHeaders headers = new HttpHeaders();
headers.setBearerAuth(jwtToken);
headers.setContentType(MediaType.APPLICATION_JSON);

BookingCancelRequest request = new BookingCancelRequest("Thay đổi kế hoạch");

HttpEntity<BookingCancelRequest> entity = new HttpEntity<>(request, headers);

ResponseEntity<Map> response = restTemplate.exchange(
    "/api/v1/customer/bookings/" + bookingId + "/cancel",
    HttpMethod.PUT,
    entity,
    Map.class
);
```

## Related APIs
- [Create Booking](./API-TestCases-Booking.md)
- [Get Booking Details](./API-TestCases-Booking.md)
- [Customer Bookings History](./API-TestCases-Customer-Bookings.md)
- [Payment APIs](../Payment/API-TestCases-Payment.md)
