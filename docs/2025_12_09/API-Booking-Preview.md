# API Booking Preview (Quote/Invoice Preview)

## Overview

This API endpoint allows customers and admins to preview a booking with detailed pricing breakdown before actually creating it. The response is formatted like an invoice, showing all services, fees, promotions, and the grand total.

**Key Features:**
- Returns complete pricing breakdown (invoice-style)
- Validates promotion/promo codes with caching (5-min TTL)
- Admin can preview on behalf of any customer
- Skips booking time validation for flexible previewing
- Always returns HTTP 200 (validation errors in response body)

---

## Endpoint

```
POST /api/v1/customer/bookings/preview
```

### Authentication
- **Required:** Yes (JWT Bearer Token)
- **Roles:** `ROLE_CUSTOMER`, `ROLE_ADMIN`

### Headers
| Header | Value | Required |
|--------|-------|----------|
| Authorization | Bearer `<token>` | Yes |
| Content-Type | application/json | Yes |

---

## Request Body

### Schema: `BookingPreviewRequest`

```json
{
  "customerId": "string (optional, Admin only)",
  "addressId": "string (required if newAddress is null)",
  "newAddress": {
    "customerId": "string",
    "fullAddress": "string",
    "ward": "string",
    "city": "string",
    "latitude": "number (optional)",
    "longitude": "number (optional)"
  },
  "bookingTime": "ISO DateTime (optional)",
  "note": "string (max 1000 chars)",
  "title": "string (max 255 chars)",
  "promoCode": "string (max 20 chars)",
  "bookingDetails": [
    {
      "serviceId": "integer (required)",
      "quantity": "integer (default: 1)",
      "selectedChoiceIds": ["integer"],
      "expectedPrice": "number (optional)"
    }
  ],
  "paymentMethodId": "integer",
  "additionalFeeIds": ["string"]
}
```

### Field Descriptions

| Field | Type | Description |
|-------|------|-------------|
| `customerId` | String | **(Admin only)** Customer ID to preview for. Customers cannot set this. |
| `addressId` | String | Existing address ID. Either this or `newAddress` must be provided. |
| `newAddress` | Object | New address details if not using existing address. |
| `bookingTime` | DateTime | Optional for preview (allows price checking without specific time). |
| `note` | String | Customer notes for the booking. |
| `promoCode` | String | Promotion code to apply for discount. |
| `bookingDetails` | Array | List of services to include in the booking. |
| `paymentMethodId` | Integer | Payment method ID (1 = Cash, 2 = VNPay, etc.). |
| `additionalFeeIds` | Array | Optional additional fee IDs (e.g., "fee-peak-10", "fee-transport-50k"). |

---

## Response Body

### Schema: `BookingPreviewResponse`

```json
{
  "valid": true,
  "errors": [],
  "customerId": "c1000001-0000-0000-0000-000000000001",
  "customerName": "John Doe",
  "customerPhone": "0901234567",
  "customerEmail": "john.doe@example.com",
  "addressInfo": {
    "addressId": "adrs0001-0000-0000-0000-000000000001",
    "fullAddress": "123 Nguyễn Huệ, Quận 1",
    "ward": "Bến Nghé",
    "city": "TP. Hồ Chí Minh",
    "latitude": 10.7731,
    "longitude": 106.7037,
    "isDefault": true
  },
  "usingNewAddress": false,
  "bookingTime": "2025-12-15T09:00:00",
  "serviceItems": [
    {
      "serviceId": 2,
      "serviceName": "Tổng vệ sinh",
      "serviceDescription": "Làm sạch sâu toàn diện...",
      "iconUrl": "https://res.cloudinary.com/...",
      "quantity": 1,
      "unit": "Gói",
      "unitPrice": 100000,
      "formattedUnitPrice": "100.000 ₫",
      "subTotal": 100000,
      "formattedSubTotal": "100.000 ₫",
      "selectedChoices": [
        {
          "choiceId": 1,
          "choiceName": "Căn hộ",
          "optionName": "Loại hình nhà ở?",
          "price": 50000,
          "formattedPrice": "50.000 ₫"
        }
      ],
      "estimatedDuration": "2 giờ",
      "recommendedStaff": 1
    }
  ],
  "totalServices": 1,
  "totalQuantity": 1,
  "subtotal": 100000,
  "formattedSubtotal": "100.000 ₫",
  "promotionInfo": {
    "promotionId": 1,
    "promoCode": "GIAM20K",
    "description": "Giảm giá 20,000đ cho mọi đơn hàng",
    "discountType": "FIXED_AMOUNT",
    "discountValue": 20000,
    "maxDiscountAmount": null
  },
  "discountAmount": 20000,
  "formattedDiscountAmount": "20.000 ₫",
  "totalAfterDiscount": 80000,
  "formattedTotalAfterDiscount": "80.000 ₫",
  "feeBreakdowns": [
    {
      "name": "Phí hệ thống",
      "type": "PERCENT",
      "value": 0.20,
      "amount": 16000,
      "formattedAmount": "16.000 ₫",
      "systemSurcharge": true
    }
  ],
  "totalFees": 16000,
  "formattedTotalFees": "16.000 ₫",
  "grandTotal": 96000,
  "formattedGrandTotal": "96.000 ₫",
  "estimatedDuration": "2 giờ",
  "recommendedStaff": 1,
  "note": "Nhà có trẻ nhỏ, vui lòng lau dọn kỹ.",
  "paymentMethodId": 1,
  "paymentMethodName": "Tiền mặt"
}
```

