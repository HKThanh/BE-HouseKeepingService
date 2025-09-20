# API Test Cases - Assignment Management

## Overview
This document describes the minimum essential test cases for the **Assignment Management** endpoints of the Employee API.  
The endpoints allow employees and admins to view their assigned tasks and cancel assignments when necessary.  
**Base URL**: `/api/v1/employee`

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
  - Employee endpoints: EMPLOYEE or ADMIN role required
  - Assignment operations: EMPLOYEE or ADMIN role required

---

## Database Test Data
Based on housekeeping_service_v8.sql:
- **Sample Employee**: jane_smith (ID: e1000001-0000-0000-0000-000000000001)
- **Sample Admin**: admin_1 (ID: a1000001-0000-0000-0000-000000000003)
- **Sample Customer**: john_doe (ID: c1000001-0000-0000-0000-000000000001)
- **Sample Assignment**: as000001-0000-0000-0000-000000000001 with status ASSIGNED
- **Assignment Statuses**: ASSIGNED, IN_PROGRESS, COMPLETED, CANCELLED
- **Sample Booking**: b0000001-0000-0000-0000-000000000001 with employee assignment

---

## API Endpoints Covered
1. **GET /{employeeId}/assignments** - Get Employee Assignments
2. **POST /assignments/{assignmentId}/cancel** - Cancel Assignment
3. **GET /available-bookings** - Get Available Bookings
4. **POST /booking-details/{detailId}/accept** - Accept Booking Detail

---

## GET /{employeeId}/assignments - Get Employee Assignments

### Test Case 1: Successfully Get Employee Assignments
- **Test Case ID**: TC_ASSIGNMENT_001
- **Description**: Verify that an employee can retrieve their assigned tasks with default pagination.
- **Preconditions**:
  - Employee is authenticated with valid token.
  - Employee has existing assignments.
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/employee/e1000001-0000-0000-0000-000000000001/assignments`
  - **Headers**:
    ```
    Authorization: Bearer <valid_employee_token>
    ```
- **Expected Output**:
  ```json
  {
    "success": true,
    "message": "Lấy danh sách công việc thành công",
    "data": [
      {
        "assignmentId": "as000001-0000-0000-0000-000000000002",
        "bookingCode": "BK000002",
        "serviceName": "Dọn dẹp theo giờ",
        "customerName": "Jane Smith Customer",
        "customerPhone": "0912345678",
        "serviceAddress": "104 Lê Lợi, Phường 1, Gò Vấp, TP. Hồ Chí Minh",
        "bookingTime": "2025-08-28 14:00:00",
        "estimatedDurationHours": 2.0,
        "pricePerUnit": 60000.00,
        "quantity": 2,
        "totalAmount": 120000.00,
        "status": "ASSIGNED",
        "assignedAt": "2025-08-28 13:00:00",
        "checkInTime": null,
        "checkOutTime": null,
        "note": "Vui lòng đến đúng giờ."
      }
    ],
    "totalItems": 1
  }
  ```
- **Status Code**: `200 OK`

---

### Test Case 2: Get Employee Assignments with Status Filter
- **Test Case ID**: TC_ASSIGNMENT_002
- **Description**: Verify that assignments can be filtered by status.
- **Preconditions**:
  - Employee is authenticated with valid token.
  - Employee has assignments with different statuses.
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/employee/e1000001-0000-0000-0000-000000000001/assignments?status=ASSIGNED&page=0&size=10`
  - **Headers**:
    ```
    Authorization: Bearer <valid_employee_token>
    ```
