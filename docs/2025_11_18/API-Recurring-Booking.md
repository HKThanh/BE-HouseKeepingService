# API Đặt Lịch Định Kỳ (Recurring Booking)

## Tổng quan
Tính năng đặt lịch định kỳ cho phép khách hàng tạo các booking tự động theo chu kỳ tuần hoặc tháng. Hệ thống sẽ tự động tạo các booking theo lịch định kỳ đã được thiết lập.

## Các API

### 1. Tạo Lịch Định Kỳ

**Endpoint:** `POST /api/v1/customer/recurring-bookings/{customerId}`

**Authorization:** Bearer Token (ROLE_CUSTOMER)

**Path Parameters:**
- `customerId`: ID của khách hàng

**Request Body:**
```json
{
    "success": true,
    "message": "Đặt lịch định kỳ thành công",
    "data": {
        "success": true,
        "message": "Đặt lịch định kỳ thành công",
        "recurringBooking": {
            "recurringBookingId": "1ddc656e-f820-4b81-8c6d-15520d1d77f1",
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
            "startDate": "2025-12-27",
            "endDate": "2026-01-15",
            "note": "Vệ sinh định kỳ căn hộ 2 phòng ngủ",
            "title": "Dọn dẹp hàng tuần",
            "promotion": null,
            "recurringBookingDetails": [
                {
                    "bookingDetailId": "a0515373-973c-4626-89d4-da2687afeaf1",
                    "service": {
                        "serviceId": 1,
                        "name": "Dọn dẹp theo giờ",
                        "description": "Lau dọn, hút bụi, làm sạch các bề mặt cơ bản trong nhà. Phù hợp cho nhu cầu duy trì vệ sinh hàng tuần.",
                        "basePrice": 50000.00,
                        "unit": "Giờ",
                        "estimatedDurationHours": 2.0,
                        "iconUrl": "https://res.cloudinary.com/dkzemgit8/image/upload/v1764171235/Cleaning_Clock-removebg-preview_o0oevs.png",
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
            "assignedEmployee": {
                "employeeId": "e1000001-0000-0000-0000-000000000020",
                "fullName": "Phạm Thị Dung Em",
                "avatar": "https://i.pravatar.cc/150?img=45",
                "rating": "HIGH",
                "employeeStatus": "AVAILABLE",
                "skills": [
                    "Vệ sinh sofa",
                    "Giặt thảm"
                ],
                "bio": "Chuyên vệ sinh nội thất cao cấp."
            },
            "status": "ACTIVE",
            "statusDisplay": "Đang hoạt động",
            "cancelledAt": null,
            "cancellationReason": null,
            "createdAt": "2025-12-09T15:43:15",
            "updatedAt": "2025-12-09T15:43:15",
            "totalGeneratedBookings": 3,
            "upcomingBookings": 3,
            "expectedBookingsInWindow": 3,
            "generatedBookingsInWindow": 3,
            "generationWindowDays": 7,
            "generationProgressPercent": 100.0
        },
        "generatedBookingIds": [
            "e61dc6d8-cd73-4d04-8a46-558e2c54faae",
            "bdb4dddb-2f17-45bc-993e-64c4ab2f9966",
            "db204132-f90b-4650-9c10-7cd6b06b1f5a"
        ],
        "totalBookingsToBeCreated": 3,
        "expectedBookingsInWindow": 3,
        "generatedBookingsInWindow": 3,
        "generationWindowDays": 7,
        "generationProgressPercent": 100.0,
        "conversation": {
            "conversationId": "b9b4769e-2313-44bc-bff2-aab04e4442f5",
            "customerId": "c1000001-0000-0000-0000-000000000004",
            "customerName": "Nguyễn Văn An",
            "customerAvatar": "https://i.pravatar.cc/150?img=11",
            "employeeId": "e1000001-0000-0000-0000-000000000020",
            "employeeName": "Phạm Thị Dung Em",
            "employeeAvatar": "https://i.pravatar.cc/150?img=45",
            "bookingId": null,
            "recurringBookingId": "1ddc656e-f820-4b81-8c6d-15520d1d77f1",
            "lastMessage": "Xin chào. Tôi là Phạm Thị Dung Em sẽ đồng hành cùng lịch sử dụng dịch vụ Dọn dẹp theo giờ định kỳ của bạn tại 45 Nguyễn Huệ, Phường Phú An, Thành phố Hồ Chí Minh vào lúc 08:00 Thứ 2, Thứ 3, Thứ 5 mỗi tuần. Nếu bạn có câu hỏi, hãy nhắn tin tại đây.",
            "lastMessageTime": "2025-12-09T15:43:39.474681014",
            "isActive": true,
            "canChat": true,
            "createdAt": null,
            "updatedAt": null
        }
    }
}
```

