# API Test Cases - Service Booking Management

## Overview
This document describes the test cases for the **Service Booking** endpoints of the Customer API.  
The endpoints allow authenticated customers to get service options and calculate pricing for bookings.  
**Base URL**: `/api/v1/customer/services`

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
- **Authorization Header**: `Bearer <valid_customer_token>`
- **Permission**: User must have "service.view" permission
- **Role**: Accessible by CUSTOMER, EMPLOYEE, and ADMIN roles

---

## Database Test Data
Based on housekeeping_service_v6.sql:
- **Services**: 
  - Service ID 1: "Dọn dẹp theo giờ" (50,000đ/hour, 2.0 hours)
  - Service ID 2: "Tổng vệ sinh" (400,000đ/package, 4.0 hours)
- **Service Options**: 
  - Option ID 1: "Loại phòng" (required, single choice)
  - Option ID 2: "Dịch vụ bổ sung" (optional, multiple choice)
- **Service Option Choices**:
  - Choice ID 1: "Phòng khách" (Option 1)
  - Choice ID 2: "Phòng ngủ" (Option 1)
  - Choice ID 3: "Giặt ủi" (Option 2, +50,000đ, +1 giờ)
  - Choice ID 4: "Lau kính" (Option 2, +30,000đ, +0.5 giờ)
- **Pricing Rules**: Applied based on selected choices
- **Permissions**: "service.view" permission required for all endpoints

---

## GET /services/{serviceId}/options - Get Service Options

### Test Case 1: Successfully Get Service Options
- **Test Case ID**: TC_SERVICE_OPTIONS_001
- **Description**: Verify that a customer can retrieve available options for a specific service.
- **Preconditions**:
  - Customer is authenticated with valid token.
  - Customer has "service.view" permission.
  - Service with ID 1 exists and has options configured.
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/customer/services/1/options`
  - **Path Parameter**: `serviceId = 1`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_customer_token>
    ```
- **Output**:
  ```json
  {
    "success": true,
    "message": "Lấy thông tin dịch vụ và tùy chọn thành công",
    "data": {
        "serviceId": 1,
        "serviceName": "Dọn dẹp theo giờ",
        "description": "Lau dọn, hút bụi, làm sạch các bề mặt cơ bản trong nhà. Phù hợp cho nhu cầu duy trì vệ sinh hàng tuần.",
        "basePrice": 50000.00,
        "unit": "Giờ",
        "estimatedDurationHours": 2.00,
        "iconUrl": "https://res.cloudinary.com/dkzemgit8/image/upload/v1757599899/Cleaning_Clock_z29juh.png",
        "formattedPrice": "50,000đ/Giờ",
        "formattedDuration": "2 giờ 0 phút",
        "options": [
            {
                "optionId": 4,
                "optionName": "Số phòng ngủ cần dọn?",
                "optionType": "QUANTITY_INPUT",
                "displayOrder": 1,
                "isRequired": true,
                "choices": []
            },
            {
                "optionId": 5,
                "optionName": "Bạn có yêu cầu thêm công việc nào?",
                "optionType": "MULTIPLE_CHOICE_CHECKBOX",
                "displayOrder": 2,
                "isRequired": true,
                "choices": [
                    {
                        "choiceId": 5,
                        "choiceName": "Giặt chăn ga",
                        "displayOrder": 1,
                        "isDefault": false
                    },
                    {
                        "choiceId": 6,
                        "choiceName": "Rửa chén",
                        "displayOrder": 2,
                        "isDefault": false
                    },
                    {
                        "choiceId": 7,
                        "choiceName": "Lau cửa kính",
                        "displayOrder": 3,
                        "isDefault": false
                    }
                ]
            }
        ]
    }
  }
  ```
- **Status Code**: `200 OK`

---

