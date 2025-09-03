# API Test Cases - User Permission Management

## Overview
This document describes the test cases for the **User Permission** endpoints of the Customer and Employee APIs.  
The endpoints allow authenticated users to retrieve their own permissions and features in the system.  
**Base URLs**: 
- Customer: `/api/v1/customer/{customerId}/features`
- Employee: `/api/v1/employee/{employeeId}/features`

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
- **Role**: 
  - Customer endpoints: ADMIN or CUSTOMER role
  - Employee endpoints: ADMIN or EMPLOYEE role
- **Security**: Users can access their own permissions; Admins can access any user's permissions

---

## Database Test Data
Based on housekeeping_service_v6.sql:
- **Accounts**: 
  - john_doe (CUSTOMER) - ID: a1000001-0000-0000-0000-000000000001
  - jane_smith (EMPLOYEE + CUSTOMER) - ID: a1000001-0000-0000-0000-000000000002
  - admin_1 (ADMIN) - ID: a1000001-0000-0000-0000-000000000003
- **Customer Profiles**:
  - Customer ID: c1000001-0000-0000-0000-000000000001 (john_doe)
  - Customer ID: c1000001-0000-0000-0000-000000000003 (jane_smith)
- **Employee Profiles**:
  - Employee ID: e1000001-0000-0000-0000-000000000001 (jane_smith)
  - Employee ID: e1000001-0000-0000-0000-000000000002 (bob_wilson)
- **Features**: Organized by modules (Booking, Account, Service, Review, Admin)
- **Sample Customer Permissions**: 
  - "booking.create", "booking.view.history", "review.create", "service.view"
- **Sample Employee Permissions**:
  - "booking.view.available", "booking.accept", "profile.employee.edit"

---

## GET /customer/{customerId}/features - Get Customer Permissions

### Test Case 1: Successfully Get Customer Own Permissions
- **Test Case ID**: TC_USER_PERMISSION_001
- **Description**: Verify that a customer can retrieve their own permissions and features.
- **Preconditions**:
  - Customer is authenticated with valid token.
  - Customer has CUSTOMER role.
  - Customer ID matches the authenticated user's profile.
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/customer/c1000001-0000-0000-0000-000000000001/features`
  - **Path Parameter**: `customerId = c1000001-0000-0000-0000-000000000001`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_customer_token_john_doe>
    ```
- **Expected Output**:
  ```json
  {
    "success": true,
    "data": {
      "username": "john_doe",
      "role": "CUSTOMER",
      "permissions": [
        "booking.create",
        "booking.view.history",
        "booking.cancel",
        "review.create",
        "profile.customer.edit",
        "service.view"
      ]
    }
  }
  ```
- **Status Code**: `200 OK`

- **Actual Output**:
  ```json
  {
    "success": true,
    "data": {
      "username": "john_doe",
      "role": "CUSTOMER",
      "permissions": [
        "booking.create",
        "booking.view.history",
        "booking.cancel",
        "review.create",
        "profile.customer.edit",
        "service.view"
      ]
    }
  }
  ```
- **Status Code**: `200 OK`

---

### Test Case 2: Admin Access Customer Permissions
- **Test Case ID**: TC_USER_PERMISSION_002
- **Description**: Verify that admin can retrieve any customer's permissions.
- **Preconditions**:
  - Admin is authenticated with valid token.
  - Admin has ADMIN role.
  - Customer profile exists.
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/customer/c1000001-0000-0000-0000-000000000001/features`
  - **Path Parameter**: `customerId = c1000001-0000-0000-0000-000000000001`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_admin_token>
    ```
- **Expected Output**:
  ```json
  {
    "success": true,
    "data": {
      "username": "admin_1",
      "role": "ADMIN",
      "permissions": [
        "admin.dashboard.view",
        "admin.user.manage",
        "admin.permission.manage",
        "booking.create",
        "booking.view.history",
        "booking.cancel",
        "review.create",
        "profile.customer.edit",
        "service.view"
      ]
    }
  }
  ```
- **Status Code**: `200 OK`

