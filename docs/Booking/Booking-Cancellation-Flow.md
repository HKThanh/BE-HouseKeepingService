# Luồng Hủy Booking - Customer

## Tổng quan
Tài liệu này mô tả chi tiết luồng xử lý khi khách hàng hủy một đơn đặt dịch vụ (booking).

## Kiến trúc

```
BookingController
    ↓ (gọi)
BookingService.cancelBooking()
    ↓ (xử lý)
├── Booking (cập nhật status)
├── Assignments (hủy các assignment)
└── Payments (xử lý hoàn tiền/hủy)
```

## Các file đã được triển khai

### 1. Controller Layer
**File:** `BookingController.java`
- **Endpoint:** `PUT /api/v1/customer/bookings/{bookingId}/cancel`
- **Method:** `cancelBooking()`
- **Chức năng:**
  - Xác thực JWT token
  - Trích xuất customer ID từ token
  - Gọi service layer để xử lý
  - Trả về response

### 2. Service Layer
**File:** `BookingServiceImpl.java`
- **Method:** `cancelBooking(String bookingId, String customerId, String reason)`
- **Chức năng chính:**
  1. Tìm booking theo ID
  2. Xác minh quyền sở hữu
  3. Kiểm tra trạng thái có thể hủy
  4. Cập nhật status booking
  5. Lưu lý do hủy
  6. Hủy tất cả assignments
  7. Xử lý payments (refund/cancel)
  8. Trả về kết quả

### 3. DTO Layer
**File:** `BookingCancelRequest.java`
```java
public record BookingCancelRequest(
    @Size(max = 500, message = "Lý do hủy không được quá 500 ký tự")
    String reason
)
```

### 4. Documentation
**File:** `API-Cancel-Booking-Customer.md`
- Chi tiết API specification
- Request/Response examples
- Test cases
- Business logic
- TODO items

## Sequence Diagram

```
Customer -> BookingController: PUT /cancel + JWT
BookingController -> JwtUtil: extractUsername(token)
JwtUtil --> BookingController: username
BookingController -> CustomerRepository: findByAccount_Username(username)
CustomerRepository --> BookingController: Customer
BookingController -> BookingService: cancelBooking(bookingId, customerId, reason)
BookingService -> BookingRepository: findById(bookingId)
BookingRepository --> BookingService: Booking
BookingService -> BookingService: verify ownership
BookingService -> BookingService: check status
BookingService -> Booking: setStatus(CANCELLED)
BookingService -> Booking: setAdminComment(reason)
BookingService -> AssignmentRepository: findByBookingDetailId()
AssignmentRepository --> BookingService: List<Assignment>
BookingService -> Assignment: setStatus(CANCELLED)
BookingService -> PaymentRepository: findByBookingId()
PaymentRepository --> BookingService: List<Payment>
BookingService -> Payment: setStatus(REFUNDED/CANCELED)
BookingService -> BookingRepository: save(booking)
BookingRepository --> BookingService: Booking
BookingService -> BookingMapper: toBookingResponse(booking)
BookingMapper --> BookingService: BookingResponse
BookingService --> BookingController: BookingResponse
BookingController --> Customer: 200 OK + Response
```

## Trạng thái Booking

### Các trạng thái có thể hủy
- ✅ `PENDING` - Đang chờ xác nhận
- ✅ `AWAITING_EMPLOYEE` - Đang chờ nhân viên (booking post)
- ✅ `CONFIRMED` - Đã xác nhận

### Các trạng thái KHÔNG được hủy
- ❌ `IN_PROGRESS` - Đang thực hiện
- ❌ `COMPLETED` - Đã hoàn thành
- ❌ `CANCELLED` - Đã hủy

## Flow Chart