**Hành vi sinh slot ngay sau khi tạo (realtime window fill):**
- Sau khi job 02:00 đã chạy, API vẫn sinh ngay các slot còn lại trong cửa sổ generate hiện tại (30 ngày nếu chưa cố định nhân viên, 7 ngày nếu đã cố định), miễn là `bookingTime` còn ở tương lai tính từ thời điểm gọi API.
- Nếu `startDate` = hôm nay và hôm nay là ngày lặp, slot hôm nay sẽ được sinh nếu thời gian còn lại (>= now). Slot đã qua trong ngày sẽ bị bỏ qua.
- Vẫn tránh trùng nhờ kiểm tra slot tồn tại trong khoảng cửa sổ.

**Ràng buộc thời gian & mã lỗi:**
- `startDate` phải từ hôm nay trở đi; `endDate` (nếu có) phải sau `startDate`.
- `bookingTime` bắt buộc.
- `recurrenceDays`: WEEKLY chỉ nhận 1-7, MONTHLY chỉ nhận 1-31; danh sách không được rỗng.
- Tổng số lần lặp tối đa 365 lần (nếu không truyền `endDate`, BE kiểm tra trong 12 tháng kể từ `startDate`). Vi phạm trả về `errorCode: RECURRING_TIME_INVALID` kèm danh sách lỗi chi tiết.

