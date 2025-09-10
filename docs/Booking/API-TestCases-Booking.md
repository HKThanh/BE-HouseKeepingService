# API Test Cases - Booking Controller

## Overview
This document describes the test cases for the **Booking Management** endpoints of the Customer API.  
The endpoints allow authenticated customers to create, validate, and retrieve booking information for house keeping services.  
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
- **Authorization Header**: `Bearer <valid_customer_token>`
- **Content-Type**: `application/json`
- **Role**: Accessible by CUSTOMER and ADMIN roles
- **Token Validation**: JWT token must be valid and not expired

---

## Database Test Data
Based on housekeeping_service_v8.sql:
- **Customers**: 
  - john_doe (ID: a1000001-0000-0000-0000-000000000001, Customer ID: c1000001-0000-0000-0000-000000000001)
  - jane_smith (ID: a1000001-0000-0000-0000-000000000002, Customer ID: c1000001-0000-0000-0000-000000000003)
- **Services**: 
  - Service ID 1: "Dọn dẹp theo giờ" (50,000đ/hour, 2.0 hours)
  - Service ID 2: "Tổng vệ sinh" (100,000đ/package, 2.0 hours)
- **Employees**: 
  - jane_smith (ID: e1000001-0000-0000-0000-000000000001) - Available in Quận Tân Phú
  - bob_wilson (ID: e1000001-0000-0000-0000-000000000002) - Available in Quận Gò Vấp
- **Addresses**: 
  - Address ID: adrs0001-0000-0000-0000-000000000001 (Quận Tân Phú)
  - Address ID: adrs0001-0000-0000-0000-000000000002 (Quận 1)
  - Address ID: adrs0001-0000-0000-0000-000000000003 (Quận Gò Vấp)
- **Service Options**: Choices with pricing adjustments
- **Promotions**: Active promotion codes for discounts
- **Payment Methods**: Method ID 1 (Momo), Method ID 2 (VNPAY)

---

