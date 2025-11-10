# API QR Code Decoder

## Ngày tạo: 09/11/2025

## Tổng quan
API để decode mã QR từ file ảnh, lấy dữ liệu JSON từ mã QR và lưu vào Redis.

## Endpoint

### Decode QR Code từ ảnh

**Endpoint:** `POST /api/v1/auth/decode-qr`

**Mô tả:** 
- Nhận file ảnh có chứa mã QR
- Decode mã QR để lấy nội dung JSON
- Lưu JSON vào Redis với thời gian hết hạn 1 giờ
- Trả về dữ liệu JSON cho client

**Content-Type:** `multipart/form-data`

**Request Parameters:**

| Tham số | Kiểu | Bắt buộc | Mô tả |
|---------|------|----------|-------|
| image | MultipartFile | Có | File ảnh chứa mã QR (max 10MB) |

**Validation:**
- File không được để trống
- File phải là định dạng ảnh (image/*)
- Kích thước file không vượt quá 10MB
- Ảnh phải chứa mã QR hợp lệ
- Nội dung mã QR phải là JSON hợp lệ

## Request Example

### Using cURL
```bash
curl -X POST "http://localhost:8080/api/v1/auth/decode-qr" \
  -H "Content-Type: multipart/form-data" \
  -F "image=@/path/to/qr-image.png"
```

### Using Postman
1. Method: POST
2. URL: `http://localhost:8080/api/v1/auth/decode-qr`
3. Body: 
   - Select "form-data"
   - Key: `image` (type: File)
   - Value: Browse and select QR code image

### Using JavaScript (Fetch API)
```javascript
const formData = new FormData();
formData.append('image', fileInput.files[0]);

fetch('http://localhost:8080/api/v1/auth/decode-qr', {
  method: 'POST',
  body: formData
})
.then(response => response.json())
.then(data => {
  console.log('QR Data:', data);
})
.catch(error => {
  console.error('Error:', error);
});
```

## Response Examples

### Success Response (200 OK)

**QR Code chứa JSON:**
```json
{
  "userId": "12345",
  "name": "Nguyen Van A",
  "email": "nguyenvana@example.com"
}
```

**API Response:**
```json
{
  "success": true,
  "message": "Decode mã QR thành công",
  "qrId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "data": {
    "userId": "12345",
    "name": "Nguyen Van A",
    "email": "nguyenvana@example.com"
  },
  "expiresIn": 3600
}
```

**Giải thích:**
- `success`: Trạng thái thành công
- `message`: Thông báo
- `qrId`: ID duy nhất để lấy dữ liệu từ Redis sau này
- `data`: Dữ liệu JSON được decode từ mã QR
- `expiresIn`: Thời gian hết hạn (giây) - dữ liệu sẽ bị xóa khỏi Redis sau thời gian này

### Error Responses

#### 1. File không được để trống (400 Bad Request)
```json
{
  "success": false,
  "message": "File ảnh không được để trống"
}
```

#### 2. File không đúng định dạng (400 Bad Request)
```json
{
  "success": false,
  "message": "File phải là định dạng ảnh"
}
```

#### 3. File quá lớn (400 Bad Request)
```json
{
  "success": false,
  "message": "Kích thước file không được vượt quá 10MB"
}
```

#### 4. Không đọc được file ảnh (400 Bad Request)
```json
{
  "success": false,
  "message": "Không thể đọc file ảnh. Vui lòng kiểm tra định dạng ảnh."
}
```

#### 5. Không tìm thấy mã QR (404 Not Found)
```json
{
  "success": false,
  "message": "Không tìm thấy mã QR trong ảnh"
}
```

#### 6. Mã QR không hợp lệ (400 Bad Request)
```json
{
  "success": false,
  "message": "Mã QR không hợp lệ hoặc bị hỏng: <error details>"
}
```

#### 7. Nội dung không phải JSON (400 Bad Request)
```json
{
  "success": false,
  "message": "Nội dung mã QR không phải là JSON hợp lệ"
}
```

#### 8. Lỗi Redis (500 Internal Server Error)
```json
{
  "success": false,
  "message": "Lỗi khi lưu dữ liệu vào Redis"
}
```

## Redis Storage

### Key Pattern
```
qr_data:<qrId>
```

**Ví dụ:**
```
qr_data:a1b2c3d4-e5f6-7890-abcd-ef1234567890
```

### Value
Dữ liệu JSON được lưu dưới dạng Map<String, Object>

### TTL (Time To Live)
- **Thời gian:** 60 phút (3600 giây)
- **Hành vi:** Dữ liệu sẽ tự động bị xóa khỏi Redis sau 60 phút

### Lấy dữ liệu từ Redis
```java
String redisKey = "qr_data:" + qrId;
Object qrData = redisTemplate.opsForValue().get(redisKey);
```

### QR Code Decoding Process

1. **Image Reading**
   - MultipartFile → InputStream → BufferedImage

2. **QR Detection**
   - BufferedImage → LuminanceSource
   - LuminanceSource → BinaryBitmap (with HybridBinarizer)

3. **Decoding**
   - BinaryBitmap → MultiFormatReader.decode()
   - With hints: TRY_HARDER, POSSIBLE_FORMATS (QR_CODE only)

4. **JSON Parsing**
   - Raw text → ObjectMapper.readValue() → Map<String, Object>

## Use Cases

### 1. QR Code Check-in
```json
{
  "bookingId": "booking-123",
  "customerId": "customer-456",
  "timestamp": "2025-11-09T10:30:00Z"
}
```

### 2. Employee Verification
```json
{
  "employeeId": "emp-789",
  "name": "Nguyen Van B",
  "role": "EMPLOYEE",
  "verified": true
}
```

### 3. Service Authorization
```json
{
  "serviceId": "service-321",
  "accessToken": "abc123xyz",
  "expiresAt": "2025-11-09T18:00:00Z"
}
```

## Test Cases

### TC01: Decode QR thành công
**Input:**
- File ảnh PNG chứa mã QR
- Mã QR có nội dung JSON hợp lệ

**Expected:**
- Status: 200 OK
- Response chứa `qrId` và `data`
- Dữ liệu được lưu vào Redis

### TC02: File không phải ảnh
**Input:**
- File PDF hoặc text

**Expected:**
- Status: 400 Bad Request
- Message: "File phải là định dạng ảnh"

### TC03: Ảnh không chứa QR
**Input:**
- File ảnh bình thường (không có QR code)

**Expected:**
- Status: 404 Not Found
- Message: "Không tìm thấy mã QR trong ảnh"

### TC04: QR chứa text thuần (không phải JSON)
**Input:**
- Ảnh có QR code chứa text: "Hello World"

**Expected:**
- Status: 400 Bad Request
- Message: "Nội dung mã QR không phải là JSON hợp lệ"

### TC05: File quá lớn
**Input:**
- File ảnh > 10MB

**Expected:**
- Status: 400 Bad Request
- Message: "Kích thước file không được vượt quá 10MB"

## Security Considerations

### 1. File Size Limit
- Max 10MB để tránh DDoS và tràn bộ nhớ

### 2. File Type Validation
- Chỉ chấp nhận file ảnh (content-type check)

### 3. Redis TTL
- Dữ liệu tự động xóa sau 1 giờ
- Tránh tích lũy dữ liệu không cần thiết

### 4. JSON Validation
- Kiểm tra JSON hợp lệ trước khi lưu Redis

### 5. Error Handling
- Không expose stack trace ra ngoài
- Log chi tiết lỗi ở server

## Performance

### Thời gian xử lý trung bình
- Upload file: < 100ms
- Decode QR: 100-300ms
- Parse JSON: < 10ms
- Store Redis: < 20ms
- **Tổng:** < 500ms

### Memory Usage
- BufferedImage sẽ được garbage collected sau khi xử lý
- Không cache ảnh trong memory

## Monitoring & Logging

### Log Levels

**INFO:**
- `Processing QR code from image: {filename}`
- `QR code decoded successfully: {qrText}`
- `QR data parsed successfully: {qrData}`
- `QR data stored in Redis with key: {redisKey}`

**WARN:**
- `No QR code found in image`

**ERROR:**
- `Error reading image file: {exception}`
- `Error decoding QR code: {exception}`
- `Error parsing JSON from QR code: {exception}`
- `Error storing data in Redis: {exception}`
- `Unexpected error in decodeQRFromImage: {exception}`

## Future Enhancements

### 1. Multiple QR Codes
- Hỗ trợ decode nhiều mã QR trong cùng một ảnh

### 2. QR Code Generation
- API để tạo mã QR từ JSON data

### 3. Advanced Validation
- JSON schema validation
- Custom validation rules

### 4. Webhook Support
- Gửi notification khi QR được decode

### 5. Analytics
- Track số lượng QR được scan
- Thống kê loại QR phổ biến

## Troubleshooting

### Issue 1: "Cannot resolve ZXing imports"
**Solution:** 
```bash
./gradlew clean build --refresh-dependencies
```

### Issue 2: Redis connection error
**Solution:** 
- Kiểm tra Redis đang chạy
- Verify Redis configuration trong application.properties

### Issue 3: QR không được detect
**Solution:**
- Đảm bảo QR code rõ ràng trong ảnh
- QR nên ở góc trên của ảnh
- Tăng resolution ảnh nếu QR quá nhỏ

### Issue 4: JSON parse error
**Solution:**
- Kiểm tra QR code có đúng format JSON
- Test JSON bằng online validator trước

## Related APIs

### Get QR Data from Redis
```java
@GetMapping("/qr-data/{qrId}")
public ResponseEntity<?> getQRData(@PathVariable String qrId) {
    String redisKey = "qr_data:" + qrId;
    Object data = redisTemplate.opsForValue().get(redisKey);
    
    if (data == null) {
        return ResponseEntity.notFound().build();
    }
    
    return ResponseEntity.ok(Map.of(
        "success", true,
        "data", data
    ));
}
```

## Contact & Support

For issues or questions:
- Check logs first
- Verify input file format
- Test with different QR codes
- Contact backend team if issue persists