**Response thành công (201):**
```json
{
    "success": true,
    "message": "Đặt lịch định kỳ thành công",
    "data": {
        "success": true,
        "message": "Đặt lịch định kỳ thành công",
        "recurringBooking": {
            "recurringBookingId": "1ddc656e-f820-4b81-8c6d-15520d1d77f1",
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
            "startDate": "2025-12-27",
            "endDate": "2026-01-15",
            "note": "Vệ sinh định kỳ căn hộ 2 phòng ngủ",
            "title": "Dọn dẹp hàng tuần",
            "promotion": null,
            "recurringBookingDetails": [
                {
                    "bookingDetailId": "a0515373-973c-4626-89d4-da2687afeaf1",
                    "service": {
                        "serviceId": 1,
                        "name": "Dọn dẹp theo giờ",
                        "description": "Lau dọn, hút bụi, làm sạch các bề mặt cơ bản trong nhà. Phù hợp cho nhu cầu duy trì vệ sinh hàng tuần.",
                        "basePrice": 50000.00,
                        "unit": "Giờ",
                        "estimatedDurationHours": 2.0,
                        "iconUrl": "https://res.cloudinary.com/dkzemgit8/image/upload/v1764171235/Cleaning_Clock-removebg-preview_o0oevs.png",
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
            "assignedEmployee": {
                "employeeId": "e1000001-0000-0000-0000-000000000020",
                "fullName": "Phạm Thị Dung Em",
                "avatar": "https://i.pravatar.cc/150?img=45",
                "rating": "HIGH",
                "employeeStatus": "AVAILABLE",
                "skills": [
                    "Vệ sinh sofa",
                    "Giặt thảm"
                ],
                "bio": "Chuyên vệ sinh nội thất cao cấp."
            },
            "status": "ACTIVE",
            "statusDisplay": "Đang hoạt động",
            "cancelledAt": null,
            "cancellationReason": null,
            "createdAt": "2025-12-09T15:43:15",
            "updatedAt": "2025-12-09T15:43:15",
            "totalGeneratedBookings": 3,
            "upcomingBookings": 3,
            "expectedBookingsInWindow": 3,
            "generatedBookingsInWindow": 3,
            "generationWindowDays": 7,
            "generationProgressPercent": 100.0
        },
        "generatedBookingIds": [
            "e61dc6d8-cd73-4d04-8a46-558e2c54faae",
            "bdb4dddb-2f17-45bc-993e-64c4ab2f9966",
            "db204132-f90b-4650-9c10-7cd6b06b1f5a"
        ],
        "totalBookingsToBeCreated": 3,
        "expectedBookingsInWindow": 3,
        "generatedBookingsInWindow": 3,
        "generationWindowDays": 7,
        "generationProgressPercent": 100.0,
        "conversation": {
            "conversationId": "b9b4769e-2313-44bc-bff2-aab04e4442f5",
            "customerId": "c1000001-0000-0000-0000-000000000004",
            "customerName": "Nguyễn Văn An",
            "customerAvatar": "https://i.pravatar.cc/150?img=11",
            "employeeId": "e1000001-0000-0000-0000-000000000020",
            "employeeName": "Phạm Thị Dung Em",
            "employeeAvatar": "https://i.pravatar.cc/150?img=45",
            "bookingId": null,
            "recurringBookingId": "1ddc656e-f820-4b81-8c6d-15520d1d77f1",
            "lastMessage": "Xin chào. Tôi là Phạm Thị Dung Em sẽ đồng hành cùng lịch sử dụng dịch vụ Dọn dẹp theo giờ định kỳ của bạn tại 45 Nguyễn Huệ, Phường Phú An, Thành phố Hồ Chí Minh vào lúc 08:00 Thứ 2, Thứ 3, Thứ 5 mỗi tuần. Nếu bạn có câu hỏi, hãy nhắn tin tại đây.",
            "lastMessageTime": "2025-12-09T15:43:39.474681014",
            "isActive": true,
            "canChat": true,
            "createdAt": null,
            "updatedAt": null
        }
    }
}
```

**Ghi chú về tiến độ sinh booking tự động:**
- `totalBookingsToBeCreated` và `expectedBookingsInWindow` đếm số slot sẽ được tạo trong cửa sổ generate hiện tại (mặc định 30 ngày; 7 ngày nếu lịch đã có nhân viên cố định).
- `generatedBookingsInWindow` và `generationProgressPercent` cho biết trạng thái sinh booking thực tế trong cửa sổ đó, giúp FE hiển thị còn bao nhiêu slot đang chờ gán/đang tạo.

---

### 2. Hủy Lịch Định Kỳ

**Endpoint:** `PUT /api/v1/customer/recurring-bookings/{customerId}/{recurringBookingId}/cancel`

**Authorization:** Bearer Token (ROLE_CUSTOMER)

**Path Parameters:**
- `customerId`: ID của khách hàng
- `recurringBookingId`: ID của lịch định kỳ cần hủy

**Request Body:**
```json
{
  "reason": "Không còn nhu cầu sử dụng dịch vụ"
}
```

