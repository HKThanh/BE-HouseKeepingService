# API Test Cases - Customer Bookings Retrieval

## Overview
This document describes the comprehensive test cases for the **Customer Bookings Retrieval** endpoint of the Customer API.  
The endpoint allows retrieving paginated bookings for a specific customer with sorting and filtering capabilities.  
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
- **Authorization Header**: `Bearer <valid_token>`
- **Role Requirements**: CUSTOMER or ADMIN role required

---

## Pagination Features
- **Default Page Size**: 10 items per page
- **Maximum Page Size**: 100 items per page
- **Default Sort**: createdAt,desc (newest first)
- **Supported Sort Fields**: createdAt, bookingTime, totalPrice, status
- **Sort Directions**: asc, desc

---

## Database Test Data
Based on housekeeping_service_v8.sql:
- **Sample Customer**: john_doe (c1000001-0000-0000-0000-000000000001)
- **Customer has multiple bookings** with various statuses:
  - COMPLETED: b0000001-0000-0000-0000-000000000001 (80,000 VND)
  - CONFIRMED: b0000001-0000-0000-0000-000000000004 (120,000 VND)
  - AWAITING_EMPLOYEE: b0000001-0000-0000-0000-000000000007 (270,000 VND)
  - CONFIRMED: b0000001-0000-0000-0000-000000000010 (630,000 VND)
  - Additional bookings: book0004, book0007, book0009, book0012
- **Address References**: Multiple addresses including default and non-default
- **Booking Codes**: BK000001, BK000004, BK000007, BK000010, HKS000004, etc.

---

## API Endpoint Covered
**GET /customer/{customerId}** - Get Customer Bookings with Pagination

---

## GET /customer/{customerId} - Get Customer Bookings

### Test Case 1: Successfully Get Customer Bookings (Default Pagination)
- **Test Case ID**: TC_CUSTOMER_BOOKINGS_001
- **Description**: Verify that a customer can retrieve their bookings with default pagination settings.
- **Preconditions**: 
  - Customer is authenticated with valid token.
  - Customer has existing bookings.
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/customer/bookings/customer/c1000001-0000-0000-0000-000000000001`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_customer_token_john_doe>
    ```