### Response Fields

| Field | Type | Description |
|-------|------|-------------|
| `valid` | Boolean | Whether the preview request is valid. |
| `errors` | Array | List of validation errors (if any). |
| `customerId` | String | Customer ID for the booking. |
| `customerName` | String | Customer's full name. |
| `addressInfo` | Object | Address details for the booking. |
| `serviceItems` | Array | Itemized list of services with pricing. |
| `subtotal` | Number | Sum of all service subtotals (before discount). |
| `promotionInfo` | Object | Applied promotion details (null if none). |
| `discountAmount` | Number | Total discount amount. |
| `totalAfterDiscount` | Number | Subtotal minus discount. |
| `feeBreakdowns` | Array | Itemized fees (system surcharge, etc.). |
| `totalFees` | Number | Sum of all fees. |
| `grandTotal` | Number | Final amount (totalAfterDiscount + totalFees). |

### ChoicePreviewItem Fields (inside `selectedChoices`)

| Field | Type | Description |
|-------|------|-------------|
| `choiceId` | Integer | The choice option ID. |
| `choiceName` | String | The display name of the choice (e.g., "Căn hộ", "Giặt chăn ga"). |
| `optionName` | String | The parent option question (e.g., "Loại hình nhà ở?"). |
| `price` | Number | Price adjustment for this choice (from PricingRule). |
| `formattedPrice` | String | Formatted price with currency (e.g., "50.000 ₫"). |

---

## Sample Requests

### 1. Customer Preview (Using Existing Address)

```json
{
  "addressId": "adrs0001-0000-0000-0000-000000000001",
  "bookingTime": "2025-12-15T09:00:00",
  "promoCode": "GIAM20K",
  "bookingDetails": [
    {
      "serviceId": 2,
      "quantity": 1,
      "selectedChoiceIds": [1]
    }
  ],
  "paymentMethodId": 1,
  "additionalFeeIds": []
}
```

### 2. Admin Preview on Behalf of Customer

```json
{
  "customerId": "c1000001-0000-0000-0000-000000000001",
  "addressId": "adrs0001-0000-0000-0000-000000000001",
  "bookingTime": "2025-12-15T14:00:00",
  "promoCode": "KHAITRUONG10",
  "bookingDetails": [
    {
      "serviceId": 1,
      "quantity": 2,
      "selectedChoiceIds": [7, 8]
    },
    {
      "serviceId": 4,
      "quantity": 2,
      "selectedChoiceIds": [13]
    }
  ],
  "paymentMethodId": 2,
  "additionalFeeIds": ["fee-peak-10"]
}
```

### 3. Preview with New Address

```json
{
  "newAddress": {
    "customerId": "c1000001-0000-0000-0000-000000000001",
    "fullAddress": "456 Lê Lợi, Quận 1",
    "ward": "Bến Thành",
    "city": "TP. Hồ Chí Minh",
    "latitude": 10.7721,
    "longitude": 106.6985
  },
  "bookingDetails": [
    {
      "serviceId": 3,
      "quantity": 1,
      "selectedChoiceIds": [10]
    }
  ],
  "paymentMethodId": 1,
  "additionalFeeIds": []
}
```

### 4. Preview Without Booking Time (Price Check Only)

```json
{
  "addressId": "adrs0001-0000-0000-0000-000000000001",
  "bookingDetails": [
    {
      "serviceId": 7,
      "quantity": 4
    }
  ],
  "paymentMethodId": 1,
  "additionalFeeIds": []
}
```