**Response thành công (200):**
```json
{
    "success": true,
    "message": "Đã hủy lịch định kỳ thành công",
    "data": {
        "recurringBookingId": "1ddc656e-f820-4b81-8c6d-15520d1d77f1",
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
        "startDate": "2025-12-27",
        "endDate": "2026-01-15",
        "note": "Vệ sinh định kỳ căn hộ 2 phòng ngủ",
        "title": "Dọn dẹp hàng tuần",
        "promotion": null,
        "recurringBookingDetails": [
            {
                "bookingDetailId": "a0515373-973c-4626-89d4-da2687afeaf1",
                "service": {
                    "serviceId": 1,
                    "name": "Dọn dẹp theo giờ",
                    "description": "Lau dọn, hút bụi, làm sạch các bề mặt cơ bản trong nhà. Phù hợp cho nhu cầu duy trì vệ sinh hàng tuần.",
                    "basePrice": 50000.00,
                    "unit": "Giờ",
                    "estimatedDurationHours": 2.0,
                    "iconUrl": "https://res.cloudinary.com/dkzemgit8/image/upload/v1764171235/Cleaning_Clock-removebg-preview_o0oevs.png",
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
        "assignedEmployee": {
            "employeeId": "e1000001-0000-0000-0000-000000000020",
            "fullName": "Phạm Thị Dung Em",
            "avatar": "https://i.pravatar.cc/150?img=45",
            "rating": "HIGH",
            "employeeStatus": "AVAILABLE",
            "skills": [
                "Vệ sinh sofa",
                "Giặt thảm"
            ],
            "bio": "Chuyên vệ sinh nội thất cao cấp."
        },
        "status": "CANCELLED",
        "statusDisplay": "Đã hủy",
        "cancelledAt": "2025-12-09T15:56:58",
        "cancellationReason": "Không còn nhu cầu sử dụng dịch vụ",
        "createdAt": "2025-12-09T15:43:15",
        "updatedAt": "2025-12-09T15:43:15",
        "totalGeneratedBookings": 0,
        "upcomingBookings": null,
        "expectedBookingsInWindow": null,
        "generatedBookingsInWindow": null,
        "generationWindowDays": null,
        "generationProgressPercent": null
    }
}
```

**Lưu ý:** 
- Khi hủy lịch định kỳ, tất cả các booking tương lai (chưa thực hiện) sẽ bị xóa
- Các booking đã hoàn thành hoặc đang thực hiện sẽ không bị ảnh hưởng

---

### 3. Lấy Danh Sách Lịch Định Kỳ

**Endpoint:** `GET /api/v1/customer/recurring-bookings/{customerId}`

**Authorization:** Bearer Token (ROLE_CUSTOMER)

**Path Parameters:**
- `customerId`: ID của khách hàng

**Query Parameters:**
- `page`: Trang (default: 0)
- `size`: Số lượng mỗi trang (default: 10)

**Response thành công (200):**
```json
{
    "data": [
        {
            "recurringBookingId": "1ddc656e-f820-4b81-8c6d-15520d1d77f1",
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
            "startDate": "2025-12-27",
            "endDate": "2026-01-15",
            "note": "Vệ sinh định kỳ căn hộ 2 phòng ngủ",
            "title": "Dọn dẹp hàng tuần",
            "promotion": null,
            "recurringBookingDetails": [
                {
                    "bookingDetailId": "a0515373-973c-4626-89d4-da2687afeaf1",
                    "service": {
                        "serviceId": 1,
                        "name": "Dọn dẹp theo giờ",
                        "description": "Lau dọn, hút bụi, làm sạch các bề mặt cơ bản trong nhà. Phù hợp cho nhu cầu duy trì vệ sinh hàng tuần.",
                        "basePrice": 50000.00,
                        "unit": "Giờ",
                        "estimatedDurationHours": 2.0,
                        "iconUrl": "https://res.cloudinary.com/dkzemgit8/image/upload/v1764171235/Cleaning_Clock-removebg-preview_o0oevs.png",
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
            "assignedEmployee": {
                "employeeId": "e1000001-0000-0000-0000-000000000020",
                "fullName": "Phạm Thị Dung Em",
                "avatar": "https://i.pravatar.cc/150?img=45",
                "rating": "HIGH",
                "employeeStatus": "AVAILABLE",
                "skills": [
                    "Vệ sinh sofa",
                    "Giặt thảm"
                ],
                "bio": "Chuyên vệ sinh nội thất cao cấp."
            },
            "status": "ACTIVE",
            "statusDisplay": "Đang hoạt động",
            "cancelledAt": null,
            "cancellationReason": null,
            "createdAt": "2025-12-09T15:43:15",
            "updatedAt": "2025-12-09T15:43:15",
            "totalGeneratedBookings": 3,
            "upcomingBookings": 3,
            "expectedBookingsInWindow": 3,
            "generatedBookingsInWindow": 3,
            "generationWindowDays": 7,
            "generationProgressPercent": 100.0
        }
    ],
    "success": true,
    "currentPage": 0,
    "totalItems": 1,
    "totalPages": 1
}
```