- **Expected Output**:
  ```json
  {
    "content": [
        {
            "bookingId": "b7b0804e-ed1b-4b71-a67a-d72310e64bb4",
            "bookingCode": "BK34606126",
            "customerId": "c1000001-0000-0000-0000-000000000001",
            "customerName": "John Doe",
            "address": {
                "addressId": "d803f91e-f477-49b2-a1f1-33335567ea74",
                "fullAddress": "456 Nguyễn Văn Linh, An Phú, Quận 2, TP. Hồ Chí Minh",
                "ward": "Phường An Phú",
                "district": "Quận 2",
                "city": "TP. Hồ Chí Minh",
                "latitude": 10.7829,
                "longitude": 106.7439,
                "isDefault": false
            },
            "bookingTime": "2025-09-26T16:00",
            "note": "New address booking test",
            "formattedTotalAmount": "700,000đ",
            "status": "PENDING",
            "promotion": null,
            "payment": {
                "paymentId": "4104f029-bca0-4575-9234-b696d5c54954",
                "amount": 700000.00,
                "paymentMethod": "Thanh toán tiền mặt",
                "paymentStatus": "PENDING",
                "transactionCode": "TXN_1758776934605",
                "createdAt": "2025-09-25 12:08:54",
                "paidAt": null
            }
        },
        {
            "bookingId": "9ed86dba-8dff-41b0-a4a8-c3d0f5d731ca",
            "bookingCode": "BK58157400",
            "customerId": "c1000001-0000-0000-0000-000000000001",
            "customerName": "John Doe",
            "address": {
                "addressId": "adrs0001-0000-0000-0000-000000000001",
                "fullAddress": "123 Lê Trọng Tấn, Tây Thạnh, Tân Phú, TP. Hồ Chí Minh",
                "ward": "Phường Tây Thạnh",
                "district": "Quận Tân Phú",
                "city": "TP. Hồ Chí Minh",
                "latitude": 10.7943,
                "longitude": 106.6256,
                "isDefault": true
            },
            "bookingTime": "2025-09-26T14:00",
            "note": "Booking without specific employee assignment",
            "formattedTotalAmount": "700,000đ",
            "status": "AWAITING_EMPLOYEE",
            "promotion": null,
            "payment": {
                "paymentId": "16fe4f1c-6184-40ae-a300-c9cc212e9435",
                "amount": 700000.00,
                "paymentMethod": "Thanh toán tiền mặt",
                "paymentStatus": "PENDING",
                "transactionCode": "TXN_1758772558152",
                "createdAt": "2025-09-25 10:55:58",
                "paidAt": null
            }
        },
        {
            "bookingId": "147d53a6-8556-4670-b803-5f05d335fca5",
            "bookingCode": "BK38797809",
            "customerId": "c1000001-0000-0000-0000-000000000001",
            "customerName": "John Doe",
            "address": {
                "addressId": "adrs0001-0000-0000-0000-000000000001",
                "fullAddress": "123 Lê Trọng Tấn, Tây Thạnh, Tân Phú, TP. Hồ Chí Minh",
                "ward": "Phường Tây Thạnh",
                "district": "Quận Tân Phú",
                "city": "TP. Hồ Chí Minh",
                "latitude": 10.7943,
                "longitude": 106.6256,
                "isDefault": true
            },
            "bookingTime": "2025-09-26T10:00",
            "note": "Cần dọn dẹp kỹ lưỡng phòng khách và bếp",
            "formattedTotalAmount": "700,000đ",
            "status": "PENDING",
            "promotion": null,
            "payment": {
                "paymentId": "4b5fcbe9-05f9-48f3-87ed-3425fa4bf09d",
                "amount": 700000.00,
                "paymentMethod": "Thanh toán tiền mặt",
                "paymentStatus": "PENDING",
                "transactionCode": "TXN_1758767838783",
                "createdAt": "2025-09-25 09:37:19",
                "paidAt": null
            }
        },
        {
            "bookingId": "book0007-0000-0000-0000-000000000001",
            "bookingCode": "HKS000007",
            "customerId": "c1000001-0000-0000-0000-000000000001",
            "customerName": "John Doe",
            "address": {
                "addressId": "adrs0001-0000-0000-0000-000000000007",
                "fullAddress": "432 Võ Văn Tần, Phường 5, Quận 3, TP. Hồ Chí Minh",
                "ward": "Phường 5",
                "district": "Quận 3",
                "city": "TP. Hồ Chí Minh",
                "latitude": 10.7756,
                "longitude": 106.6914,
                "isDefault": false
            },
            "bookingTime": "2024-09-26T16:00",
            "note": "Dọn dẹp sau tiệc, nhiều rác cần dọn",
            "formattedTotalAmount": "500,000đ",
            "status": "CONFIRMED",
            "promotion": null,
            "payment": {
                "paymentId": "pay00004-0000-0000-0000-000000000001",
                "amount": 500000.00,
                "paymentMethod": null,
                "paymentStatus": "PAID",
                "transactionCode": "TXN20240926001",
                "createdAt": "2025-09-24 17:01:13",
                "paidAt": "2024-09-26 15:30:00"
            }
        },
        {
            "bookingId": "book0004-0000-0000-0000-000000000001",
            "bookingCode": "HKS000004",
            "customerId": "c1000001-0000-0000-0000-000000000001",
            "customerName": "John Doe",
            "address": {
                "addressId": "adrs0001-0000-0000-0000-000000000004",
                "fullAddress": "789 Nguyễn Văn Cừ, Phường 3, Quận 5, TP. Hồ Chí Minh",
                "ward": "Phường 3",
                "district": "Quận 5",
                "city": "TP. Hồ Chí Minh",
                "latitude": 10.7594,
                "longitude": 106.6822,
                "isDefault": false
            },
            "bookingTime": "2024-09-25T08:00",
            "note": "Cần dọn dẹp tổng quát, chú ý khu vực bếp",
            "formattedTotalAmount": "450,000đ",
            "status": "AWAITING_EMPLOYEE",
            "promotion": null,
            "payment": null
        },
        {
            "bookingId": "book0009-0000-0000-0000-000000000001",
            "bookingCode": "HKS000009",
            "customerId": "c1000001-0000-0000-0000-000000000001",
            "customerName": "John Doe",
            "address": {
                "addressId": "adrs0001-0000-0000-0000-000000000001",
                "fullAddress": "123 Lê Trọng Tấn, Tây Thạnh, Tân Phú, TP. Hồ Chí Minh",
                "ward": "Phường Tây Thạnh",
                "district": "Quận Tân Phú",
                "city": "TP. Hồ Chí Minh",
                "latitude": 10.7943,
                "longitude": 106.6256,
                "isDefault": true
            },
            "bookingTime": "2024-09-27T15:30",
            "note": "Cần dọn nhà trước khi có khách",
            "formattedTotalAmount": "300,000đ",
            "status": "AWAITING_EMPLOYEE",
            "promotion": null,
            "payment": null
        },
        {
            "bookingId": "book0012-0000-0000-0000-000000000001",
            "bookingCode": "HKS000012",
            "customerId": "c1000001-0000-0000-0000-000000000001",
            "customerName": "John Doe",
            "address": {
                "addressId": "adrs0001-0000-0000-0000-000000000004",
                "fullAddress": "789 Nguyễn Văn Cừ, Phường 3, Quận 5, TP. Hồ Chí Minh",
                "ward": "Phường 3",
                "district": "Quận 5",
                "city": "TP. Hồ Chí Minh",
                "latitude": 10.7594,
                "longitude": 106.6822,
                "isDefault": false
            },
            "bookingTime": "2024-09-29T11:00",
            "note": "Vệ sinh máy lạnh và quạt trần",
            "formattedTotalAmount": "550,000đ",
            "status": "AWAITING_EMPLOYEE",
            "promotion": null,
            "payment": null
        },
        {
            "bookingId": "b0000001-0000-0000-0000-000000000010",
            "bookingCode": "BK000010",
            "customerId": "c1000001-0000-0000-0000-000000000001",
            "customerName": "John Doe",
            "address": {
                "addressId": "adrs0001-0000-0000-0000-000000000001",
                "fullAddress": "123 Lê Trọng Tấn, Tây Thạnh, Tân Phú, TP. Hồ Chí Minh",
                "ward": "Phường Tây Thạnh",
                "district": "Quận Tân Phú",
                "city": "TP. Hồ Chí Minh",
                "latitude": 10.7943,
                "longitude": 106.6256,
                "isDefault": true
            },
            "bookingTime": "2025-09-02T13:00",
            "note": "Tổng vệ sinh nhà phố 2 tầng.",
            "formattedTotalAmount": "630,000đ",
            "status": "CONFIRMED",
            "promotion": {
                "promotionId": 2,
                "promoCode": "KHAITRUONG10",
                "description": "Giảm 10% mừng khai trương",
                "discountType": "PERCENTAGE",
                "discountValue": 10.00,
                "maxDiscountAmount": 50000.00
            },
            "payment": {
                "paymentId": "pay00001-0000-0000-0000-000000000006",
                "amount": 630000.00,
                "paymentMethod": "Cổng thanh toán VNPAY",
                "paymentStatus": "PENDING",
                "transactionCode": null,
                "createdAt": "2025-09-24 17:01:13",
                "paidAt": null
            }
        },
        {
            "bookingId": "b0000001-0000-0000-0000-000000000007",
            "bookingCode": "BK000007",
            "customerId": "c1000001-0000-0000-0000-000000000001",
            "customerName": "John Doe",
            "address": {
                "addressId": "adrs0001-0000-0000-0000-000000000001",
                "fullAddress": "123 Lê Trọng Tấn, Tây Thạnh, Tân Phú, TP. Hồ Chí Minh",
                "ward": "Phường Tây Thạnh",
                "district": "Quận Tân Phú",
                "city": "TP. Hồ Chí Minh",
                "latitude": 10.7943,
                "longitude": 106.6256,
                "isDefault": true
            },
            "bookingTime": "2025-09-05T09:00",
            "note": "Vệ sinh sofa phòng khách.",
            "formattedTotalAmount": "330,000đ",
            "status": "AWAITING_EMPLOYEE",
            "promotion": {
                "promotionId": 1,
                "promoCode": "GIAM20K",
                "description": "Giảm giá 20,000đ cho mọi đơn hàng",
                "discountType": "FIXED_AMOUNT",
                "discountValue": 20000.00,
                "maxDiscountAmount": null
            },
            "payment": null
        },
        {
            "bookingId": "b0000001-0000-0000-0000-000000000004",
            "bookingCode": "BK000004",
            "customerId": "c1000001-0000-0000-0000-000000000001",
            "customerName": "John Doe",
            "address": {
                "addressId": "adrs0001-0000-0000-0000-000000000001",
                "fullAddress": "123 Lê Trọng Tấn, Tây Thạnh, Tân Phú, TP. Hồ Chí Minh",
                "ward": "Phường Tây Thạnh",
                "district": "Quận Tân Phú",
                "city": "TP. Hồ Chí Minh",
                "latitude": 10.7943,
                "longitude": 106.6256,
                "isDefault": true
            },
            "bookingTime": "2025-09-01T08:00",
            "note": "Giặt vest cho buổi họp quan trọng.",
            "formattedTotalAmount": "150,000đ",
            "status": "CONFIRMED",
            "promotion": null,
            "payment": {
                "paymentId": "pay00001-0000-0000-0000-000000000003",
                "amount": 150000.00,
                "paymentMethod": "Thanh toán tiền mặt",
                "paymentStatus": "PAID",
                "transactionCode": null,
                "createdAt": "2025-09-24 17:01:13",
                "paidAt": "2025-09-01 08:30:00"
            }
        }
    ],
    "pageable": {
        "pageNumber": 0,
        "pageSize": 10,
        "sort": {
            "empty": false,
            "sorted": true,
            "unsorted": false
        },
        "offset": 0,
        "paged": true,
        "unpaged": false
    },
    "totalElements": 11,
    "totalPages": 2,
    "last": false,
    "size": 10,
    "number": 0,
    "sort": {
        "empty": false,
        "sorted": true,
        "unsorted": false
    },
    "numberOfElements": 10,
    "first": true,
    "empty": false
  }
  ```
