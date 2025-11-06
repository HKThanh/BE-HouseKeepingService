# API Test Cases - Get Booking Details

## Endpoint Information

### Customer Endpoint
- **URL**: `GET /api/v1/bookings/{bookingId}`
- **Authentication**: Required (ROLE_CUSTOMER, ROLE_ADMIN)
- **Authorization**: Customer and Admin

### Employee Endpoint
- **URL**: `GET /api/v1/employee/bookings/details/{bookingId}`
- **Authentication**: Required (ROLE_EMPLOYEE, ROLE_ADMIN)
- **Authorization**: Employee and Admin

### Admin Endpoint
- **URL**: `GET /api/v1/admin/bookings/{bookingId}`
- **Authentication**: Required (ROLE_ADMIN)
- **Authorization**: Admin only

---

## Test Cases - Customer Get Booking Details

### Test Case 1: Customer Gets COMPLETED Booking with Payment
**Request**:
```http
GET /api/v1/customer/bookings/b0000001-0000-0000-0000-000000000001
Authorization: Bearer {customer_token_john_doe}
```

**Expected Response (200 OK)**:
```json
{
    "success": true,
    "message": "Đặt lịch thành công",
    "data": {
        "bookingId": "b0000001-0000-0000-0000-000000000001",
        "bookingCode": "BK000001",
        "customerId": "c1000001-0000-0000-0000-000000000001",
        "customerName": "John Doe",
        "customer": {
            "customerId": "c1000001-0000-0000-0000-000000000001",
            "fullName": "John Doe",
            "avatar": "https://picsum.photos/200",
            "email": "john.doe@example.com",
            "phoneNumber": "0901234567",
            "isMale": true,
            "birthdate": "2003-09-10",
            "rating": null,
            "vipLevel": null
        },
        "address": {
            "addressId": "adrs0001-0000-0000-0000-000000000001",
            "fullAddress": "123 Đường Nguyễn Văn Linh, Phường Tân Phú",
            "ward": "Phường Tân Phú",
            "city": "Quận 7",
            "latitude": 10.7333,
            "longitude": 106.7181,
            "isDefault": true
        },
        "bookingTime": "2025-08-20T09:00:00",
        "note": "Nhà có trẻ nhỏ, vui lòng lau dọn kỹ khu vực phòng khách.",
        "totalAmount": 80000.00,
        "formattedTotalAmount": "80.000 ₫",
        "status": "COMPLETED",
        "title": null,
        "imageUrl": null,
        "isVerified": true,
        "adminComment": null,
        "promotion": {
            "promotionId": "promo001",
            "promoCode": "GIAM20K",
            "description": "Giảm 20.000đ cho đơn hàng đầu tiên",
            "discountType": "FIXED_AMOUNT",
            "discountValue": 20000.0,
            "maxDiscountAmount": 20000.00
        },
        "bookingDetails": [
            {
                "id": "bd000001-0000-0000-0000-000000000001",
                "service": {
                    "serviceId": 2,
                    "name": "Tổng vệ sinh",
                    "description": "Dịch vụ vệ sinh tổng thể toàn bộ ngôi nhà",
                    "basePrice": 100000.00,
                    "unit": "lần",
                    "estimatedDurationHours": 4.0,
                    "iconUrl": "https://example.com/icons/deep-cleaning.png",
                    "categoryName": "Vệ sinh",
                    "isActive": true
                },
                "quantity": 1,
                "pricePerUnit": 100000.00,
                "formattedPricePerUnit": "100.000 ₫",
                "subTotal": 100000.00,
                "formattedSubTotal": "100.000 ₫",
                "selectedChoices": [],
                "assignments": [
                    {
                        "assignmentId": "as000001-0000-0000-0000-000000000001",
                        "employee": {
                            "employeeId": "e1000001-0000-0000-0000-000000000002",
                            "fullName": "Bob Wilson",
                            "email": "bob.wilson@examplefieldset.com",
                            "phoneNumber": "0923456789",
                            "avatar": "https://picsum.photos/200",
                            "rating": null,
                            "employeeStatus": "ACTIVE",
                            "skills": ["Deep Cleaning", "Laundry"],
                            "bio": "Chuyên gia giặt ủi và làm sạch sâu."
                        },
                        "status": "COMPLETED",
                        "checkInTime": "2025-08-20T09:00:00",
                        "checkOutTime": "2025-08-20T13:00:00",
                        "createdAt": null,
                        "updatedAt": null
                    }
                ],
                "estimatedDuration": "4.0 giờ",
                "formattedEstimatedDuration": "4.0 giờ"
            }
        ],
        "payment": {
            "id": "pay00001-0000-0000-0000-000000000001",
            "amount": 80000.00,
            "paymentMethodName": "Tiền mặt",
            "paymentStatus": "PAID",
            "transactionCode": "TXN-20250820-001",
            "createdAt": "2025-08-20T13:00:00",
            "paidAt": "2025-08-20T13:05:00"
        },
        "createdAt": "2025-08-20T08:00:00"
    }
}
```

