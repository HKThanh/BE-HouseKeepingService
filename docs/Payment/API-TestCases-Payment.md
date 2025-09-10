# API Test Cases - Payment Management

## Overview
This document describes the minimum essential test cases for the **Payment Management** endpoints of the Customer API.  
The endpoints allow customers to create payments, view payment history, and handle payment status updates.  
**Base URL**: `/api/v1/customer/payments`

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
  - Customer endpoints: CUSTOMER role required
  - Booking lookup: CUSTOMER, EMPLOYEE, or ADMIN role required
  - Webhook: No authentication (called by payment gateways)

---

## Database Test Data
Based on housekeeping_service_v8.sql:
- **Sample Customer**: john_doe (ID: a1000001-0000-0000-0000-000000000001)
- **Sample Employee**: jane_smith (ID: a1000001-0000-0000-0000-000000000002)
- **Sample Booking**: booking_001 with amount 500000 VND
- **Payment Methods**: 
  - Method ID 1: "MOMO" (Ví điện tử Momo)
  - Method ID 2: "VNPAY" (Cổng thanh toán VNPAY)
- **Payment Statuses**: PENDING, COMPLETED, FAILED, CANCELED, REFUNDED

---

## API Endpoints Covered
1. **POST /** - Create Payment
2. **GET /booking/{bookingId}** - Get Payment for Booking  
3. **GET /history/me** - Get My Payment History
4. **POST /webhook/update-status** - Payment Webhook
5. **GET /methods** - Get Available Payment Methods

---

## POST / - Create Payment

### Test Case 1: Successfully Create Payment for Customer Booking
- **Test Case ID**: TC_PAYMENT_001
- **Description**: Verify that a customer can create a payment for their own booking.
- **Preconditions**:
  - Customer is authenticated with valid token.
  - Booking exists and belongs to the customer.
  - Payment method is active and available.
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/customer/payments`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_customer_token_john_doe>
    Content-Type: application/json
    ```
  - **Request Body**:
    ```json
    {
      "bookingId": "booking_001",
      "methodId": 1
    }
    ```
- **Expected Output**:
  ```json
  {
    "paymentId": "payment_001",
    "bookingCode": "BK20250109001",
    "amount": 500000,
    "status": "PENDING",
    "paymentMethodName": "Ví điện tử Momo",
    "transactionCode": null,
    "createdAt": "2025-01-15T10:30:00.000+00:00",
    "paidAt": null
  }
  ```
- **Status Code**: `201 Created`

---

### Test Case 2: Create Payment for Non-Existent Booking
- **Test Case ID**: TC_PAYMENT_002
- **Description**: Verify error handling when booking ID does not exist.
- **Preconditions**: Customer is authenticated with valid token.
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/customer/payments`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_customer_token>
    Content-Type: application/json
    ```
  - **Request Body**:
    ```json
    {
      "bookingId": "non_existent_booking",
      "methodId": 1
    }
    ```
- **Expected Output**:
  ```json
  {
    "error": "RuntimeException",
    "message": "Không tìm thấy Booking với ID: non_existent_booking"
  }
  ```
- **Status Code**: `400 Bad Request`

---

### Test Case 3: Create Payment with Invalid Payment Method
- **Test Case ID**: TC_PAYMENT_003
- **Description**: Verify error handling when payment method ID does not exist.
- **Preconditions**: Customer is authenticated with valid token.
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/customer/payments`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_customer_token>
    Content-Type: application/json
    ```
  - **Request Body**:
    ```json
    {
      "bookingId": "booking_001",
      "methodId": 999
    }
    ```
- **Expected Output**:
  ```json
  {
    "error": "RuntimeException",
    "message": "Không tìm thấy Payment Method với ID: 999"
  }
  ```
- **Status Code**: `400 Bad Request`

---

## GET /booking/{bookingId} - Get Payment for Booking

### Test Case 4: Successfully Get Payment for Booking
- **Test Case ID**: TC_PAYMENT_004
- **Description**: Verify that an authorized user can retrieve payment information for a booking.
- **Preconditions**:
  - User is authenticated with valid token (CUSTOMER, EMPLOYEE, or ADMIN).
  - Booking exists with payment.
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/customer/payments/booking/booking_001`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_customer_token>
    ```
- **Expected Output**:
  ```json
  {
    "paymentId": "payment_001",
    "bookingCode": "BK20250109001",
    "amount": 500000,
    "status": "COMPLETED",
    "paymentMethodName": "Ví điện tử Momo",
    "transactionCode": "MOMO_TXN_123456789",
    "createdAt": "2025-01-15T10:30:00.000+00:00",
    "paidAt": "2025-01-15T10:35:00.000+00:00"
  }
  ```
- **Status Code**: `200 OK`

---

### Test Case 5: Get Payment for Non-Existent Booking
- **Test Case ID**: TC_PAYMENT_005
- **Description**: Verify error handling when no payment exists for booking ID.
- **Preconditions**: User is authenticated with valid token.
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/customer/payments/booking/non_existent_booking`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_customer_token>
    ```
- **Expected Output**:
  ```json
  {
    "error": "RuntimeException",
    "message": "Không tìm thấy thanh toán cho Booking ID: non_existent_booking"
  }
  ```
- **Status Code**: `400 Bad Request`

---

## GET /history/me - Get My Payment History

### Test Case 6: Successfully Get Customer Payment History
- **Test Case ID**: TC_PAYMENT_006
- **Description**: Verify that a customer can retrieve their own payment history with pagination.
- **Preconditions**: Customer is authenticated and has payment history.
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/customer/payments/history/me?page=0&size=10&sort=createdAt,desc`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_customer_token_john_doe>
    ```
- **Expected Output**:
  ```json
  {
    "content": [
      {
        "paymentId": "payment_002",
        "bookingCode": "BK20250109002",
        "amount": 750000,
        "status": "COMPLETED",
        "paymentMethodName": "Cổng thanh toán VNPAY",
        "transactionCode": "VNPAY_TXN_987654321",
        "createdAt": "2025-01-15T14:30:00.000+00:00",
        "paidAt": "2025-01-15T14:32:00.000+00:00"
      },
      {
        "paymentId": "payment_001",
        "bookingCode": "BK20250109001",
        "amount": 500000,
        "status": "COMPLETED",
        "paymentMethodName": "Ví điện tử Momo",
        "transactionCode": "MOMO_TXN_123456789",
        "createdAt": "2025-01-15T10:30:00.000+00:00",
        "paidAt": "2025-01-15T10:35:00.000+00:00"
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10,
      "sort": {
        "sorted": true,
        "ascending": false
      }
    },
    "totalElements": 2,
    "totalPages": 1,
    "last": true,
    "first": true
  }
  ```
- **Status Code**: `200 OK`

---

### Test Case 7: Get Payment History with Custom Pagination
- **Test Case ID**: TC_PAYMENT_007
- **Description**: Verify pagination works correctly with different page sizes.
- **Preconditions**: Customer is authenticated and has payment history.
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/customer/payments/history/me?page=1&size=5&sort=amount,asc`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_customer_token>
    ```
- **Expected Output**:
  ```json
  {
    "content": [],
    "pageable": {
      "pageNumber": 1,
      "pageSize": 5,
      "sort": {
        "sorted": true,
        "ascending": true
      }
    },
    "totalElements": 2,
    "totalPages": 1,
    "last": true,
    "first": false
  }
  ```
- **Status Code**: `200 OK`

---

## POST /webhook/update-status - Payment Webhook

### Test Case 8: Successfully Update Payment Status via Webhook
- **Test Case ID**: TC_PAYMENT_008
- **Description**: Verify that payment gateways can update payment status via webhook.
- **Preconditions**: Payment exists with transaction code.
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/customer/payments/webhook/update-status`
  - **Headers**: 
    ```
    Content-Type: application/json
    ```
  - **Request Body**:
    ```json
    {
      "transactionCode": "MOMO_TXN_123456789",
      "status": "COMPLETED",
      "paidAt": "2025-01-15T10:35:00.000+00:00"
    }
    ```
- **Expected Output**: No response body (HTTP 200 OK acknowledgment)
- **Status Code**: `200 OK`

---

### Test Case 9: Update Status for Non-Existent Transaction
- **Test Case ID**: TC_PAYMENT_009
- **Description**: Verify error handling when transaction code does not exist.
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/customer/payments/webhook/update-status`
  - **Headers**: 
    ```
    Content-Type: application/json
    ```
  - **Request Body**:
    ```json
    {
      "transactionCode": "NON_EXISTENT_TXN",
      "status": "FAILED"
    }
    ```
- **Expected Output**:
  ```json
  {
    "error": "RuntimeException",
    "message": "Không tìm thấy giao dịch với mã: NON_EXISTENT_TXN"
  }
  ```
- **Status Code**: `400 Bad Request`

---

## Error Scenarios

### Test Case 10: Unauthorized Access - Missing Token
- **Test Case ID**: TC_PAYMENT_010
- **Description**: Verify that requests fail when Authorization header is missing.
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/customer/payments`
  - **Headers**: 
    ```
    Content-Type: application/json
    ```
  - **Request Body**:
    ```json
    {
      "bookingId": "booking_001",
      "methodId": 1
    }
    ```
- **Expected Output**:
  ```json
  {
    "timestamp": "2025-01-15T10:30:00.000+00:00",
    "status": 401,
    "error": "Unauthorized",
    "path": "/api/v1/customer/payments"
  }
  ```
- **Status Code**: `401 Unauthorized`

---

### Test Case 11: Invalid Token
- **Test Case ID**: TC_PAYMENT_011
- **Description**: Verify that requests fail when token is invalid or expired.
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/customer/payments/history/me`
  - **Headers**: 
    ```
    Authorization: Bearer invalid_token_here
    ```
- **Expected Output**:
  ```json
  {
    "timestamp": "2025-01-15T10:30:00.000+00:00",
    "status": 401,
    "error": "Unauthorized",
    "path": "/api/v1/customer/payments/history/me"
  }
  ```
- **Status Code**: `401 Unauthorized`

---

### Test Case 12: Role Authorization - Employee Access to Customer Payment Creation
- **Test Case ID**: TC_PAYMENT_012
- **Description**: Verify that employee role cannot create payments (customer-only endpoint).
- **Preconditions**: Employee is authenticated with valid token.
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/customer/payments`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_employee_token>
    Content-Type: application/json
    ```
  - **Request Body**:
    ```json
    {
      "bookingId": "booking_001",
      "methodId": 1
    }
    ```
- **Expected Output**:
  ```json
  {
    "timestamp": "2025-01-15T10:30:00.000+00:00",
    "status": 403,
    "error": "Forbidden",
    "path": "/api/v1/customer/payments"
  }
  ```
- **Status Code**: `403 Forbidden`

---

## GET /methods - Get Available Payment Methods

### Test Case 13: Successfully Get Available Payment Methods
- **Test Case ID**: TC_PAYMENT_013
- **Description**: Verify that users can retrieve all available payment methods.
- **Preconditions**: None (public endpoint).
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/customer/payments/methods`
  - **Headers**: None required
- **Expected Output**:
  ```json
  [
    {
        "methodId": 1,
        "methodCode": "CASH",
        "methodName": "Thanh toán tiền mặt"
    },
    {
        "methodId": 2,
        "methodCode": "MOMO",
        "methodName": "Ví điện tử Momo"
    },
    {
        "methodId": 3,
        "methodCode": "VNPAY",
        "methodName": "Cổng thanh toán VNPAY"
    },
    {
        "methodId": 4,
        "methodCode": "BANK_TRANSFER",
        "methodName": "Chuyển khoản ngân hàng"
    }
  ]
  ```
- **Status Code**: `200 OK`

---

## Notes
- **Test Environment**: Database should be configured with test data from housekeeping_service_v8.sql.
- **Authentication**: Most endpoints require valid JWT tokens except webhook endpoint.
- **Authorization**: 
  - Payment creation and history: CUSTOMER role required
  - Booking payment lookup: CUSTOMER, EMPLOYEE, or ADMIN role required
  - Payment methods lookup: No authentication required (public endpoint)
  - Webhook: No authentication (called by external payment gateways)
- **Transaction Management**: Payment operations are wrapped in database transactions.
- **Error Handling**: Service layer catches exceptions and returns appropriate error responses.
- **Security**: JWT tokens are validated for format, expiration, and role authorization.
- **Pagination**: Payment history supports standard Spring Boot pagination with customizable page size and sorting.
- **Payment Gateway Integration**: Webhook endpoint designed for external payment gateway callbacks.
- **Transaction Codes**: Generated by payment gateways and used for payment tracking and status updates.
- **Amount Format**: Payment amounts in VND (Vietnamese Dong) as BigDecimal for precision.