---

## Sample Responses

### Success Response (HTTP 200)

```json
{
  "valid": true,
  "errors": [],
  "customerId": "c1000001-0000-0000-0000-000000000001",
  "customerName": "John Doe",
  "customerPhone": "0901234567",
  "customerEmail": "john.doe@example.com",
  "addressInfo": {
    "addressId": "adrs0001-0000-0000-0000-000000000001",
    "fullAddress": "123 Nguyễn Huệ, Phường Bến Nghé, Quận 1, TP.HCM",
    "ward": "Bến Nghé",
    "city": "TP. Hồ Chí Minh",
    "latitude": 10.7731,
    "longitude": 106.7037,
    "isDefault": true
  },
  "usingNewAddress": false,
  "bookingTime": "2025-12-15T09:00:00",
  "serviceItems": [
    {
      "serviceId": 1,
      "serviceName": "Dọn dẹp theo giờ",
      "serviceDescription": "Lau dọn, hút bụi, làm sạch các bề mặt cơ bản trong nhà.",
      "iconUrl": "https://res.cloudinary.com/dkzemgit8/image/upload/v1764171235/Cleaning_Clock-removebg-preview_o0oevs.png",
      "quantity": 2,
      "unit": "Giờ",
      "unitPrice": 50000,
      "formattedUnitPrice": "50.000 ₫",
      "subTotal": 100000,
      "formattedSubTotal": "100.000 ₫",
      "selectedChoices": [
        {
          "choiceId": 7,
          "choiceName": "Giặt chăn ga",
          "optionName": "Bạn có yêu cầu thêm công việc nào?",
          "price": 30000,
          "formattedPrice": "30.000 ₫"
        },
        {
          "choiceId": 8,
          "choiceName": "Rửa chén",
          "optionName": "Bạn có yêu cầu thêm công việc nào?",
          "price": 20000,
          "formattedPrice": "20.000 ₫"
        }
      ],
      "estimatedDuration": "2 giờ",
      "recommendedStaff": 1
    },
    {
      "serviceId": 4,
      "serviceName": "Vệ sinh máy lạnh",
      "serviceDescription": "Bảo trì, làm sạch dàn nóng và dàn lạnh, bơm gas nếu cần.",
      "iconUrl": "https://res.cloudinary.com/dkzemgit8/image/upload/v1764171599/cooler-removebg-preview_trw5g2.png",
      "quantity": 2,
      "unit": "Máy",
      "unitPrice": 150000,
      "formattedUnitPrice": "150.000 ₫",
      "subTotal": 300000,
      "formattedSubTotal": "300.000 ₫",
      "selectedChoices": [
        {
          "choiceId": 13,
          "choiceName": "Treo tường",
          "optionName": "Loại máy lạnh?",
          "price": 0,
          "formattedPrice": "0 ₫"
        }
      ],
      "estimatedDuration": "1 giờ",
      "recommendedStaff": 1
    }
  ],
  "totalServices": 2,
  "totalQuantity": 4,
  "subtotal": 400000,
  "formattedSubtotal": "400.000 ₫",
  "promotionInfo": {
    "promotionId": 2,
    "promoCode": "KHAITRUONG10",
    "description": "Giảm 10% mừng khai trương",
    "discountType": "PERCENTAGE",
    "discountValue": 10,
    "maxDiscountAmount": 50000
  },
  "discountAmount": 40000,
  "formattedDiscountAmount": "40.000 ₫",
  "totalAfterDiscount": 360000,
  "formattedTotalAfterDiscount": "360.000 ₫",
  "feeBreakdowns": [
    {
      "name": "Phí hệ thống",
      "type": "PERCENT",
      "value": 0.20,
      "amount": 72000,
      "formattedAmount": "72.000 ₫",
      "systemSurcharge": true
    },
    {
      "name": "Phụ phí cao điểm",
      "type": "PERCENT",
      "value": 0.10,
      "amount": 36000,
      "formattedAmount": "36.000 ₫",
      "systemSurcharge": false
    }
  ],
  "totalFees": 108000,
  "formattedTotalFees": "108.000 ₫",
  "grandTotal": 468000,
  "formattedGrandTotal": "468.000 ₫",
  "estimatedDuration": "3 giờ",
  "recommendedStaff": 1,
  "note": null,
  "paymentMethodId": 2,
  "paymentMethodName": "VNPay"
}
```

### Validation Error Response (HTTP 200)