- **Status Code**: `200 OK`

---

### Test Case 2: Get Customer Bookings with Custom Pagination
- **Test Case ID**: TC_CUSTOMER_BOOKINGS_002
- **Description**: Verify pagination works with custom page size and page number.
- **Preconditions**: 
  - Customer is authenticated with valid token.
  - Customer has multiple bookings (more than 3).
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/customer/bookings/customer/c1000001-0000-0000-0000-000000000001?page=0&size=2`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_customer_token_john_doe>
    ```
- **Expected Output**:
  ```json
  {
    "content": [
        {
            "bookingId": "b7b0804e-ed1b-4b71-a67a-d72310e64bb4",
            "bookingCode": "BK34606126",
            "customerId": "c1000001-0000-0000-0000-000000000001",
            "customerName": "John Doe",
            "address": {
                "addressId": "d803f91e-f477-49b2-a1f1-33335567ea74",
                "fullAddress": "456 Nguyễn Văn Linh, An Phú, Quận 2, TP. Hồ Chí Minh",
                "ward": "Phường An Phú",
                "district": "Quận 2",
                "city": "TP. Hồ Chí Minh",
                "latitude": 10.7829,
                "longitude": 106.7439,
                "isDefault": false
            },
            "bookingTime": "2025-09-26T16:00",
            "note": "New address booking test",
            "formattedTotalAmount": "700,000đ",
            "status": "PENDING",
            "promotion": null,
            "payment": {
                "paymentId": "4104f029-bca0-4575-9234-b696d5c54954",
                "amount": 700000.00,
                "paymentMethod": "Thanh toán tiền mặt",
                "paymentStatus": "PENDING",
                "transactionCode": "TXN_1758776934605",
                "createdAt": "2025-09-25 12:08:54",
                "paidAt": null
            }
        },
        {
            "bookingId": "9ed86dba-8dff-41b0-a4a8-c3d0f5d731ca",
            "bookingCode": "BK58157400",
            "customerId": "c1000001-0000-0000-0000-000000000001",
            "customerName": "John Doe",
            "address": {
                "addressId": "adrs0001-0000-0000-0000-000000000001",
                "fullAddress": "123 Lê Trọng Tấn, Tây Thạnh, Tân Phú, TP. Hồ Chí Minh",
                "ward": "Phường Tây Thạnh",
                "district": "Quận Tân Phú",
                "city": "TP. Hồ Chí Minh",
                "latitude": 10.7943,
                "longitude": 106.6256,
                "isDefault": true
            },
            "bookingTime": "2025-09-26T14:00",
            "note": "Booking without specific employee assignment",
            "formattedTotalAmount": "700,000đ",
            "status": "AWAITING_EMPLOYEE",
            "promotion": null,
            "payment": {
                "paymentId": "16fe4f1c-6184-40ae-a300-c9cc212e9435",
                "amount": 700000.00,
                "paymentMethod": "Thanh toán tiền mặt",
                "paymentStatus": "PENDING",
                "transactionCode": "TXN_1758772558152",
                "createdAt": "2025-09-25 10:55:58",
                "paidAt": null
            }
        }
    ],
    "pageable": {
        "pageNumber": 0,
        "pageSize": 2,
        "sort": {
            "empty": false,
            "sorted": true,
            "unsorted": false
        },
        "offset": 0,
        "paged": true,
        "unpaged": false
    },
    "totalElements": 11,
    "totalPages": 6,
    "last": false,
    "size": 2,
    "number": 0,
    "sort": {
        "empty": false,
        "sorted": true,
        "unsorted": false
    },
    "numberOfElements": 2,
    "first": true,
    "empty": false
  }
  ```