- **Actual Output**:
  ```json
  {
    "success": true,
    "data": {
      "username": "admin_1",
      "role": "ADMIN",
      "permissions": [
        "admin.dashboard.view",
        "admin.user.manage",
        "admin.permission.manage",
        "booking.create",
        "booking.view.history",
        "booking.cancel",
        "review.create",
        "profile.customer.edit",
        "service.view"
      ]
    }
  }
  ```
- **Status Code**: `200 OK`

---

### Test Case 3: Customer Not Found
- **Test Case ID**: TC_USER_PERMISSION_003
- **Description**: Verify error handling when customer ID does not exist.
- **Preconditions**:
  - User is authenticated with valid token.
  - Customer ID 999 does not exist.
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/customer/999/features`
  - **Path Parameter**: `customerId = 999`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_customer_token>
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Khách hàng không tồn tại"
  }
  ```
- **Status Code**: `400 Bad Request`

- **Actual Output**:
  ```json
  {
    "success": false,
    "message": "Khách hàng không tồn tại"
  }
  ```
- **Status Code**: `400 Bad Request`

---

### Test Case 4: Invalid Token for Customer Permissions
- **Test Case ID**: TC_USER_PERMISSION_004
- **Description**: Verify that request fails when Authorization header is missing or invalid.
- **Preconditions**: None
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/customer/c1000001-0000-0000-0000-000000000001/features`
  - **Headers**: 
    ```
    Authorization: Bearer invalid_token
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Token không hợp lệ",
    "data": null
  }
  ```
- **Status Code**: `400 Bad Request`

- **Actual Output**:
  ```json
  {
    "success": false,
    "message": "Token không hợp lệ",
    "data": null
  }
  ```
- **Status Code**: `400 Bad Request`

---

### Test Case 5: Multi-Role Customer Permissions (Jane Smith)
- **Test Case ID**: TC_USER_PERMISSION_005
- **Description**: Verify permissions for user with multiple roles (CUSTOMER + EMPLOYEE).
- **Preconditions**:
  - Jane Smith is authenticated with valid token.
  - Jane Smith has both CUSTOMER and EMPLOYEE roles.
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/customer/c1000001-0000-0000-0000-000000000003/features`
  - **Path Parameter**: `customerId = c1000001-0000-0000-0000-000000000003`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_jane_smith_token>
    ```
- **Expected Output**:
  ```json
  {
    "success": true,
    "data": {
      "username": "jane_smith",
      "role": "CUSTOMER",
      "permissions": [
        "booking.create",
        "booking.view.history",
        "booking.cancel",
        "review.create",
        "profile.customer.edit",
        "service.view",
        "booking.view.available"
      ]
    }
  }
  ```
- **Status Code**: `200 OK`

- **Actual Output**:
  ```json
  {
    "success": true,
    "data": {
      "username": "jane_smith",
      "role": "CUSTOMER",
      "permissions": [
        "booking.create",
        "booking.view.history",
        "booking.cancel",
        "review.create",
        "profile.customer.edit",
        "service.view",
        "booking.view.available"
      ]
    }
  }
  ```
- **Status Code**: `200 OK`

---

## GET /employee/{employeeId}/features - Get Employee Permissions

### Test Case 6: Successfully Get Employee Own Permissions
- **Test Case ID**: TC_USER_PERMISSION_006
- **Description**: Verify that an employee can retrieve their own permissions and features.
- **Preconditions**:
  - Employee is authenticated with valid token.
  - Employee has EMPLOYEE role.
  - Employee ID matches the authenticated user's profile.
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/employee/e1000001-0000-0000-0000-000000000001/features`
  - **Path Parameter**: `employeeId = e1000001-0000-0000-0000-000000000001`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_employee_token_jane_smith>
    ```
- **Expected Output**:
  ```json
  {
    "success": true,
    "data": {
      "username": "jane_smith",
      "role": "EMPLOYEE",
      "permissions": [
        "booking.view.available",
        "booking.accept",
        "booking.view.assigned",
        "profile.employee.edit"
      ]
    }
  }
  ```
- **Status Code**: `200 OK`

- **Actual Output**:
  ```json
  {
    "success": true,
    "data": {
      "username": "jane_smith",
      "role": "EMPLOYEE",
      "permissions": [
        "booking.view.available",
        "booking.accept",
        "booking.view.assigned",
        "profile.employee.edit"
      ]
    }
  }
  ```
- **Status Code**: `200 OK`

---

### Test Case 7: Admin Access Employee Permissions
- **Test Case ID**: TC_USER_PERMISSION_007
- **Description**: Verify that admin can retrieve any employee's permissions.
- **Preconditions**:
  - Admin is authenticated with valid token.
  - Admin has ADMIN role.
  - Employee profile exists.
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/employee/e1000001-0000-0000-0000-000000000002/features`
  - **Path Parameter**: `employeeId = e1000001-0000-0000-0000-000000000002`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_admin_token>
    ```
- **Expected Output**:
  ```json
  {
    "success": true,
    "data": {
      "username": "admin_1",
      "role": "ADMIN",
      "permissions": [
        "admin.dashboard.view",
        "admin.user.manage",
        "admin.permission.manage",
        "booking.view.available",
        "booking.accept",
        "booking.view.assigned",
        "profile.employee.edit"
      ]
    }
  }
  ```
- **Status Code**: `200 OK`

- **Actual Output**:
  ```json
  {
    "success": true,
    "data": {
      "username": "admin_1",
      "role": "ADMIN",
      "permissions": [
        "admin.dashboard.view",
        "admin.user.manage",
        "admin.permission.manage",
        "booking.view.available",
        "booking.accept",
        "booking.view.assigned",
        "profile.employee.edit"
      ]
    }
  }
  ```
- **Status Code**: `200 OK`

---

### Test Case 8: Employee Not Found
- **Test Case ID**: TC_USER_PERMISSION_008
- **Description**: Verify error handling when employee ID does not exist.
- **Preconditions**:
  - User is authenticated with valid token.
  - Employee ID 999 does not exist.
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/employee/999/features`
  - **Path Parameter**: `employeeId = 999`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_employee_token>
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Nhân viên không tồn tại"
  }
  ```
- **Status Code**: `400 Bad Request`

- **Actual Output**:
  ```json
  {
    "success": false,
    "message": "Nhân viên không tồn tại"
  }
  ```
- **Status Code**: `400 Bad Request`

---

### Test Case 9: Unauthorized Access - Customer Accessing Employee Permissions
- **Test Case ID**: TC_USER_PERMISSION_009
- **Description**: Verify that customer role cannot access employee permission endpoints.
- **Preconditions**:
  - Customer is authenticated with valid token.
  - Customer has only CUSTOMER role.
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/employee/e1000001-0000-0000-0000-000000000001/features`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_customer_token>
    ```
- **Expected Output**:
  ```json
  {
    "timestamp": "2025-09-03T10:30:00.000+00:00",
    "status": 403,
    "error": "Forbidden",
    "path": "/api/v1/employee/e1000001-0000-0000-0000-000000000001/features"
  }
  ```
- **Status Code**: `403 Forbidden`

- **Actual Output**:
  ```json
  {
    "timestamp": "2025-09-03T10:30:00.000+00:00",
    "status": 403,
    "error": "Forbidden",
    "path": "/api/v1/employee/e1000001-0000-0000-0000-000000000001/features"
  }
  ```
- **Status Code**: `403 Forbidden`

---

### Test Case 10: Unauthorized Access - Employee Accessing Customer Permissions
- **Test Case ID**: TC_USER_PERMISSION_010
- **Description**: Verify that employee role cannot access customer permission endpoints.
- **Preconditions**:
  - Employee is authenticated with valid token.
  - Employee has only EMPLOYEE role.
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/customer/c1000001-0000-0000-0000-000000000001/features`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_employee_token_bob_wilson>
    ```
- **Expected Output**:
  ```json
  {
    "timestamp": "2025-09-03T10:30:00.000+00:00",
    "status": 403,
    "error": "Forbidden",
    "path": "/api/v1/customer/c1000001-0000-0000-0000-000000000001/features"
  }
  ```
- **Status Code**: `403 Forbidden`

- **Actual Output**:
  ```json
  {
    "timestamp": "2025-09-03T10:30:00.000+00:00",
    "status": 403,
    "error": "Forbidden",
    "path": "/api/v1/customer/c1000001-0000-0000-0000-000000000001/features"
  }
  ```
- **Status Code**: `403 Forbidden`

---

## Error Scenarios

### Test Case 11: Missing Authorization Header
- **Test Case ID**: TC_USER_PERMISSION_011
- **Description**: Verify that request fails when Authorization header is completely missing.
- **Preconditions**: None
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/customer/c1000001-0000-0000-0000-000000000001/features`
  - **Headers**: None
