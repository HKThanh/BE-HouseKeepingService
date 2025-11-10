# VNPay Payment Integration - Booking Flow

## Tài khoản test VNPay Sandbox

**Ngân hàng NCB**
- Số thẻ: `9704198526191432198`
- Tên chủ thẻ: `NGUYEN VAN A`
- Ngày phát hành: `07/15`
- Mật khẩu OTP: `123456`

---

## Flow thanh toán từ Booking đến Payment

### Bước 1: Tạo Booking

**HTTP Request**:
```http
POST /api/v1/customer/bookings HTTP/1.1
Host: localhost:8080
Content-Type: application/json
Authorization: Bearer {jwt_token}

{
  "serviceId": "service-uuid",
  "scheduleDate": "2025-11-15",
  "scheduleTime": "09:00",
  "addressId": "address-uuid",
  "notes": "Dọn dẹp toàn bộ nhà",
  "images": []
}
```

**HTTP Response**:
```http
HTTP/1.1 201 Created
Content-Type: application/json

{
  "success": true,
  "message": "Đặt lịch thành công",
  "data": {
    "bookingId": "booking-123-uuid",
    "customerId": "customer-uuid",
    "serviceId": "service-uuid",
    "serviceName": "Dọn dẹp nhà cửa",
    "scheduleDate": "2025-11-15",
    "scheduleTime": "09:00",
    "status": "PENDING",
    "totalPrice": 500000,
    "paymentStatus": "UNPAID",
    "createdAt": "2025-11-10T14:30:00"
  }
}
```

---

### Bước 2: Tạo Payment URL với VNPay

**HTTP Request**:
```http
POST /api/v1/payment/vnpay/create HTTP/1.1
Host: localhost:8080
Content-Type: application/json
Authorization: Bearer {jwt_token}

{
  "bookingId": "booking-123-uuid",
  "amount": 500000,
  "orderInfo": "Thanh toan don hang booking-123-uuid",
  "locale": "vn"
}
```

**HTTP Response**:
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "success": true,
  "message": "Tạo URL thanh toán thành công",
  "data": {
    "paymentUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=50000000&vnp_Command=pay&vnp_CreateDate=20251110143500&vnp_CurrCode=VND&vnp_IpAddr=127.0.0.1&vnp_Locale=vn&vnp_OrderInfo=Thanh+toan+don+hang+booking-123-uuid&vnp_OrderType=other&vnp_ReturnUrl=http://localhost:8080/api/v1/payment/vnpay/callback&vnp_TmnCode=DEMO&vnp_TxnRef=payment-uuid_1699614900000&vnp_Version=2.1.0&vnp_SecureHash=abc123..."
  }
}
```

---

### Bước 3: User thực hiện thanh toán trên VNPay

**User Action**: 
- Frontend redirect user đến `paymentUrl`
- User nhập thông tin thẻ:
  - Số thẻ: 9704198526191432198
  - Tên: NGUYEN VAN A
  - Ngày phát hành: 07/15
  - OTP: 123456

---

### Bước 4: VNPay callback về Backend

**HTTP Request từ VNPay**:
```http
GET /api/v1/payment/vnpay/callback?vnp_Amount=50000000&vnp_BankCode=NCB&vnp_BankTranNo=VNP01234567&vnp_CardType=ATM&vnp_OrderInfo=Thanh+toan+don+hang+booking-123-uuid&vnp_PayDate=20251110144500&vnp_ResponseCode=00&vnp_TmnCode=DEMO&vnp_TransactionNo=14012678&vnp_TransactionStatus=00&vnp_TxnRef=payment-uuid_1699614900000&vnp_SecureHash=xyz789... HTTP/1.1
Host: localhost:8080
```

**HTTP Response từ Backend**:
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "success": true,
  "message": "Thanh toán thành công",
  "data": {
    "transactionNo": "14012678",
    "amount": 500000,
    "bankCode": "NCB",
    "cardType": "ATM",
    "orderInfo": "Thanh toan don hang booking-123-uuid",
    "payDate": "20251110144500"
  }
}
```

