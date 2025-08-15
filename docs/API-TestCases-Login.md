# API-TestCases-Login.md

## Overview
This document describes the test cases for the **Login** endpoint of the Authentication API.  
The endpoint authenticates users and returns access and refresh tokens upon successful login.  
**Base URL**: `/api/auth`  
**Endpoint**: `POST /login`

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

## Test Cases

### Test Case 1: Successful Login (Customer)
- **Test Case ID**: TC_LOGIN_001
- **Description**: Verify that a customer can log in with valid credentials and receive tokens and user data.
- **Preconditions**:
  - A customer account exists in the `accounts` table with username `john_doe`, password `P@ssw0rd!`, and role `CUSTOMER`.
  - Account is not locked in Redis (`login:locked:john_doe` does not exist).
- **Input**:
  ```json
  {
    "username": "john_doe",
    "password": "123456",
    "role": "CUSTOMER"
  }
  ```
- **Expected Output**:
  ```json
  {
    "success": true,
    "message": "Đăng nhập thành công",
    "data": {
      "access_token": "<valid_jwt_token>",
      "refresh_token": "<valid_refresh_token>",
      "expire_in": 3600,
      "role": "CUSTOMER",
      "data": {
        "username": "john_doe",
        "avatar": "https://example.com/avatars/john.jpg",
        "full_name": "John Doe",
        "email": "john.doe@example.com",
        "phone_number": "0901234567",
        "is_male": true,
        "status": "ACTIVE",
        "address": "123 Nguyen Van Cu, Hanoi"
      }
    }
  }
  ```

- **Actual Output**:
  ```json
  {
    "success": true,
    "message": "Đăng nhập thành công",
    "data": {
        "access_Token": "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJqb2huX2RvZSIsImlhdCI6MTc1NTIyNjA5OSwiZXhwIjoxNzU1MjI5Njk5fQ.WzQ_kpdJ1SDKCl8xexZbpo3eesG-iCfpkCSbjzV-rwKbaqOz-5u40Ytc8R_lEPWl",
        "refresh_Token": "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJqb2huX2RvZSIsImlhdCI6MTc1NTIyNjA5OSwiZXhwIjoxNzU1MzEyNDk5fQ.3qJwGjNQO8ivMFbndcxUVmOmX4MhN7hIlqxrFrz9HH56nszm-pRXoMGro3RDeyTq",
        "expire_In": 3600,
        "role": "CUSTOMER",
        "data": {
            "username": "john_doe",
            "avatar": "https://example.com/avatars/john.jpg",
            "full_name": "John Doe",
            "email": "john.doe@example.com",
            "phone_number": "0901234567",
            "is_male": true,
            "status": "ACTIVE",
            "address": "123 Nguyen Van Cu, Hanoi"
        }
    }
  }
  ```
- **Status Code**: 200 OK

---

### Test Case 2: Successful Login (Employee)
- **Test Case ID**: TC_LOGIN_002
- **Description**: Verify that an employee can log in with valid credentials and receive tokens and employee-specific data.
- **Preconditions**:
  - An employee account exists in the `accounts` table with username `john_doe`, password `P@ssw0rd!`, and role `EMPLOYEE`.
  - Account is not locked in Redis (`login:locked:john_doe` does not exist).
- **Input**:
  ```json
  {
    "username": "jane_smith",
    "password": "123456",
    "role": "EMPLOYEE"
  }
  ```
- **Expected Output**:
  ```json
  {
    "success": true,
    "message": "Đăng nhập thành công",
    "data": {
      "access_token": "<valid_jwt_token>",
      "refresh_token": "<valid_refresh_token>",
      "expire_in": 3600,
      "role": "EMPLOYEE",
      "data": {
        "username": "jane_smith",
        "avatar": "https://example.com/avatars/jane.jpg",
        "full_name": "Jane Smith",
        "email": "jane.smith@example.com",
        "phone_number": "0912345678",
        "is_male": false,
        "status": "ACTIVE",
        "address": "789 Tran Hung Dao, Hanoi"
      }
    }
  }
  ```
- **Status Code**: 200 OK

- **Actual Output**:
  ```json
  {
    "success": true,
    "message": "Đăng nhập thành công",
    "data": {
        "access_Token": "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJqYW5lX3NtaXRoIiwiaWF0IjoxNzU1MjI2MjY3LCJleHAiOjE3NTUyMjk4Njd9.d1ABvPNDhFr5x0_rT2qAmhmpbxyKt-V9g_F5ZMKw_tnhfOOhBrZdBlnOlyvviD9d",
        "refresh_Token": "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJqYW5lX3NtaXRoIiwiaWF0IjoxNzU1MjI2MjY3LCJleHAiOjE3NTUzMTI2Njd9.KW1gX9balnNLju7Hdj0osCHMC9Qe0SrV8dpwJB9w8-efCic7p63PCuXRasA1g0tS",
        "expire_In": 3600,
        "role": "EMPLOYEE",
        "data": {
            "username": "jane_smith",
            "avatar": "https://example.com/avatars/jane.jpg",
            "full_name": "Jane Smith",
            "email": "jane.smith@example.com",
            "phone_number": "0912345678",
            "is_male": false,
            "status": "ACTIVE",
            "address": "789 Tran Hung Dao, Hanoi"
        }
    }
  }
  ```
- **Status Code**: 200 OK

---

### Test Case 3: Successful Login (Admin)
- **Test Case ID**: TC_LOGIN_003
- **Description**: Verify that an admin can log in with valid credentials and receive tokens and admin-specific data.
- **Preconditions**:
  - An admin account exists in the `accounts` table with username `admin_user`, password `P@ssw0rd!`, and role `ADMIN`.
  - Account is not locked in Redis (`login:locked:admin_user` does not exist).
