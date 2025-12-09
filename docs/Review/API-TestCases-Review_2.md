# API Test Cases - Review Management (Part 2)

## Overview
This document describes comprehensive test cases for the **NEW and UPDATED Review Management** endpoints.  
**Base URL**: `/api/v1/reviews`  
**Test Date Context**: December 9, 2025

---

## API Endpoints Covered

### Existing Endpoints (Updated)
1. **POST /** - Create Review (Response updated)
2. **GET /criteria** - Get Review Criteria (Path fixed from `/reviews/criteria`)
3. **GET /employees/{employeeId}/reviews** - Get Reviews for Employee (Response updated, Paginated)
4. **GET /employees/{employeeId}/summary** - Get Employee Review Summary (Path changed from `/employees/{employeeId}/reviews/summary`)

### New Endpoints
5. **GET /pending** - Get Pending Reviews for Customer (Paginated)
6. **GET /bookings/{bookingId}/reviewable-employees** - Get Reviewable Employees for Booking (Paginated)

---

## Authentication Requirements
| Endpoint | Authentication | Role |
|----------|---------------|------|
| POST / | Required | CUSTOMER |
| GET /criteria | Not Required | - |
| GET /employees/{employeeId}/reviews | Not Required | - |
| GET /employees/{employeeId}/summary | Not Required | - |
| GET /pending | Required | CUSTOMER |
| GET /bookings/{bookingId}/reviewable-employees | Required | CUSTOMER |

---

## GET /criteria - Get Review Criteria

> **Note**: Path fixed from `/api/v1/reviews/reviews/criteria` to `/api/v1/reviews/criteria`

### Test Case 1: Get All Review Criteria
- **Test Case ID**: TC_REVIEW_2_001
- **Description**: Verify retrieval of all review criteria for rating employees
- **Preconditions**:
  - Review criteria exist in database (Thái độ, Đúng giờ, Chất lượng công việc)
- **Input**:
  - **Method**: GET
  - **URL**: `/api/v1/reviews/criteria`
  - **Headers**: 
    ```
    Content-Type: application/json
    ```
- **Expected Output**:
  ```json
  [
    {
      "criteriaId": 1,
      "criteriaName": "Thái độ"
    },
    {
      "criteriaId": 2,
      "criteriaName": "Đúng giờ"
    },
    {
      "criteriaId": 3,
      "criteriaName": "Chất lượng công việc"
    }
  ]
  ```
- **Status Code**: 200 OK

---

## GET /pending - Get Pending Reviews for Customer

> **Response Format**: Paginated (Spring Page format)

### Test Case 2: Successful Get Pending Reviews (First Page)
- **Test Case ID**: TC_REVIEW_2_002
- **Description**: Verify customer can retrieve paginated list of bookings pending review
- **Preconditions**:
  - Customer John Doe logged in
  - Customer has completed bookings with assigned employees
  - Some employees have not been reviewed yet
- **Input**:
  - **Method**: GET
  - **URL**: `/api/v1/reviews/pending?page=0&size=10`
  - **Headers**: 
    ```
    Authorization: Bearer <john_doe_customer_token>
    Content-Type: application/json
    ```
- **Expected Output**:
  ```json
  {
    "content": [
      {
        "bookingId": "b0000001-0000-0000-0000-000000000001",
        "bookingCode": "BK000001",
        "bookingTime": "2025-12-08T09:00:00+07:00",
        "assignmentId": "ASN000001",
        "serviceName": "Dọn dẹp nhà cửa",
        "employeeId": "e1000001-0000-0000-0000-000000000001",
        "employeeName": "Trần Thị B",
        "employeeAvatar": "https://storage.example.com/avatars/employee1.jpg"
      },
      {
        "bookingId": "b0000001-0000-0000-0000-000000000002",
        "bookingCode": "BK000002",
        "bookingTime": "2025-12-07T14:00:00+07:00",
        "assignmentId": "ASN000002",
        "serviceName": "Giặt ủi quần áo",
        "employeeId": "e1000001-0000-0000-0000-000000000002",
        "employeeName": "Lê Văn C",
        "employeeAvatar": "https://storage.example.com/avatars/employee2.jpg"
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10,
      "sort": { "sorted": false, "unsorted": true, "empty": true }
    },
    "totalElements": 2,
    "totalPages": 1,
    "last": true,
    "first": true,
    "size": 10,
    "number": 0,
    "numberOfElements": 2,
    "empty": false
  }
  ```
- **Status Code**: 200 OK

### Test Case 3: Customer With No Pending Reviews
- **Test Case ID**: TC_REVIEW_2_003
- **Description**: Verify response when customer has reviewed all completed bookings
- **Preconditions**:
  - Customer Mary Jones logged in
  - Customer has reviewed all employees in completed bookings
- **Input**:
  - **Method**: GET
  - **URL**: `/api/v1/reviews/pending?page=0&size=10`
  - **Headers**: 
    ```
    Authorization: Bearer <mary_jones_customer_token>
    Content-Type: application/json
    ```
- **Expected Output**:
  ```json
  {
    "content": [],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10,
      "sort": { "sorted": false, "unsorted": true, "empty": true }
    },
    "totalElements": 0,
    "totalPages": 0,
    "last": true,
    "first": true,
    "size": 10,
    "number": 0,
    "numberOfElements": 0,
    "empty": true
  }
  ```
- **Status Code**: 200 OK

### Test Case 4: Unauthorized Access to Pending Reviews
- **Test Case ID**: TC_REVIEW_2_004
- **Description**: Verify access denied without authentication
- **Preconditions**:
  - No authorization header provided
- **Input**:
  - **Method**: GET
  - **URL**: `/api/v1/reviews/pending`
  - **Headers**: 
    ```
    Content-Type: application/json
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Unauthorized"
  }
  ```
- **Status Code**: 401 Unauthorized

### Test Case 5: Non-Customer Role Access Pending Reviews
- **Test Case ID**: TC_REVIEW_2_005
- **Description**: Verify access denied for non-customer roles (Employee, Admin)
- **Preconditions**:
  - User logged in with EMPLOYEE role
- **Input**:
  - **Method**: GET
  - **URL**: `/api/v1/reviews/pending`
  - **Headers**: 
    ```
    Authorization: Bearer <employee_token>
    Content-Type: application/json
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Access Denied"
  }
  ```
- **Status Code**: 403 Forbidden

---

## GET /bookings/{bookingId}/reviewable-employees - Get Reviewable Employees

> **Response Format**: Paginated (Spring Page format)

### Test Case 6: Successful Get Reviewable Employees (First Page)
- **Test Case ID**: TC_REVIEW_2_006
- **Description**: Verify customer can get paginated list of employees they can review for a booking
- **Preconditions**:
  - Customer John Doe logged in
  - Booking b0000001-0000-0000-0000-000000000001 belongs to John Doe
  - Booking is in COMPLETED status
  - Two employees were assigned: one reviewed, one not reviewed
- **Input**:
  - **Method**: GET
  - **URL**: `/api/v1/reviews/bookings/b0000001-0000-0000-0000-000000000001/reviewable-employees?page=0&size=10`
  - **Headers**: 
    ```
    Authorization: Bearer <john_doe_customer_token>
    Content-Type: application/json
    ```
- **Expected Output**:
  ```json
  {
    "content": [
      {
        "employeeId": "e1000001-0000-0000-0000-000000000002",
        "employeeName": "Lê Văn C",
        "employeeAvatar": "https://storage.example.com/avatars/employee2.jpg",
        "serviceName": "Dọn dẹp nhà cửa"
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10,
      "sort": { "sorted": false, "unsorted": true, "empty": true }
    },
    "totalElements": 1,
    "totalPages": 1,
    "last": true,
    "first": true,
    "size": 10,
    "number": 0,
    "numberOfElements": 1,
    "empty": false
  }
  ```
- **Status Code**: 200 OK

### Test Case 7: All Employees Already Reviewed
- **Test Case ID**: TC_REVIEW_2_007
- **Description**: Verify empty page when all employees in booking have been reviewed
- **Preconditions**:
  - Customer John Doe logged in
  - Booking `b0000001-0000-0000-0000-000000000002` belongs to John Doe
  - All assigned employees have been reviewed
- **Input**:
  - **Method**: GET
  - **URL**: `/api/v1/reviews/bookings/b0000001-0000-0000-0000-000000000002/reviewable-employees?page=0&size=10`
  - **Headers**: 
    ```
    Authorization: Bearer <john_doe_customer_token>
    Content-Type: application/json
    ```
- **Expected Output**:
  ```json
  {
    "content": [],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10,
      "sort": { "sorted": false, "unsorted": true, "empty": true }
    },
    "totalElements": 0,
    "totalPages": 0,
    "last": true,
    "first": true,
    "size": 10,
    "number": 0,
    "numberOfElements": 0,
    "empty": true
  }
  ```
- **Status Code**: 200 OK

### Test Case 8: Access Other Customer's Booking
- **Test Case ID**: TC_REVIEW_2_008
- **Description**: Verify access denied when trying to view reviewable employees for another customer's booking
- **Preconditions**:
  - Customer John Doe logged in
  - Booking `b0000001-0000-0000-0000-000000000003` belongs to Mary Jones (different customer)
- **Input**:
  - **Method**: GET
  - **URL**: `/api/v1/reviews/bookings/b0000001-0000-0000-0000-000000000003/reviewable-employees`
  - **Headers**: 
    ```
    Authorization: Bearer <john_doe_customer_token>
    Content-Type: application/json
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Bạn không có quyền xem thông tin đánh giá của đơn đặt này"
  }
  ```
- **Status Code**: 403 Forbidden

### Test Case 9: Booking Not Completed
- **Test Case ID**: TC_REVIEW_2_009
- **Description**: Verify error when booking is not in COMPLETED status
- **Preconditions**:
  - Customer John Doe logged in
  - Booking `b0000001-0000-0000-0000-000000000004` belongs to John Doe
  - Booking status is CONFIRMED (not COMPLETED)
- **Input**:
  - **Method**: GET
  - **URL**: `/api/v1/reviews/bookings/b0000001-0000-0000-0000-000000000004/reviewable-employees`
  - **Headers**: 
    ```
    Authorization: Bearer <john_doe_customer_token>
    Content-Type: application/json
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Chỉ có thể xem nhân viên cần đánh giá khi dịch vụ đã hoàn thành"
  }
  ```
- **Status Code**: 400 Bad Request

### Test Case 10: Non-existent Booking
- **Test Case ID**: TC_REVIEW_2_010
- **Description**: Verify error when booking ID doesn't exist
- **Preconditions**:
  - Customer John Doe logged in
  - Booking ID "NONEXISTENT" does not exist
- **Input**:
  - **Method**: GET
  - **URL**: `/api/v1/reviews/bookings/NONEXISTENT/reviewable-employees`
  - **Headers**: 
    ```
    Authorization: Bearer <john_doe_customer_token>
    Content-Type: application/json
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Booking not found with ID: NONEXISTENT"
  }
  ```
- **Status Code**: 400 Bad Request

### Test Case 11: Unauthorized Access
- **Test Case ID**: TC_REVIEW_2_011
- **Description**: Verify access denied without authentication
- **Preconditions**:
  - No authorization header provided
- **Input**:
  - **Method**: GET
  - **URL**: `/api/v1/reviews/bookings/b0000001-0000-0000-0000-000000000001/reviewable-employees`
  - **Headers**: 
    ```
    Content-Type: application/json
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Unauthorized"
  }
  ```
- **Status Code**: 401 Unauthorized

---

## GET /employees/{employeeId}/summary - Updated Response

> **Note**: Path changed from `/employees/{employeeId}/reviews/summary` to `/employees/{employeeId}/summary`

### Test Case 12: Get Employee Review Summary with Rating Distribution
- **Test Case ID**: TC_REVIEW_2_012
- **Description**: Verify employee summary includes rating distribution for histogram display
- **Preconditions**:
  - Employee Jane Smith exists with ID 'e1000001-0000-0000-0000-000000000001'
  - Employee has multiple reviews with various ratings
- **Input**:
  - **Method**: GET
  - **URL**: `/api/v1/reviews/employees/e1000001-0000-0000-0000-000000000001/summary`
  - **Headers**: 
    ```
    Content-Type: application/json
    ```
- **Expected Output**:
  ```json
  {
    "employeeId": "e1000001-0000-0000-0000-000000000001",
    "employeeName": "Jane Smith",
    "employeeAvatar": "https://storage.example.com/avatars/jane.jpg",
    "totalReviews": 15,
    "averageRating": 4.35,
    "ratingTier": "HIGH",
    "ratingDistribution": {
      "1": 0,
      "2": 1,
      "3": 2,
      "4": 5,
      "5": 7
    }
  }
  ```
- **Status Code**: 200 OK

### Test Case 13: Employee Summary with No Reviews
- **Test Case ID**: TC_REVIEW_2_013
- **Description**: Verify summary response for employee with no reviews
- **Preconditions**:
  - Employee Bob Wilson exists with ID 'e1000001-0000-0000-0000-000000000002'
  - Employee has no reviews
- **Input**:
  - **Method**: GET
  - **URL**: `/api/v1/reviews/employees/e1000001-0000-0000-0000-000000000002/summary`
  - **Headers**: 
    ```
    Content-Type: application/json
    ```
- **Expected Output**:
  ```json
  {
    "employeeId": "e1000001-0000-0000-0000-000000000002",
    "employeeName": "Bob Wilson",
    "employeeAvatar": "https://storage.example.com/avatars/bob.jpg",
    "totalReviews": 0,
    "averageRating": 0.0,
    "ratingTier": null,
    "ratingDistribution": {
      "1": 0,
      "2": 0,
      "3": 0,
      "4": 0,
      "5": 0
    }
  }
  ```
- **Status Code**: 200 OK

### Test Case 14: Non-existent Employee Summary
- **Test Case ID**: TC_REVIEW_2_014
- **Description**: Verify error when requesting summary for non-existent employee
- **Preconditions**:
  - Employee ID "NONEXISTENT" does not exist
- **Input**:
  - **Method**: GET
  - **URL**: `/api/v1/reviews/employees/NONEXISTENT/summary`
  - **Headers**: 
    ```
    Content-Type: application/json
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Employee not found with ID: NONEXISTENT"
  }
  ```
- **Status Code**: 400 Bad Request

---

## GET /employees/{employeeId}/reviews - Updated Response

### Test Case 15: Get Employee Reviews with Full Details
- **Test Case ID**: TC_REVIEW_2_015
- **Description**: Verify paginated reviews include all new fields
- **Preconditions**:
  - Employee Jane Smith exists with reviews
- **Input**:
  - **Method**: GET
  - **URL**: `/api/v1/reviews/employees/e1000001-0000-0000-0000-000000000001/reviews?page=0&size=10`
  - **Headers**: 
    ```
    Content-Type: application/json
    ```
- **Expected Output**:
  ```json
  {
    "content": [
      {
        "reviewId": 1,
        "bookingId": "b0000001-0000-0000-0000-000000000001",
        "bookingCode": "BK000001",
        "customerId": "c1000001-0000-0000-0000-000000000001",
        "customerName": "Nguyễn Văn A",
        "employeeId": "e1000001-0000-0000-0000-000000000001",
        "employeeName": "Jane Smith",
        "employeeAvatar": "https://storage.example.com/avatars/jane.jpg",
        "comment": "Dịch vụ tuyệt vời, nhân viên rất nhiệt tình!",
        "averageRating": 4.67,
        "createdAt": "2025-12-08T15:30:00+07:00",
        "details": [
          {
            "criteriaId": 1,
            "criteriaName": "Thái độ",
            "rating": 5.0
          },
          {
            "criteriaId": 2,
            "criteriaName": "Đúng giờ",
            "rating": 4.0
          },
          {
            "criteriaId": 3,
            "criteriaName": "Chất lượng công việc",
            "rating": 5.0
          }
        ]
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10,
      "sort": {
        "sorted": true,
        "unsorted": false,
        "empty": false
      }
    },
    "totalElements": 15,
    "totalPages": 2,
    "last": false,
    "first": true,
    "size": 10,
    "number": 0,
    "numberOfElements": 10,
    "empty": false
  }
  ```
- **Status Code**: 200 OK

---

## POST / - Create Review (Updated Response)

### Test Case 16: Successful Review Creation with Updated Response
- **Test Case ID**: TC_REVIEW_2_016
- **Description**: Verify review creation returns all new fields in response
- **Preconditions**:
  - Customer John Doe logged in
  - Booking `b0000001-0000-0000-0000-000000000005` is COMPLETED and owned by John Doe
  - Employee assigned but not yet reviewed
- **Input**:
  - **Method**: POST
  - **URL**: `/api/v1/reviews`
  - **Headers**: 
    ```
    Authorization: Bearer <john_doe_customer_token>
    Content-Type: application/json
    ```
  - **Request Body**:
    ```json
    {
      "bookingId": "b0000001-0000-0000-0000-000000000005",
      "employeeId": "e1000001-0000-0000-0000-000000000001",
      "comment": "Nhân viên làm việc rất chuyên nghiệp, dọn dẹp sạch sẽ!",
      "criteriaRatings": [
        {
          "criteriaId": 1,
          "rating": 5.0
        },
        {
          "criteriaId": 2,
          "rating": 4.5
        },
        {
          "criteriaId": 3,
          "rating": 5.0
        }
      ]
    }
    ```
- **Expected Output**:
  ```json
  {
    "reviewId": 26,
    "bookingId": "b0000001-0000-0000-0000-000000000005",
    "bookingCode": "BK000005",
    "customerId": "c1000001-0000-0000-0000-000000000001",
    "customerName": "John Doe",
    "employeeId": "e1000001-0000-0000-0000-000000000001",
    "employeeName": "Jane Smith",
    "employeeAvatar": "https://storage.example.com/avatars/jane.jpg",
    "comment": "Nhân viên làm việc rất chuyên nghiệp, dọn dẹp sạch sẽ!",
    "averageRating": 4.83,
    "createdAt": "2025-12-09T10:30:00+07:00",
    "details": [
      {
        "criteriaId": 1,
        "criteriaName": "Thái độ",
        "rating": 5.0
      },
      {
        "criteriaId": 2,
        "criteriaName": "Đúng giờ",
        "rating": 4.5
      },
      {
        "criteriaId": 3,
        "criteriaName": "Chất lượng công việc",
        "rating": 5.0
      }
    ]
  }
  ```
- **Status Code**: 201 Created

---

## WebSocket Events

### Pending Review Removed Event
When a customer creates a review, a WebSocket event is sent to remove the item from pending reviews list.

- **Topic**: `/topic/reviews/pending/{accountId}`
- **Event Payload**:
  ```json
  {
    "action": "REMOVE",
    "bookingId": "b0000001-0000-0000-0000-000000000005",
    "employeeId": "e1000001-0000-0000-0000-000000000001",
    "payload": {
      "bookingId": "b0000001-0000-0000-0000-000000000005",
      "bookingCode": "BK000005",
      "bookingTime": "2025-12-08T09:00:00+07:00",
      "employeeId": "e1000001-0000-0000-0000-000000000001",
      "employeeName": "Jane Smith",
      "employeeAvatar": "https://storage.example.com/avatars/jane.jpg"
    }
  }
  ```

---

## Rating Tier System
| Tier | Average Rating Range |
|------|---------------------|
| LOWEST | ≤ 2.0 |
| LOW | 2.0 - 2.9 |
| MEDIUM | 3.0 - 3.9 |
| HIGH | 4.0 - 4.4 |
| HIGHEST | ≥ 4.5 |

---

## Frontend Implementation Guide

### 1. Display Reviews for Employee Profile
```
GET /api/v1/reviews/employees/{employeeId}/summary
GET /api/v1/reviews/employees/{employeeId}/reviews?page=0&size=10
```
- Use `ratingDistribution` to render star histogram
- Display `employeeName` and `employeeAvatar` in header
- Show `totalReviews` and `averageRating` prominently
- Use `ratingTier` for badge/indicator display

### 2. Customer Pending Reviews List
```
GET /api/v1/reviews/pending?page=0&size=10
```
- Paginated response with `content`, `totalElements`, `totalPages`
- Display `employeeName`, `employeeAvatar`, `serviceName`
- Link to review form with `bookingId` and `employeeId`

### 3. Check Reviewable Employees Before Showing Review Button
```
GET /api/v1/reviews/bookings/{bookingId}/reviewable-employees?page=0&size=10
```
- Paginated response
- Call when viewing completed booking details
- Show "Review" button only if `totalElements > 0`
- List all reviewable employees with their service names

### 4. Submit Review
```
POST /api/v1/reviews
GET /api/v1/reviews/criteria (to get criteria list for form)
```
- Fetch criteria first to build rating form
- Submit with all criteria ratings
- Handle WebSocket event to update pending list in real-time

### 5. WebSocket Integration
```javascript
// Subscribe to pending review updates
stompClient.subscribe('/topic/reviews/pending/' + accountId, (message) => {
  const event = JSON.parse(message.body);
  if (event.action === 'REMOVE') {
    // Remove item from pending reviews list
    removePendingReview(event.bookingId, event.employeeId);
  }
});
```

---

## Notes
- **Test Date Context**: All test cases assume current date is December 9, 2025
- **Breaking Changes**: 
  - `GET /reviews/criteria` path changed to `GET /criteria`
  - `GET /employees/{employeeId}/reviews/summary` path changed to `GET /employees/{employeeId}/summary`
  - `GET /pending` now returns paginated response (Page format) instead of `{ success, data, total }`
  - `GET /bookings/{bookingId}/reviewable-employees` now returns paginated response (Page format)
  - `ReviewSummaryResponse` now includes `employeeName`, `employeeAvatar`, and `ratingDistribution`
  - `ReviewResponse` now includes `bookingCode`, `customerName`, `employeeName`, `employeeAvatar`, and `averageRating`
- **Pagination Parameters**: 
  - `page`: Page number (0-indexed, default: 0)
  - `size`: Page size (default: 10, max: 50)
- **Performance**: Rating distribution query uses GROUP BY for efficient aggregation
- **Security**: All customer-specific endpoints validate ownership before returning data