- **Expected Output**:
  ```json
  {
    "timestamp": "2025-09-03T10:30:00.000+00:00",
    "status": 401,
    "error": "Unauthorized",
    "path": "/api/v1/customer/c1000001-0000-0000-0000-000000000001/features"
  }
  ```
- **Status Code**: `401 Unauthorized`

- **Actual Output**:
  ```json
  {
    "timestamp": "2025-09-03T10:30:00.000+00:00",
    "status": 401,
    "error": "Unauthorized",
    "path": "/api/v1/customer/c1000001-0000-0000-0000-000000000001/features"
  }
  ```
- **Status Code**: `401 Unauthorized`

---

### Test Case 12: Malformed Authorization Header
- **Test Case ID**: TC_USER_PERMISSION_012
- **Description**: Verify error handling when Authorization header format is incorrect.
- **Preconditions**: None
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/employee/e1000001-0000-0000-0000-000000000001/features`
  - **Headers**: 
    ```
    Authorization: InvalidFormat token_here
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Token không hợp lệ",
    "data": null
  }
  ```
- **Status Code**: `400 Bad Request`

- **Actual Output**:
  ```json
  {
    "success": false,
    "message": "Token không hợp lệ",
    "data": null
  }
  ```
- **Status Code**: `400 Bad Request`

---

### Test Case 13: Expired Token
- **Test Case ID**: TC_USER_PERMISSION_013
- **Description**: Verify that request fails when token is expired.
- **Preconditions**: 
  - Token is properly formatted but expired
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/customer/c1000001-0000-0000-0000-000000000001/features`
  - **Headers**: 
    ```
    Authorization: Bearer <expired_token>
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Token không hợp lệ",
    "data": null
  }
  ```