- **Expected Output**:
  ```json
  {
    "success": true,
    "message": "Lấy danh sách công việc thành công",
    "data": [
      {
        "assignmentId": "as000001-0000-0000-0000-000000000002",
        "bookingCode": "BK000002",
        "serviceName": "Dọn dẹp theo giờ",
        "customerName": "Jane Smith Customer",
        "customerPhone": "0912345678",
        "serviceAddress": "104 Lê Lợi, Phường 1, Gò Vấp, TP. Hồ Chí Minh",
        "bookingTime": "2025-08-28 14:00:00",
        "estimatedDurationHours": 2.0,
        "pricePerUnit": 60000.00,
        "quantity": 2,
        "totalAmount": 120000.00,
        "status": "ASSIGNED",
        "assignedAt": "2025-08-28 13:00:00",
        "checkInTime": null,
        "checkOutTime": null,
        "note": "Vui lòng đến đúng giờ."
      }
    ],
    "totalItems": 1
  }
  ```
- **Status Code**: `200 OK`

---

### Test Case 3: Get Assignments with Invalid Status Filter
- **Test Case ID**: TC_ASSIGNMENT_003
- **Description**: Verify that invalid status filters are handled gracefully and return all assignments.
- **Preconditions**: Employee is authenticated with valid token.
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/employee/e1000001-0000-0000-0000-000000000001/assignments?status=INVALID_STATUS`
  - **Headers**:
    ```
    Authorization: Bearer <valid_employee_token>
    ```
- **Expected Output**:
  ```json
  {
    "success": true,
    "message": "Lấy danh sách công việc thành công",
    "data": [
      {
        "assignmentId": "as000001-0000-0000-0000-000000000002",
        "bookingCode": "BK000002",
        "serviceName": "Dọn dẹp theo giờ",
        "customerName": "Jane Smith Customer",
        "customerPhone": "0912345678",
        "serviceAddress": "104 Lê Lợi, Phường 1, Gò Vấp, TP. Hồ Chí Minh",
        "bookingTime": "2025-08-28 14:00:00",
        "estimatedDurationHours": 2.0,
        "pricePerUnit": 60000.00,
        "quantity": 2,
        "totalAmount": 120000.00,
        "status": "ASSIGNED",
        "assignedAt": "2025-08-28 13:00:00",
        "checkInTime": null,
        "checkOutTime": null,
        "note": "Vui lòng đến đúng giờ."
      }
    ],
    "totalItems": 1
  }
  ```
- **Status Code**: `200 OK`

---

### Test Case 4: Get Employee Assignments - Empty Result
- **Test Case ID**: TC_ASSIGNMENT_004
- **Description**: Verify that empty result is handled correctly when employee has no assignments.
- **Preconditions**: Employee is authenticated with valid token but has no assignments.
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/employee/e1000001-0000-0000-0000-000000000002/assignments`
  - **Headers**:
    ```
    Authorization: Bearer <valid_employee_token>
    ```
- **Expected Output**:
  ```json
  {
    "success": true,
    "message": "Lấy danh sách công việc thành công",
    "data": [],
    "totalItems": 0
  }
  ```
- **Status Code**: `200 OK`

---

### Test Case 5: Get Employee Assignments - Internal Server Error
- **Test Case ID**: TC_ASSIGNMENT_005
- **Description**: Verify error handling when service throws unexpected exception.
- **Preconditions**: Employee is authenticated but service encounters database error.
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/employee/e1000001-0000-0000-0000-000000000001/assignments`
  - **Headers**:
    ```
    Authorization: Bearer <valid_employee_token>
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Lỗi khi lấy danh sách công việc: [error_message]"
  }
  ```
- **Status Code**: `500 Internal Server Error`

---

## POST /assignments/{assignmentId}/cancel - Cancel Assignment

### Test Case 6: Successfully Cancel Assignment
- **Test Case ID**: TC_ASSIGNMENT_006
- **Description**: Verify that an employee can cancel their assigned task with valid reason.
- **Preconditions**:
  - Employee is authenticated with valid token.
  - Assignment exists and is in ASSIGNED status.
  - Booking time is more than 2 hours from current time.
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/employee/assignments/as000001-0000-0000-0000-000000000002/cancel`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_employee_token>
    Content-Type: application/json
    ```
  - **Request Body**:
    ```json
    {
      "reason": "Có việc đột xuất không thể tham gia"
    }
    ```
- **Expected Output**:
  ```json
  {
    "success": true,
    "message": "Hủy công việc thành công. Hệ thống sẽ thông báo cho khách hàng."
  }
  ```
- **Status Code**: `200 OK`

---

### Test Case 7: Cancel Assignment - Assignment Not Found
- **Test Case ID**: TC_ASSIGNMENT_007
- **Description**: Verify error handling when assignment ID does not exist.
- **Preconditions**: Employee is authenticated with valid token.
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/employee/assignments/non-existent-assignment/cancel`
  - **Headers**:
    ```
    Authorization: Bearer <valid_employee_token>
    Content-Type: application/json
    ```
  - **Request Body**:
    ```json
    {
      "reason": "Có việc đột xuất không thể tham gia"
    }
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Không tìm thấy công việc"
  }
  ```