### Test Case 2: Service Not Found for Options
- **Test Case ID**: TC_SERVICE_OPTIONS_002
- **Description**: Verify that request fails when serviceId does not exist or service is inactive.
- **Preconditions**:
  - Customer is authenticated with valid token.
  - Customer has "service.view" permission.
  - Service with ID 999 does not exist.
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/customer/services/999/options`
  - **Path Parameter**: `serviceId = 999`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_customer_token>
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Không tìm thấy dịch vụ hoặc dịch vụ đã ngừng hoạt động",
    "data": null
  }
  ```
- **Status Code**: `404 Not Found`

---

### Test Case 3: Invalid Token for Service Options
- **Test Case ID**: TC_SERVICE_OPTIONS_003
- **Description**: Verify that request fails when Authorization header is missing or invalid.
- **Preconditions**: None
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/customer/services/1/options`
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

---

### Test Case 4: Service Options with No Options Configured
- **Test Case ID**: TC_SERVICE_OPTIONS_004
- **Description**: Verify response when service exists but has no options configured.
- **Preconditions**:
  - Customer is authenticated with valid token.
  - Service exists but has no options configured.
- **Input**:
  - **Method**: `GET`
  - **URL**: `/api/v1/customer/services/2/options`
  - **Path Parameter**: `serviceId = 2`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_customer_token>
    ```
- **Expected Output**:
  ```json
  {
    "success": true,
    "message": "Lấy tùy chọn dịch vụ thành công",
    "data": {
      "serviceId": 2,
      "serviceName": "Tổng vệ sinh",
      "options": []
    }
  }
  ```
- **Status Code**: `200 OK`

---

## POST /services/calculate-price - Calculate Service Price

### Test Case 5: Successfully Calculate Price with Basic Choices
- **Test Case ID**: TC_CALCULATE_PRICE_001
- **Description**: Verify that customer can calculate price for a service with selected options.
- **Preconditions**:
  - Customer is authenticated with valid token.
  - Service with ID 1 exists.
  - Choice IDs 1 and 3 exist with pricing rules.
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/customer/services/calculate-price`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_customer_token>
    Content-Type: application/json
    ```
  - **Body**:
    ```json
    {
      "serviceId": 1,
      "selectedChoiceIds": [1, 3]
    }
    ```
- **Expected Output**:
  ```json
  {
    "success": true,
    "message": "Tính toán giá thành công",
    "data": {
      "serviceId": 1,
      "serviceName": "Dọn dẹp theo giờ",
      "finalPrice": 100000,
      "suggestedStaff": 1,
      "estimatedDuration": 3.0,
      "formattedPrice": "100,000đ/giờ",
      "formattedDuration": "3 giờ"
    }
  }
  ```
- **Status Code**: `200 OK`

---

### Test Case 6: Calculate Price with Multiple Add-on Services
- **Test Case ID**: TC_CALCULATE_PRICE_002
- **Description**: Verify price calculation with multiple add-on services that affect price, staff, and duration.
- **Preconditions**:
  - Customer is authenticated with valid token.
  - Service and choices exist with complex pricing rules.
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/customer/services/calculate-price`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_customer_token>
    Content-Type: application/json
    ```
  - **Body**:
    ```json
    {
      "serviceId": 1,
      "selectedChoiceIds": [1, 3, 4]
    }
    ```
- **Expected Output**:
  ```json
  {
    "success": true,
    "message": "Tính toán giá thành công",
    "data": {
      "serviceId": 1,
      "serviceName": "Dọn dẹp theo giờ",
      "finalPrice": 130000,
      "suggestedStaff": 2,
      "estimatedDuration": 3.5,
      "formattedPrice": "130,000đ/giờ",
      "formattedDuration": "3 giờ 30 phút"
    }
  }
  ```
- **Status Code**: `200 OK`

---

