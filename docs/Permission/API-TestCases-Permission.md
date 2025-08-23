# API Test Cases - Permission Management

## Overview
This document describes the test cases for the **Permission Management** endpoints of the Admin API.  
The endpoints allow administrators to manage roles and permissions for users in the system.  
**Base URL**: `/api/v1/admin/permissions`

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
- **Authorization Header**: `Bearer <valid_admin_token>`
- **Content-Type**: `application/json` (for POST/PUT requests)
- **Role**: Only ADMIN role can access these endpoints (enforced by SecurityConfig)

---

## Field Requirements

### Authorization Token
- **Format**: Must be a valid JWT token in Bearer format
- **Role**: Must have ADMIN role to access permission management endpoints
- **Status**: Token must not be expired or revoked

### Request Parameters
**roleId (Path Parameter)**
- **Type**: Integer
- **Validation**: Must be a valid existing role ID in the system

**featureId (Path Parameter)**
- **Type**: Integer  
- **Validation**: Must be a valid existing feature ID in the system

**isEnabled (Request Body)**
- **Type**: Boolean
- **Required**: Yes for PUT requests
- **Values**: `true` to enable permission, `false` to disable permission

---

## Business Rules
- **Admin Only Access**: Only users with ADMIN role can manage permissions
- **Non-Admin Roles**: Only CUSTOMER and EMPLOYEE roles can be managed
- **Feature Organization**: Features are organized by modules
- **Permission States**: Each role-feature combination can be enabled or disabled
- **Transaction Safety**: All permission updates are wrapped in database transactions

---

## GET /roles - Get All Manageable Roles

### Test Case 1: Successfully Get All Manageable Roles
- **Test Case ID**: TC_PERMISSION_001
- **Description**: Verify that an admin can retrieve all manageable roles (non-admin roles).
- **Preconditions**:
  - Admin is authenticated with valid token.
  - Database contains CUSTOMER and EMPLOYEE roles.
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/admin/permissions/roles`
  - **Headers**: 
    ```
    Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
    ```
- **Expected Output**:
  ```json
  {
    "success": true,
    "message": "Lấy danh sách vai trò thành công",
    "data": [
      {
        "roleId": 1,
        "roleName": "CUSTOMER"
      },
      {
        "roleId": 2,
        "roleName": "EMPLOYEE"
      }
    ]
  }
  ```
- **Status Code**: `200 OK`

- **Actual Output**:
  ```json
  {
    "success": true,
    "message": "Lấy danh sách vai trò thành công",
    "data": [
      {
        "roleId": 1,
        "roleName": "CUSTOMER"
      },
      {
        "roleId": 2,
        "roleName": "EMPLOYEE"
      }
    ]
  }
  ```
- **Status Code**: `200 OK`

---

### Test Case 2: Get Roles Without Authorization Header
- **Test Case ID**: TC_PERMISSION_002
- **Description**: Verify that request fails when Authorization header is missing.
- **Preconditions**: None
- **Input**: 
  - **Method**: `GET`
  - **URL**: `/api/v1/admin/permissions/roles`
  - No Authorization header
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

### Test Case 3: Non-Admin User Access (Security Config Test)
- **Test Case ID**: TC_PERMISSION_003
- **Description**: Verify that non-admin users cannot access permission management endpoints.
- **Preconditions**: 
  - User has valid token but with CUSTOMER role
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/admin/permissions/roles`
  - **Headers**: 
    ```
    Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.customer.token
    ```
- **Expected Output**:
  ```json
  {
    "timestamp": "2025-08-23T10:30:00.000+00:00",
    "status": 403,
    "error": "Forbidden",
    "path": "/api/v1/admin/permissions/roles"
  }
  ```
- **Status Code**: `403 Forbidden`

- **Actual Output**:
  ```json
  {
    "timestamp": "2025-08-23T10:30:00.000+00:00",
    "status": 403,
    "error": "Forbidden",
    "path": "/api/v1/admin/permissions/roles"
  }
  ```
- **Status Code**: `403 Forbidden`

---

## GET /roles/{roleId} - Get Role Permissions