- **Status Code**: `400 Bad Request`

---

### Test Case 8: Cancel Assignment - Invalid Status
- **Test Case ID**: TC_ASSIGNMENT_008
- **Description**: Verify that only assignments in ASSIGNED status can be cancelled.
- **Preconditions**:
  - Employee is authenticated with valid token.
  - Assignment exists but is in COMPLETED status.
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/employee/assignments/as000001-0000-0000-0000-000000000001/cancel`
  - **Headers**:
    ```
    Authorization: Bearer <valid_employee_token>
    Content-Type: application/json
    ```
  - **Request Body**:
    ```json
    {
      "reason": "Có việc đột xuất không thể tham gia"
    }
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Chỉ có thể hủy công việc đang ở trạng thái 'Đã nhận'"
  }
  ```
- **Status Code**: `400 Bad Request`

---

### Test Case 9: Cancel Assignment - Too Close to Start Time
- **Test Case ID**: TC_ASSIGNMENT_009
- **Description**: Verify that assignments cannot be cancelled within 2 hours of start time.
- **Preconditions**:
  - Employee is authenticated with valid token.
  - Assignment exists in ASSIGNED status.
  - Booking time is within 2 hours from current time.
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/employee/assignments/as000001-0000-0000-0000-000000000004/cancel`
  - **Headers**:
    ```
    Authorization: Bearer <valid_employee_token>
    Content-Type: application/json
    ```
  - **Request Body**:
    ```json
    {
      "reason": "Có việc đột xuất không thể tham gia"
    }
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Không thể hủy công việc trong vòng 2 giờ trước giờ bắt đầu"
  }
  ```
- **Status Code**: `400 Bad Request`

---

### Test Case 10: Cancel Assignment - Missing Reason
- **Test Case ID**: TC_ASSIGNMENT_010
- **Description**: Verify validation error when required reason field is missing.
- **Preconditions**: Employee is authenticated with valid token.
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/employee/assignments/as000001-0000-0000-0000-000000000002/cancel`
  - **Headers**:
    ```
    Authorization: Bearer <valid_employee_token>
    Content-Type: application/json
    ```
  - **Request Body**:
    ```json
    {
      "reason": ""
    }
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Lý do hủy là bắt buộc",
    "validationErrors": [
      {
        "field": "reason",
        "message": "Lý do hủy là bắt buộc"
      }
    ]
  }
  ```
- **Status Code**: `400 Bad Request`

---

### Test Case 11: Cancel Assignment - Service Returns False
- **Test Case ID**: TC_ASSIGNMENT_011
- **Description**: Verify handling when service returns false (assignment cannot be cancelled).
- **Preconditions**: Employee is authenticated but assignment cannot be cancelled for business reasons.
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/employee/assignments/as000001-0000-0000-0000-000000000003/cancel`
  - **Headers**:
    ```
    Authorization: Bearer <valid_employee_token>
    Content-Type: application/json
    ```
  - **Request Body**:
    ```json
    {
      "reason": "Có việc đột xuất không thể tham gia"
    }
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Không thể hủy công việc này"
  }
  ```
