# API Test Cases - Get Verified Awaiting Employee Bookings (matchEmployeeZones Parameter)

## Endpoint Information

- **URL**: `GET /api/v1/employee/bookings/verified-awaiting-employee`
- **Authentication**: Required (ROLE_EMPLOYEE, ROLE_ADMIN)
- **Parameters**: 
  - `fromDate` (optional): LocalDateTime (ISO format)
  - `page` (optional, default: 0): int
  - `size` (optional, default: 10): int
  - `matchEmployeeZones` (optional, default: true): boolean - Lọc theo khu vực làm việc của employee

---

## Test Case 1: matchEmployeeZones = true (Lọc theo khu vực employee)

**Request**:
```http
GET /api/v1/employee/bookings/verified-awaiting-employee?page=0&size=10&matchEmployeeZones=true
Authorization: Bearer {employee_token}
```

**Expected Response (200 OK)**:
```json
{
    "success": true,
    "data": [
        {
            "bookingId": "b0000001-0000-0000-0000-000000000010",
            "bookingCode": "BK000010",
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
                "fullAddress": "123 Đường Lê Văn Việt, Phường Tăng Nhơn Phú A",
                "ward": "Phường Tăng Nhơn Phú A",
                "city": "Thành phố Thủ Đức",
                "latitude": 10.8506,
                "longitude": 106.7629,
                "isDefault": true
            },
            "bookingTime": "2025-11-07T10:00:00",
            "note": "Vệ sinh tổng quát căn hộ",
            "totalAmount": 500000.00,
            "formattedTotalAmount": "500.000 ₫",
            "status": "AWAITING_EMPLOYEE",
            "title": null,
            "imageUrl": null,
            "isVerified": true,
            "adminComment": "Đã kiểm tra và xác nhận yêu cầu",
            "promotion": null,
            "bookingDetails": [
                {
                    "id": "bd000001-0000-0000-0000-000000000010",
                    "service": {
                        "serviceId": 2,
                        "name": "Tổng vệ sinh",
                        "description": "Dịch vụ vệ sinh tổng thể toàn bộ ngôi nhà",
                        "basePrice": 500000.00,
                        "unit": "lần",
                        "estimatedDurationHours": 4.0,
                        "iconUrl": "https://example.com/icons/deep-cleaning.png",
                        "categoryName": "Vệ sinh",
                        "isActive": true
                    },
                    "quantity": 1,
                    "pricePerUnit": 500000.00,
                    "formattedPricePerUnit": "500.000 ₫",
                    "subTotal": 500000.00,
                    "formattedSubTotal": "500.000 ₫",
                    "selectedChoices": [],
                    "assignments": [],
                    "estimatedDuration": "4.0 giờ",
                    "formattedEstimatedDuration": "4.0 giờ"
                }
            ],
            "payment": null,
            "createdAt": "2025-11-06T09:00:00"
        },
        {
            "bookingId": "b0000001-0000-0000-0000-000000000011",
            "bookingCode": "BK000011",
            "customerId": "c1000001-0000-0000-0000-000000000005",
            "customerName": "Trần Thị Bích",
            "address": {
                "city": "Thành phố Thủ Đức",
                "ward": "Phường Linh Chiểu"
            },
            "bookingTime": "2025-11-08T14:00:00",
            "totalAmount": 300000.00,
            "formattedTotalAmount": "300.000 ₫",
            "status": "AWAITING_EMPLOYEE",
            "isVerified": true
        }
    ],
    "currentPage": 0,
    "totalItems": 2,
    "totalPages": 1
}
```

**Notes**:
- Chỉ trả về bookings trong khu vực làm việc của employee
- `isVerified = true` và `status = AWAITING_EMPLOYEE`
- Sắp xếp theo `createdAt DESC`

---

## Test Case 2: matchEmployeeZones = false (Không lọc theo khu vực)

**Request**:
```http
GET /api/v1/employee/bookings/verified-awaiting-employee?page=0&size=10&matchEmployeeZones=false
Authorization: Bearer {employee_token}
```

**Expected Response (200 OK)**:
```json
{
    "success": true,
    "data": [
        {
            "bookingId": "b0000001-0000-0000-0000-000000000010",
            "bookingCode": "BK000010",
            "customerName": "Nguyễn Văn An",
            "address": {
                "city": "Thành phố Thủ Đức",
                "ward": "Phường Tăng Nhơn Phú A"
            },
            "bookingTime": "2025-11-07T10:00:00",
            "status": "AWAITING_EMPLOYEE",
            "isVerified": true
        },
        {
            "bookingId": "b0000001-0000-0000-0000-000000000011",
            "bookingCode": "BK000011",
            "customerName": "Trần Thị Bích",
            "address": {
                "city": "Thành phố Thủ Đức",
                "ward": "Phường Linh Chiểu"
            },
            "bookingTime": "2025-11-08T14:00:00",
            "status": "AWAITING_EMPLOYEE",
            "isVerified": true
        },
        {
            "bookingId": "b0000001-0000-0000-0000-000000000012",
            "bookingCode": "BK000012",
            "customerName": "Lê Minh Tuấn",
            "address": {
                "city": "Quận 10",
                "ward": "Phường 5"
            },
            "bookingTime": "2025-11-09T09:00:00",
            "note": "Làm vệ sinh sau sửa chữa nhà",
            "totalAmount": 800000.00,
            "formattedTotalAmount": "800.000 ₫",
            "status": "AWAITING_EMPLOYEE",
            "isVerified": true,
            "adminComment": "Công việc lớn, cần 2 nhân viên"
        }
    ],
    "currentPage": 0,
    "totalItems": 3,
    "totalPages": 1
}
```

**Notes**:
- Trả về tất cả bookings không phân biệt khu vực
- Bao gồm cả booking ở "Quận 10" (ngoài khu vực employee)
- `isVerified = true` và `status = AWAITING_EMPLOYEE`

---

## Error Cases

**Error Case 1: Unauthorized Access (Customer token)**
```http
GET /api/v1/employee/bookings/verified-awaiting-employee
Authorization: Bearer {customer_token}
```

Response (403 Forbidden):
```json
{
    "timestamp": "2025-11-06T12:00:00",
    "status": 403,
    "error": "Forbidden",
    "message": "Access Denied"
}
```

**Error Case 2: No Authentication**
```http
GET /api/v1/employee/bookings/verified-awaiting-employee
```

Response (401 Unauthorized):
```json
{
    "timestamp": "2025-11-06T12:00:00",
    "status": 401,
    "error": "Unauthorized",
    "message": "Full authentication is required to access this resource"
}
```
