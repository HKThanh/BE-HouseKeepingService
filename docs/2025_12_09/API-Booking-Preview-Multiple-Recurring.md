# API: Booking Preview — Multiple Time Slots & Recurring Bookings

## Overview

This document describes two API endpoints that return pricing previews (invoice-style) for multiple bookings and for recurring (periodic) bookings. The preview endpoints perform pricing validation only: they calculate service prices, apply promotions, calculate fees, and return itemized invoices — they do not check employee availability or create bookings.

Endpoints covered:
- `POST /api/v1/customer/bookings/preview/multiple` — Preview multiple bookings with different time slots (same services and other parameters applied to all time slots).
- `POST /api/v1/customer/bookings/preview/recurring` — Preview a recurring booking pattern and show planned occurrences and pricing.

Authentication: JWT Bearer token required. Allowed roles: `ROLE_CUSTOMER`, `ROLE_ADMIN`.

All responses return HTTP 200 and include a `valid` boolean and `errors` array for validation feedback. Validation warnings or non-critical issues are included in the response body rather than via non-200 status codes.

---

## Common Headers

- `Authorization`: `Bearer <jwt-token>` (required)
- `Content-Type`: `application/json`


---

## 1) Preview Multiple Bookings

Endpoint
```
POST /api/v1/customer/bookings/preview/multiple
```

Purpose
- Provide an itemized pricing preview for multiple independent bookings that share the same booking details (services, promo code, address, additional fees), but occur at different `bookingTime` values.
- Promotion codes and additional fees in the request are applied to every time slot.
- The preview is for pricing validation only and will not reserve employee availability.

Roles
- `ROLE_CUSTOMER` and `ROLE_ADMIN`
- Admin may set `customerId` in the request to preview on behalf of a customer.

Request JSON schema (fields explained)
```json
{
  "customerId": "string (optional; admin only)",
  "addressId": "string (either addressId or newAddress required)",
  "newAddress": { /* optional if addressId present */ },
  "bookingTimes": ["ISO-8601 LocalDateTime string (required)"],
  "note": "string (optional; max 1000 chars)",
  "title": "string (optional; max 255 chars)",
  "promoCode": "string (optional; max 20 chars)",
  "bookingDetails": [ /* list of services */ ],
  "paymentMethodId": "integer (optional)",
  "additionalFeeIds": ["string"]
}
```

- `customerId` (string): optional; only honored when the caller is an admin. Customers should not provide another customer's id.
- `addressId` (string): existing address id linked to the customer. Either `addressId` or `newAddress` must be present.
- `newAddress` (object): structure identical to other address DTOs. If present, the preview will use the address data without saving it.
- `bookingTimes` (array of datetime strings): required. Example item: `"2025-12-15T09:00:00"`.
- `bookingDetails` (array): same format as `BookingPreviewRequest.bookingDetails`. Each item must include `serviceId`, `quantity` (default 1 if omitted), optional `selectedChoiceIds` and optional `expectedPrice`/`expectedPricePerUnit` when applicable.
- `promoCode`: promotion code applied to each booking preview. The promotion validation is performed once per booking and applied consistently across all returned previews.
- `additionalFeeIds`: list of additional fee identifiers to include in pricing (applied to each preview).

