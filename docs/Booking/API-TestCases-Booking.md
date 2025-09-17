# API Test Cases - Booking Management

## Overview
This document describes the minimum essential test cases for the **Booking Management** endpoints of the Customer API.  
The endpoints allow customers to manage bookings including creation, retrieval, validation, and default address lookup.  
**Base URL**: `/api/v1/customer/bookings`

---

## Test Case Structure
Each test case includes:
- **Test Case ID**: Unique identifier for the test case.
- **Description**: Purpose of the test.
- **Preconditions**: Requirements before executing the test.
- **Input**: Request data or headers.
- **Expected Output**: Expected response based on the API specification.
- **Status Code**: HTTP status code expected.

---

## Authentication Requirements
All endpoints require:
- **Authorization Header**: `Bearer <valid_token>`
- **Content-Type**: `application/json` (for POST requests)
- **Role Requirements**: 
  - Booking endpoints: CUSTOMER or ADMIN role required
  - Default address: "booking.create" permission required

---

## Database Test Data
Based on housekeeping_service_v8.sql:
- **Sample Customer**: john_doe (c1000001-0000-0000-0000-000000000001)
- **Sample Employee**: jane_smith (e1000001-0000-0000-0000-000000000001)
- **Sample Address**: adrs0001-0000-0000-0000-000000000001 (default address)
- **Sample Services**: Service ID 2 "Tổng vệ sinh" with base price 100000 VND
- **Service Choices**: Choice ID 2 "Nhà phố" (+250000), Choice ID 4 "Trên 80m²" (+250000)
- **Payment Methods**: Method ID 1 "MOMO", Method ID 2 "VNPAY"
- **Booking Statuses**: PENDING, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED

---