- **Status Code**: `400 Bad Request`

---

### Test Case 12: Cancel Assignment - Internal Server Error
- **Test Case ID**: TC_ASSIGNMENT_012
- **Description**: Verify error handling when service throws unexpected exception.
- **Preconditions**: Employee is authenticated but service encounters runtime error.
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/employee/assignments/as000001-0000-0000-0000-000000000002/cancel`
  - **Headers**:
    ```
    Authorization: Bearer <valid_employee_token>
    Content-Type: application/json
    ```
  - **Request Body**:
    ```json
    {
      "reason": "Có việc đột xuất không thể tham gia"
    }
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Lỗi khi hủy công việc: [error_message]"
  }
  ```
- **Status Code**: `500 Internal Server Error`

---

## GET /available-bookings - Get Available Bookings

### Test Case 13: Successfully Get Available Bookings
- **Test Case ID**: TC_ASSIGNMENT_013
- **Description**: Verify that an employee can retrieve available bookings waiting for assignment.
- **Preconditions**: Employee is authenticated with valid token.
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/employee/available-bookings?page=0&size=10`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_employee_token>
    ```
- **Expected Output**:
  ```json
  {
    "success": true,
    "message": "Lấy danh sách booking chờ thành công",
    "data": [
      {
        "detailId": "bd000001-0000-0000-0000-000000000007",
        "bookingCode": "BK000007",
        "serviceName": "Vệ sinh Sofa - Nệm - Rèm",
        "serviceAddress": "123 Lê Trọng Tấn, Tây Thạnh, Tân Phú, TP. Hồ Chí Minh",
        "bookingTime": "2025-09-05 09:00:00",
        "estimatedDurationHours": 3.0,
        "quantity": 1
      },
      {
        "detailId": "bd000001-0000-0000-0000-000000000009",
        "bookingCode": "BK000009",
        "serviceName": "Dọn dẹp theo giờ",
        "serviceAddress": "456 Lê Lợi, Bến Nghé, Quận 1,TP. Hồ Chí Minh",
        "bookingTime": "2025-09-03 16:00:00",
        "estimatedDurationHours": 2.0,
        "quantity": 3
      }
    ],
    "totalItems": 2
  }
  ```
- **Status Code**: `200 OK`

---

### Test Case 14: Get Available Bookings - Empty Result
- **Test Case ID**: TC_ASSIGNMENT_014
- **Description**: Verify that empty result is handled correctly when no bookings are available.
- **Preconditions**: Employee is authenticated with valid token but no bookings are waiting.
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/employee/available-bookings?page=0&size=10`
  - **Headers**:
    ```
    Authorization: Bearer <valid_employee_token>
    ```
- **Expected Output**:
  ```json
  {
    "success": true,
    "message": "Không có booking chờ",
    "data": [],
    "totalItems": 0
  }
  ```
- **Status Code**: `200 OK`

---