---

### Bước 5: Kiểm tra Payment Status

**HTTP Request**:
```http
GET /api/v1/payment/vnpay/status/booking-123-uuid HTTP/1.1
Host: localhost:8080
Authorization: Bearer {jwt_token}
```

**HTTP Response**:
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "success": true,
  "message": "Get payment status successfully",
  "data": {
    "bookingId": "booking-123-uuid",
    "paymentId": "payment-uuid",
    "amount": 500000,
    "status": "PAID",
    "transactionCode": "14012678",
    "paidAt": "2025-11-10T14:45:00"
  }
}
```

---

### Bước 6: Kiểm tra Booking đã thanh toán

**HTTP Request**:
```http
GET /api/v1/customer/bookings/booking-123-uuid HTTP/1.1
Host: localhost:8080
Authorization: Bearer {jwt_token}
```

**HTTP Response**:
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "success": true,
  "data": {
    "bookingId": "booking-123-uuid",
    "customerId": "customer-uuid",
    "serviceId": "service-uuid",
    "serviceName": "Dọn dẹp nhà cửa",
    "scheduleDate": "2025-11-15",
    "scheduleTime": "09:00",
    "status": "CONFIRMED",
    "totalPrice": 500000,
    "paymentStatus": "PAID",
    "paymentInfo": {
      "paymentId": "payment-uuid",
      "amount": 500000,
      "status": "PAID",
      "transactionCode": "14012678",
      "paidAt": "2025-11-10T14:45:00"
    },
    "createdAt": "2025-11-10T14:30:00",
    "updatedAt": "2025-11-10T14:45:00"
  }
}
```

---

## Flow Diagram

```
┌──────────┐                                                                    
│ Customer │                                                                    
└────┬─────┘                                                                    
     │                                                                          
     │ 1. POST /api/v1/customer/bookings                                       
     │    Input: {serviceId, scheduleDate, scheduleTime, addressId}            
     ▼                                                                          
┌─────────────┐                                                                
│   Backend   │                                                                
│  (Booking)  │                                                                
└──────┬──────┘                                                                
       │ Output: {bookingId, totalPrice, status: PENDING, paymentStatus: UNPAID}
       │                                                                        
       │ 2. POST /api/v1/payment/vnpay/create                                  
       │    Input: {bookingId, amount}                                         
       ▼                                                                        
┌─────────────┐                                                                
│   Backend   │                                                                
│  (VNPay)    │                                                                
└──────┬──────┘                                                                
       │ Output: {paymentUrl}                                                  
       │                                                                        
       │ 3. Redirect to paymentUrl                                             
       ▼                                                                        
┌─────────────┐                                                                
│    VNPay    │                                                                
│  Gateway    │ ◄── User nhập thông tin thẻ                                   
└──────┬──────┘                                                                
       │                                                                        
       │ 4. GET /api/v1/payment/vnpay/callback                                 
       │    Input: Query params (vnp_ResponseCode, vnp_TransactionNo, etc.)    
       ▼                                                                        
┌─────────────┐                                                                
│   Backend   │                                                                
│  (Callback) │ ──► Update Payment status to PAID                              
└──────┬──────┘     Update Booking paymentStatus to PAID                       
       │                                                                        
       │ Output: {success, transactionNo, amount, payDate}                     
       │                                                                        
       │ 5. GET /api/v1/payment/vnpay/status/{bookingId}                       
       ▼                                                                        
┌─────────────┐                                                                
│   Backend   │                                                                
└──────┬──────┘                                                                
       │ Output: {bookingId, paymentId, status: PAID, transactionCode}         
       │                                                                        
       │ 6. GET /api/v1/customer/bookings/{bookingId}                          
       ▼                                                                        
┌──────────┐                                                                   
│ Customer │ ◄── Hiển thị booking đã thanh toán thành công                     
└──────────┘                                                                   
```