```
START
  ↓
[Nhận request từ customer]
  ↓
[Xác thực JWT token] ──No──> [Return 401 Unauthorized]
  ↓ Yes
[Lấy customer ID từ token]
  ↓
[Tìm booking theo ID] ──Not Found──> [Return 404]
  ↓ Found
[Kiểm tra ownership] ──No──> [Return 400: Không có quyền]
  ↓ Yes
[Kiểm tra status có thể hủy?]
  ├──> CANCELLED ──> [Return 400: Đã hủy]
  ├──> COMPLETED ──> [Return 400: Không thể hủy]
  ├──> IN_PROGRESS ──> [Return 400: Không thể hủy]
  └──> PENDING/CONFIRMED/AWAITING_EMPLOYEE
         ↓ OK
[Cập nhật booking status = CANCELLED]
  ↓
[Lưu lý do hủy vào adminComment]
  ↓
[Tìm tất cả BookingDetails]
  ↓
FOR EACH BookingDetail
  ↓
  [Tìm tất cả Assignments]
    ↓
  FOR EACH Assignment
    ↓
    [Status != COMPLETED?] ──Yes──> [Set status = CANCELLED]
    ↓
  END FOR
  ↓
END FOR
  ↓
[Tìm tất cả Payments]
  ↓
FOR EACH Payment
  ├──> Status = PAID ──> [Set status = REFUNDED]
  ├──> Status = PENDING ──> [Set status = CANCELED]
  └──> Other ──> [No change]
END FOR
  ↓
[Save booking to database]
  ↓
[Convert to BookingResponse]
  ↓
[Return 200 OK + Response]
  ↓
END
```

## Chi tiết xử lý

### 1. Xác thực và Phân quyền
```java
// 1. Validate JWT token
String token = authHeader.substring(7);
String username = jwtUtil.extractUsername(token);
jwtUtil.validateToken(token, username);

// 2. Get customer from username
Customer customer = customerRepository.findByAccount_Username(username)
    .orElseThrow();

// 3. Check ownership
if (!booking.getCustomer().getCustomerId().equals(customerId)) {
    throw BookingValidationException.singleError("Bạn không có quyền hủy booking này");
}
```

### 2. Kiểm tra trạng thái
```java
BookingStatus currentStatus = booking.getStatus();

// Not allowed statuses
if (currentStatus == BookingStatus.CANCELLED) {
    throw BookingValidationException.singleError("Booking đã bị hủy trước đó");
}
if (currentStatus == BookingStatus.COMPLETED) {
    throw BookingValidationException.singleError("Không thể hủy booking đã hoàn thành");
}
if (currentStatus == BookingStatus.IN_PROGRESS) {
    throw BookingValidationException.singleError("Không thể hủy booking đang thực hiện");
}
```

### 3. Cập nhật Booking
```java
// Update status
booking.setStatus(BookingStatus.CANCELLED);

// Save cancellation reason
String cancelNote = reason != null && !reason.trim().isEmpty()
    ? "Khách hàng hủy: " + reason.trim()
    : "Khách hàng hủy booking";

if (booking.getAdminComment() != null) {
    booking.setAdminComment(booking.getAdminComment() + " | " + cancelNote);
} else {
    booking.setAdminComment(cancelNote);
}
```

### 4. Hủy Assignments
```java
for (BookingDetail detail : bookingDetails) {
    List<Assignment> assignments = assignmentRepository.findByBookingDetailId(detail.getId());
    
    for (Assignment assignment : assignments) {
        if (assignment.getStatus() != AssignmentStatus.CANCELLED && 
            assignment.getStatus() != AssignmentStatus.COMPLETED) {
            
            assignment.setStatus(AssignmentStatus.CANCELLED);
            assignmentRepository.save(assignment);
            
            log.info("Cancelled assignment {} for employee {}", 
                assignment.getAssignmentId(), 
                assignment.getEmployee().getEmployeeId());
        }
    }
}
```

### 5. Xử lý Payments
```java
List<Payment> payments = paymentRepository.findByBookingIdOrderByCreatedAtDesc(bookingId);

for (Payment payment : payments) {
    if (payment.getPaymentStatus() == PaymentStatus.PAID) {
        // Mark for refund
        payment.setPaymentStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(payment);
        log.info("Marked payment {} for refund", payment.getId());
        
    } else if (payment.getPaymentStatus() == PaymentStatus.PENDING) {
        // Cancel pending payment
        payment.setPaymentStatus(PaymentStatus.CANCELED);
        paymentRepository.save(payment);
        log.info("Cancelled pending payment {}", payment.getId());
    }
}
```

## Database Schema Impact

### Bảng: `bookings`
| Column | Before | After | Note |
|--------|--------|-------|------|
| status | PENDING/CONFIRMED/AWAITING_EMPLOYEE | CANCELLED | Updated |
| admin_comment | NULL or existing | "Khách hàng hủy: {reason}" | Appended |
| updated_at | old timestamp | current timestamp | Auto-updated |