### Test Case 15: Get Available Bookings - Internal Server Error
- **Test Case ID**: TC_ASSIGNMENT_015
- **Description**: Verify error handling when service throws unexpected exception.
- **Preconditions**: Employee is authenticated but service encounters database error.
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/employee/available-bookings`
  - **Headers**:
    ```
    Authorization: Bearer <valid_employee_token>
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Lỗi khi lấy danh sách booking chờ: [error_message]"
  }
  ```
- **Status Code**: `500 Internal Server Error`

---

## POST /booking-details/{detailId}/accept - Accept Booking Detail

### Test Case 16: Successfully Accept Booking Detail
- **Test Case ID**: TC_ASSIGNMENT_016
- **Description**: Verify that an employee can accept an available booking detail.
- **Preconditions**:
  - Employee is authenticated with valid token.
  - Booking detail exists and is available for assignment.
  - Employee is not already assigned to this booking detail.
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/employee/booking-details/bd000001-0000-0000-0000-000000000007/accept?employeeId=e1000001-0000-0000-0000-000000000001`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_employee_token>
    Content-Type: application/json
    ```
- **Expected Output**:
  ```json
  {
    "success": true,
    "message": "Nhận công việc thành công",
    "data": {
      "assignmentId": "as000001-0000-0000-0000-000000000008",
      "bookingCode": "BK000007",
      "serviceName": "Vệ sinh Sofa - Nệm - Rèm",
      "customerName": "John Doe",
      "customerPhone": "0901234567",
      "serviceAddress": "123 Lê Trọng Tấn, Tây Thạnh, Tân Phú, TP. Hồ Chí Minh",
      "bookingTime": "2025-09-05 09:00:00",
      "estimatedDurationHours": 3.0,
      "pricePerUnit": 350000.00,
      "quantity": 1,
      "totalAmount": 350000.00,
      "status": "ASSIGNED",
      "assignedAt": "2025-09-20 10:30:00",
      "checkInTime": null,
      "checkOutTime": null,
      "note": "Vệ sinh sofa phòng khách."
    }
  }
  ```
- **Status Code**: `200 OK`

---

### Test Case 17: Accept Booking Detail - Already Fully Assigned
- **Test Case ID**: TC_ASSIGNMENT_017
- **Description**: Verify error handling when booking detail already has enough employees assigned.
- **Preconditions**:
  - Employee is authenticated with valid token.
  - Booking detail already has the required number of employees assigned.
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/employee/booking-details/bd000001-0000-0000-0000-000000000010/accept?employeeId=e1000001-0000-0000-0000-000000000001`
  - **Headers**:
    ```
    Authorization: Bearer <valid_employee_token>
    Content-Type: application/json
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Chi tiết dịch vụ đã có đủ nhân viên"
  }
  ```
- **Status Code**: `400 Bad Request`

---

### Test Case 18: Accept Booking Detail - Not Found
- **Test Case ID**: TC_ASSIGNMENT_018
- **Description**: Verify error handling when booking detail ID does not exist.
- **Preconditions**: Employee is authenticated with valid token.
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/employee/booking-details/non-existent-detail/accept?employeeId=e1000001-0000-0000-0000-000000000001`
  - **Headers**:
    ```
    Authorization: Bearer <valid_employee_token>
    Content-Type: application/json
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Không tìm thấy dịch vụ"
  }
  ```
- **Status Code**: `400 Bad Request`

---

### Test Case 19: Accept Booking Detail - Employee Not Found
- **Test Case ID**: TC_ASSIGNMENT_019
- **Description**: Verify error handling when employee ID does not exist.
- **Preconditions**: Valid booking detail exists but employee ID is invalid.
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/employee/booking-details/bd000001-0000-0000-0000-000000000007/accept?employeeId=non-existent-employee`
  - **Headers**:
    ```
    Authorization: Bearer <valid_employee_token>
    Content-Type: application/json
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Không tìm thấy nhân viên"
  }
  ```
- **Status Code**: `400 Bad Request`

---