---

### 4. Lấy Chi Tiết Lịch Định Kỳ

**Endpoint:** `GET /api/v1/customer/recurring-bookings/{customerId}/{recurringBookingId}`

**Authorization:** Bearer Token (ROLE_CUSTOMER)

**Path Parameters:**
- `customerId`: ID của khách hàng
- `recurringBookingId`: ID của lịch định kỳ

**Response thành công (200):**
```json
{
    "data": {
        "recurringBookingId": "1ddc656e-f820-4b81-8c6d-15520d1d77f1",
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
        "startDate": "2025-12-27",
        "endDate": "2026-01-15",
        "note": "Vệ sinh định kỳ căn hộ 2 phòng ngủ",
        "title": "Dọn dẹp hàng tuần",
        "promotion": null,
        "recurringBookingDetails": [
            {
                "bookingDetailId": "a0515373-973c-4626-89d4-da2687afeaf1",
                "service": {
                    "serviceId": 1,
                    "name": "Dọn dẹp theo giờ",
                    "description": "Lau dọn, hút bụi, làm sạch các bề mặt cơ bản trong nhà. Phù hợp cho nhu cầu duy trì vệ sinh hàng tuần.",
                    "basePrice": 50000.00,
                    "unit": "Giờ",
                    "estimatedDurationHours": 2.0,
                    "iconUrl": "https://res.cloudinary.com/dkzemgit8/image/upload/v1764171235/Cleaning_Clock-removebg-preview_o0oevs.png",
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
        "assignedEmployee": {
            "employeeId": "e1000001-0000-0000-0000-000000000020",
            "fullName": "Phạm Thị Dung Em",
            "avatar": "https://i.pravatar.cc/150?img=45",
            "rating": "HIGH",
            "employeeStatus": "AVAILABLE",
            "skills": [
                "Vệ sinh sofa",
                "Giặt thảm"
            ],
            "bio": "Chuyên vệ sinh nội thất cao cấp."
        },
        "status": "ACTIVE",
        "statusDisplay": "Đang hoạt động",
        "cancelledAt": null,
        "cancellationReason": null,
        "createdAt": "2025-12-09T15:43:15",
        "updatedAt": "2025-12-09T15:43:15",
        "totalGeneratedBookings": 3,
        "upcomingBookings": 3,
        "expectedBookingsInWindow": 3,
        "generatedBookingsInWindow": 3,
        "generationWindowDays": 7,
        "generationProgressPercent": 100.0
    },
    "success": true
}
```

---

## Luồng Hoạt Động

### Tạo Lịch Định Kỳ
1. Khách hàng gửi request tạo lịch định kỳ
2. Hệ thống validate:
   - Địa chỉ
   - Loại lặp lại (WEEKLY/MONTHLY)
   - Ngày lặp lại hợp lệ
   - Ngày bắt đầu phải từ hôm nay
   - Dịch vụ có thể đặt