Sample Request
```json
{
  "addressId": "adrs0001-0000-0000-0000-000000000001",
  "bookingTimes": [
    "2025-12-15T09:00:00",
    "2025-12-16T14:00:00"
  ],
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

Response schema (MultipleBookingPreviewResponse)
```json
{
  "valid": true,
  "errors": [],
  "bookingCount": 2,
  
  "serviceItems": [
    {
      "serviceId": 2,
      "serviceName": "Tổng vệ sinh",
      "serviceDescription": "Làm sạch sâu toàn diện căn nhà, bao gồm lau chùi, hút bụi, vệ sinh các góc khuất.",
      "iconUrl": "https://res.cloudinary.com/dkzemgit8/image/upload/v1764171235/cleaning_deep.png",
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
  "totalQuantityPerBooking": 1,
  "subtotalPerBooking": 150000,
  "formattedSubtotalPerBooking": "150.000 ₫",
  
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
  
  "paymentMethodId": 1,
  "paymentMethodName": "Tiền mặt",
  
  "feeBreakdowns": [
    {
      "name": "Phí hệ thống",
      "type": "PERCENT",
      "value": 0.20,
      "amount": 26000,
      "formattedAmount": "26.000 ₫",
      "systemSurcharge": true
    }
  ],
  "totalFeesPerBooking": 26000,
  "formattedTotalFeesPerBooking": "26.000 ₫",
  
  "promotionInfo": {
    "promotionId": 1,
    "promoCode": "GIAM20K",
    "description": "Giảm giá 20,000đ cho mọi đơn hàng",
    "discountType": "FIXED_AMOUNT",
    "discountValue": 20000,
    "maxDiscountAmount": null
  },
  "discountPerBooking": 20000,
  "formattedDiscountPerBooking": "20.000 ₫",
  
  "pricePerBooking": 156000,
  "formattedPricePerBooking": "156.000 ₫",
  "estimatedDurationPerBooking": "2 giờ",
  "recommendedStaff": 1,
  
  "bookingPreviews": [
    {
      "valid": true,
      "errors": [],
      "bookingTime": "2025-12-15T09:00:00",
      "grandTotal": 156000,
      "formattedGrandTotal": "156.000 ₫"
    },
    {
      "valid": true,
      "errors": [],
      "bookingTime": "2025-12-16T14:00:00",
      "grandTotal": 156000,
      "formattedGrandTotal": "156.000 ₫"
    }
  ],
  
  "totalEstimatedPrice": 312000,
  "formattedTotalEstimatedPrice": "312.000 ₫",
  "totalEstimatedDuration": "4 giờ",
  
  "promoCode": "GIAM20K",
  "promoAppliedToAll": true,
  "validBookingsCount": 2,
  "invalidBookingsCount": 0,
  "validBookingTimes": ["2025-12-15T09:00:00", "2025-12-16T14:00:00"],
  "invalidBookingTimes": []
}
```

### Response Fields Explanation

#### Shared Service Info (dùng chung cho tất cả booking)
| Field | Type | Description |
|-------|------|-------------|
| `serviceItems` | Array | Danh sách dịch vụ với chi tiết giá. Dùng chung cho tất cả các booking time. |
| `totalServices` | Integer | Tổng số dịch vụ được chọn |
| `totalQuantityPerBooking` | Integer | Tổng số lượng dịch vụ cho mỗi booking |
| `subtotalPerBooking` | BigDecimal | Tổng tiền dịch vụ trước giảm giá và phí (cho mỗi booking) |

#### ServicePreviewItem (trong `serviceItems`)
| Field | Type | Description |
|-------|------|-------------|
| `serviceId` | Integer | ID của dịch vụ |
| `serviceName` | String | Tên dịch vụ (VD: "Tổng vệ sinh") |
| `serviceDescription` | String | Mô tả dịch vụ |
| `iconUrl` | String | URL icon dịch vụ |
| `quantity` | Integer | Số lượng |
| `unit` | String | Đơn vị (VD: "Giờ", "Gói", "Máy") |
| `unitPrice` | BigDecimal | Đơn giá |
| `formattedUnitPrice` | String | Đơn giá đã format (VD: "100.000 ₫") |
| `subTotal` | BigDecimal | Thành tiền = unitPrice × quantity |
| `selectedChoices` | Array | Danh sách option đã chọn với giá |
| `estimatedDuration` | String | Thời gian ước tính |
| `recommendedStaff` | Integer | Số nhân viên đề xuất |

#### ChoicePreviewItem (trong `selectedChoices`)
| Field | Type | Description |
|-------|------|-------------|
| `choiceId` | Integer | ID của lựa chọn |
| `choiceName` | String | Tên lựa chọn (VD: "Căn hộ", "Giặt chăn ga") |
| `optionName` | String | Tên câu hỏi/option (VD: "Loại hình nhà ở?") |
| `price` | BigDecimal | Giá điều chỉnh cho lựa chọn này |
| `formattedPrice` | String | Giá đã format |

#### Customer & Address Info (dùng chung)
| Field | Type | Description |
|-------|------|-------------|
| `customerId` | String | ID khách hàng |
| `customerName` | String | Tên khách hàng |
| `customerPhone` | String | Số điện thoại |
| `customerEmail` | String | Email |
| `addressInfo` | Object | Thông tin địa chỉ |
| `usingNewAddress` | Boolean | Có đang dùng địa chỉ mới không |

#### Fees & Promotion (dùng chung, áp dụng cho mỗi booking)
| Field | Type | Description |
|-------|------|-------------|
| `feeBreakdowns` | Array | Chi tiết các loại phí |
| `totalFeesPerBooking` | BigDecimal | Tổng phí cho mỗi booking |
| `promotionInfo` | Object | Thông tin khuyến mãi |
| `discountPerBooking` | BigDecimal | Số tiền giảm cho mỗi booking |
| `pricePerBooking` | BigDecimal | Giá cuối cùng cho mỗi booking (sau giảm giá + phí) |

#### Aggregated Totals
| Field | Type | Description |
|-------|------|-------------|
| `totalEstimatedPrice` | BigDecimal | Tổng tiền tất cả booking = pricePerBooking × bookingCount |
| `totalEstimatedDuration` | String | Tổng thời gian ước tính |
| `validBookingsCount` | Integer | Số booking hợp lệ |
| `invalidBookingsCount` | Integer | Số booking không hợp lệ |

- `bookingPreviews`: an array containing one `BookingPreviewResponse` per requested `bookingTime`. Each element is the same object structure used by the single booking preview: itemized services, promotion info, fees, and `grandTotal`. See the `BookingPreviewResponse` structure described in the project's API or the single-preview documentation (the frontend can reuse the same UI representation for each item).
- `totalEstimatedPrice`: the sum of `grandTotal` from all valid individual previews.
- `totalEstimatedDuration`: textual total of durations across valid previews (e.g. "4 giờ").

Validation and behavior notes
- If an individual time slot has a *critical error* (e.g., missing customer or invalid address), that item will return a `BookingPreviewResponse` with `valid=false` and `errors`; the endpoint will still return other preview items.
- `valid` in the top-level MultipleBookingPreviewResponse is `true` only if at least one booking preview is valid and there are no invalid bookings; otherwise it will be `false` and the `errors` field will include combined messages.
- The promo code is validated independently for each preview (same result expected if promo conditions and amount are identical).

---

## 2) Preview Recurring Booking

Endpoint
```
POST /api/v1/customer/bookings/preview/recurring
```

Purpose
- Provide a pricing preview for a recurring booking pattern. The endpoint calculates planned occurrences within the requested window, returns a detailed preview for one occurrence, and computes per-occurrence and total pricing across all planned occurrences.
- This endpoint is for pricing validation only. It does not create a `RecurringBooking` record or reserve employees.

Roles
- `ROLE_CUSTOMER` and `ROLE_ADMIN`.

Request JSON schema (fields explained)
```json
{
  "customerId": "string (optional; admin only)",
  "addressId": "string (either addressId or newAddress required)",
  "newAddress": { /* optional */ },
  "recurrenceType": "WEEKLY or MONTHLY",
  "recurrenceDays": [ /* integers */ ],
  "bookingTime": "LocalTime string (HH:mm:ss required)",
  "startDate": "ISO Date (required)",
  "endDate": "ISO Date (optional)",
  "maxPreviewOccurrences": "integer (optional, default 30, maximum 30)",
  "note": "string (optional)",
  "title": "string (optional)",
  "promoCode": "string (optional)",
  "bookingDetails": [ /* list of RecurringBookingDetailRequest items */ ],
  "paymentMethodId": "integer (optional)",
  "additionalFeeIds": ["string"]
}
```

- `recurrenceType`: `WEEKLY` or `MONTHLY`.
- `recurrenceDays`:
  - If `WEEKLY`: values are 1..7, where 1 = Monday, 7 = Sunday.
  - If `MONTHLY`: values are 1..31 (days of month).
- `bookingTime`: time-of-day for each occurrence (LocalTime). Planned occurrences combine the date determined by recurrence with this time.
- `startDate`, `endDate`: date window used to generate planned occurrences. If `endDate` is omitted, occurrences will be generated up to one year forward but capped by `maxPreviewOccurrences`.
- `maxPreviewOccurrences`: limits the number of occurrences returned; default is 30 and the endpoint will indicate if there are more occurrences beyond the limit.

Sample Request
```json
{
  "addressId": "adrs0001-0000-0000-0000-000000000001",
  "recurrenceType": "WEEKLY",
  "recurrenceDays": [1,3,5],
  "bookingTime": "09:00:00",
  "startDate": "2025-12-01",
  "endDate": "2026-01-31",
  "maxPreviewOccurrences": 10,
  "promoCode": "KHAITRUONG10",
  "bookingDetails": [
    { "serviceId": 2, "quantity": 1, "selectedChoiceIds": [1] }
  ],
  "paymentMethodId": 1
}
```

Response schema (RecurringBookingPreviewResponse)
```json
{
  "valid": true,
  "errors": [],
  
  "serviceItems": [
    {
      "serviceId": 2,
      "serviceName": "Tổng vệ sinh",
      "serviceDescription": "Làm sạch sâu toàn diện căn nhà, bao gồm lau chùi, hút bụi, vệ sinh các góc khuất.",
      "iconUrl": "https://res.cloudinary.com/dkzemgit8/image/upload/v1764171235/cleaning_deep.png",
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
  "totalQuantityPerOccurrence": 1,
  "subtotalPerOccurrence": 150000,
  "formattedSubtotalPerOccurrence": "150.000 ₫",
  
  "feeBreakdowns": [
    {
      "name": "Phí hệ thống",
      "type": "PERCENT",
      "value": 0.20,
      "amount": 26000,
      "formattedAmount": "26.000 ₫",
      "systemSurcharge": true
    }
  ],
  "totalFeesPerOccurrence": 26000,
  "formattedTotalFeesPerOccurrence": "26.000 ₫",
  
  "discountPerOccurrence": 15000,
  "formattedDiscountPerOccurrence": "15.000 ₫",
  
  "recurrenceType": "WEEKLY",
  "recurrenceDays": [1, 3, 5],
  "recurrenceDescription": "Hàng tuần vào Thứ Hai, Thứ Tư, Thứ Sáu lúc 09:00",
  "bookingTime": "09:00",
  "startDate": "2025-12-01",
  "endDate": "2026-01-31",
  
  "plannedBookingTimes": [
    "2025-12-09T09:00:00",
    "2025-12-11T09:00:00",
    "2025-12-13T09:00:00",
    "2025-12-16T09:00:00",
    "2025-12-18T09:00:00",
    "2025-12-20T09:00:00",
    "2025-12-23T09:00:00",
    "2025-12-25T09:00:00",
    "2025-12-27T09:00:00",
    "2025-12-30T09:00:00"
  ],
  "occurrenceCount": 10,
  "maxPreviewOccurrences": 10,
  "hasMoreOccurrences": true,
  
  "singleBookingPreview": {
    "valid": true,
    "errors": [],
    "customerId": "c1000001-0000-0000-0000-000000000001",
    "customerName": "John Doe",
    "bookingTime": "2025-12-09T09:00:00",
    "grandTotal": 161000,
    "formattedGrandTotal": "161.000 ₫"
  },
  
  "pricePerOccurrence": 161000,
  "formattedPricePerOccurrence": "161.000 ₫",
  "totalEstimatedPrice": 1610000,
  "formattedTotalEstimatedPrice": "1.610.000 ₫",
  
  "estimatedDurationPerOccurrence": "2 giờ",
  "recommendedStaff": 1,
  
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
  
  "paymentMethodId": 1,
  "paymentMethodName": "Tiền mặt",
  
  "promoCode": "KHAITRUONG10",
  "promoAppliedToAll": true,
  "promotionInfo": {
    "promotionId": 2,
    "promoCode": "KHAITRUONG10",
    "description": "Giảm 10% mừng khai trương",
    "discountType": "PERCENTAGE",
    "discountValue": 10,
    "maxDiscountAmount": 50000
  }
}
```

### Response Fields Explanation

#### Shared Service Info (dùng chung cho tất cả occurrence)
| Field | Type | Description |
|-------|------|-------------|
| `serviceItems` | Array | Danh sách dịch vụ với chi tiết giá. Dùng chung cho tất cả các lần đặt định kỳ. |
| `totalServices` | Integer | Tổng số dịch vụ được chọn |
| `totalQuantityPerOccurrence` | Integer | Tổng số lượng dịch vụ cho mỗi lần đặt |
| `subtotalPerOccurrence` | BigDecimal | Tổng tiền dịch vụ trước giảm giá và phí (cho mỗi occurrence) |

#### Fees & Discount (dùng chung, áp dụng cho mỗi occurrence)
| Field | Type | Description |
|-------|------|-------------|
| `feeBreakdowns` | Array | Chi tiết các loại phí |
| `totalFeesPerOccurrence` | BigDecimal | Tổng phí cho mỗi lần đặt |
| `discountPerOccurrence` | BigDecimal | Số tiền giảm cho mỗi lần đặt |

#### Recurrence Info
| Field | Type | Description |
|-------|------|-------------|
| `recurrenceType` | Enum | `WEEKLY` (hàng tuần) hoặc `MONTHLY` (hàng tháng) |
| `recurrenceDays` | Array<Integer> | Các ngày trong tuần (1-7) hoặc tháng (1-31) |
| `recurrenceDescription` | String | Mô tả lịch định kỳ bằng tiếng Việt |
| `bookingTime` | LocalTime | Giờ đặt cho mỗi occurrence |
| `startDate` | LocalDate | Ngày bắt đầu |
| `endDate` | LocalDate | Ngày kết thúc (nullable) |

#### Planned Occurrences
| Field | Type | Description |
|-------|------|-------------|
| `plannedBookingTimes` | Array<LocalDateTime> | Danh sách các thời điểm đặt dự kiến |
| `occurrenceCount` | Integer | Số lần đặt trong preview |
| `maxPreviewOccurrences` | Integer | Giới hạn số lần preview (mặc định 30) |
| `hasMoreOccurrences` | Boolean | `true` nếu còn nhiều occurrence hơn maxPreviewOccurrences |

#### Pricing
| Field | Type | Description |
|-------|------|-------------|
| `pricePerOccurrence` | BigDecimal | Giá cho mỗi lần đặt (sau giảm giá + phí) |
| `totalEstimatedPrice` | BigDecimal | Tổng tiền = pricePerOccurrence × occurrenceCount |

#### Customer & Address
| Field | Type | Description |
|-------|------|-------------|
| `customerId` | String | ID khách hàng |
| `customerName` | String | Tên khách hàng |
| `customerPhone` | String | Số điện thoại |
| `customerEmail` | String | Email |
| `addressInfo` | Object | Thông tin địa chỉ |
| `usingNewAddress` | Boolean | Có đang dùng địa chỉ mới không |
| `paymentMethodId` | Integer | ID phương thức thanh toán |
| `paymentMethodName` | String | Tên phương thức thanh toán |

Explanation of response fields
- `plannedBookingTimes`: list of LocalDateTime values representing the scheduled occurrences that fall in the requested window and pass the "future times" filter. Values are sorted ascending.
- `singleBookingPreview`: detailed preview for one occurrence (the first one returned). Contains the same fields as `BookingPreviewResponse` described in the single-preview documentation.
- `pricePerOccurrence`: price for a single occurrence (the `grandTotal` value of `singleBookingPreview`), including promotion and fees.
- `totalEstimatedPrice`: `pricePerOccurrence * occurrenceCount` (simple multiplication because the services, fees, and promo code are the same for each occurrence in the preview request).
- `hasMoreOccurrences`: if `true`, there are more planned occurrences beyond `maxPreviewOccurrences`.

Validation and behavior notes
- The endpoint generates planned occurrence dates using `recurrenceType`, `recurrenceDays`, `bookingTime`, `startDate`, and `endDate`. It only includes future datetimes (occurrences strictly after the current time).
- If no planned occurrences fall within the requested window, the response will have `valid=false` and an appropriate `errors` message.
- Promo code is applied uniformly to all occurrences when calculating totals for the preview. Promotion validation is the same as for single previews.

---

## Sample Data Reference (for example values)
Use data seeded in the project's SQL files as sample values when building UI fixtures. Example values include service IDs and names, promo codes, and additional fee identifiers. Representative samples are:

Services (sample):
- `serviceId`: 1 — Dọn dẹp theo giờ — base price 50,000 (unit: Giờ)
- `serviceId`: 2 — Tổng vệ sinh — base price 100,000 (unit: Gói)
- `serviceId`: 4 — Vệ sinh máy lạnh — base price 150,000 (unit: Máy)

Promotions (sample):
- `GIAM20K` — fixed 20,000 discount
- `KHAITRUONG10` — 10% discount up to 50,000

Additional fees (sample ids):
- `fee-system-20` — system percentage fee 20% (typically auto-applied)
- `fee-peak-10` — peak hour extra 10%
- `fee-transport-50k` — flat transport fee 50,000

Customer sample accounts (for testing authentication):
- username: `john_doe`, customerId: `c1000001-0000-0000-0000-000000000001`
- username: `admin_1` (admin account)

Address sample ids:
- `adrs0001-0000-0000-0000-000000000001` — John Doe's primary address

---

## Important Notes for Frontend Implementation
- The frontend should use the same UI component that renders a single `BookingPreviewResponse` to display each element in `bookingPreviews` (for the multiple preview endpoint).
- Show per-occurrence pricing and total aggregated price for recurring previews. Indicate clearly when `hasMoreOccurrences` is `true` and that totals are calculated only for the occurrences returned (or for `occurrenceCount` if you show the computed total for the full requested window).
- Promotion application: since promo codes apply to every occurrence in our preview behavior, show the promo code in the UI and the discounted price per occurrence.
- Error handling: even on HTTP 200, the response can contain `valid=false` and `errors`. The frontend must display these errors to users and still show valid previews if present.

---