### Test Case 4: Successfully Get Role Permissions
- **Test Case ID**: TC_PERMISSION_004
- **Description**: Verify that admin can retrieve permissions for a specific role.
- **Preconditions**:
  - Admin is authenticated with valid token.
  - Role with ID 1 (CUSTOMER) exists in database.
  - Features and permissions are configured for the role.
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/admin/permissions/roles/1`
  - **Path Parameter**: `roleId = 1`
  - **Headers**: 
    ```
    Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
    ```
- **Expected Output**:
  ```json
  {
    "success": true,
    "message": "Lấy quyền vai trò thành công",
    "data": [
      {
        "roleId": 1,
        "roleName": "CUSTOMER",
        "modules": [
          {
            "moduleName": "User Management",
            "features": [
              {
                "featureId": 1,
                "featureName": "View Profile",
                "description": "View user profile information",
                "isEnabled": true
              },
              {
                "featureId": 2,
                "featureName": "Edit Profile",
                "description": "Edit user profile information",
                "isEnabled": false
              }
            ]
          }
        ]
      }
    ]
  }
  ```
- **Status Code**: `200 OK`

- **Actual Output**:
  ```json
  {
    "success": true,
    "message": "Lấy quyền vai trò thành công",
    "data": [
      {
        "roleId": 1,
        "roleName": "CUSTOMER",
        "modules": [
          {
            "moduleName": "User Management",
            "features": [
              {
                "featureId": 1,
                "featureName": "View Profile",
                "description": "View user profile information",
                "isEnabled": true
              }
            ]
          }
        ]
      }
    ]
  }
  ```
- **Status Code**: `200 OK`

---

### Test Case 5: Get Permissions for Non-Existent Role
- **Test Case ID**: TC_PERMISSION_005
- **Description**: Verify that request fails when roleId does not exist.
- **Preconditions**:
  - Admin is authenticated with valid token.
  - Role with ID 999 does not exist in database.
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/admin/permissions/roles/999`
  - **Path Parameter**: `roleId = 999`
  - **Headers**: 
    ```
    Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Vai trò không tồn tại",
    "data": []
  }
  ```
- **Status Code**: `200 OK`

- **Actual Output**:
  ```json
  {
    "success": false,
    "message": "Vai trò không tồn tại",
    "data": []
  }
  ```
- **Status Code**: `200 OK`

---

## PUT /roles/{roleId}/features/{featureId} - Update Role Permission

### Test Case 6: Successfully Enable Permission
- **Test Case ID**: TC_PERMISSION_006
- **Description**: Verify that admin can enable a permission for a role.
- **Preconditions**:
  - Admin is authenticated with valid token.
  - Role with ID 1 exists in database.
  - Feature with ID 2 exists in database.
  - Permission is currently disabled.
- **Input**:
  - **Method**: `PUT`
  - **URL**: `/api/v1/admin/permissions/roles/1/features/2`
  - **Path Parameters**: `roleId = 1`, `featureId = 2`
  - **Headers**: 
    ```
    Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
    Content-Type: application/json
    ```
  - **Request Body**:
    ```json
    {
      "isEnabled": true
    }
    ```
- **Expected Output**:
  ```json
  {
    "success": true,
    "message": "Lấy quyền vai trò thành công",
    "data": [
      {
        "roleId": 1,
        "roleName": "CUSTOMER",
        "modules": [
          {
            "moduleName": "User Management",
            "features": [
              {
                "featureId": 2,
                "featureName": "Edit Profile",
                "description": "Edit user profile information",
                "isEnabled": true
              }
            ]
          }
        ]
      }
    ]
  }
  ```
- **Status Code**: `200 OK`

- **Actual Output**:
  ```json
  {
    "success": true,
    "message": "Lấy quyền vai trò thành công",
    "data": [
      {
        "roleId": 1,
        "roleName": "CUSTOMER",
        "modules": [
          {
            "moduleName": "User Management",
            "features": [
              {
                "featureId": 2,
                "featureName": "Edit Profile",
                "description": "Edit user profile information",
                "isEnabled": true
              }
            ]
          }
        ]
      }
    ]
  }
  ```
- **Status Code**: `200 OK`

---

### Test Case 7: Update Permission for Non-Existent Role
- **Test Case ID**: TC_PERMISSION_007
- **Description**: Verify that request fails when roleId does not exist.
- **Preconditions**:
  - Admin is authenticated with valid token.
  - Role with ID 999 does not exist in database.
- **Input**:
  - **Method**: `PUT`
  - **URL**: `/api/v1/admin/permissions/roles/999/features/1`
  - **Path Parameters**: `roleId = 999`, `featureId = 1`
  - **Headers**: 
    ```
    Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
    Content-Type: application/json
    ```
  - **Request Body**:
    ```json
    {
      "isEnabled": true
    }
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Vai trò không tồn tại",
    "data": []
  }
  ```