```json
{
  "valid": false,
  "errors": [
    "Service not found or not bookable: 999",
    "Promotion code has already been used by this customer"
  ],
  "customerId": null,
  "customerName": null,
  "customerPhone": null,
  "customerEmail": null,
  "addressInfo": null,
  "usingNewAddress": false,
  "bookingTime": null,
  "serviceItems": null,
  "totalServices": 0,
  "totalQuantity": 0,
  "subtotal": null,
  "formattedSubtotal": null,
  "promotionInfo": null,
  "discountAmount": null,
  "formattedDiscountAmount": null,
  "totalAfterDiscount": null,
  "formattedTotalAfterDiscount": null,
  "feeBreakdowns": null,
  "totalFees": null,
  "formattedTotalFees": null,
  "grandTotal": null,
  "formattedGrandTotal": null,
  "estimatedDuration": null,
  "recommendedStaff": 0,
  "note": null,
  "paymentMethodId": null,
  "paymentMethodName": null
}
```

---

## Sample Data Reference

### Services (from seed data)

| service_id | name | base_price | unit |
|------------|------|------------|------|
| 1 | Dọn dẹp theo giờ | 50,000 | Giờ |
| 2 | Tổng vệ sinh | 100,000 | Gói |
| 3 | Vệ sinh Sofa - Nệm - Rèm | 300,000 | Gói |
| 4 | Vệ sinh máy lạnh | 150,000 | Máy |
| 5 | Giặt sấy theo kg | 30,000 | Kg |
| 6 | Giặt hấp cao cấp | 120,000 | Bộ |
| 7 | Nấu ăn gia đình | 60,000 | Giờ |
| 8 | Đi chợ hộ | 40,000 | Lần |

### Promotions (from seed data)

| promo_code | description | discount_type | discount_value | max_discount |
|------------|-------------|---------------|----------------|--------------|
| GIAM20K | Giảm giá 20,000đ cho mọi đơn hàng | FIXED_AMOUNT | 20,000 | - |
| KHAITRUONG10 | Giảm 10% mừng khai trương | PERCENTAGE | 10% | 50,000 |

### Additional Fees (from seed data)

| fee_id | name | type | value |
|--------|------|------|-------|
| fee-system-20 | Phí hệ thống | PERCENT | 20% (auto-applied) |
| fee-peak-10 | Phụ phí cao điểm | PERCENT | 10% |
| fee-transport-50k | Phí di chuyển | FLAT | 50,000 |

### Customer Accounts (for testing)

| username | customerId | role |
|----------|------------|------|
| john_doe | c1000001-0000-0000-0000-000000000001 | CUSTOMER |
| nguyenvana | c1000001-0000-0000-0000-000000000004 | CUSTOMER |
| admin_1 | - | ADMIN |

### Sample Address IDs

| addressId | customer | fullAddress |
|-----------|----------|-------------|
| adrs0001-0000-0000-0000-000000000001 | John Doe | 123 Nguyễn Huệ, Q.1, TP.HCM |
| adrs0001-0000-0000-0000-000000000003 | Jane Smith | 456 Lê Lợi, Q.3, TP.HCM |
| adrs0001-0000-0000-0000-000000000009 | Nguyễn Văn A | 789 Pasteur, Q.1, TP.HCM |

---

## Caching Behavior

### Promotion Validation Cache
- **Cache Name:** `promotionValidation`
- **TTL:** 5 minutes
- **Key Pattern:** `{promoCode}:{amount}:{customerId}`
- **Eviction:** All entries evicted when a booking with promo code is created

### Notes
- Cache improves performance for repeated preview requests with the same promotion
- Cache is automatically cleared when the promotion is actually used in a booking
- Invalid/error results are NOT cached (only successful validations)

---

## Error Scenarios

| Scenario | Error Message |
|----------|---------------|
| Invalid service ID | "Service not found or not bookable: {serviceId}" |
| Invalid promotion | "Promotion code is invalid or expired: {code}" |
| Promotion already used | "Promotion code has already been used by this customer" |
| Invalid address | "Either addressId or newAddress must be provided" |
| Customer not found | "Customer not found: {customerId}" |
| Address not found | "Address not found: {addressId}" |

---

## Usage Flow (Frontend)

1. **User selects services** → Call preview API to get initial pricing
2. **User enters promo code** → Call preview API again to see discount
3. **User reviews invoice** → Display the `BookingPreviewResponse` as an invoice
4. **User confirms** → Call actual booking creation endpoint

---