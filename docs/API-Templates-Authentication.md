# API-Templates-Authentication.md

## Overview
Authentication APIs manage **user registration, login, token handling, and logout**.  
They integrate with the **`accounts`** table and support **role-based access control**.
---
## Base URL
```
/api/auth
```
---
## 1. Register
**POST** `/register`  
Create a new account.
### Request
```json
{
  "username": "john_doe",
  "password": "P@ssw0rd!",
  "full_name": "John Doe",
  "email": "john@example.com",
  "phone_number": "+84123456789",
  "role": "CUSTOMER"
}
```

### Response (201 Created)
```json
{
  "success": true,
  "message": "Registration successful",
  "data": {
    "username": "john_doe",
    "email": "john@example.com",
    "role": "CUSTOMER"
  }
}
```

### Response (400 Bad Request)
```json
{
  "success": false,
  "message": "Username already exists"
}
```

### Response (500 Internal Server Error)
```json
{
"success": false,
"message": "Registration failed"
}
```
---

## 2. Login
**POST** `/login`  
Authenticate and receive tokens.

### Request
```json
{
  "username": "john_doe",
  "password": "P@ssw0rd!",
  "role": "CUSTOMER"
}
```

### Response (200 OK)

```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6Ikp...",
    "refresh_token": "dGhpc2lzYXJlZnJlc2h0b2tlbg==",
    "expire_in": 3600,
    "role": "CUSTOMER",
    "data": {
      "username": "john_doe",
      "full_name": "John Doe",
      "email": "john@example.com",
      "phoneNumber": "+84123456789",
      "is_male": true,
      "status": "active",
      "address": "123 Main St, City, Country"
    }
  }
}
```
### Response (200 OK) - Employee
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6Ikp...",
    "refresh_token": "dGhpc2lzYXJlZnJlc2h0b2tlbg==",
    "expire_in": 3600,
    "role": "EMPLOYEE",
    "data": {
      "username": "john_doe",
      "full_name": "John Doe",
      "email": "john@example.com",
      "phoneNumber": "+84123456789",
      "is_male": true,
      "status": "active",
      "address": "123 Main St, City, Country",
      "hire_date": "2023-01-01"
    }
  }
}
```

### Response (401 Unauthorized)
```json
{
  "success": false,
  "message": "Invalid credentials"
}
```

### Response (400 Bad Request)

```json
{
"success": false,
"message": "Username is required"
}
```
---

## 3. Refresh Token
**POST** `/refresh`  
Get a new access token using the refresh token.

Headers
```
Authorization: Bearer <access_token>
```

### Request
```json
{ "refresh_token": "dGhpc2lzYXJlZnJlc2h0b2tlbg==" }
```

### Response (200 OK)
```json
{
  "success": true,
  "message": "Token refreshed successfully",
  "access_token": "newAccessTokenHere",
  "expire_in": 3600
}
```

### Response (403 Forbidden)
```json
{ "error": "Invalid or expired refresh token" }
```

---

## 4. Logout
**POST** `/logout`  
Invalidate refresh token.

Headers
```
Authorization: Bearer <access_token>
```

### Request
```json
{ "refresh_token": "dGhpc2lzYXJlZnJlc2h0b2tlbg==" }
```

### Response (200 OK)
```json
{
  "success": true,
  "message": "Logout successful"
}
```

### Response (400 Bad Request)
```json
{
  "success": false,
  "message": "Authorization header is required"
}
```

### Response (500 Internal Server Error)
```json
{
  "success": false,
  "message": "Logout failed"
}
```
---

## 5. validate-token
GET /validate-token Validate if the current token is still valid.

Headers
```
Authorization: Bearer <access_token>
```

### Response (200 OK)
```json
{
  "success": true,
  "message": "Token is valid",
  "valid": true
}
```

### Response (401 Unauthorized)
```json
{
  "success": false,
  "message": "Token is invalid",
  "valid": false
}
```

### Response (400 Bad Request)
```json
{
  "success": false,
  "message": "Authorization header is required"
}
```

---
## 6. Get Current User (!Not done yet)
**GET** `/me`  
Return current user's profile.

### Headers
```
Authorization: Bearer <access_token>
```

### Response (200 OK)
```json
{
  "account_id": 1,
  "username": "john_doe",
  "roles": "customer",
  "status": "active",
  "full_name": "John Doe",
  "email": "john@example.com",
  "phone_number": "+84123456789"
}
```

### Response (401 Unauthorized)
```json
{ "error": "Token is missing or invalid" }
```