### Test Case 20: Accept Booking Detail - Invalid Booking Status
- **Test Case ID**: TC_ASSIGNMENT_020
- **Description**: Verify error handling when booking is not in allowed status for assignment.
- **Preconditions**: 
  - Employee is authenticated with valid token.
  - Booking detail exists but booking is in CANCELLED status.
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/employee/booking-details/bd000001-0000-0000-0000-000000000006/accept?employeeId=e1000001-0000-0000-0000-000000000001`
  - **Headers**:
    ```
    Authorization: Bearer <valid_employee_token>
    Content-Type: application/json
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Không thể nhận booking khi đang ở trạng thái CANCELLED"
  }
  ```
- **Status Code**: `400 Bad Request`

---

### Test Case 21: Accept Booking Detail - Employee Schedule Conflict
- **Test Case ID**: TC_ASSIGNMENT_021
- **Description**: Verify error handling when employee has conflicting assignments in the same time slot.
- **Preconditions**:
  - Employee is authenticated with valid token.
  - Employee already has another assignment during the booking time.
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/employee/booking-details/bd000001-0000-0000-0000-000000000009/accept?employeeId=e1000001-0000-0000-0000-000000000001`
  - **Headers**:
    ```
    Authorization: Bearer <valid_employee_token>
    Content-Type: application/json
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Nhân viên đã được phân công công việc khác trong khung giờ này"
  }
  ```
- **Status Code**: `400 Bad Request`

---

### Test Case 22: Accept Booking Detail - Employee Unavailable
- **Test Case ID**: TC_ASSIGNMENT_022
- **Description**: Verify error handling when employee has approved leave during booking time.
- **Preconditions**:
  - Employee is authenticated with valid token.
  - Employee has approved leave during the booking time.
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/employee/booking-details/bd000001-0000-0000-0000-000000000003/accept?employeeId=e1000001-0000-0000-0000-000000000002`
  - **Headers**:
    ```
    Authorization: Bearer <valid_employee_token>
    Content-Type: application/json
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Nhân viên đang có lịch nghỉ được phê duyệt trong khung giờ này"
  }
  ```
- **Status Code**: `400 Bad Request`

---

### Test Case 23: Accept Booking Detail - Employee Already Assigned
- **Test Case ID**: TC_ASSIGNMENT_023
- **Description**: Verify error handling when employee is already assigned to the same booking detail.
- **Preconditions**:
  - Employee is authenticated with valid token.
  - Employee is already assigned to this booking detail.
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/employee/booking-details/bd000001-0000-0000-0000-000000000002/accept?employeeId=e1000001-0000-0000-0000-000000000001`
  - **Headers**:
    ```
    Authorization: Bearer <valid_employee_token>
    Content-Type: application/json
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Nhân viên đã nhận chi tiết dịch vụ này"
  }
  ```
- **Status Code**: `400 Bad Request`

---

### Test Case 24: Accept Booking Detail - Booking Detail Missing Booking Reference
- **Test Case ID**: TC_ASSIGNMENT_024
- **Description**: Verify error handling when booking detail doesn't have valid booking reference.
- **Preconditions**: 
  - Employee is authenticated with valid token.
  - Booking detail exists but has null booking reference.
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/employee/booking-details/bd000001-0000-0000-0000-000000000999/accept?employeeId=e1000001-0000-0000-0000-000000000001`
  - **Headers**:
    ```
    Authorization: Bearer <valid_employee_token>
    Content-Type: application/json
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Không thể xác định booking của chi tiết dịch vụ này"
  }
  ```
- **Status Code**: `400 Bad Request`

---

### Test Case 25: Accept Booking Detail - Internal Server Error
- **Test Case ID**: TC_ASSIGNMENT_025
- **Description**: Verify error handling when service throws unexpected exception.
- **Preconditions**: Employee is authenticated but service encounters runtime error.
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/employee/booking-details/bd000001-0000-0000-0000-000000000007/accept?employeeId=e1000001-0000-0000-0000-000000000001`
  - **Headers**:
    ```
    Authorization: Bearer <valid_employee_token>
    Content-Type: application/json
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Lỗi khi nhận booking: [error_message]"
  }
  ```
- **Status Code**: `500 Internal Server Error`

---

## Error Scenarios