### Bảng: `assignments`
| Column | Before | After | Note |
|--------|--------|-------|------|
| status | ASSIGNED | CANCELLED | For non-completed assignments |

### Bảng: `payments`
| Column | Before | After | Note |
|--------|--------|-------|------|
| payment_status | PAID | REFUNDED | Chờ hoàn tiền |
| payment_status | PENDING | CANCELED | Hủy thanh toán |

## Exception Handling

### BookingNotFoundException
- **Trigger:** Không tìm thấy booking với ID
- **HTTP Status:** 404 Not Found
- **Message:** "Booking not found with ID: {bookingId}"

### BookingValidationException
- **Trigger:** Các điều kiện validation fail
- **HTTP Status:** 400 Bad Request
- **Messages:**
  - "Bạn không có quyền hủy booking này"
  - "Booking đã bị hủy trước đó"
  - "Không thể hủy booking đã hoàn thành"
  - "Không thể hủy booking đang thực hiện"

### RuntimeException
- **Trigger:** Lỗi không xác định
- **HTTP Status:** 500 Internal Server Error
- **Message:** "Đã xảy ra lỗi khi hủy booking"

## Testing

### Unit Tests (TODO)
```java
@Test
void testCancelBooking_Success() {
    // Given: valid booking, customer is owner, status is PENDING
    // When: cancelBooking()
    // Then: booking status = CANCELLED, assignments cancelled, payments handled
}

@Test
void testCancelBooking_NotOwner() {
    // Given: valid booking, customer is NOT owner
    // When: cancelBooking()
    // Then: throw BookingValidationException
}

@Test
void testCancelBooking_AlreadyCancelled() {
    // Given: booking status = CANCELLED
    // When: cancelBooking()
    // Then: throw BookingValidationException
}
```

### Integration Tests (TODO)
- Test with real database
- Test transaction rollback
- Test concurrent cancellations
- Test notification sending (when implemented)

## Monitoring & Logging

### Log Levels
- **INFO:** Normal operations
  - "Customer {customerId} cancelling booking {bookingId}"
  - "Cancelled assignment {assignmentId} for employee {employeeId}"
  - "Marked payment {paymentId} for refund"
  - "Booking {bookingId} cancelled successfully by customer {customerId}"

- **WARN:** Non-critical issues
  - "Booking {bookingId} is already cancelled"
  - "Cannot cancel completed booking {bookingId}"
  - "Cannot cancel in-progress booking {bookingId}"

- **ERROR:** Critical issues
  - "Booking {bookingId} not found"
  - "Customer {customerId} tried to cancel booking {bookingId} which belongs to customer {otherCustomerId}"

### Metrics to Monitor
- Cancellation rate by status
- Average time between booking creation and cancellation
- Refund processing time
- Failed cancellation attempts

## Future Enhancements

### 1. Cancellation Policy
- Phí hủy dựa trên thời gian
- Hoàn tiền theo policy
- Grace period cho customer

### 2. Notification System
- Email/SMS cho customer
- Push notification cho employee
- Admin notification dashboard

### 3. Auto Refund
- Tích hợp payment gateway
- Automatic refund processing
- Refund tracking

### 4. Employee Compensation
- Compensation cho employee nếu hủy muộn
- Point system cho affected employees

### 5. Analytics
- Dashboard hủy booking
- Lý do hủy phổ biến
- Customer behavior analysis

## Dependencies

### Repositories
- `BookingRepository`
- `CustomerRepository`
- `AssignmentRepository`
- `PaymentRepository`

### Services
- `JwtUtil` - JWT token handling
- `BookingMapper` - Entity to DTO mapping

### Models
- `Booking`
- `Customer`
- `BookingDetail`
- `Assignment`
- `Payment`

### Enums
- `BookingStatus`
- `AssignmentStatus`
- `PaymentStatus`

### DTOs
- `BookingCancelRequest`
- `BookingResponse`

### Exceptions
- `BookingNotFoundException`
- `BookingValidationException`

## Changelog

### Version 1.0 (2025-10-26)
- ✅ Initial implementation
- ✅ Basic cancellation logic
- ✅ Assignment cancellation
- ✅ Payment refund marking
- ✅ API documentation
- ⏳ Notification system (TODO)
- ⏳ Auto refund processing (TODO)
- ⏳ Cancellation policy (TODO)