- **Status Code**: `400 Bad Request`

- **Actual Output**:
  ```json
  {
    "success": false,
    "message": "Token không hợp lệ",
    "data": null
  }
  ```
- **Status Code**: `400 Bad Request`

---

### Test Case 14: Internal Server Error Simulation
- **Test Case ID**: TC_USER_PERMISSION_014
- **Description**: Verify error handling when internal server error occurs.
- **Preconditions**:
  - User is authenticated with valid token.
  - Database connection or service error occurs.
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/customer/c1000001-0000-0000-0000-000000000001/features`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_customer_token>
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Đã xảy ra lỗi khi lấy danh sách tính năng của khách hàng"
  }
  ```
- **Status Code**: `500 Internal Server Error`

- **Actual Output**:
  ```json
  {
    "success": false,
    "message": "Đã xảy ra lỗi khi lấy danh sách tính năng của khách hàng"
  }
  ```
- **Status Code**: `500 Internal Server Error`

---

## Notes
- **Test Environment**: Database should be configured with test data including accounts, roles, and features from housekeeping_service_v6.sql.
- **Authentication**: All endpoints require valid JWT tokens.
- **Authorization**: 
  - Customer endpoints: ADMIN or CUSTOMER role required
  - Employee endpoints: ADMIN or EMPLOYEE role required
- **Multi-Role Support**: Users with multiple roles receive combined permissions from all their roles.
- **Primary Role**: The first role found is returned as the primary role in response.
- **Permission Aggregation**: All enabled features from all user roles are combined into a single permissions list.
- **Error Handling**: Service layer catches exceptions and returns appropriate error responses.
- **Security**: JWT tokens are validated for format, expiration, and role authorization.
- **Permission System**: Based on dynamic feature-role mapping in database.
- **Response Format**: Consistent UserPermissionsResponse structure with username, role, and permissions list.
- **Path Parameters**: Customer/Employee IDs must be valid UUIDs matching existing profiles.
- **Token Extraction**: JWT utility extracts username for permission lookup regardless of requested profile ID.