### Test Case 7: Calculate Price - Service Not Found
- **Test Case ID**: TC_CALCULATE_PRICE_003
- **Description**: Verify that price calculation fails when service does not exist.
- **Preconditions**:
  - Customer is authenticated with valid token.
  - Service with ID 999 does not exist.
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/customer/services/calculate-price`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_customer_token>
    Content-Type: application/json
    ```
  - **Body**:
    ```json
    {
      "serviceId": 999,
      "selectedChoiceIds": [1]
    }
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Không tìm thấy dịch vụ hoặc dịch vụ đã ngừng hoạt động",
    "data": null
  }
  ```
- **Status Code**: `200 OK`

---

### Test Case 8: Calculate Price - Missing Service ID
- **Test Case ID**: TC_CALCULATE_PRICE_004
- **Description**: Verify validation when service ID is missing from request.
- **Preconditions**:
  - Customer is authenticated with valid token.
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/customer/services/calculate-price`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_customer_token>
    Content-Type: application/json
    ```
  - **Body**:
    ```json
    {
      "selectedChoiceIds": [1, 2]
    }
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Service ID không được để trống",
    "data": null
  }
  ```
- **Status Code**: `200 OK`

---

### Test Case 9: Calculate Price - Empty Choice Selection
- **Test Case ID**: TC_CALCULATE_PRICE_005
- **Description**: Verify validation when no choices are selected.
- **Preconditions**:
  - Customer is authenticated with valid token.
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/customer/services/calculate-price`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_customer_token>
    Content-Type: application/json
    ```
  - **Body**:
    ```json
    {
      "serviceId": 1,
      "selectedChoiceIds": []
    }
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Vui lòng chọn ít nhất một tùy chọn",
    "data": null
  }
  ```
- **Status Code**: `200 OK`

---

### Test Case 10: Calculate Price - Invalid Choice IDs
- **Test Case ID**: TC_CALCULATE_PRICE_006
- **Description**: Verify behavior when non-existent choice IDs are provided.
- **Preconditions**:
  - Customer is authenticated with valid token.
  - Service exists but choice IDs 999, 998 do not exist.
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/customer/services/calculate-price`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_customer_token>
    Content-Type: application/json
    ```
  - **Body**:
    ```json
    {
      "serviceId": 1,
      "selectedChoiceIds": [999, 998]
    }
    ```
- **Expected Output**:
  ```json
  {
    "success": true,
    "message": "Tính toán giá thành công",
    "data": {
      "serviceId": 1,
      "serviceName": "Dọn dẹp theo giờ",
      "finalPrice": 50000,
      "suggestedStaff": 1,
      "estimatedDuration": 2.0,
      "formattedPrice": "50,000đ/giờ",
      "formattedDuration": "2 giờ"
    }
  }
  ```
- **Status Code**: `200 OK`

---

### Test Case 11: Calculate Price - Invalid Token
- **Test Case ID**: TC_CALCULATE_PRICE_007
- **Description**: Verify that price calculation fails with invalid authentication.
- **Preconditions**: None
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/customer/services/calculate-price`
  - **Headers**: 
    ```
    Authorization: Bearer invalid_token_here
    Content-Type: application/json
    ```
  - **Body**:
    ```json
    {
      "serviceId": 1,
      "selectedChoiceIds": [1]
    }
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

---

### Test Case 12: Calculate Price - Malformed JSON
- **Test Case ID**: TC_CALCULATE_PRICE_008
- **Description**: Verify error handling when request body contains invalid JSON.
- **Preconditions**:
  - Customer is authenticated with valid token.
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/customer/services/calculate-price`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_customer_token>
    Content-Type: application/json
    ```
  - **Body**:
    ```json
    {
      "serviceId": 1,
      "selectedChoiceIds": [1, 2]
    }
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Lỗi hệ thống khi tính toán giá",
    "data": null
  }
  ```
- **Status Code**: `500 Internal Server Error`

---