### Test Case 26: Unauthorized Access - Missing Token
- **Test Case ID**: TC_ASSIGNMENT_026
- **Description**: Verify that requests fail when Authorization header is missing.
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/employee/e1000001-0000-0000-0000-000000000001/assignments`
  - **Headers**: None
- **Expected Output**:
  ```json
  {
    "error": "Unauthorized",
    "message": "Access token is missing or invalid"
  }
  ```
- **Status Code**: `401 Unauthorized`

---

### Test Case 27: Invalid Token
- **Test Case ID**: TC_ASSIGNMENT_027
- **Description**: Verify that requests fail when token is invalid or expired.
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/employee/e1000001-0000-0000-0000-000000000001/assignments`
  - **Headers**:
    ```
    Authorization: Bearer invalid_or_expired_token
    ```
- **Expected Output**:
  ```json
  {
    "error": "Unauthorized",
    "message": "Invalid or expired token"
  }
  ```
- **Status Code**: `401 Unauthorized`

---

### Test Case 28: Role Authorization - Customer Access to Employee Endpoints
- **Test Case ID**: TC_ASSIGNMENT_028
- **Description**: Verify that customer role cannot access employee assignment endpoints.
- **Preconditions**: Customer is authenticated with valid token.
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/employee/e1000001-0000-0000-0000-000000000001/assignments`
  - **Headers**:
    ```
    Authorization: Bearer <valid_customer_token>
    ```
- **Expected Output**:
  ```json
  {
    "error": "Forbidden",
    "message": "Access denied. Required role: EMPLOYEE or ADMIN"
  }
  ```
- **Status Code**: `403 Forbidden`

---

### Test Case 29: Customer Role Access to Available Bookings
- **Test Case ID**: TC_ASSIGNMENT_029
- **Description**: Verify that customer role cannot access available bookings endpoint.
- **Preconditions**: Customer is authenticated with valid token.
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/employee/available-bookings`
  - **Headers**:
    ```
    Authorization: Bearer <valid_customer_token>
    ```
- **Expected Output**:
  ```json
  {
    "error": "Forbidden",
    "message": "Access denied. Required role: EMPLOYEE or ADMIN"
  }
  ```
- **Status Code**: `403 Forbidden`

---

### Test Case 30: Customer Role Access to Accept Booking Detail
- **Test Case ID**: TC_ASSIGNMENT_030
- **Description**: Verify that customer role cannot access accept booking detail endpoint.
- **Preconditions**: Customer is authenticated with valid token.
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/employee/booking-details/bd000001-0000-0000-0000-000000000007/accept?employeeId=e1000001-0000-0000-0000-000000000001`
  - **Headers**:
    ```
    Authorization: Bearer <valid_customer_token>
    Content-Type: application/json
    ```
- **Expected Output**:
  ```json
  {
    "error": "Forbidden",
    "message": "Access denied. Required role: EMPLOYEE"
  }
  ```
- **Status Code**: `403 Forbidden`

---

### Test Case 31: Admin Access to Employee Assignments
- **Test Case ID**: TC_ASSIGNMENT_031
- **Description**: Verify that admin can access employee assignment endpoints.
- **Preconditions**: Admin is authenticated with valid token.
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/employee/e1000001-0000-0000-0000-000000000001/assignments`
  - **Headers**:
    ```
    Authorization: Bearer <valid_admin_token>
    ```
- **Expected Output**:
  ```json
  {
    "success": true,
    "message": "Lấy danh sách công việc thành công",
    "data": [
      {
        "assignmentId": "as000001-0000-0000-0000-000000000002",
        "bookingCode": "BK000002",
        "serviceName": "Dọn dẹp theo giờ",
        "customerName": "Jane Smith Customer",
        "customerPhone": "0912345678",
        "serviceAddress": "104 Lê Lợi, Phường 1, Gò Vấp, TP. Hồ Chí Minh",
        "bookingTime": "2025-08-28 14:00:00",
        "estimatedDurationHours": 2.0,
        "pricePerUnit": 60000.00,
        "quantity": 2,
        "totalAmount": 120000.00,
        "status": "ASSIGNED",
        "assignedAt": "2025-08-28 13:00:00",
        "checkInTime": null,
        "checkOutTime": null,
        "note": "Vui lòng đến đúng giờ."
      }
    ],
    "totalItems": 1
  }
  ```
