# API thay đổi ngày 2025-12-16: Hình ảnh Assignment và tọa độ Check-in/Check-out

Tài liệu này mô tả đầy đủ các API đã thay đổi để Front End có thể tích hợp.

## 1) Thay đổi cơ sở dữ liệu

### 1.1. Migration mới

- File migration: `postgres_data/init_sql/20_add_assignment_coordinates.sql`
- Mục tiêu: thêm tọa độ check-in và check-out cho bảng `assignments`.

### 1.2. Cột mới trong bảng `assignments`

Bốn cột mới (giá trị có thể rỗng):

- `check_in_latitude` (DOUBLE PRECISION): vĩ độ khi nhân viên check-in
- `check_in_longitude` (DOUBLE PRECISION): kinh độ khi nhân viên check-in
- `check_out_latitude` (DOUBLE PRECISION): vĩ độ khi nhân viên check-out
- `check_out_longitude` (DOUBLE PRECISION): kinh độ khi nhân viên check-out

### 1.3. Truy vấn kiểm tra dữ liệu mẫu

Bạn có thể sử dụng dữ liệu seed trong `postgres_data/init_sql/A01_seed_check_in_assignments.sql`.

## 2) API Assignment: danh sách và các thao tác trả về media và tọa độ

Tất cả các API trả về `AssignmentDetailResponse` đều đã thay đổi output: bổ sung danh sách hình ảnh `media` và 4 trường tọa độ.

### 2.1. GET danh sách công việc của nhân viên

- Method: `GET`
- URL: `/api/v1/employee/{employeeId}/assignments`
- Authorization: yêu cầu Bearer token; quyền `ROLE_EMPLOYEE` hoặc `ROLE_ADMIN`

#### Input

- Path parameter:
  - `employeeId` (string): mã nhân viên
- Query parameters:
  - `status` (string, tùy chọn): trạng thái assignment
  - `page` (integer, mặc định 0)
  - `size` (integer, mặc định 10)

#### Output

- HTTP 200