---

## Test Cases - Employee Get Booking Details

### Test Case 2: Employee Gets Booking with CONFIRMED Status
**Request**:
```http
GET /api/v1/employee/bookings/details/b0000001-0000-0000-0000-000000000012
Authorization: Bearer {employee_token_le_van_nam}
```

**Expected Response (200 OK)**:
```json
{
    "success": true,
    "message": "Đặt lịch thành công",
    "data": {
        "bookingId": "b0000001-0000-0000-0000-000000000012",
        "bookingCode": "BK000012",
        "customerId": "c1000001-0000-0000-0000-000000000006",
        "customerName": "Lê Văn Cường",
        "customer": {
            "customerId": "c1000001-0000-0000-0000-000000000006",
            "fullName": "Lê Văn Cường",
            "avatar": "https://i.pravatar.cc/150?img=12",
            "email": "levancuong@gmail.com",
            "phoneNumber": "0965432109",
            "isMale": true,
            "birthdate": "1992-11-08",
            "rating": null,
            "vipLevel": null
        },
        "address": {
            "addressId": "adrs0001-0000-0000-0000-000000000011",
            "fullAddress": "789 Đường Võ Văn Ngân, Phường Linh Chiểu",
            "ward": "Phường Linh Chiểu",
            "city": "Thành phố Thủ Đức",
            "latitude": 10.8507,
            "longitude": 106.7634,
            "isDefault": true
        },
        "bookingTime": "2025-11-07T09:00:00",
        "note": "Vệ sinh sofa da chuyên dụng",
        "totalAmount": 200000.00,
        "formattedTotalAmount": "200.000 ₫",
        "status": "CONFIRMED",
        "title": null,
        "imageUrl": null,
        "isVerified": true,
        "adminComment": null,
        "promotion": null,
        "bookingDetails": [
            {
                "id": "bd000001-0000-0000-0000-000000000012",
                "service": {
                    "serviceId": 3,
                    "name": "Vệ sinh Sofa - Nệm - Rèm",
                    "description": "Dịch vụ vệ sinh chuyên sâu cho sofa, nệm và rèm cửa",
                    "basePrice": 200000.00,
                    "unit": "bộ",
                    "estimatedDurationHours": 2.0,
                    "iconUrl": "https://example.com/icons/sofa-cleaning.png",
                    "categoryName": "Vệ sinh",
                    "isActive": true
                },
                "quantity": 1,
                "pricePerUnit": 200000.00,
                "formattedPricePerUnit": "200.000 ₫",
                "subTotal": 200000.00,
                "formattedSubTotal": "200.000 ₫",
                "selectedChoices": [],
                "assignments": [],
                "estimatedDuration": "2.0 giờ",
                "formattedEstimatedDuration": "2.0 giờ"
            }
        ],
        "payment": null,
        "createdAt": "2025-11-07T08:00:00"
    }
}
```

---

## Available Bookings in Seed Data

### For Customer Testing:
- **BK000001** (b0000001-0000-0000-0000-000000000001) - Customer: John Doe - COMPLETED - 80,000 VND - Has payment
- **BK000002** (b0000001-0000-0000-0000-000000000002) - Customer: Jane Smith - CONFIRMED - 90,000 VND - Has assignment
- **BK000003** (b0000001-0000-0000-0000-000000000003) - Customer: Nguyễn Văn An - PENDING - 500,000 VND
- **BK000004** (b0000001-0000-0000-0000-000000000004) - Customer: Trần Thị Bích - PENDING - 300,000 VND
- **BK000005** (b0000001-0000-0000-0000-000000000005) - Customer: Lê Văn Cường - PENDING - 350,000 VND

### For Employee Testing:
- **BK000010** (b0000001-0000-0000-0000-000000000010) - PENDING - 500,000 VND - Not verified
- **BK000011** (b0000001-0000-0000-0000-000000000011) - PENDING - 150,000 VND - Not verified
- **BK000012** (b0000001-0000-0000-0000-000000000012) - CONFIRMED - 200,000 VND - Verified
- **BK000013** (b0000001-0000-0000-0000-000000000013) - PENDING - 100,000 VND - Not verified

---

## Notes

- **Customer information is included in all responses** with full details (name, email, phone, avatar, etc.)
- **Address information** includes full address, ward, city, and coordinates
- **Booking details** include complete service information, pricing, and formatting
- **Assignments** show employee details when available
- **Payment information** is included for completed bookings with payment records
- **All endpoints require authentication** - appropriate role tokens must be provided
- **Customer endpoint** allows ROLE_CUSTOMER and ROLE_ADMIN
- **Employee endpoint** allows ROLE_EMPLOYEE and ROLE_ADMIN
- **Admin endpoint** allows ROLE_ADMIN only
- **Formatted amounts** use Vietnamese currency format (e.g., "500.000 ₫")
- **Date/time format** follows ISO 8601 (yyyy-MM-dd'T'HH:mm:ss)