- **Status Code**: `200 OK`

---

### Test Case 32: Admin Access to Available Bookings
- **Test Case ID**: TC_ASSIGNMENT_032
- **Description**: Verify that admin can access available bookings endpoint.
- **Preconditions**: Admin is authenticated with valid token.
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/employee/available-bookings`
  - **Headers**:
    ```
    Authorization: Bearer <valid_admin_token>
    ```
- **Expected Output**:
  ```json
  {
    "success": true,
    "message": "Lấy danh sách booking chờ thành công",
    "data": [
      {
        "detailId": "bd000001-0000-0000-0000-000000000007",
        "bookingCode": "BK000007",
        "serviceName": "Vệ sinh Sofa - Nệm - Rèm",
        "serviceAddress": "123 Lê Trọng Tấn, Tây Thạnh, Tân Phú, TP. Hồ Chí Minh",
        "bookingTime": "2025-09-05 09:00:00",
        "estimatedDurationHours": 3.0,
        "quantity": 1
      }
    ],
    "totalItems": 1
  }
  ```
- **Status Code**: `200 OK`

---

### Test Case 33: Admin Cannot Accept Booking Details
- **Test Case ID**: TC_ASSIGNMENT_033
- **Description**: Verify that admin role cannot accept booking details (only EMPLOYEE role allowed).
- **Preconditions**: Admin is authenticated with valid token.
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/employee/booking-details/bd000001-0000-0000-0000-000000000007/accept?employeeId=e1000001-0000-0000-0000-000000000001`
  - **Headers**:
    ```
    Authorization: Bearer <valid_admin_token>
    Content-Type: application/json
    ```
- **Expected Output**:
  ```json
  {
    "error": "Forbidden",
    "message": "Access denied. Required role: EMPLOYEE"
  }
  ```
- **Status Code**: `403 Forbidden`

---

## Notes
- **Test Environment**: Database should be configured with test data from housekeeping_service_v8.sql.
- **Authentication**: All endpoints require valid JWT tokens with appropriate roles.
- **Authorization**: 
  - Employee assignment retrieval: EMPLOYEE or ADMIN role required
  - Assignment cancellation: EMPLOYEE or ADMIN role required
  - Available bookings: EMPLOYEE or ADMIN role required
  - Accept booking detail: EMPLOYEE role required (Admin cannot accept assignments)
- **Transaction Management**: Assignment operations are wrapped in database transactions.
- **Error Handling**: Service layer catches exceptions and returns appropriate error responses.
- **Security**: JWT tokens are validated for format, expiration, and role authorization.
- **Pagination**: Assignment listing and available bookings support standard Spring Boot pagination.
- **Status Filtering**: Assignments can be filtered by status (ASSIGNED, IN_PROGRESS, COMPLETED, CANCELLED).
- **Business Rules**: 
  - Only ASSIGNED assignments can be cancelled
  - Assignments cannot be cancelled within 2 hours of start time
  - Assignment cancellation triggers customer notification
  - If all assignments for a booking are cancelled, booking status is updated to CANCELLED
  - Booking details can only be accepted if they don't already have enough employees
  - When all booking details are fully assigned, booking status changes to CONFIRMED
  - Employees cannot accept bookings that conflict with existing assignments
  - Employees cannot accept bookings during approved leave periods
  - Employees cannot accept the same booking detail twice
- **Validation**: Assignment cancellation requires a valid reason (not blank).
- **Crisis Management**: Assignment cancellations trigger crisis notifications to customers and admin monitoring.
- **Time Management**: Assignment times are managed in the system timezone with proper formatting.
- **Conflict Detection**: System checks for schedule conflicts and employee unavailability before accepting assignments.