- **Status Code**: `200 OK`

- **Actual Output**:
  ```json
  {
    "success": false,
    "message": "Vai trò không tồn tại",
    "data": []
  }
  ```
- **Status Code**: `200 OK`

---

### Test Case 8: Update Permission for Non-Existent Feature
- **Test Case ID**: TC_PERMISSION_008
- **Description**: Verify that request fails when featureId does not exist.
- **Preconditions**:
  - Admin is authenticated with valid token.
  - Role with ID 1 exists in database.
  - Feature with ID 999 does not exist in database.
- **Input**:
  - **Method**: `PUT`
  - **URL**: `/api/v1/admin/permissions/roles/1/features/999`
  - **Path Parameters**: `roleId = 1`, `featureId = 999`
  - **Headers**: 
    ```
    Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
    Content-Type: application/json
    ```
  - **Request Body**:
    ```json
    {
      "isEnabled": true
    }
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Chức năng không tồn tại",
    "data": []
  }
  ```
- **Status Code**: `200 OK`

- **Actual Output**:
  ```json
  {
    "success": false,
    "message": "Chức năng không tồn tại",
    "data": []
  }
  ```
- **Status Code**: `200 OK`

---

### Test Case 9: Invalid Request Body for PUT
- **Test Case ID**: TC_PERMISSION_009
- **Description**: Verify that request fails when request body is invalid.
- **Preconditions**:
  - Admin is authenticated with valid token.
  - Role and feature exist.
- **Input**:
  - **Method**: `PUT`
  - **URL**: `/api/v1/admin/permissions/roles/1/features/1`
  - **Headers**: 
    ```
    Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
    Content-Type: application/json
    ```
  - **Request Body**:
    ```json
    {
      "invalidField": "value"
    }
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Lỗi hệ thống",
    "data": null
  }
  ```
- **Status Code**: `500 Internal Server Error`

- **Actual Output**:
  ```json
  {
    "success": false,
    "message": "Lỗi hệ thống",
    "data": null
  }
  ```
- **Status Code**: `500 Internal Server Error`

---

## Service Layer Test Cases

### Test Case 10: Service - Get User Permissions
- **Test Case ID**: TC_PERMISSION_010
- **Description**: Verify getUserPermissions service method works correctly.
- **Preconditions**:
  - User "john_doe" exists in database.
  - User has roles with enabled permissions.
- **Input**: `username = "john_doe"`
- **Expected Output**:
  ```json
  {
    "success": true,
    "message": "Lấy quyền người dùng thành công",
    "data": {
      "username": "john_doe",
      "primaryRole": "CUSTOMER",
      "permissions": [
        "VIEW_PROFILE",
        "CREATE_BOOKING"
      ]
    }
  }
  ```

---

### Test Case 11: Service - Check User Permission
- **Test Case ID**: TC_PERMISSION_011
- **Description**: Verify hasPermission service method works correctly.
- **Preconditions**:
  - User "john_doe" exists in database.
  - User has CUSTOMER role with VIEW_PROFILE permission enabled.
- **Input**: 
  - `username = "john_doe"`
  - `featureName = "VIEW_PROFILE"`
- **Expected Output**: `true`

---

### Test Case 12: Service - Check Permission for Non-Existent User
- **Test Case ID**: TC_PERMISSION_012
- **Description**: Verify hasPermission returns false for non-existent user.
- **Preconditions**:
  - User "non_existent_user" does not exist in database.
- **Input**: 
  - `username = "non_existent_user"`
  - `featureName = "VIEW_PROFILE"`
- **Expected Output**: `false`

---

## Notes
- **Test Environment**: Ensure the database is properly configured with test data including roles, features, and role-feature relationships.
- **Authentication**: All endpoints require valid admin authentication token with proper JWT format.
- **Authorization**: Only ADMIN role users can access permission management endpoints (enforced by SecurityConfig).
- **Transaction Management**: Update operations are wrapped in database transactions for data consistency.
- **Error Handling**: Service layer catches exceptions and returns appropriate error responses with consistent format.
- **Security**: JWT tokens are validated for format, expiration, and role authorization by Spring Security.
- **Data Validation**: Input validation is performed at both controller and service layers.