- **Status Code**: `200 OK`

---

### Test Case 3: Get Customer Bookings with Custom Sort (Price Ascending)
- **Test Case ID**: TC_CUSTOMER_BOOKINGS_003
- **Description**: Verify sorting functionality by total price in ascending order.
- **Preconditions**: 
  - Customer is authenticated with valid token.
  - Customer has bookings with different prices.
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/customer/bookings/customer/c1000001-0000-0000-0000-000000000001?sort=totalAmount,asc&size=5`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_customer_token_john_doe>
    ```
- **Expected Output**:
  ```json
  {
    "content": [
        {
            "bookingId": "b7b0804e-ed1b-4b71-a67a-d72310e64bb4",
            "bookingCode": "BK34606126",
            "customerId": "c1000001-0000-0000-0000-000000000001",
            "customerName": "John Doe",
            "address": {
                "addressId": "d803f91e-f477-49b2-a1f1-33335567ea74",
                "fullAddress": "456 Nguyễn Văn Linh, An Phú, Quận 2, TP. Hồ Chí Minh",
                "ward": "Phường An Phú",
                "district": "Quận 2",
                "city": "TP. Hồ Chí Minh",
                "latitude": 10.7829,
                "longitude": 106.7439,
                "isDefault": false
            },
            "bookingTime": "2025-09-26T16:00",
            "note": "New address booking test",
            "formattedTotalAmount": "700,000đ",
            "status": "PENDING",
            "promotion": null,
            "payment": {
                "paymentId": "4104f029-bca0-4575-9234-b696d5c54954",
                "amount": 700000.00,
                "paymentMethod": "Thanh toán tiền mặt",
                "paymentStatus": "PENDING",
                "transactionCode": "TXN_1758776934605",
                "createdAt": "2025-09-25 12:08:54",
                "paidAt": null
            }
        },
        {
            "bookingId": "9ed86dba-8dff-41b0-a4a8-c3d0f5d731ca",
            "bookingCode": "BK58157400",
            "customerId": "c1000001-0000-0000-0000-000000000001",
            "customerName": "John Doe",
            "address": {
                "addressId": "adrs0001-0000-0000-0000-000000000001",
                "fullAddress": "123 Lê Trọng Tấn, Tây Thạnh, Tân Phú, TP. Hồ Chí Minh",
                "ward": "Phường Tây Thạnh",
                "district": "Quận Tân Phú",
                "city": "TP. Hồ Chí Minh",
                "latitude": 10.7943,
                "longitude": 106.6256,
                "isDefault": true
            },
            "bookingTime": "2025-09-26T14:00",
            "note": "Booking without specific employee assignment",
            "formattedTotalAmount": "700,000đ",
            "status": "AWAITING_EMPLOYEE",
            "promotion": null,
            "payment": {
                "paymentId": "16fe4f1c-6184-40ae-a300-c9cc212e9435",
                "amount": 700000.00,
                "paymentMethod": "Thanh toán tiền mặt",
                "paymentStatus": "PENDING",
                "transactionCode": "TXN_1758772558152",
                "createdAt": "2025-09-25 10:55:58",
                "paidAt": null
            }
        },
        {
            "bookingId": "147d53a6-8556-4670-b803-5f05d335fca5",
            "bookingCode": "BK38797809",
            "customerId": "c1000001-0000-0000-0000-000000000001",
            "customerName": "John Doe",
            "address": {
                "addressId": "adrs0001-0000-0000-0000-000000000001",
                "fullAddress": "123 Lê Trọng Tấn, Tây Thạnh, Tân Phú, TP. Hồ Chí Minh",
                "ward": "Phường Tây Thạnh",
                "district": "Quận Tân Phú",
                "city": "TP. Hồ Chí Minh",
                "latitude": 10.7943,
                "longitude": 106.6256,
                "isDefault": true
            },
            "bookingTime": "2025-09-26T10:00",
            "note": "Cần dọn dẹp kỹ lưỡng phòng khách và bếp",
            "formattedTotalAmount": "700,000đ",
            "status": "PENDING",
            "promotion": null,
            "payment": {
                "paymentId": "4b5fcbe9-05f9-48f3-87ed-3425fa4bf09d",
                "amount": 700000.00,
                "paymentMethod": "Thanh toán tiền mặt",
                "paymentStatus": "PENDING",
                "transactionCode": "TXN_1758767838783",
                "createdAt": "2025-09-25 09:37:19",
                "paidAt": null
            }
        },
        {
            "bookingId": "book0009-0000-0000-0000-000000000001",
            "bookingCode": "HKS000009",
            "customerId": "c1000001-0000-0000-0000-000000000001",
            "customerName": "John Doe",
            "address": {
                "addressId": "adrs0001-0000-0000-0000-000000000001",
                "fullAddress": "123 Lê Trọng Tấn, Tây Thạnh, Tân Phú, TP. Hồ Chí Minh",
                "ward": "Phường Tây Thạnh",
                "district": "Quận Tân Phú",
                "city": "TP. Hồ Chí Minh",
                "latitude": 10.7943,
                "longitude": 106.6256,
                "isDefault": true
            },
            "bookingTime": "2024-09-27T15:30",
            "note": "Cần dọn nhà trước khi có khách",
            "formattedTotalAmount": "300,000đ",
            "status": "AWAITING_EMPLOYEE",
            "promotion": null,
            "payment": null
        },
        {
            "bookingId": "book0004-0000-0000-0000-000000000001",
            "bookingCode": "HKS000004",
            "customerId": "c1000001-0000-0000-0000-000000000001",
            "customerName": "John Doe",
            "address": {
                "addressId": "adrs0001-0000-0000-0000-000000000004",
                "fullAddress": "789 Nguyễn Văn Cừ, Phường 3, Quận 5, TP. Hồ Chí Minh",
                "ward": "Phường 3",
                "district": "Quận 5",
                "city": "TP. Hồ Chí Minh",
                "latitude": 10.7594,
                "longitude": 106.6822,
                "isDefault": false
            },
            "bookingTime": "2024-09-25T08:00",
            "note": "Cần dọn dẹp tổng quát, chú ý khu vực bếp",
            "formattedTotalAmount": "450,000đ",
            "status": "AWAITING_EMPLOYEE",
            "promotion": null,
            "payment": null
        }
    ],
    "pageable": {
        "pageNumber": 0,
        "pageSize": 5,
        "sort": {
            "empty": false,
            "sorted": true,
            "unsorted": false
        },
        "offset": 0,
        "paged": true,
        "unpaged": false
    },
    "totalElements": 11,
    "totalPages": 3,
    "last": false,
    "size": 5,
    "number": 0,
    "sort": {
        "empty": false,
        "sorted": true,
        "unsorted": false
    },
    "numberOfElements": 5,
    "first": true,
    "empty": false
  }
  ```