- **Input**:
  ```json
  {
    "username": "admin_1",
    "password": "123456",
    "role": "ADMIN"
  }
  ```
- **Expected Output**:
  ```json
  {
    "success": true,
    "message": "Đăng nhập thành công",
    "data": {
      "accessToken": "<valid_jwt_token>",
      "refreshToken": "<valid_refresh_token>",
      "expireIn": 3600,
      "role": "ADMIN",
      "data": {
        "username": "admin_1",
        "full_name": "Admin One",
        "is_male": true,
        "address": "Ho Chi Minh City",
        "department": "Management",
        "contact_info": "admin1@example.com",
        "hire_date": "2023-03-01"
      }
    }
  }
  ```
- **Status Code**: 200 OK

- **Actual Output**:
  ```json
  {
    "success": true,
    "message": "Đăng nhập thành công",
    "data": {
        "data": {
            "username": "admin_1",
            "full_name": "Admin One",
            "is_male": true,
            "address": "Ho Chi Minh City",
            "department": "Management",
            "contact_info": "admin1@example.com",
            "hire_date": "2023-03-01"
        },
        "role": "ADMIN",
        "refreshToken": "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJhZG1pbl8xIiwiaWF0IjoxNzU1MjI3ODc1LCJleHAiOjE3NTUzMTQyNzV9.dyNkcVaraNDmPzT2mWDfit-w9KGapZdT6rTPKGmCoyHxvsZ3uFBzp0PMF6Fz0W3_",
        "accessToken": "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJhZG1pbl8xIiwiaWF0IjoxNzU1MjI3ODc1LCJleHAiOjE3NTUyMzE0NzV9.RjFoPijxS8AA7Ps8W6qNzvZcOgaRbEgrFjPwuvuAvnkmYR1VJGTaHLxcM6JmfVOW",
        "expireIn": 3600
    }
  }
  ```
- **Status Code**: 200 OK

---

### Test Case 4: Invalid Credentials
- **Test Case ID**: TC_LOGIN_004
- **Description**: Verify that login fails with incorrect username or password.
- **Preconditions**:
  - Account with username `john_doe` exists but the password is incorrect.
- **Input**:
  ```json
  {
    "username": "john_doe",
    "password": "1234",
    "role": "CUSTOMER"
  }
  ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Thông tin đăng nhập không hợp lệ"
  }
  ```
- **Status Code**: 401 Unauthorized
- 
- **Actual Output**:
  ```json
  {
    "success": false,
    "message": "Thông tin đăng nhập không hợp lệ"
  }
  ```
- **Status Code**: 401 Unauthorized

---

### Test Case 5: Missing Username
- **Test Case ID**: TC_LOGIN_005
- **Description**: Verify that login fails when username is missing or blank.
- **Preconditions**:
  - None.
- **Input**:
  ```json
  {
    "username": "",
    "password": "123456",
    "role": "CUSTOMER"
  }
  ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Tên đăng nhập không được để trống"
  }
  ```
- **Status Code**: 400 Bad Request

- **Actual Output**:
  ```json
  {
    "success": false,
    "message": "Tên đăng nhập không được để trống"
  }
  ```
- **Status Code**: 400 Bad Request

---

### Test Case 6: Missing Password
- **Test Case ID**: TC_LOGIN_006
- **Description**: Verify that login fails when password is missing or blank.
- **Preconditions**:
  - None.
- **Input**:
  ```json
  {
    "username": "john_doe",
    "password": "",
    "role": "CUSTOMER"
  }
  ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Mật khẩu không được để trống"
  }
  ```
- **Status Code**: 400 Bad Request

- **Actual Output**:
  ```json
  {
    "success": false,
    "message": "Mật khẩu không được để trống"
  }
  ```
- **Status Code**: 400 Bad Request

---

### Test Case 7: Account Locked
- **Test Case ID**: TC_LOGIN_007
- **Description**: Verify that login fails when the account is locked due to 3 failed attempts.
- **Preconditions**:
  - Account with username `john_doe` exists.
  - Redis key `login:locked:john_doe` exists, indicating the account is locked.
- **Input**:
  ```json
  {
    "username": "john_doe",
    "password": "1234",
    "role": "CUSTOMER"
  }
  ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Tài khoản tạm thời bị khóa do đăng nhập sai nhiều lần"
  }
  ```
- **Status Code**: 401 Unauthorized

- **Actual Output**:
  ```json
  {
    "success": false,
    "message": "Tài khoản tạm thời bị khóa do đăng nhập sai nhiều lần"
  }
  ```
- **Status Code**: 401 Unauthorized

---

### Test Case 8: Internal Server Error
- **Test Case ID**: TC_LOGIN_008
- **Description**: Verify that login fails with an internal server error when an unexpected issue occurs (e.g., Redis failure).
- **Preconditions**:
  - Account with username `john_doe` exists.
  - Redis throws an unexpected exception during the login process.
- **Input**:
  ```json
  {
    "username": "john_doe",
    "password": "123456",
    "role": "CUSTOMER"
  }
  ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Đã xảy ra lỗi khi đăng nhập"
  }
  ```
- **Status Code**: 500 Internal Server Error

- **Actual Output**:
  ```json
  {
    "success": false,
    "message": "Đã xảy ra lỗi khi đăng nhập"
  }
  ```
- **Status Code**: 500 Internal Server Error

---

## Notes
- **Test Environment**: Ensure Redis and the database are properly configured with test data before running tests.
- **Mocking**: Use mocking for `AuthService`, `AccountRepository`, `CustomerService`, `EmployeeService`, `AdminService`, and `RedisTemplate` to isolate the controller logic.
- **Edge Cases**: Additional test cases may be added for scenarios like invalid role input or expired tokens in future iterations.