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
- **Sample Employee**: jane_smith (ID: a1000001-0000-0000-0000-000000000002)
- **Sample Admin**: admin_1 (ID: a1000001-0000-0000-0000-000000000003)
- **Sample Customer**: john_doe (ID: a1000001-0000-0000-0000-000000000001)
- **Sample Assignment**: assignment_001 with status ASSIGNED
- **Assignment Statuses**: ASSIGNED, IN_PROGRESS, COMPLETED, CANCELLED
- **Sample Booking**: booking_001 with employee assignment

---

## API Endpoints Covered
1. **GET /{employeeId}/assignments** - Get Employee Assignments
2. **POST /assignments/{assignmentId}/cancel** - Cancel Assignment

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
        "assignmentId": "assg0001-0000-0000-0000-000000000001",
        "bookingCode": "BK62589569",
        "serviceName": "Tổng vệ sinh",
        "customerName": "Nguyễn Văn A",
        "customerPhone": "0901234567",
        "serviceAddress": "123 Lê Trọng Tấn, Tây Thạnh, Tân Phú, TP. Hồ Chí Minh",
        "bookingTime": "2025-09-12 10:00:00",
        "estimatedDurationHours": 2.0,
        "pricePerUnit": 350000.00,
        "quantity": 1,
        "totalAmount": 350000.00,
        "status": "ASSIGNED",
        "assignedAt": "2025-09-06 14:39:22",
        "checkInTime": null,
        "checkOutTime": null,
        "note": "Cần dọn dẹp kỹ lưỡng phòng khách và bếp"
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
        "assignmentId": "assg0001-0000-0000-0000-000000000001",
        "bookingCode": "BK62589569",
        "serviceName": "Tổng vệ sinh",
        "customerName": "Nguyễn Văn A",
        "customerPhone": "0901234567",
        "serviceAddress": "123 Lê Trọng Tấn, Tây Thạnh, Tân Phú, TP. Hồ Chí Minh",
        "bookingTime": "2025-09-12 10:00:00",
        "estimatedDurationHours": 2.0,
        "pricePerUnit": 350000.00,
        "quantity": 1,
        "totalAmount": 350000.00,
        "status": "ASSIGNED",
        "assignedAt": "2025-09-06 14:39:22",
        "checkInTime": null,
        "checkOutTime": null,
        "note": "Cần dọn dẹp kỹ lưỡng phòng khách và bếp"
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
        "assignmentId": "assg0001-0000-0000-0000-000000000001",
        "bookingCode": "BK62589569",
        "serviceName": "Tổng vệ sinh",
        "customerName": "Nguyễn Văn A",
        "customerPhone": "0901234567",
        "serviceAddress": "123 Lê Trọng Tấn, Tây Thạnh, Tân Phú, TP. Hồ Chí Minh",
        "bookingTime": "2025-09-12 10:00:00",
        "estimatedDurationHours": 2.0,
        "pricePerUnit": 350000.00,
        "quantity": 1,
        "totalAmount": 350000.00,
        "status": "ASSIGNED",
        "assignedAt": "2025-09-06 14:39:22",
        "checkInTime": null,
        "checkOutTime": null,
        "note": "Cần dọn dẹp kỹ lưỡng phòng khách và bếp"
      }
    ],
    "totalItems": 1
  }
  ```
- **Status Code**: `200 OK`

---

## POST /assignments/{assignmentId}/cancel - Cancel Assignment

### Test Case 4: Successfully Cancel Assignment
- **Test Case ID**: TC_ASSIGNMENT_004
- **Description**: Verify that an employee can cancel their assigned task with valid reason.
- **Preconditions**:
  - Employee is authenticated with valid token.
  - Assignment exists and is in ASSIGNED status.
  - Booking time is more than 2 hours from current time.
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/employee/assignments/assg0001-0000-0000-0000-000000000001/cancel`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_employee_token>
    Content-Type: application/json
    ```
  - **Request Body**:
    ```json
    {
      "reason": "Có việc đột xuất không thể tham gia",
      "note": "Xin lỗi quý khách vì sự bất tiện này"
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

### Test Case 5: Cancel Assignment - Assignment Not Found
- **Test Case ID**: TC_ASSIGNMENT_005
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
      "reason": "Có việc đột xuất không thể tham gia",
      "note": "Xin lỗi quý khách vì sự bất tiện này"
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

### Test Case 6: Cancel Assignment - Invalid Status
- **Test Case ID**: TC_ASSIGNMENT_006
- **Description**: Verify that only assignments in ASSIGNED status can be cancelled.
- **Preconditions**:
  - Employee is authenticated with valid token.
  - Assignment exists but is in COMPLETED status.
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/employee/assignments/assg0002-0000-0000-0000-000000000002/cancel`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_employee_token>
    Content-Type: application/json
    ```
  - **Request Body**:
    ```json
    {
      "reason": "Có việc đột xuất không thể tham gia",
      "note": "Xin lỗi quý khách vì sự bất tiện này"
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

### Test Case 7: Cancel Assignment - Too Close to Start Time
- **Test Case ID**: TC_ASSIGNMENT_007
- **Description**: Verify that assignments cannot be cancelled within 2 hours of start time.
- **Preconditions**:
  - Employee is authenticated with valid token.
  - Assignment exists in ASSIGNED status.
  - Booking time is within 2 hours from current time.
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/employee/assignments/assg0003-0000-0000-0000-000000000003/cancel`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_employee_token>
    Content-Type: application/json
    ```
  - **Request Body**:
    ```json
    {
      "reason": "Có việc đột xuất không thể tham gia",
      "note": "Xin lỗi quý khách vì sự bất tiện này"
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

### Test Case 8: Cancel Assignment - Missing Reason
- **Test Case ID**: TC_ASSIGNMENT_008
- **Description**: Verify validation error when required reason field is missing.
- **Preconditions**: Employee is authenticated with valid token.
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/employee/assignments/assg0001-0000-0000-0000-000000000001/cancel`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_employee_token>
    Content-Type: application/json
    ```
  - **Request Body**:
    ```json
    {
      "reason": "",
      "note": "Xin lỗi quý khách vì sự bất tiện này"
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

## Error Scenarios

### Test Case 9: Unauthorized Access - Missing Token
- **Test Case ID**: TC_ASSIGNMENT_009
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

### Test Case 10: Invalid Token
- **Test Case ID**: TC_ASSIGNMENT_010
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

### Test Case 11: Role Authorization - Customer Access to Employee Endpoints
- **Test Case ID**: TC_ASSIGNMENT_011
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

### Test Case 12: Admin Access to Employee Assignments
- **Test Case ID**: TC_ASSIGNMENT_012
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
        "assignmentId": "assg0001-0000-0000-0000-000000000001",
        "bookingCode": "BK62589569",
        "serviceName": "Tổng vệ sinh",
        "customerName": "Nguyễn Văn A",
        "customerPhone": "0901234567",
        "serviceAddress": "123 Lê Trọng Tấn, Tây Thạnh, Tân Phú, TP. Hồ Chí Minh",
        "bookingTime": "2025-09-12 10:00:00",
        "estimatedDurationHours": 2.0,
        "pricePerUnit": 350000.00,
        "quantity": 1,
        "totalAmount": 350000.00,
        "status": "ASSIGNED",
        "assignedAt": "2025-09-06 14:39:22",
        "checkInTime": null,
        "checkOutTime": null,
        "note": "Cần dọn dẹp kỹ lưỡng phòng khách và bếp"
      }
    ],
    "totalItems": 1
  }
  ```
- **Status Code**: `200 OK`

---

## Notes
- **Test Environment**: Database should be configured with test data from housekeeping_service_v8.sql.
- **Authentication**: All endpoints require valid JWT tokens with EMPLOYEE or ADMIN roles.
- **Authorization**: 
  - Employee assignment retrieval: EMPLOYEE or ADMIN role required
  - Assignment cancellation: EMPLOYEE or ADMIN role required
- **Transaction Management**: Assignment operations are wrapped in database transactions.
- **Error Handling**: Service layer catches exceptions and returns appropriate error responses.
- **Security**: JWT tokens are validated for format, expiration, and role authorization.
- **Pagination**: Assignment listing supports standard Spring Boot pagination with customizable page size and sorting.
- **Status Filtering**: Assignments can be filtered by status (ASSIGNED, IN_PROGRESS, COMPLETED, CANCELLED).
- **Business Rules**: 
  - Only ASSIGNED assignments can be cancelled
  - Assignments cannot be cancelled within 2 hours of start time
  - Assignment cancellation triggers customer notification
  - If all assignments for a booking are cancelled, booking status is updated to CANCELLED
- **Validation**: Assignment cancellation requires a valid reason (not blank).
- **Crisis Management**: Assignment cancellations trigger crisis notifications to customers and admin monitoring.
- **Time Management**: Assignment times are managed in the system timezone with proper formatting.