## API Endpoints Covered
1. **GET /{customerId}/default-address** - Get Customer Default Address
2. **POST /** - Create Booking
3. **GET /{bookingId}** - Get Booking Details
4. **POST /validate** - Validate Booking Request

---

## GET /{customerId}/default-address - Get Customer Default Address

### Test Case 1: Successfully Get Customer Default Address
- **Test Case ID**: TC_BOOKING_ADDRESS_001
- **Description**: Verify that a customer can retrieve their default address for booking.
- **Preconditions**: 
  - Customer is authenticated with valid token.
  - Customer has booking.create permission.
  - Customer has a default address.
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/customer/bookings/c1000001-0000-0000-0000-000000000001/default-address`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_customer_token_john_doe>
    ```
- **Expected Output**:
  ```json
  {
    "success": true,
    "data": {
      "addressId": "adrs0001-0000-0000-0000-000000000001",
      "customerId": "c1000001-0000-0000-0000-000000000001",
      "fullAddress": "123 Lê Trọng Tấn, Tây Thạnh, Tân Phú, TP. Hồ Chí Minh",
      "ward": "Phường Tây Thạnh",
      "district": "Quận Tân Phú",
      "city": "TP. Hồ Chí Minh",
      "isDefault": true
    }
  }
  ```
- **Status Code**: `200 OK`

---

### Test Case 2: Customer Without Default Address
- **Test Case ID**: TC_BOOKING_ADDRESS_002
- **Description**: Verify error handling when customer has no default address.
- **Preconditions**: 
  - Customer is authenticated with valid token.
  - Customer has no default address set.
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/customer/bookings/c1000001-0000-0000-0000-000000000999/default-address`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_customer_token>
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Khách hàng chưa có địa chỉ mặc định: c1000001-0000-0000-0000-000000000999"
  }
  ```
- **Status Code**: `404 Not Found`

---

### Test Case 3: Unauthorized Access to Default Address
- **Test Case ID**: TC_BOOKING_ADDRESS_003
- **Description**: Verify that users without booking.create permission cannot access address endpoint.
- **Preconditions**: User is authenticated but lacks booking.create permission.
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/customer/bookings/c1000001-0000-0000-0000-000000000001/default-address`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_token_no_booking_permission>
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Không có quyền đặt dịch vụ"
  }
  ```
- **Status Code**: `403 Forbidden`

---

## POST / - Create Booking

### Test Case 4: Successfully Create Booking
- **Test Case ID**: TC_BOOKING_004
- **Description**: Verify that a customer can successfully create a new booking with valid data.
- **Preconditions**:
  - Customer is authenticated with valid token.
  - Valid address ID exists for customer.
  - Selected employees are available at booking time.
  - Payment method exists and is active.
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/customer/bookings`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_customer_token>
    Content-Type: application/json
    ```
  - **Body**:
    ```json
    {
      "addressId": "adrs0001-0000-0000-0000-000000000001",
      "bookingTime": "2025-09-20T10:00:00",
      "note": "Cần dọn dẹp kỹ lưỡng phòng khách và bếp",
      "promoCode": null,
      "bookingDetails": [
        {
          "serviceId": 2,
          "quantity": 1,
          "expectedPrice": 600000,
          "expectedPricePerUnit": 600000,
          "selectedChoiceIds": [2, 4]
        }
      ],
      "assignments": [
        {
          "serviceId": 2,
          "employeeId": "e1000001-0000-0000-0000-000000000001"
        }
      ],
      "paymentMethodId": 1
    }
    ```
- **Expected Output**:
  ```json
  {
    "bookingId": "7a35373e-20c6-43a2-aab2-1486fb6c89e5",
    "bookingCode": "BK62589569",
    "status": "PENDING",
    "totalAmount": 700000.00,
    "formattedTotalAmount": "600,000đ",
    "bookingTime": "2025-09-20T10:00:00",
    "createdAt": "2025-09-15T14:39:22.589451644",
    "customerInfo": {
      "addressId": "adrs0001-0000-0000-0000-000000000001",
      "fullAddress": "123 Lê Trọng Tấn, Tây Thạnh, Tân Phú, TP. Hồ Chí Minh",
      "ward": "Phường Tây Thạnh",
      "district": "Quận Tân Phú",
      "city": "TP. Hồ Chí Minh",
      "latitude": null,
      "longitude": null,
      "isDefault": true
    },
    "serviceDetails": [
        {
            "bookingDetailId": "5bbc2de4-e9b9-4fc1-8ff1-28d64e6d19c3",
            "service": {
                "serviceId": 2,
                "name": "Tổng vệ sinh",
                "description": "Làm sạch sâu toàn diện, bao gồm các khu vực khó tiếp cận, trần nhà, lau cửa kính. Thích hợp cho nhà mới hoặc dọn dẹp theo mùa.",
                "basePrice": 500000.00,
                "unit": "Gói",
                "estimatedDurationHours": 2.0,
                "iconUrl": "https://res.cloudinary.com/dkzemgit8/image/upload/v1757599581/house_cleaning_nob_umewqf.png",
                "categoryName": "Dọn dẹp nhà",
                "isActive": true
            },
            "quantity": 1,
            "pricePerUnit": 700000,
            "formattedPricePerUnit": "700,000đ",
            "subTotal": 700000.00,
            "formattedSubTotal": "700,000đ",
            "selectedChoices": [
                {
                    "choiceId": 2,
                    "choiceName": "Nhà phố",
                    "optionName": "Loại hình nhà ở?",
                    "priceAdjustment": 200000.00,
                    "formattedPriceAdjustment": "200,000đ"
                },
                {
                    "choiceId": 4,
                    "choiceName": "Trên 80m²",
                    "optionName": "Diện tích dọn dẹp?",
                    "priceAdjustment": 200000.00,
                    "formattedPriceAdjustment": "200,000đ"
                }
            ],
            "assignments": [],
            "duration": "2 giờ",
            "formattedDuration": "2 giờ"
        }
    ],
    "paymentInfo": {
      "paymentId": "d2068e26-7333-43a4-a9e5-5a17b23ca7dc",
      "amount": 600000.00,
      "paymentMethod": null,
      "paymentStatus": "PENDING",
      "transactionCode": "TXN_1757169562581",
      "createdAt": "2025-09-15 14:39:22",
      "paidAt": null
    },
    "promotionApplied": null,
    "assignedEmployees": [
      {
        "employeeId": "e1000001-0000-0000-0000-000000000001",
        "fullName": "Jane Smith",
        "email": "jane.smith@example.com",
        "phoneNumber": "0912345678",
        "avatar": "https://picsum.photos/200",
        "rating": null,
        "employeeStatus": "AVAILABLE",
        "skills": ["Cleaning", "Organizing"],
        "bio": "Có kinh nghiệm dọn dẹp nhà cửa và sắp xếp đồ đạc."
      }
    ],
    "totalServices": 1,
    "totalEmployees": 1,
    "estimatedDuration": "2 hours 0 minutes",
    "hasPromotion": false
  }
  ```
- **Status Code**: `201 Created`

---

### Test Case 5: Create Booking - Employee Conflict
- **Test Case ID**: TC_BOOKING_005
- **Description**: Verify that booking creation fails when selected employee has scheduling conflict.
- **Preconditions**:
  - Customer is authenticated with valid token.
  - Employee has another assignment at requested time.
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/customer/bookings`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_customer_token>
    Content-Type: application/json
    ```
  - **Body**:
    ```json
    {
      "addressId": "adrs0001-0000-0000-0000-000000000001",
      "bookingTime": "2025-09-20T09:00:00",
      "note": "Test conflict",
      "promoCode": null,
      "bookingDetails": [
        {
          "serviceId": 2,
          "quantity": 1,
          "expectedPrice": 600000,
          "expectedPricePerUnit": 600000,
          "selectedChoiceIds": [2, 4]
        }
      ],
      "assignments": [
        {
          "serviceId": 2,
          "employeeId": "e1000001-0000-0000-0000-000000000001"
        }
      ],
      "paymentMethodId": 1
    }
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Nhân viên Jane Smith có lịch trình xung đột vào thời gian từ 2025-09-20T08:00:00 đến 2025-09-20T10:00:00",
    "errorCode": "EMPLOYEE_CONFLICT_ERROR",
    "validationErrors": [],
    "conflicts": [
      {
        "conflictType": "ASSIGNMENT_CONFLICT",
        "employeeId": "e1000001-0000-0000-0000-000000000001",
        "employeeName": "Jane Smith",
        "conflictStartTime": "2025-09-20T08:00:00",
        "conflictEndTime": "2025-09-20T10:00:00",
        "reason": "Nhân viên Jane Smith có một cuộc hẹn khác trong thời gian này"
      }
    ],
    "timestamp": "2025-09-15T15:30:00"
  }
  ```
- **Status Code**: `400 Bad Request`

---

### Test Case 6: Create Booking - Validation Error
- **Test Case ID**: TC_BOOKING_006
- **Description**: Verify validation error when required fields are missing or invalid.
- **Preconditions**: Customer is authenticated with valid token.
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/customer/bookings`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_customer_token>
    Content-Type: application/json
    ```
  - **Body**:
    ```json
    {
      "addressId": "",
      "bookingTime": "2025-09-15T10:00:00",
      "note": "Test validation",
      "promoCode": null,
      "bookingDetails": [],
      "assignments": [],
      "paymentMethodId": 1
    }
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Dữ liệu không hợp lệ",
    "validationErrors": [
      {
        "field": "addressId",
        "message": "Address ID is required"
      },
      {
        "field": "bookingTime",
        "message": "Booking time must be in the future"
      },
      {
        "field": "bookingDetails",
        "message": "Booking details cannot be empty"
      },
      {
        "field": "assignments",
        "message": "Assignments cannot be empty"
      }
    ]
  }
  ```
- **Status Code**: `400 Bad Request`

---

### Test Case 7: Create Booking - Service Not Found
- **Test Case ID**: TC_BOOKING_007
- **Description**: Verify validation error when service ID does not exist or is not bookable.
- **Preconditions**: Customer is authenticated with valid token.
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/customer/bookings`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_customer_token>
    Content-Type: application/json
    ```
  - **Body**:
    ```json
    {
      "addressId": "adrs0001-0000-0000-0000-000000000001",
      "bookingTime": "2025-09-20T10:00:00",
      "note": "Service validation test",
      "promoCode": null,
      "bookingDetails": [
        {
          "serviceId": 9999,
          "quantity": 1,
          "expectedPrice": 100000,
          "expectedPricePerUnit": 100000,
          "selectedChoiceIds": []
        }
      ],
      "assignments": [
        {
          "serviceId": 9999,
          "employeeId": "e1000001-0000-0000-0000-000000000001"
        }
      ],
      "paymentMethodId": 1
    }
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Booking validation failed",
    "errors": [
      "Service not found or not bookable: 9999"
    ],
    "validationErrors": []
  }
  ```
- **Status Code**: `400 Bad Request`

---

### Test Case 8: Create Booking - Invalid Choice IDs
- **Test Case ID**: TC_BOOKING_008
- **Description**: Verify validation error when selected choice IDs are invalid for the service.
- **Preconditions**: Customer is authenticated with valid token.
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/customer/bookings`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_customer_token>
    Content-Type: application/json
    ```
  - **Body**:
    ```json
    {
      "addressId": "adrs0001-0000-0000-0000-000000000001",
      "bookingTime": "2025-09-20T10:00:00",
      "note": "Invalid choice IDs test",
      "promoCode": null,
      "bookingDetails": [
        {
          "serviceId": 2,
          "quantity": 1,
          "expectedPrice": 600000,
          "expectedPricePerUnit": 600000,
          "selectedChoiceIds": [2, 4, 999, 888]
        }
      ],
      "assignments": [
        {
          "serviceId": 2,
          "employeeId": "e1000001-0000-0000-0000-000000000001"
        }
      ],
      "paymentMethodId": 1
    }
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Booking validation failed",
    "errors": [
      "Invalid choice IDs for service 2: [999, 888]"
    ],
    "validationErrors": []
  }
  ```
- **Status Code**: `400 Bad Request`

---

### Test Case 9: Create Booking - Booking Time Too Soon
- **Test Case ID**: TC_BOOKING_009
- **Description**: Verify validation error when booking time is less than 2 hours from now.
- **Preconditions**: Customer is authenticated with valid token.
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/customer/bookings`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_customer_token>
    Content-Type: application/json
    ```
  - **Body**:
    ```json
    {
      "addressId": "adrs0001-0000-0000-0000-000000000001",
      "bookingTime": "2025-09-15T16:00:00",
      "note": "Booking time too soon test",
      "promoCode": null,
      "bookingDetails": [
        {
          "serviceId": 2,
          "quantity": 1,
          "expectedPrice": 600000,
          "expectedPricePerUnit": 600000,
          "selectedChoiceIds": [2, 4]
        }
      ],
      "assignments": [
        {
          "serviceId": 2,
          "employeeId": "e1000001-0000-0000-0000-000000000001"
        }
      ],
      "paymentMethodId": 1
    }
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Booking validation failed",
    "errors": [
      "Booking time must be at least 2 hours from now"
    ],
    "validationErrors": []
  }
  ```
- **Status Code**: `400 Bad Request`

---

### Test Case 10: Create Booking - Outside Business Hours
- **Test Case ID**: TC_BOOKING_010
- **Description**: Verify validation error when booking time is outside business hours (8 AM - 8 PM).
- **Preconditions**: Customer is authenticated with valid token.
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/customer/bookings`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_customer_token>
    Content-Type: application/json
    ```
  - **Body**:
    ```json
    {
      "addressId": "adrs0001-0000-0000-0000-000000000001",
      "bookingTime": "2025-09-20T22:00:00",
      "note": "Outside business hours test",
      "promoCode": null,
      "bookingDetails": [
        {
          "serviceId": 2,
          "quantity": 1,
          "expectedPrice": 600000,
          "expectedPricePerUnit": 600000,
          "selectedChoiceIds": [2, 4]
        }
      ],
      "assignments": [
        {
          "serviceId": 2,
          "employeeId": "e1000001-0000-0000-0000-000000000001"
        }
      ],
      "paymentMethodId": 1
    }
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Booking validation failed",
    "errors": [
      "Booking time must be between 8:00 AM and 8:00 PM"
    ],
    "validationErrors": []
  }
  ```
- **Status Code**: `400 Bad Request`

---

### Test Case 11: Create Booking - Employee Assignment Mismatch
- **Test Case ID**: TC_BOOKING_011
- **Description**: Verify validation error when assigned employees don't match required quantity.
- **Preconditions**: Customer is authenticated with valid token.
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/customer/bookings`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_customer_token>
    Content-Type: application/json
    ```
  - **Body**:
    ```json
    {
      "addressId": "adrs0001-0000-0000-0000-000000000001",
      "bookingTime": "2025-09-20T10:00:00",
      "note": "Employee assignment mismatch test",
      "promoCode": null,
      "bookingDetails": [
        {
          "serviceId": 2,
          "quantity": 2,
          "expectedPrice": 1200000,
          "expectedPricePerUnit": 600000,
          "selectedChoiceIds": [2, 4]
        }
      ],
      "assignments": [
        {
          "serviceId": 2,
          "employeeId": "e1000001-0000-0000-0000-000000000001"
        }
      ],
      "paymentMethodId": 1
    }
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Booking validation failed",
    "errors": [
      "Total employees assigned (1) does not match required employees (2)"
    ],
    "validationErrors": []
  }
  ```
- **Status Code**: `400 Bad Request`

---

### Test Case 12: Create Booking - Invalid Promotion Code
- **Test Case ID**: TC_BOOKING_012
- **Description**: Verify validation error when promotion code is invalid or expired.
- **Preconditions**: Customer is authenticated with valid token.
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/customer/bookings`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_customer_token>
    Content-Type: application/json
    ```
  - **Body**:
    ```json
    {
      "addressId": "adrs0001-0000-0000-0000-000000000001",
      "bookingTime": "2025-09-20T10:00:00",
      "note": "Invalid promo code test",
      "promoCode": "INVALID_CODE",
      "bookingDetails": [
        {
          "serviceId": 2,
          "quantity": 1,
          "expectedPrice": 600000,
          "expectedPricePerUnit": 600000,
          "selectedChoiceIds": [2, 4]
        }
      ],
      "assignments": [
        {
          "serviceId": 2,
          "employeeId": "e1000001-0000-0000-0000-000000000001"
        }
      ],
      "paymentMethodId": 1
    }
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Booking validation failed",
    "errors": [
      "Promotion code is invalid or expired: INVALID_CODE"
    ],
    "validationErrors": []
  }
  ```
- **Status Code**: `400 Bad Request`

---

### Test Case 13: Create Booking - Employee Not Found
- **Test Case ID**: TC_BOOKING_013
- **Description**: Verify validation error when assigned employee does not exist.
- **Preconditions**: Customer is authenticated with valid token.
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/customer/bookings`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_customer_token>
    Content-Type: application/json
    ```
  - **Body**:
    ```json
    {
      "addressId": "adrs0001-0000-0000-0000-000000000001",
      "bookingTime": "2025-09-20T10:00:00",
      "note": "Employee not found test",
      "promoCode": null,
      "bookingDetails": [
        {
          "serviceId": 2,
          "quantity": 1,
          "expectedPrice": 600000,
          "expectedPricePerUnit": 600000,
          "selectedChoiceIds": [2, 4]
        }
      ],
      "assignments": [
        {
          "serviceId": 2,
          "employeeId": "non-existent-employee-id"
        }
      ],
      "paymentMethodId": 1
    }
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Employee validation failed",
    "conflicts": [
      {
        "conflictType": "EMPLOYEE_NOT_FOUND",
        "employeeId": "non-existent-employee-id",
        "conflictStartTime": "2025-09-20T10:00:00",
        "conflictEndTime": "2025-09-20T10:00:00",
        "reason": "Employee not found"
      }
    ],
    "validationErrors": []
  }
  ```
- **Status Code**: `400 Bad Request`

---

### Test Case 14: Create Booking - Price Calculation Mismatch
- **Test Case ID**: TC_BOOKING_014
- **Description**: Verify validation error when expected price differs significantly from calculated price.
- **Preconditions**: Customer is authenticated with valid token.
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/customer/bookings`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_customer_token>
    Content-Type: application/json
    ```
  - **Body**:
    ```json
    {
      "addressId": "adrs0001-0000-0000-0000-000000000001",
      "bookingTime": "2025-09-20T10:00:00",
      "note": "Price mismatch test",
      "promoCode": null,
      "bookingDetails": [
        {
          "serviceId": 2,
          "quantity": 1,
          "expectedPrice": 100000,
          "expectedPricePerUnit": 100000,
          "selectedChoiceIds": [2, 4]
        }
      ],
      "assignments": [
        {
          "serviceId": 2,
          "employeeId": "e1000001-0000-0000-0000-000000000001"
        }
      ],
      "paymentMethodId": 1
    }
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Booking validation failed",
    "errors": [
      "Price calculation mismatch for service 2: expected 100000 VND, calculated 600000 VND (difference exceeds 1000 VND tolerance)"
    ],
    "validationErrors": []
  }
  ```
- **Status Code**: `400 Bad Request`

---

### Test Case 15: Create Booking - Booking Too Far in Future
- **Test Case ID**: TC_BOOKING_015
- **Description**: Verify validation error when booking time is more than 30 days in the future.
- **Preconditions**: Customer is authenticated with valid token.
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/customer/bookings`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_customer_token>
    Content-Type: application/json
    ```
  - **Body**:
    ```json
    {
      "addressId": "adrs0001-0000-0000-0000-000000000001",
      "bookingTime": "2025-11-20T10:00:00",
      "note": "Too far in future test",
      "promoCode": null,
      "bookingDetails": [
        {
          "serviceId": 2,
          "quantity": 1,
          "expectedPrice": 600000,
          "expectedPricePerUnit": 600000,
          "selectedChoiceIds": [2, 4]
        }
      ],
      "assignments": [
        {
          "serviceId": 2,
          "employeeId": "e1000001-0000-0000-0000-000000000001"
        }
      ],
      "paymentMethodId": 1
    }
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Booking validation failed",
    "errors": [
      "Booking time cannot be more than 30 days in the future"
    ],
    "validationErrors": []
  }
  ```
- **Status Code**: `400 Bad Request`

---

### Test Case 16: Create Booking - Customer Address Not Found
- **Test Case ID**: TC_BOOKING_016
- **Description**: Verify validation error when address ID does not belong to the customer.
- **Preconditions**: Customer is authenticated with valid token.
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/customer/bookings`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_customer_token>
    Content-Type: application/json
    ```
  - **Body**:
    ```json
    {
      "addressId": "adrs0001-0000-0000-0000-000000000999",
      "bookingTime": "2025-09-20T10:00:00",
      "note": "Address not found test",
      "promoCode": null,
      "bookingDetails": [
        {
          "serviceId": 2,
          "quantity": 1,
          "expectedPrice": 600000,
          "expectedPricePerUnit": 600000,
          "selectedChoiceIds": [2, 4]
        }
      ],
      "assignments": [
        {
          "serviceId": 2,
          "employeeId": "e1000001-0000-0000-0000-000000000001"
        }
      ],
      "paymentMethodId": 1
    }
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Booking validation failed",
    "errors": [
      "Address not found or does not belong to customer: adrs0001-0000-0000-0000-000000000999"
    ],
    "validationErrors": []
  }
  ```
- **Status Code**: `400 Bad Request`

---

### Test Case 17: Create Booking - Promotion Usage Limit Exceeded
- **Test Case ID**: TC_BOOKING_017
- **Description**: Verify validation error when promotion code has already been used by the customer (limit exceeded).
- **Preconditions**: 
  - Customer is authenticated with valid token.
  - Customer has already used the promotion code "NEWCUSTOMER10" once.
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/customer/bookings`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_customer_token>
    Content-Type: application/json
    ```
  - **Body**:
    ```json
    {
      "addressId": "adrs0001-0000-0000-0000-000000000001",
      "bookingTime": "2025-09-20T10:00:00",
      "note": "Promo usage limit test",
      "promoCode": "NEWCUSTOMER10",
      "bookingDetails": [
        {
          "serviceId": 2,
          "quantity": 1,
          "expectedPrice": 540000,
          "expectedPricePerUnit": 540000,
          "selectedChoiceIds": [2, 4]
        }
      ],
      "assignments": [
        {
          "serviceId": 2,
          "employeeId": "e1000001-0000-0000-0000-000000000001"
        }
      ],
      "paymentMethodId": 1
    }
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Booking validation failed",
    "errors": [
      "Promotion code usage limit exceeded: NEWCUSTOMER10"
    ],
    "validationErrors": []
  }
  ```
- **Status Code**: `400 Bad Request`

---

### Test Case 18: Create Booking - Inactive Service
- **Test Case ID**: TC_BOOKING_018
- **Description**: Verify validation error when trying to book an inactive service.
- **Preconditions**: Customer is authenticated with valid token.
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/customer/bookings`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_customer_token>
    Content-Type: application/json
    ```
  - **Body**:
    ```json
    {
      "addressId": "adrs0001-0000-0000-0000-000000000001",
      "bookingTime": "2025-09-20T10:00:00",
      "note": "Inactive service test",
      "promoCode": null,
      "bookingDetails": [
        {
          "serviceId": 99,
          "quantity": 1,
          "expectedPrice": 100000,
          "expectedPricePerUnit": 100000,
          "selectedChoiceIds": []
        }
      ],
      "assignments": [
        {
          "serviceId": 99,
          "employeeId": "e1000001-0000-0000-0000-000000000001"
        }
      ],
      "paymentMethodId": 1
    }
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Booking validation failed",
    "errors": [
      "Service not found or not bookable: 99"
    ],
    "validationErrors": []
  }
  ```
- **Status Code**: `400 Bad Request`

---

## GET /{bookingId} - Get Booking Details

### Test Case 19: Successfully Get Booking Details
- **Test Case ID**: TC_BOOKING_019
- **Description**: Verify that a customer can retrieve detailed information about their booking.
- **Preconditions**: 
  - Customer is authenticated with valid token.
  - Booking exists and belongs to the customer.
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/customer/bookings/7a35373e-20c6-43a2-aab2-1486fb6c89e5`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_customer_token>
    ```
- **Expected Output**:
  ```json
  {
    "bookingId": "7a35373e-20c6-43a2-aab2-1486fb6c89e5",
    "bookingCode": "BK62589569",
    "status": "PENDING",
    "totalAmount": 600000.00,
    "formattedTotalAmount": "600,000đ",
    "bookingTime": "2025-09-20T10:00:00",
    "note": "Cần dọn dẹp kỹ lưỡng phòng khách và bếp",
    "createdAt": "2025-09-15T14:39:22.589451644",
    "customerInfo": {
      "customerId": "c1000001-0000-0000-0000-000000000001",
      "fullName": "John Doe",
      "email": "john.doe@example.com",
      "phoneNumber": "0123456789",
      "addressInfo": {
        "addressId": "adrs0001-0000-0000-0000-000000000001",
        "fullAddress": "123 Lê Trọng Tấn, Tây Thạnh, Tân Phú, TP. Hồ Chí Minh",
        "ward": "Phường Tây Thạnh",
        "district": "Quận Tân Phú",
        "city": "TP. Hồ Chí Minh",
        "latitude": null,
        "longitude": null,
        "isDefault": true
      }
    },
    "serviceDetails": [
      {
        "bookingDetailId": "eb5fdc71-eeda-4588-9c56-e2ac72bbd859",
        "service": {
          "serviceId": 2,
          "name": "Tổng vệ sinh",
          "description": "Làm sạch sâu toàn diện, bao gồm các khu vực khó tiếp cận, trần nhà, lau cửa kính. Thích hợp cho nhà mới hoặc dọn dẹp theo mùa.",
          "basePrice": 100000.00,
          "unit": "Gói",
          "estimatedDurationHours": 2.0,
          "recommendedStaff": 3,
          "categoryName": "Dọn dẹp nhà",
          "isActive": true
        },
        "quantity": 1,
        "pricePerUnit": 600000,
        "formattedPricePerUnit": "600,000đ",
        "subTotal": 600000.00,
        "formattedSubTotal": "600,000đ",
        "selectedChoices": [
          {
            "choiceId": 2,
            "choiceName": "Nhà phố",
            "optionName": "Loại hình nhà ở?",
            "priceAdjustment": 250000.00,
            "formattedPriceAdjustment": "250,000đ"
          },
          {
            "choiceId": 4,
            "choiceName": "Trên 80m²",
            "optionName": "Diện tích dọn dẹp?",
            "priceAdjustment": 250000.00,
            "formattedPriceAdjustment": "250,000đ"
          }
        ]
      }
    ],
    "paymentInfo": {
      "paymentId": "d2068e26-7333-43a4-a9e5-5a17b23ca7dc",
      "amount": 600000.00,
      "paymentMethod": {
        "paymentMethodId": 1,
        "name": "MOMO",
        "isActive": true
      },
      "paymentStatus": "PENDING",
      "transactionCode": "TXN_1757169562581",
      "createdAt": "2025-09-15 14:39:22",
      "paidAt": null
    },
    "promotionApplied": null,
    "assignedEmployees": [
      {
        "employeeId": "e1000001-0000-0000-0000-000000000001",
        "fullName": "Jane Smith",
        "email": "jane.smith@example.com",
        "phoneNumber": "0912345678",
        "avatar": "https://picsum.photos/200",
        "rating": null,
        "employeeStatus": "AVAILABLE",
        "skills": ["Cleaning", "Organizing"],
        "bio": "Có kinh nghiệm dọn dẹp nhà cửa và sắp xếp đồ đạc."
      }
    ]
  }
  ```
- **Status Code**: `200 OK`

---

### Test Case 20: Get Booking Details - Booking Not Found
- **Test Case ID**: TC_BOOKING_020
- **Description**: Verify error handling when booking ID does not exist.
- **Preconditions**: Customer is authenticated with valid token.
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/customer/bookings/non-existent-booking-id`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_customer_token>
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Booking not found with ID: non-existent-booking-id",
    "errorCode": "BOOKING_NOT_FOUND",
    "timestamp": "2025-09-15T15:30:00"
  }
  ```
- **Status Code**: `404 Not Found`

---

### Test Case 21: Get Booking Details - Unauthorized Access
- **Test Case ID**: TC_BOOKING_021
- **Description**: Verify that customers cannot access bookings that don't belong to them.
- **Preconditions**: 
  - Customer is authenticated with valid token.
  - Booking exists but belongs to a different customer.
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/customer/bookings/7a35373e-20c6-43a2-aab2-1486fb6c89e5`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_customer_token_different_customer>
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Access denied. You can only view your own bookings.",
    "errorCode": "ACCESS_DENIED",
    "timestamp": "2025-09-15T15:30:00"
  }
  ```
- **Status Code**: `403 Forbidden`

---

## POST /validate - Validate Booking Request

### Test Case 22: Successfully Validate Booking Request
- **Test Case ID**: TC_BOOKING_022
- **Description**: Verify that a valid booking request passes validation and returns calculated pricing.
- **Preconditions**: 
  - Customer is authenticated with valid token.
  - All services, employees, and addresses are valid.
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/customer/bookings/validate`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_customer_token>
    Content-Type: application/json
    ```
  - **Body**:
    ```json
    {
      "addressId": "adrs0001-0000-0000-0000-000000000001",
      "bookingTime": "2025-09-20T10:00:00",
      "note": "Validation test",
      "promoCode": "NEWCUSTOMER10",
      "bookingDetails": [
        {
          "serviceId": 2,
          "quantity": 1,
          "expectedPrice": 540000,
          "expectedPricePerUnit": 540000,
          "selectedChoiceIds": [2, 4]
        }
      ],
      "assignments": [
        {
          "serviceId": 2,
          "employeeId": "e1000001-0000-0000-0000-000000000001"
        }
      ],
      "paymentMethodId": 1
    }
    ```
- **Expected Output**:
  ```json
  {
    "valid": true,
    "calculatedTotalAmount": 540000.00,
    "formattedTotalAmount": "540,000đ",
    "serviceValidations": [
      {
        "serviceId": 2,
        "serviceName": "Tổng vệ sinh",
        "exists": true,
        "active": true,
        "basePrice": 100000.00,
        "validChoiceIds": [2, 4],
        "invalidChoiceIds": [],
        "calculatedPrice": 600000.00,
        "expectedPrice": 540000.00,
        "priceMatches": true
      }
    ],
    "promotionApplied": {
      "promoCode": "NEWCUSTOMER10",
      "discountType": "PERCENTAGE",
      "discountValue": 10.0,
      "discountAmount": 60000.00,
      "minimumOrderAmount": 500000.00
    },
    "errors": [],
    "conflicts": []
  }
  ```
- **Status Code**: `200 OK`

---

### Test Case 23: Validate Booking Request - Validation Errors
- **Test Case ID**: TC_BOOKING_023
- **Description**: Verify that invalid booking requests return validation errors without creating booking.
- **Preconditions**: Customer is authenticated with valid token.
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/customer/bookings/validate`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_customer_token>
    Content-Type: application/json
    ```
  - **Body**:
    ```json
    {
      "addressId": "adrs0001-0000-0000-0000-000000000001",
      "bookingTime": "2025-09-15T16:00:00",
      "note": "Validation test with errors",
      "promoCode": null,
      "bookingDetails": [
        {
          "serviceId": 9999,
          "quantity": 1,
          "expectedPrice": 100000,
          "expectedPricePerUnit": 100000,
          "selectedChoiceIds": [999]
        }
      ],
      "assignments": [
        {
          "serviceId": 9999,
          "employeeId": "non-existent-employee"
        }
      ],
      "paymentMethodId": 1
    }
    ```
- **Expected Output**:
  ```json
  {
    "valid": false,
    "calculatedTotalAmount": 0.00,
    "formattedTotalAmount": "0đ",
    "serviceValidations": [],
    "promotionApplied": null,
    "errors": [
      "Booking time must be at least 2 hours from now",
      "Service not found or not bookable: 9999"
    ],
    "conflicts": [
      {
        "conflictType": "EMPLOYEE_NOT_FOUND",
        "employeeId": "non-existent-employee",
        "conflictStartTime": "2025-09-15T16:00:00",
        "conflictEndTime": "2025-09-15T16:00:00",
        "reason": "Employee not found"
      }
    ]
  }
  ```
- **Status Code**: `200 OK`

---

### Test Case 24: Validate Booking Request - Employee Conflicts
- **Test Case ID**: TC_BOOKING_024
- **Description**: Verify that employee conflicts are detected during validation.
- **Preconditions**: 
  - Customer is authenticated with valid token.
  - Employee has existing assignment at the requested time.
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/customer/bookings/validate`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_customer_token>
    Content-Type: application/json
    ```
  - **Body**:
    ```json
    {
      "addressId": "adrs0001-0000-0000-0000-000000000001",
      "bookingTime": "2025-09-20T09:00:00",
      "note": "Conflict validation test",
      "promoCode": null,
      "bookingDetails": [
        {
          "serviceId": 2,
          "quantity": 1,
          "expectedPrice": 600000,
          "expectedPricePerUnit": 600000,
          "selectedChoiceIds": [2, 4]
        }
      ],
      "assignments": [
        {
          "serviceId": 2,
          "employeeId": "e1000001-0000-0000-0000-000000000001"
        }
      ],
      "paymentMethodId": 1
    }
    ```
- **Expected Output**:
  ```json
  {
    "valid": false,
    "calculatedTotalAmount": 600000.00,
    "formattedTotalAmount": "600,000đ",
    "serviceValidations": [
      {
        "serviceId": 2,
        "serviceName": "Tổng vệ sinh",
        "exists": true,
        "active": true,
        "basePrice": 100000.00,
        "validChoiceIds": [2, 4],
        "invalidChoiceIds": [],
        "calculatedPrice": 600000.00,
        "expectedPrice": 600000.00,
        "priceMatches": true
      }
    ],
    "promotionApplied": null,
    "errors": [],
    "conflicts": [
      {
        "conflictType": "ASSIGNMENT_CONFLICT",
        "employeeId": "e1000001-0000-0000-0000-000000000001",
        "employeeName": "Jane Smith",
        "conflictStartTime": "2025-09-20T08:00:00",
        "conflictEndTime": "2025-09-20T10:00:00",
        "reason": "Nhân viên Jane Smith có một cuộc hẹn khác trong thời gian này"
      }
    ]
  }
  ```
- **Status Code**: `200 OK`

---

### Test Case 25: Successfully Create Booking
- **Test Case ID**: TC_BOOKING_025
- **Description**: Verify that a customer can successfully create a new booking with valid data.
- **Preconditions**:
  - Customer is authenticated with valid token.
  - Valid address ID exists for customer.
  - Selected employees are available at booking time.
  - Payment method exists and is active.
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/customer/bookings`
  - **Headers**:
    ```
    Authorization: Bearer <valid_customer_token>
    Content-Type: application/json
    ```
  - **Body**:
    ```json
    {
    "addressId": "adrs0001-0000-0000-0000-000000000001",
    "bookingTime": "2025-09-20T10:00:00",
    "note": "Cần dọn dẹp kỹ lưỡng phòng khách và bếp",
    "promoCode": null,
    "bookingDetails": [
        {
            "serviceId": 2,
            "quantity": 1,
            "expectedPrice": 700000,
            "expectedPricePerUnit": 700000,
            "selectedChoiceIds": [
                2,
                4
            ]
        }
    ],
    "assignments": [],
    "paymentMethodId": 1
    }
    ```
- **Expected Output**:
  ```json
  {
    "bookingId": "827eb307-64bd-47ba-894a-88bb8085beeb",
    "bookingCode": "BK65574301",
    "status": "AWAITING_EMPLOYEE",
    "totalAmount": 700000.00,
    "formattedTotalAmount": "700,000đ",
    "bookingTime": "2025-09-20T10:00:00",
    "createdAt": "2025-09-17T20:49:25.642672",
    "customerInfo": {
        "addressId": "adrs0001-0000-0000-0000-000000000001",
        "fullAddress": "123 Lê Trọng Tấn, Tây Thạnh, Tân Phú, TP. Hồ Chí Minh",
        "ward": "Phường Tây Thạnh",
        "district": "Quận Tân Phú",
        "city": "TP. Hồ Chí Minh",
        "latitude": null,
        "longitude": null,
        "isDefault": true
    },
    "serviceDetails": [
        {
            "bookingDetailId": "5bbc2de4-e9b9-4fc1-8ff1-28d64e6d19c3",
            "service": {
                "serviceId": 2,
                "name": "Tổng vệ sinh",
                "description": "Làm sạch sâu toàn diện, bao gồm các khu vực khó tiếp cận, trần nhà, lau cửa kính. Thích hợp cho nhà mới hoặc dọn dẹp theo mùa.",
                "basePrice": 500000.00,
                "unit": "Gói",
                "estimatedDurationHours": 2.0,
                "iconUrl": "https://res.cloudinary.com/dkzemgit8/image/upload/v1757599581/house_cleaning_nob_umewqf.png",
                "categoryName": "Dọn dẹp nhà",
                "isActive": true
            },
            "quantity": 1,
            "pricePerUnit": 700000,
            "formattedPricePerUnit": "700,000đ",
            "subTotal": 700000.00,
            "formattedSubTotal": "700,000đ",
            "selectedChoices": [
                {
                    "choiceId": 2,
                    "choiceName": "Nhà phố",
                    "optionName": "Loại hình nhà ở?",
                    "priceAdjustment": 200000.00,
                    "formattedPriceAdjustment": "200,000đ"
                },
                {
                    "choiceId": 4,
                    "choiceName": "Trên 80m²",
                    "optionName": "Diện tích dọn dẹp?",
                    "priceAdjustment": 200000.00,
                    "formattedPriceAdjustment": "200,000đ"
                }
            ],
            "assignments": [],
            "duration": "2 giờ",
            "formattedDuration": "2 giờ"
        }
    ],
    "paymentInfo": {
        "paymentId": "d7b35dc4-54ea-44c9-91ac-4305c6ee0c4b",
        "amount": 700000.00,
        "paymentMethod": "Thanh toán tiền mặt",
        "paymentStatus": "PENDING",
        "transactionCode": "TXN_1758116965567",
        "createdAt": "2025-09-17 20:49:25",
        "paidAt": null
    },
    "promotionApplied": null,
    "assignedEmployees": [],
    "totalServices": 1,
    "totalEmployees": 0,
    "estimatedDuration": "2 hours 0 minutes",
    "hasPromotion": false
    }
  ```
---

## Notes
- **Test Environment**: Database should be configured with test data from housekeeping_service_v8.sql.
- **Authentication**: All endpoints require valid JWT tokens with CUSTOMER or ADMIN roles.
- **Authorization**: 
  - Default address endpoint requires "booking.create" permission
  - Other endpoints require CUSTOMER or ADMIN role
- **Transaction Management**: Booking operations are wrapped in database transactions.
- **Error Handling**: Service layer catches exceptions and returns appropriate error responses.
- **Security**: JWT tokens are validated for format, expiration, and role authorization.
- **Business Rules**: 
  - Booking time must be at least 2 hours in the future
  - Booking time must be within business hours (8 AM - 8 PM)
  - Booking time cannot be more than 30 days in advance
  - Employee availability is checked for conflicts
  - Service prices are validated against expected prices (tolerance: 1000 VND)
  - Address must belong to the customer
  - Services must be active and available
- **Validation**: All request DTOs are validated using Jakarta Bean Validation annotations.
- **Price Calculation**: Service pricing includes base price plus selected choice adjustments.
- **Employee Management**: Employee conflicts are checked using estimated service duration.
- **Payment Integration**: Payment records are created with pending status for external gateway processing.