3. Tạo bản ghi recurring booking
4. Hệ thống tìm nhân viên phù hợp (bất đồng bộ). Khi tìm được sẽ gán vào lịch định kỳ.
5. Ngay khi có nhân viên, hệ thống tạo (hoặc tái sử dụng) 1 cuộc hội thoại giữa khách hàng và nhân viên cho toàn bộ lịch định kỳ.
6. Scheduler/tác vụ nền sẽ tạo booking con cho các ngày phù hợp; các booking này dùng chung conversation của lịch định kỳ, không tạo conversation riêng.
7. Trả về thông tin lịch định kỳ, thống kê booking và conversation (có thể null nếu chưa gán được nhân viên ngay lúc tạo).

### Hủy Lịch Định Kỳ
1. Khách hàng gửi request hủy với lý do
2. Hệ thống:
   - Đánh dấu lịch định kỳ là CANCELLED
   - Xóa tất cả booking tương lai (status = PENDING hoặc AWAITING_EMPLOYEE)
   - Giữ lại các booking đã hoàn thành hoặc đang thực hiện

### Tự Động Tạo Booking
1. Scheduler chạy mỗi ngày lúc 2:00 AM
2. Tìm tất cả lịch định kỳ đang ACTIVE
3. Tạo booking cho 30 ngày tới nếu chưa có
4. Booking được tạo tự động có link về lịch định kỳ gốc

---

## Enum Values

### RecurrenceType
- `WEEKLY`: Lặp lại theo tuần
- `MONTHLY`: Lặp lại theo tháng

### RecurringBookingStatus
- `ACTIVE`: Đang hoạt động
- `CANCELLED`: Đã hủy
- `COMPLETED`: Đã hoàn thành (hết hạn)

---

## Validation Rules

### Recurrence Days
- **WEEKLY**: 
  - Giá trị: 1-7
  - 1 = Thứ 2, 2 = Thứ 3, ..., 7 = Chủ nhật
  - Ví dụ: [1, 3, 5] = Thứ 2, Thứ 4, Thứ 6
  
- **MONTHLY**:
  - Giá trị: 1-31
  - Ngày trong tháng
  - Ví dụ: [1, 15, 30] = Ngày 1, 15, 30 hàng tháng

### Dates
- `startDate`: Phải từ hôm nay trở đi
- `endDate`: Nếu có, phải sau `startDate`

### Booking Time
- Format: HH:mm:ss
- Giờ trong ngày khi tạo booking tự động

---

## Error Codes

| Code | Message |
|------|---------|
| 400 | Địa chỉ là bắt buộc |
| 400 | Ngày lặp lại không được để trống |
| 400 | Ngày trong tuần phải từ 1 (Thứ 2) đến 7 (Chủ nhật) |
| 400 | Ngày trong tháng phải từ 1 đến 31 |
| 400 | Ngày bắt đầu phải từ hôm nay trở đi |
| 400 | Ngày kết thúc phải sau ngày bắt đầu |
| 401 | Invalid or expired token |
| 404 | Không tìm thấy lịch định kỳ |
| 500 | Lỗi tạo lịch định kỳ |

---

## Notes

1. **Tự động tạo booking**: Scheduler chạy mỗi ngày lúc 2:00 AM để tạo booking cho 30 ngày tới
2. **Xóa booking tương lai**: Khi hủy lịch định kỳ, chỉ xóa booking có status PENDING hoặc AWAITING_EMPLOYEE
3. **Link booking với recurring**: Mỗi booking tự động tạo sẽ có `recurring_booking_id` để trace về lịch gốc
4. **Promotion**: Có thể áp dụng mã giảm giá cho tất cả booking trong lịch định kỳ
5. **Auto-assign nhân viên**: Booking tự động tạo sẽ được auto-assign nhân viên nếu có
6. **Conversation cho lịch định kỳ**: Lịch định kỳ sử dụng một conversation duy nhất giữa khách hàng và nhân viên được gán. Các booking con được sinh ra (kể cả trong `generatedBookingIds`) sẽ không tạo conversation riêng; mọi trao đổi diễn ra trong conversation của lịch định kỳ.
