# API-TestCases-GetRole.md

## Overview
This document describes the test cases for the **Get Role** endpoint of the Authentication API.  
The endpoint retrieves the role(s) associated with a user account based on provided username and password.  
**Base URL**: `/api/v1/auth`  
**Endpoint**: `POST /get-role`

---

## Test Case Structure
Each test case includes:
- **Test Case ID**: Unique identifier for the test case.
- **Description**: Purpose of the test.
- **Preconditions**: Requirements before executing the test.
- **Input**: Request data.
- **Expected Output**: Expected response based on the API specification.
- **Status Code**: HTTP status code expected.

---

## Test Cases

### Test Case 1: Successful Get Role (Single Role)
- **Test Case ID**: TC_GETROLE_001
- **Description**: Verify that the endpoint returns the correct role for a user with valid credentials and a single role.
- **Preconditions**:
    - Account with username `john_doe` exists in the `accounts` table with password `123456789` and role `CUSTOMER`.
- **Input**:
  ```json
  {
    "username": "john_doe",
    "password": "123456789"
  }
  ```
- **Expected Output**:
  ```json
  {
    "success": true,
    "message": "Lấy vai trò thành công",
    "data": "CUSTOMER"
  }
  ```
- **Status Code**: 200 OK

---

### Test Case 2: Successful Get Role (Multiple Roles)
- **Test Case ID**: TC_GETROLE_002
- **Description**: Verify that the endpoint returns multiple roles separated by commas for a user with multiple accounts/roles.
- **Preconditions**:
    - Multiple accounts exist with username `multi_role_user` and password `123456789`, having roles `CUSTOMER` and `EMPLOYEE`.
- **Input**:
  ```json
  {
    "username": "jane_smith",
    "password": "123456789"
  }
  ```
- **Expected Output**:
  ```json
  {
    "success": true,
    "message": "Lấy vai trò thành công",
    "data": "CUSTOMER,EMPLOYEE"
  }
  ```
- **Status Code**: 200 OK

---

### Test Case 3: Missing Username
- **Test Case ID**: TC_GETROLE_003
- **Description**: Verify that the endpoint fails when username is missing or blank.
- **Preconditions**:
    - None.
- **Input**:
  ```json
  {
    "username": "",
    "password": "123456789"
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

---

### Test Case 4: Missing Password
- **Test Case ID**: TC_GETROLE_004
- **Description**: Verify that the endpoint fails when password is missing or blank.
- **Preconditions**:
    - None.
- **Input**:
  ```json
  {
    "username": "john_doe",
    "password": ""
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

---

### Test Case 5: Account Not Found
- **Test Case ID**: TC_GETROLE_005
- **Description**: Verify that the endpoint fails when the account does not exist.
- **Preconditions**:
    - No account with username `non_existent_user` exists in the `accounts` table.
- **Input**:
  ```json
  {
    "username": "non_existent_user",
    "password": "123456789"
  }
  ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Tài khoản không tồn tại"
  }
  ```
- **Status Code**: 401 Unauthorized

---

### Test Case 6: Invalid Password
- **Test Case ID**: TC_GETROLE_006
- **Description**: Verify that the endpoint fails when the provided password is incorrect.
- **Preconditions**:
    - Account with username `john_doe` exists but with a different password.
- **Input**:
  ```json
  {
    "username": "john_doe",
    "password": "WrongPassword!"
  }
  ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Mật khẩu không chính xác"
  }
  ```
- **Status Code**: 401 Unauthorized

---

### Test Case 7: Internal Server Error
- **Test Case ID**: TC_GETROLE_007
- **Description**: Verify that the endpoint fails with an internal server error when an unexpected issue occurs (e.g., database failure).
- **Preconditions**:
    - Account with username `john_doe` exists.
    - The database or service throws an unexpected exception during role retrieval.
- **Input**:
  ```json
  {
    "username": "john_doe",
    "password": "123456789"
  }
  ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Đã xảy ra lỗi khi lấy vai trò"
  }
  ```
- **Status Code**: 500 Internal Server Error

---

## Notes
- **Test Environment**: Ensure the database is properly configured with test data before running tests.
- **Mocking**: Use mocking for `AuthService` and `AccountRepository` to isolate the controller logic.
- **Edge Cases**: Additional test cases may be added for scenarios like duplicate roles or special characters in credentials in future iterations.