- **Status Code**: `200 OK`

---

### Test Case 4: Customer with No Bookings
- **Test Case ID**: TC_CUSTOMER_BOOKINGS_004
- **Description**: Verify response when customer exists but has no bookings.
- **Preconditions**: 
  - Customer is authenticated with valid token.
  - Customer exists but has no bookings.
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/customer/bookings/customer/c1000002-0000-0000-0000-000000000001`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_customer_token>
    ```
- **Expected Output**:
  ```json
  {
    "content": [],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10,
      "sort": {
        "sorted": true,
        "orders": [
          {
            "property": "createdAt",
            "direction": "DESC"
          }
        ]
      }
    },
    "totalElements": 0,
    "totalPages": 0,
    "last": true,
    "first": true,
    "numberOfElements": 0,
    "size": 10,
    "number": 0,
    "empty": true
  }
  ```
- **Status Code**: `200 OK`

---

### Test Case 5: Non-Existent Customer
- **Test Case ID**: TC_CUSTOMER_BOOKINGS_005
- **Description**: Verify error handling when customer does not exist.
- **Preconditions**: 
  - User is authenticated with valid token.
  - Customer ID does not exist in the system.
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/customer/bookings/customer/invalid-customer-id`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_token>
    ```
- **Expected Output**:
  ```json
  {
    "content": [],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10
    },
    "totalElements": 0,
    "totalPages": 0,
    "last": true,
    "first": true,
    "numberOfElements": 0,
    "size": 10,
    "number": 0,
    "empty": true
  }
  ```