## API Endpoints Covered
1. **GET /{customerId}/default-address** - Get Customer Default Address
2. **POST /** - Create New Booking
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

## POST / - Create New Booking

### Test Case 4: Successfully Create Basic Booking
- **Test Case ID**: TC_BOOKING_CREATE_001
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
      "bookingTime": "2025-09-12T10:00:00",
      "note": "Cần dọn dẹp kỹ lưỡng phòng khách và bếp",
      "promoCode": null,
      "bookingDetails": [
        {
          "serviceId": 2,
          "quantity": 1,
          "expectedPrice": 350000,
          "expectedPricePerUnit": 350000,
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
    "totalAmount": 350000.00,
    "formattedTotalAmount": "350,000đ",
    "bookingTime": "2025-09-12T10:00:00",
    "createdAt": "2025-09-06T14:39:22.589451644",
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
        "bookingDetailId": "eb5fdc71-eeda-4588-9c56-e2ac72bbd859",
        "service": {
          "serviceId": 2,
          "name": "Tổng vệ sinh",
          "description": "Làm sạch sâu toàn diện, bao gồm các khu vực khó tiếp cận, trần nhà, lau cửa kính. Thích hợp cho nhà mới hoặc dọn dẹp theo mùa.",
          "basePrice": 100000.00,
          "unit": "Gói",
          "estimatedDurationHours": 2.0,
          "categoryName": "Dọn dẹp nhà",
          "isActive": true
        },
        "quantity": 1,
        "pricePerUnit": 350000,
        "formattedPricePerUnit": "350,000đ",
        "subTotal": 350000.00,
        "formattedSubTotal": "350,000đ",
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
        ],
        "assignments": [],
        "duration": "2 giờ",
        "formattedDuration": "2 giờ"
      }
    ],
    "paymentInfo": {
      "paymentId": "d2068e26-7333-43a4-a9e5-5a17b23ca7dc",
      "amount": 350000.00,
      "paymentMethod": null,
      "paymentStatus": "PENDING",
      "transactionCode": "TXN_1757169562581",
      "createdAt": "2025-09-06 14:39:22",
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
        "skills": [
          "Cleaning",
          "Organizing"
        ],
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

### Test Case 5: Booking Creation Fails - Employee Conflict
- **Test Case ID**: TC_BOOKING_CREATE_005
- **Description**: Verify that booking creation fails when selected employee has scheduling conflict.
- **Preconditions**:
  - Customer is authenticated with valid token.
  - Employee has another assignment at requested time.
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
      "bookingTime": "2025-09-12T09:00:00",
      "note": "Test conflict",
      "promoCode": null,
      "bookingDetails": [
        {
          "serviceId": 2,
          "quantity": 1,
          "expectedPrice": 350000,
          "expectedPricePerUnit": 350000,
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
    "message": "Nhân viên Jane Smith có lịch trình xung đột vào thời gian từ 2025-09-12T08:00:00 đến 2025-09-12T10:00:00",
    "errorCode": "EMPLOYEE_CONFLICT_ERROR",
    "validationErrors": [],
    "conflicts": [
      {
        "conflictType": "ASSIGNMENT_CONFLICT",
        "employeeId": "e1000001-0000-0000-0000-000000000001",
        "employeeName": "Jane Smith",
        "conflictStartTime": "2025-09-12T08:00:00",
        "conflictEndTime": "2025-09-12T10:00:00",
        "reason": "Nhân viên Jane Smith có một cuộc hẹn khác trong thời gian này"
      }
    ],
    "timestamp": "2025-09-06T15:30:00"
  }
  ```
- **Status Code**: `409 Conflict`

- **Actual Output**:
  ```json
  
  ```
- **Status Code**: `409 Conflict`

---

### Test Case 4: Booking Creation Fails - Invalid Booking Time
- **Test Case ID**: TC_BOOKING_CREATE_004
- **Description**: Verify that booking creation fails when booking time violates business rules.
- **Preconditions**:
  - Customer is authenticated with valid token.
  - Booking time is outside business hours or too soon.
- **Input**:
  - **Method**: `POST`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_customer_token>
    Content-Type: application/json
    ```
  - **Body**:
    ```json
    {
      "addressId": "addr-123-valid",
      "bookingTime": "2025-09-06T16:00:00",
      "note": "Same day booking (too soon)",
      "promoCode": null,
      "bookingDetails": [
        {
          "serviceId": 1,
          "quantity": 1,
          "expectedPrice": 100000,
          "expectedPricePerUnit": 100000,
          "selectedChoiceIds": []
        }
      ],
      "assignments": [
        {
          "serviceId": 1,
          "employeeId": "emp-456-available"
        }
      ]
    }
    ```

- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Booking validation failed",
    "errorCode": "VALIDATION_ERROR",
    "validationErrors": [
      "Booking time must be at least 2 hours from now"
    ],
    "conflicts": [],
    "timestamp": "2025-09-06T15:30:00"
  }
  ```
- **Status Code**: `400 Bad Request`

- **Actual Output**:
  ```json
  
  ```
- **Status Code**: `400 Bad Request`

---

## POST /validate - Validate Booking Request

---

## GET /{customerId} - Get Customer Bookings

### Test Case 6: Successfully Retrieve Customer Bookings
- **Test Case ID**: TC_BOOKING_GET_001
- **Description**: Verify that customer can retrieve their booking list with pagination.
- **Preconditions**:
  - Customer is authenticated with valid token.
  - Customer has existing bookings.
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/customer/bookings/c1000001-0000-0000-0000-000000000001?page=0&size=10&sort=createdAt,desc`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_customer_token>
    ```
- **Expected Output**:
  ```json
  {
    "content": [
      {
        "bookingId": "7a35373e-20c6-43a2-aab2-1486fb6c89e5",
        "bookingCode": "BK62589569",
        "status": "PENDING",
        "totalAmount": 350000.00,
        "formattedTotalAmount": "350,000đ",
        "bookingTime": "2025-09-12T10:00:00",
        "createdAt": "2025-09-06T14:39:22.589451644",
        "customerInfo": {
          "addressId": "adrs0001-0000-0000-0000-000000000001",
          "fullAddress": "123 Lê Trọng Tấn, Tây Thạnh, Tân Phú, TP. Hồ Chí Minh",
          "ward": "Phường Tây Thạnh",
          "district": "Quận Tân Phú",
          "city": "TP. Hồ Chí Minh"
        },
        "serviceDetails": [
          {
            "service": {
              "serviceId": 2,
              "name": "Tổng vệ sinh",
              "categoryName": "Dọn dẹp nhà"
            },
            "quantity": 1,
            "subTotal": 350000.00
          }
        ],
        "assignedEmployees": [
          {
            "employeeId": "e1000001-0000-0000-0000-000000000001",
            "fullName": "Jane Smith",
            "phoneNumber": "0912345678"
          }
        ]
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10,
      "sort": {
        "sorted": true,
        "unsorted": false
      }
    },
    "totalElements": 1,
    "totalPages": 1,
    "first": true,
    "last": true,
    "numberOfElements": 1
  }
  ```
- **Status Code**: `200 OK`

---

## POST /validate - Validate Booking Request

### Test Case 7: Successfully Validate Booking Request
- **Test Case ID**: TC_BOOKING_VALIDATE_001
- **Description**: Verify that booking validation returns success for valid request data.
- **Preconditions**:
  - Customer is authenticated with valid token.
  - All request data is valid and available.
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
      "bookingTime": "2025-09-12T11:00:00",
      "note": "Validation test",
      "promoCode": null,
      "bookingDetails": [
        {
          "serviceId": 2,
          "quantity": 1,
          "expectedPrice": 350000,
          "expectedPricePerUnit": 350000,
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
    "errors": [],
    "conflicts": [],
    "calculatedTotalAmount": 75000,
    "serviceValidations": [
      {
        "serviceId": 1,
        "serviceName": "Dọn dẹp theo giờ",
        "exists": true,
        "active": true,
        "basePrice": 50000,
        "validChoiceIds": [1],
        "invalidChoiceIds": [],
        "calculatedPrice": 75000,
        "expectedPrice": 75000,
        "priceMatches": true
      }
    ]
  }
  ```
- **Status Code**: `200 OK`

- **Actual Output**:
  ```json
  
  ```
- **Status Code**: `200 OK`

---

### Test Case 6: Validation Fails - Price Mismatch
- **Test Case ID**: TC_BOOKING_VALIDATE_002
- **Description**: Verify that validation fails when expected price doesn't match calculated price.
- **Preconditions**:
  - Customer is authenticated with valid token.
  - Expected price differs from actual calculated price.
- **Input**:
  - **Method**: `POST`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_customer_token>
    Content-Type: application/json
    ```
  - **Body**:
    ```json
    {
      "addressId": "addr-123-valid",
      "bookingTime": "2025-09-07T13:00:00",
      "note": "Price mismatch test",
      "promoCode": null,
      "bookingDetails": [
        {
          "serviceId": 1,
          "quantity": 1,
          "expectedPrice": 50000,
          "expectedPricePerUnit": 50000,
          "selectedChoiceIds": [1]
        }
      ],
      "assignments": [
        {
          "serviceId": 1,
          "employeeId": "emp-456-available"
        }
      ]
    }
    ```

- **Expected Output**:
  ```json
  {
    "valid": false,
    "errors": [
      "Price mismatch for service 1. Expected: 50,000đ, Calculated: 75,000đ"
    ],
    "conflicts": [],
    "calculatedTotalAmount": 75000,
    "serviceValidations": [
      {
        "serviceId": 1,
        "serviceName": "Dọn dẹp theo giờ",
        "exists": true,
        "active": true,
        "calculatedPrice": 75000,
        "expectedPrice": 50000,
        "priceMatches": false
      }
    ]
  }
  ```
- **Status Code**: `200 OK`

- **Actual Output**:
  ```json
  
  ```
- **Status Code**: `200 OK`

---

## GET /{bookingId} - Get Booking Details

### Test Case 7: Successfully Retrieve Booking Details
- **Test Case ID**: TC_BOOKING_GET_001
- **Description**: Verify that customer can retrieve detailed information of an existing booking.
- **Preconditions**:
  - Customer is authenticated with valid token.
  - Booking with ID "book-789-existing" exists and belongs to customer.
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/customer/bookings/book-789-existing`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_customer_token>
    ```

- **Expected Output**:
  ```json
  {
    "success": true,
    "message": "Booking details retrieved successfully",
    "data": {
      "bookingId": "book-789-existing",
      "bookingCode": "HKS2025090001",
      "customerId": "cust-123",
      "customerName": "Nguyễn Văn A",
      "address": {
        "addressId": "addr-123-valid",
        "fullAddress": "123 Nguyễn Văn Cừ, Quận 1, TP.HCM",
        "district": "Quận 1",
        "city": "TP.HCM",
        "customerName": "Nguyễn Văn A",
        "customerPhone": "0123456789"
      },
      "bookingTime": "2025-09-07T10:00:00",
      "note": "Cần dọn dẹp kỹ lưỡng phòng khách và bếp",
      "totalAmount": 200000,
      "formattedTotalAmount": "200,000đ",
      "status": "PENDING",
      "bookingDetails": [
        {
          "bookingDetailId": "detail-001",
          "service": {
            "serviceId": 1,
            "serviceName": "Dọn dẹp theo giờ",
            "description": "Dịch vụ dọn dẹp nhà cửa theo giờ",
            "unit": "giờ",
            "basePrice": 50000,
            "formattedBasePrice": "50,000đ",
            "estimatedDurationHours": 2.0,
            "formattedDuration": "2 giờ",
            "categoryName": "Dọn dẹp cơ bản"
          },
          "quantity": 2,
          "pricePerUnit": 100000,
          "formattedPricePerUnit": "100,000đ",
          "subTotal": 200000,
          "formattedSubTotal": "200,000đ",
          "assignments": [
            {
              "assignmentId": "assign-001",
              "employee": {
                "employeeId": "emp-456-available",
                "fullName": "Trần Thị B",
                "phoneNumber": "0987654321",
                "rating": "4.5",
                "status": "AVAILABLE",
                "skills": ["Dọn dẹp", "Vệ sinh"],
                "completedJobs": 25
              },
              "status": "ASSIGNED",
              "startTime": "2025-09-07T10:00:00",
              "endTime": "2025-09-07T12:00:00",
              "formattedDuration": "2 giờ"
            }
          ]
        }
      ],
      "payment": {
        "paymentId": "pay-001",
        "amount": 200000,
        "formattedAmount": "200,000đ",
        "paymentStatus": "PENDING",
        "transactionCode": "TXN_1725634200123",
        "createdAt": "2025-09-06T15:30:00"
      },
      "createdAt": "2025-09-06T15:30:00"
    }
  }
  ```
- **Status Code**: `200 OK`

- **Actual Output**:
  ```json
  
  ```
- **Status Code**: `200 OK`

---

### Test Case 8: Get Booking Details Fails - Booking Not Found
- **Test Case ID**: TC_BOOKING_GET_002
- **Description**: Verify that request fails when booking ID does not exist.
- **Preconditions**:
  - Customer is authenticated with valid token.
  - Booking with ID "book-999-nonexistent" does not exist.
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/customer/bookings/book-999-nonexistent`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_customer_token>
    ```

- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Booking not found with ID: book-999-nonexistent",
    "errorCode": "BOOKING_NOT_FOUND",
    "validationErrors": [],
    "conflicts": [],
    "timestamp": "2025-09-06T15:30:00"
  }
  ```
- **Status Code**: `404 Not Found`

- **Actual Output**:
  ```json
  
  ```
- **Status Code**: `404 Not Found`

---

### Test Case 9: Authentication Fails - Invalid Token
- **Test Case ID**: TC_BOOKING_AUTH_001
- **Description**: Verify that all booking endpoints fail when token is invalid or missing.
- **Preconditions**: None
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/customer/bookings`
  - **Headers**: 
    ```
    Authorization: Bearer invalid_token_123
    Content-Type: application/json
    ```
  - **Body**:
    ```json
    {
      "addressId": "addr-123-valid",
      "bookingTime": "2025-09-07T10:00:00",
      "bookingDetails": [],
      "assignments": []
    }
    ```

- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Token không hợp lệ",
    "errorCode": "INVALID_TOKEN",
    "timestamp": "2025-09-06T15:30:00"
  }
  ```
- **Status Code**: `401 Unauthorized`

- **Actual Output**:
  ```json
  
  ```
- **Status Code**: `401 Unauthorized`

---

## Summary

This test suite covers the essential booking functionality with **9 minimum test cases**:

1. **TC_BOOKING_CREATE_001**: Successful booking creation
2. **TC_BOOKING_CREATE_002**: Booking with promotion code
3. **TC_BOOKING_CREATE_003**: Employee conflict handling
4. **TC_BOOKING_CREATE_004**: Invalid booking time validation
5. **TC_BOOKING_VALIDATE_001**: Successful request validation
6. **TC_BOOKING_VALIDATE_002**: Price mismatch validation
7. **TC_BOOKING_GET_001**: Successful booking retrieval
8. **TC_BOOKING_GET_002**: Booking not found error
9. **TC_BOOKING_AUTH_001**: Authentication failure

These test cases cover:
- ✅ **Happy path scenarios** (successful operations)
- ✅ **Business rule validation** (time, employee conflicts, prices)
- ✅ **Error handling** (not found, conflicts, validation)
- ✅ **Security** (authentication and authorization)
- ✅ **Data integrity** (price calculations, promotions)

The test cases use DTOs from the Booking folder, exceptions from the exceptions package, and align with the BookingServiceImpl methods and database schema from housekeeping_service_v7.sql.