### Test Case 13: Calculate Price - Package Service with Complex Pricing
- **Test Case ID**: TC_CALCULATE_PRICE_009
- **Description**: Verify price calculation for package-based services with complex pricing rules.
- **Preconditions**:
  - Customer is authenticated with valid token.
  - Package service (ID 2) exists with complex pricing options.
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/customer/services/calculate-price`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_customer_token>
    Content-Type: application/json
    ```
  - **Body**:
    ```json
    {
      "serviceId": 2,
      "selectedChoiceIds": [5, 6, 7]
    }
    ```
- **Expected Output**:
  ```json
  {
    "success": true,
    "message": "Tính toán giá thành công",
    "data": {
      "serviceId": 2,
      "serviceName": "Tổng vệ sinh",
      "finalPrice": 600000,
      "suggestedStaff": 3,
      "estimatedDuration": 6.0,
      "formattedPrice": "600,000đ/gói",
      "formattedDuration": "6 giờ"
    }
  }
  ```
- **Status Code**: `200 OK`

---

### Test Case 14: Calculate Price - Duration Under 1 Hour
- **Test Case ID**: TC_CALCULATE_PRICE_010
- **Description**: Verify proper formatting when calculated duration is less than 1 hour.
- **Preconditions**:
  - Customer is authenticated with valid token.
  - Service and choices configured to result in < 1 hour duration.
- **Input**:
  - **Method**: `POST`
  - **URL**: `/api/v1/customer/services/calculate-price`
  - **Headers**: 
    ```
    Authorization: Bearer <valid_customer_token>
    Content-Type: application/json
    ```
  - **Body**:
    ```json
    {
      "serviceId": 3,
      "selectedChoiceIds": [8]
    }
    ```
- **Expected Output**:
  ```json
  {
    "success": true,
    "message": "Tính toán giá thành công",
    "data": {
      "serviceId": 3,
      "serviceName": "Dịch vụ nhanh",
      "finalPrice": 25000,
      "suggestedStaff": 1,
      "estimatedDuration": 0.5,
      "formattedPrice": "25,000đ/giờ",
      "formattedDuration": "30 phút"
    }
  }
  ```
- **Status Code**: `200 OK`

---

## Integration Test Scenarios

### Test Case 15: End-to-End: Get Options then Calculate Price
- **Test Case ID**: TC_INTEGRATION_001
- **Description**: Verify complete workflow from getting service options to calculating price.
- **Preconditions**:
  - Customer is authenticated with valid token.
  - Service with ID 1 exists with options.
- **Steps**:
  1. **Step 1**: Get service options
     - **URL**: `/api/v1/customer/services/1/options`
     - **Method**: `GET`
     - **Expected**: Successfully retrieve available options
  
  2. **Step 2**: Calculate price with selected options
     - **URL**: `/api/v1/customer/services/calculate-price`
     - **Method**: `POST`
     - **Body**: Selected choices from step 1
     - **Expected**: Successfully calculate final price
- **Expected Flow**:
  - Customer gets available options for service
  - Customer selects desired options
  - System calculates final price, staff count, and duration
  - All calculations are properly formatted for display

---

## Notes
- **Test Environment**: Database should be configured with test data including services, service options, choices, and pricing rules from housekeeping_service_v6.sql.
- **Authentication**: All endpoints require valid JWT tokens.
- **Permission**: Users must have "service.view" permission to access these endpoints.
- **Service Status**: Only active services can have their options retrieved or prices calculated.
- **Price Formatting**: Different formatting based on service unit (hour, m2, package).
- **Duration Formatting**: Smart formatting for hours and minutes display.
- **Error Handling**: Service layer catches exceptions and returns appropriate error responses.
- **Security**: JWT tokens are validated for each request.
- **Business Logic**: 
  - Price calculation includes base price + pricing rule adjustments
  - Staff suggestions based on pricing rules and service complexity
  - Duration calculation includes base duration + rule adjustments
  - Minimum values enforced (price ≥ 0, staff ≥ 1, duration ≥ 0)
- **Data Validation**: Request validation for required fields and data types.
- **Integration**: Options endpoint provides data structure for calculate-price endpoint input.