- **Status Code**: `200 OK`

---

### Test Case 6: Unauthorized Access
- **Test Case ID**: TC_CUSTOMER_BOOKINGS_006
- **Description**: Verify authentication is required to access customer bookings.
- **Preconditions**: 
  - No authentication token provided.
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/customer/bookings/customer/c1000001-0000-0000-0000-000000000001`
  - **Headers**: (No Authorization header)
- **Expected Output**:
  ```json
  {
    "error": "Unauthorized",
    "message": "Authentication required"
  }
  ```
- **Status Code**: `401 Unauthorized`

---

### Test Case 7: Invalid Pagination Parameters
- **Test Case ID**: TC_CUSTOMER_BOOKINGS_007
- **Description**: Verify handling of invalid pagination parameters with automatic correction.
- **Preconditions**: 
  - Customer is authenticated with valid token.
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/customer/bookings/customer/c1000001-0000-0000-0000-000000000001?page=-1&size=0&sort=invalidField,invalidDirection`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_customer_token_john_doe>
    ```
- **Expected Output**:
  ```json
  {
    "content": [
      {
        "bookingId": "b0000001-0000-0000-0000-000000000010",
        "bookingCode": "BK000010",
        "status": "CONFIRMED"
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10,
      "sort": {
        "sorted": true,
        "orders": [
          {
            "property": "createdAt",
            "direction": "DESC"
          }
        ]
      }
    },
    "totalElements": 8,
    "totalPages": 1
  }
  ```
- **Status Code**: `200 OK`

---

### Test Case 8: Large Page Size Limitation
- **Test Case ID**: TC_CUSTOMER_BOOKINGS_008
- **Description**: Verify maximum page size limitation is enforced.
- **Preconditions**: 
  - Customer is authenticated with valid token.
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/customer/bookings/customer/c1000001-0000-0000-0000-000000000001?size=150`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_customer_token_john_doe>
    ```
- **Expected Output**:
  ```json
  {
    "content": [
      {
        "bookingId": "b0000001-0000-0000-0000-000000000010"
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10,
      "sort": {
        "sorted": true,
        "orders": [
          {
            "property": "createdAt",
            "direction": "DESC"
          }
        ]
      }
    },
    "totalElements": 8,
    "totalPages": 1
  }
  ```
- **Status Code**: `200 OK`

---

## Summary
The customer bookings retrieval endpoint provides:
- **Pagination Support**: Configurable page size and page number with safety limits
- **Flexible Sorting**: Multiple sort fields with ascending/descending options
- **Security**: Role-based access control (CUSTOMER/ADMIN roles)
- **Error Handling**: Graceful handling of invalid parameters and missing data
- **Performance**: Optimized queries with pagination to handle large datasets
- **Data Consistency**: Returns complete booking information including addresses, services, and promotions