```json
{
  "success": true,
  "message": "Lấy danh sách công việc thành công",
  "data": [
    {
      "assignmentId": "as000001-0000-0000-0000-000000100001",
      "bookingCode": "BKCI00001",
      "serviceName": "Dọn dẹp theo giờ",
      "customerName": "Nguyễn Văn A",
      "customerPhone": "0900000000",
      "serviceAddress": "Số nhà, đường, phường, quận, thành phố",
      "bookingTime": "2025-12-13 00:00:00",
      "estimatedDurationHours": 2.0,
      "pricePerUnit": 50000.0,
      "quantity": 4,
      "totalAmount": 200000.0,
      "status": "ASSIGNED",
      "assignedAt": null,
      "checkInTime": null,
      "checkOutTime": null,
      "note": "Check-in test 00h - Dọn dẹp đêm khuya (dangthir1)",
      "checkInLatitude": null,
      "checkInLongitude": null,
      "checkOutLatitude": null,
      "checkOutLongitude": null,
      "media": []
    }
  ],
  "pagination": {
    "currentPage": 0,
    "pageSize": 10,
    "totalItems": 25,
    "totalPages": 3,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

- Lưu ý:
  - `media` là danh sách ảnh của assignment, có thể rỗng.
  - `checkInLatitude`, `checkInLongitude`, `checkOutLatitude`, `checkOutLongitude` có thể là `null` nếu chưa gửi tọa độ hoặc chưa check-in/check-out.

### 2.2. POST nhận công việc (assignment)

- Method: `POST`
- URL: `/api/v1/employee/assignments/{assignmentId}/accept`
- Authorization: yêu cầu Bearer token; quyền `ROLE_EMPLOYEE`

#### Input

- Path parameter:
  - `assignmentId` (string)
- Query parameter:
  - `employeeId` (string)

Ví dụ:

`/api/v1/employee/assignments/as000001-0000-0000-0000-000000100001/accept?employeeId=e1000001-0000-0000-0000-000000000033`

#### Output

- HTTP 200

```json
{
  "success": true,
  "message": "Nhận công việc thành công",
  "data": {
    "assignmentId": "as000001-0000-0000-0000-000000100001",
    "bookingCode": "BKCI00001",
    "serviceName": "Dọn dẹp theo giờ",
    "customerName": "Nguyễn Văn A",
    "customerPhone": "0900000000",
    "serviceAddress": "Số nhà, đường, phường, quận, thành phố",
    "bookingTime": "2025-12-13 00:00:00",
    "estimatedDurationHours": 2.0,
    "pricePerUnit": 50000.0,
    "quantity": 4,
    "totalAmount": 200000.0,
    "status": "ASSIGNED",
    "assignedAt": null,
    "checkInTime": null,
    "checkOutTime": null,
    "note": "Check-in test 00h - Dọn dẹp đêm khuya (dangthir1)",
    "checkInLatitude": null,
    "checkInLongitude": null,
    "checkOutLatitude": null,
    "checkOutLongitude": null,
    "media": []
  }
}
```

### 2.3. POST nhận công việc theo booking detail

- Method: `POST`
- URL: `/api/v1/employee/booking-details/{detailId}/accept`
- Authorization: yêu cầu Bearer token; quyền `ROLE_EMPLOYEE`

#### Input

- Path parameter:
  - `detailId` (string): mã booking detail
- Query parameter:
  - `employeeId` (string)

#### Output

- HTTP 200

```json
{
  "success": true,
  "message": "Nhận công việc thành công",
  "data": {
    "assignmentId": "as000001-0000-0000-0000-000000100001",
    "bookingCode": "BKCI00001",
    "serviceName": "Dọn dẹp theo giờ",
    "customerName": "Nguyễn Văn A",
    "customerPhone": "0900000000",
    "serviceAddress": "12 Nguyễn Trãi, Phường Bến Thành, Quận 1, Thành phố Hồ Chí Minh",
    "bookingTime": "2025-12-13 00:00:00",
    "estimatedDurationHours": 2.0,
    "pricePerUnit": 50000.0,
    "quantity": 4,
    "totalAmount": 200000.0,
    "status": "ASSIGNED",
    "assignedAt": null,
    "checkInTime": null,
    "checkOutTime": null,
    "note": "Check-in test 00h - Dọn dẹp đêm khuya (dangthir1)",
    "checkInLatitude": null,
    "checkInLongitude": null,
    "checkOutLatitude": null,
    "checkOutLongitude": null,
    "media": []
  }
}
```

### 2.4. POST check-in (thay đổi input: thêm tọa độ)

- Method: `POST`
- URL: `/api/v1/employee/assignments/{assignmentId}/check-in`
- Authorization: yêu cầu Bearer token; quyền `ROLE_EMPLOYEE`
- Content-Type: `multipart/form-data`

#### Input

- Path parameter:
  - `assignmentId` (string)
- Form-data parts:
  - `request` (string, bắt buộc): chuỗi JSON theo cấu trúc `AssignmentCheckInRequest`
  - `images` (file list, tùy chọn): danh sách ảnh

`AssignmentCheckInRequest`:

```json
{
  "employeeId": "e1000001-0000-0000-0000-000000000033",
  "imageDescription": "Ảnh trước khi bắt đầu công việc",
  "latitude": 10.823098,
  "longitude": 106.629664
}
```

- Quy tắc ảnh:
  - Tối đa 10 file
  - Mỗi file phải là định dạng hình ảnh (Content-Type bắt đầu bằng `image/`)
  - Mỗi file không vượt quá 10 MB

- Quy tắc tọa độ:
  - `latitude` và `longitude` là tùy chọn.
  - Hệ thống chỉ lưu tọa độ khi cả `latitude` và `longitude` đều có giá trị.

#### Output

- HTTP 200

```json
{
  "success": true,
  "message": "Điểm danh bắt đầu công việc thành công",
  "assignment": {
    "assignmentId": "as000001-0000-0000-0000-000000100001",
    "bookingCode": "BKCI00001",
    "serviceName": "Dọn dẹp theo giờ",
    "customerName": "Nguyễn Văn A",
    "customerPhone": "0900000000",
    "serviceAddress": "Số nhà, đường, phường, quận, thành phố",
    "bookingTime": "2025-12-13 00:00:00",
    "estimatedDurationHours": 2.0,
    "pricePerUnit": 50000.0,
    "quantity": 4,
    "totalAmount": 200000.0,
    "status": "IN_PROGRESS",
    "assignedAt": null,
    "checkInTime": "2025-12-13 00:00:30",
    "checkOutTime": null,
    "note": "Check-in test 00h - Dọn dẹp đêm khuya (dangthir1)",
    "checkInLatitude": 10.823098,
    "checkInLongitude": 106.629664,
    "checkOutLatitude": null,
    "checkOutLongitude": null,
    "media": [
      {
        "mediaId": "m0000001-0000-0000-0000-000000000001",
        "assignmentId": "as000001-0000-0000-0000-000000100001",
        "mediaUrl": "https://res.cloudinary.com/housekeeping/image/upload/v1765824000/booking_media/check_in/sample_check_in_1.jpg",
        "publicId": "booking_media/check_in/sample_check_in_1",
        "mediaType": "CHECK_IN_IMAGE",
        "description": "Ảnh trước khi bắt đầu công việc",
        "uploadedAt": "2025-12-13T00:00:35"
      }
    ]
  }
}
```

### 2.5. POST check-out (thay đổi input: thêm tọa độ)

- Method: `POST`
- URL: `/api/v1/employee/assignments/{assignmentId}/check-out`
- Authorization: yêu cầu Bearer token; quyền `ROLE_EMPLOYEE`
- Content-Type: `multipart/form-data`

#### Input

- Path parameter:
  - `assignmentId` (string)
- Form-data parts:
  - `request` (string, bắt buộc): chuỗi JSON theo cấu trúc `AssignmentCheckOutRequest`
  - `images` (file list, tùy chọn): danh sách ảnh

`AssignmentCheckOutRequest`:

```json
{
  "employeeId": "e1000001-0000-0000-0000-000000000033",
  "imageDescription": "Ảnh sau khi hoàn thành công việc",
  "latitude": 10.823120,
  "longitude": 106.629700
}
```

- Quy tắc ảnh:
  - Tối đa 10 file
  - Mỗi file phải là định dạng hình ảnh (Content-Type bắt đầu bằng `image/`)
  - Mỗi file không vượt quá 10 MB

- Quy tắc tọa độ:
  - `latitude` và `longitude` là tùy chọn.
  - Hệ thống chỉ lưu tọa độ khi cả `latitude` và `longitude` đều có giá trị.

#### Output

- HTTP 200

```json
{
  "success": true,
  "message": "Chấm công kết thúc công việc thành công",
  "assignment": {
    "assignmentId": "as000001-0000-0000-0000-000000100001",
    "bookingCode": "BKCI00001",
    "serviceName": "Dọn dẹp theo giờ",
    "customerName": "Nguyễn Văn A",
    "customerPhone": "0900000000",
    "serviceAddress": "Số nhà, đường, phường, quận, thành phố",
    "bookingTime": "2025-12-13 00:00:00",
    "estimatedDurationHours": 2.0,
    "pricePerUnit": 50000.0,
    "quantity": 4,
    "totalAmount": 200000.0,
    "status": "COMPLETED",
    "assignedAt": null,
    "checkInTime": "2025-12-13 00:00:30",
    "checkOutTime": "2025-12-13 02:00:00",
    "note": "Check-in test 00h - Dọn dẹp đêm khuya (dangthir1)",
    "checkInLatitude": 10.823098,
    "checkInLongitude": 106.629664,
    "checkOutLatitude": 10.823120,
    "checkOutLongitude": 106.629700,
    "media": [
      {
        "mediaId": "m0000001-0000-0000-0000-000000000001",
        "assignmentId": "as000001-0000-0000-0000-000000100001",
        "mediaUrl": "https://res.cloudinary.com/housekeeping/image/upload/v1765824000/booking_media/check_in/sample_check_in_1.jpg",
        "publicId": "booking_media/check_in/sample_check_in_1",
        "mediaType": "CHECK_IN_IMAGE",
        "description": "Ảnh trước khi bắt đầu công việc",
        "uploadedAt": "2025-12-13T00:00:35"
      },
      {
        "mediaId": "m0000001-0000-0000-0000-000000000002",
        "assignmentId": "as000001-0000-0000-0000-000000100001",
        "mediaUrl": "https://res.cloudinary.com/housekeeping/image/upload/v1765824000/booking_media/check_out/sample_check_out_1.jpg",
        "publicId": "booking_media/check_out/sample_check_out_1",
        "mediaType": "CHECK_OUT_IMAGE",
        "description": "Ảnh sau khi hoàn thành công việc",
        "uploadedAt": "2025-12-13T02:00:10"
      }
    ]
  }
}
```

## 3) API Booking: các response chứa AssignmentInfo đã bổ sung media và tọa độ

Các API trả về `BookingResponse` vẫn giữ nguyên endpoint và cấu trúc cấp cao, nhưng thay đổi phần thông tin assignment trong `data.bookingDetails[].assignments[]`.

### 3.1. Danh sách endpoint trả về BookingResponse có thay đổi output

Các endpoint sau trả về `BookingResponse` hoặc danh sách `BookingResponse` và sẽ bao gồm assignment có `media` và 4 trường tọa độ:

1. `GET /api/v1/customer/bookings/{bookingId}`
2. `GET /api/v1/customer/bookings/{bookingId}/charges`
3. `GET /api/v1/employee/bookings/details/{bookingId}`
4. `GET /api/v1/employee/bookings/{employeeId}`
5. `GET /api/v1/employee/bookings/verified-awaiting-employee`

### 3.2. Output BookingResponse (tóm tắt cấu trúc đầy đủ, không viết tắt)

`BookingResponse`:

```json
{
  "success": true,
  "message": "Lấy chi tiết booking thành công",
  "data": {
    "bookingId": "b0000001-0000-0000-0000-000000100001",
    "bookingCode": "BKCI00001",
    "customerId": "c1000001-0000-0000-0000-000000000001",
    "customerName": "Nguyễn Văn A",
    "customer": {
      "customerId": "c1000001-0000-0000-0000-000000000001",
      "fullName": "Nguyễn Văn A",
      "avatar": "https://res.cloudinary.com/housekeeping/image/upload/v1765824000/avatars/customer_1.jpg",
      "email": "customer@example.com",
      "phoneNumber": "0900000000",
      "isMale": true,
      "birthdate": "1995-05-20",
      "rating": "VIP",
      "vipLevel": 2
    },
    "address": {
      "addressId": "adrs0001-0000-0000-0000-000000000001",
      "fullAddress": "12 Nguyễn Trãi, Phường Bến Thành, Quận 1, Thành phố Hồ Chí Minh",
      "ward": "Phường Bến Thành",
      "city": "Thành phố Hồ Chí Minh",
      "latitude": 10.772180,
      "longitude": 106.698230,
      "isDefault": true
    },
    "bookingTime": "2025-12-13T00:00:00",
    "note": "Ghi chú booking",
    "totalAmount": 200000.0,
    "formattedTotalAmount": "200.000",
    "baseAmount": 200000.0,
    "totalFees": 0.0,
    "fees": [],
    "status": "CONFIRMED",
    "title": null,
    "imageUrls": [],
    "isPost": false,
    "isVerified": true,
    "adminComment": null,
    "promotion": null,
    "bookingDetails": [
      {
        "bookingDetailId": "bd000001-0000-0000-0000-000000100001",
        "service": {
          "serviceId": 1,
          "name": "Dọn dẹp theo giờ",
          "description": "Mô tả dịch vụ",
          "basePrice": 50000.0,
          "unit": "giờ",
          "estimatedDurationHours": 2.0,
          "iconUrl": "https://res.cloudinary.com/housekeeping/image/upload/v1765824000/services/icon_cleaning_hourly.png",
          "categoryName": "Dọn dẹp",
          "isActive": true
        },
        "quantity": 4,
        "pricePerUnit": 50000.0,
        "formattedPricePerUnit": "50.000",
        "subTotal": 200000.0,
        "formattedSubTotal": "200.000",
        "selectedChoices": [],
        "assignments": [
          {
            "assignmentId": "as000001-0000-0000-0000-000000100001",
            "employee": {
              "employeeId": "e1000001-0000-0000-0000-000000000033",
              "fullName": "Đặng Thị R",
              "email": "employee@example.com",
              "phoneNumber": "0900000001",
              "avatar": "https://res.cloudinary.com/housekeeping/image/upload/v1765824000/avatars/employee_33.jpg",
              "rating": "GOOD",
              "employeeStatus": "AVAILABLE",
              "skills": [],
              "bio": null
            },
            "status": "ASSIGNED",
            "checkInTime": null,
            "checkOutTime": null,
            "createdAt": null,
            "updatedAt": null,
            "checkInLatitude": null,
            "checkInLongitude": null,
            "checkOutLatitude": null,
            "checkOutLongitude": null,
            "media": [
              {
                "mediaId": "m0000001-0000-0000-0000-000000000001",
                "assignmentId": "as000001-0000-0000-0000-000000100001",
                "mediaUrl": "https://res.cloudinary.com/housekeeping/image/upload/v1765824000/booking_media/check_in/sample_check_in_1.jpg",
                "publicId": "booking_media/check_in/sample_check_in_1",
                "mediaType": "CHECK_IN_IMAGE",
                "description": "Ảnh trước khi bắt đầu công việc",
                "uploadedAt": "2025-12-13T00:00:35"
              }
            ]
          }
        ],
        "duration": "2 giờ",
        "formattedDuration": "2 giờ"
      }
    ],
    "payment": null,
    "createdAt": "2025-12-12T10:00:00"
  }
}
```

- Lưu ý quan trọng cho Front End:
  - Mục tiêu thay đổi là bổ sung `media` và 4 trường tọa độ vào `assignments` trong booking.
  - Thứ tự phần tử trong `media` có thể không cố định; nếu cần hiển thị theo thời gian, Front End nên sắp xếp theo `uploadedAt`.

