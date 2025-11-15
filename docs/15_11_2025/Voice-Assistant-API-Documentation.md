# Voice Assistant API - Trợ lý AI đặt lịch bằng giọng nói

## Tổng quan

Tính năng trợ lý AI cho phép khách hàng đặt lịch dịch vụ giúp việc nhà bằng cách sử dụng giọng nói. Hệ thống sẽ tự động:
1. Chuyển đổi giọng nói thành văn bản (Speech-to-Text)
2. Phân tích và trích xuất thông tin đặt lịch bằng AI
3. Tạo booking tự động dựa trên thông tin trích xuất

## Công nghệ sử dụng

- **AssemblyAI**: Chuyển đổi giọng nói sang văn bản (hỗ trợ tiếng Việt)
- **Spring AI + OpenAI**: Phân tích ngữ nghĩa và trích xuất thông tin booking
- **Spring Boot**: Backend framework

## Cấu hình

### 1. Environment Variables

Cần cấu hình các biến môi trường sau trong `application.yml` hoặc environment:

```yaml
# API Keys
ASSEMBLYAI_API_KEY=your_assemblyai_api_key_here
OPENAI_API_KEY=your_openai_api_key_here

# Optional configurations
OPENAI_MODEL=gpt-4  # hoặc gpt-3.5-turbo
VOICE_TEMP_DIR=/tmp/voice-assistant
```

### 2. Lấy API Keys

**AssemblyAI:**
1. Truy cập https://www.assemblyai.com/
2. Đăng ký tài khoản
3. Lấy API key từ dashboard
4. Free tier: 5 giờ transcription/tháng

**OpenAI:**
1. Truy cập https://platform.openai.com/
2. Đăng ký tài khoản
3. Tạo API key từ dashboard
4. Cần có credit để sử dụng

## API Endpoints

### 1. Health Check

Kiểm tra trạng thái của Voice Assistant API.

**Endpoint:** `GET /api/v1/voice-assistant/health`

**Response:**
```json
{
  "success": true,
  "message": "Voice Assistant API is running",
  "features": {
    "transcription": "available",
    "intentExtraction": "available",
    "voiceBooking": "available"
  }
}
```

---

### 2. Transcribe Voice to Text

Chuyển đổi file âm thanh thành văn bản.

**Endpoint:** `POST /api/v1/voice-assistant/transcribe`

**Headers:**
```
Authorization: Bearer {jwt_token}
Content-Type: multipart/form-data
```

**Request Body (multipart/form-data):**
- `audio` (file): File âm thanh (mp3, wav, m4a, ogg, webm)

**Example using cURL:**
```bash
curl -X POST http://localhost:8080/api/v1/voice-assistant/transcribe \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "audio=@voice_recording.mp3"
```

**Response (Success):**
```json
{
  "success": true,
  "transcription": "Tôi muốn đặt dịch vụ vệ sinh nhà cửa vào ngày mai lúc 9 giờ sáng",
  "message": "Chuyển đổi giọng nói thành công",
  "processingTimeMs": 2500
}
```

**Response (Error):**
```json
{
  "success": false,
  "transcription": null,
  "message": "Invalid audio file type",
  "processingTimeMs": 150
}
```

---

### 3. Extract Booking Intent

Trích xuất thông tin đặt lịch từ văn bản.

**Endpoint:** `POST /api/v1/voice-assistant/extract-intent`

**Headers:**
```
Authorization: Bearer {jwt_token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "transcription": "Tôi muốn đặt dịch vụ vệ sinh nhà cửa vào ngày mai lúc 9 giờ sáng",
  "customerId": "CUST001"
}
```

**Response (Success):**
```json
{
  "success": true,
  "message": "Trích xuất thông tin thành công",
  "intent": {
    "serviceType": "Vệ sinh nhà cửa",
    "bookingTime": "2025-11-15T09:00:00",
    "address": null,
    "note": "Tôi muốn đặt dịch vụ vệ sinh nhà cửa vào ngày mai lúc 9 giờ sáng",
    "detectedServices": ["Vệ sinh nhà cửa"],
    "confidence": 0.95
  },
  "bookingRequest": {
    "addressId": "ADDR001",
    "newAddress": null,
    "bookingTime": "2025-11-15T09:00:00",
    "note": "Tôi muốn đặt dịch vụ vệ sinh nhà cửa vào ngày mai lúc 9 giờ sáng",
    "title": "Đặt lịch bằng giọng nói",
    "imageUrls": null,
    "promoCode": null,
    "bookingDetails": [
      {
        "serviceId": "SRV001",
        "quantity": 1,
        "note": null
      }
    ],
    "assignments": null,
    "paymentMethodId": 1
  }
}
```

---

### 4. Complete Voice Booking (Recommended)

Xử lý toàn bộ quy trình đặt lịch bằng giọng nói: transcribe + extract + create booking.

**Endpoint:** `POST /api/v1/voice-assistant/book`

**Headers:**
```
Authorization: Bearer {jwt_token}
Content-Type: multipart/form-data
```

**Request Body (multipart/form-data):**
- `audio` (file): File âm thanh
- `customerId` (string): ID khách hàng

**Example using cURL:**
```bash
curl -X POST http://localhost:8080/api/v1/voice-assistant/book \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "audio=@voice_booking.mp3" \
  -F "customerId=CUST001"
```

**Response (Success):**
```json
{
  "success": true,
  "message": "Đặt lịch thành công qua giọng nói",
  "transcription": "Tôi muốn đặt dịch vụ vệ sinh nhà cửa vào ngày mai lúc 9 giờ sáng",
  "intent": {
    "serviceType": "Vệ sinh nhà cửa",
    "bookingTime": "2025-11-15T09:00:00",
    "address": null,
    "note": "Tôi muốn đặt dịch vụ vệ sinh nhà cửa vào ngày mai lúc 9 giờ sáng",
    "detectedServices": ["Vệ sinh nhà cửa"],
    "confidence": 0.95
  },
  "bookingResult": {
    "success": true,
    "message": "Booking created successfully",
    "bookingId": "BK202511140001",
    "totalPrice": 300000,
    "estimatedDuration": 120,
    "bookingDetails": [...]
  },
  "processingTimeMs": 3500
}
```

**Response (Error):**
```json
{
  "success": false,
  "message": "Không thể tạo booking: Service not found",
  "transcription": "Tôi muốn đặt dịch vụ xyz",
  "intent": {
    "serviceType": "xyz",
    "bookingTime": "2025-11-15T09:00:00",
    "address": null,
    "note": "Tôi muốn đặt dịch vụ xyz",
    "detectedServices": ["xyz"],
    "confidence": 0.60
  },
  "bookingResult": null,
  "processingTimeMs": 2800
}
```

## Ví dụ sử dụng

### JavaScript/TypeScript (Frontend)

```javascript
async function createVoiceBooking(audioBlob, customerId, token) {
  const formData = new FormData();
  formData.append('audio', audioBlob, 'recording.wav');
  formData.append('customerId', customerId);

  try {
    const response = await fetch('http://localhost:8080/api/v1/voice-assistant/book', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`
      },
      body: formData
    });

    const result = await response.json();
    
    if (result.success) {
      console.log('Booking created:', result.bookingResult);
      return result;
    } else {
      console.error('Booking failed:', result.message);
      throw new Error(result.message);
    }
  } catch (error) {
    console.error('Error:', error);
    throw error;
  }
}

// Usage with MediaRecorder API
let mediaRecorder;
let audioChunks = [];

navigator.mediaDevices.getUserMedia({ audio: true })
  .then(stream => {
    mediaRecorder = new MediaRecorder(stream);
    
    mediaRecorder.ondataavailable = event => {
      audioChunks.push(event.data);
    };
    
    mediaRecorder.onstop = async () => {
      const audioBlob = new Blob(audioChunks, { type: 'audio/wav' });
      audioChunks = [];
      
      await createVoiceBooking(audioBlob, 'CUST001', 'your_jwt_token');
    };
    
    // Start recording
    mediaRecorder.start();
    
    // Stop after 5 seconds (or use button)
    setTimeout(() => mediaRecorder.stop(), 5000);
  });
```

### Python Example

```python
import requests

def create_voice_booking(audio_file_path, customer_id, token):
    url = 'http://localhost:8080/api/v1/voice-assistant/book'
    
    headers = {
        'Authorization': f'Bearer {token}'
    }
    
    files = {
        'audio': open(audio_file_path, 'rb')
    }
    
    data = {
        'customerId': customer_id
    }
    
    response = requests.post(url, headers=headers, files=files, data=data)
    
    return response.json()

# Usage
result = create_voice_booking('voice_recording.mp3', 'CUST001', 'your_jwt_token')
print(result)
```

## Các câu lệnh giọng nói được hỗ trợ

### Ví dụ đặt lịch cơ bản:
- "Tôi muốn đặt dịch vụ vệ sinh nhà cửa vào ngày mai lúc 9 giờ sáng"
- "Đặt lịch giặt là vào thứ 7 tuần sau"
- "Tôi cần người giúp việc dọn dẹp nhà vào chiều mai"

### Ví dụ với địa chỉ cụ thể:
- "Đặt dịch vụ vệ sinh nhà tại số 123 Nguyễn Văn Linh, Quận 7 vào sáng thứ 2"

### Ví dụ với ghi chú:
- "Đặt dịch vụ nấu ăn vào tối thứ 6, nhớ mang theo nguyên liệu"

### Ví dụ với nhiều dịch vụ:
- "Tôi cần dịch vụ vệ sinh và giặt là vào cuối tuần"

## Các dịch vụ được nhận diện

Hệ thống có thể nhận diện các từ khóa sau:

| Từ khóa | Dịch vụ mapping |
|---------|-----------------|
| vệ sinh, lau nhà, quét nhà | Vệ sinh nhà cửa |
| giặt là | Giặt là |
| nấu ăn | Nấu ăn |
| chăm sóc | Chăm sóc người già |
| trông trẻ | Trông trẻ |
| sửa chữa | Sửa chữa điện nước |
| tổng vệ sinh | Tổng vệ sinh |

## Định dạng thời gian

Hệ thống có thể hiểu các cách nói về thời gian:
- "ngày mai", "hôm nay", "hôm sau"
- "thứ 2", "thứ 3", "cuối tuần"
- "tuần sau", "tuần này"
- "9 giờ sáng", "2 giờ chiều", "7 giờ tối"
- "sáng", "chiều", "tối" (mặc định 9h, 14h, 19h)

## Xử lý lỗi

### Common Error Codes

| HTTP Status | Error Message | Giải pháp |
|-------------|---------------|-----------|
| 400 | File âm thanh không được để trống | Đảm bảo gửi file audio trong request |
| 401 | Token không hợp lệ | Kiểm tra JWT token |
| 500 | Invalid audio file type | Chỉ chấp nhận file audio (mp3, wav, etc.) |
| 500 | Could not transcribe audio | Kiểm tra API key AssemblyAI |
| 500 | Service not found | Dịch vụ không tồn tại trong hệ thống |

## Giới hạn và Lưu ý

1. **Kích thước file audio**: Tối đa 10MB
2. **Định dạng audio hỗ trợ**: mp3, wav, m4a, ogg, webm
3. **Ngôn ngữ**: Tiếng Việt (có thể mở rộng)
4. **Thời gian xử lý**: Thường 2-5 giây tùy độ dài audio
5. **API Rate Limit**: Phụ thuộc vào plan của AssemblyAI và OpenAI

## Security

- Yêu cầu JWT authentication cho tất cả endpoints
- Chỉ CUSTOMER và ADMIN có quyền sử dụng
- File audio được lưu tạm và tự động xóa sau khi xử lý
- Không lưu trữ audio recordings lâu dài

## Performance Tips

1. **Tối ưu audio quality**: Sử dụng bitrate vừa phải (128-256 kbps)
2. **Giảm background noise**: Thu âm trong môi trường yên tĩnh
3. **Độ dài phù hợp**: 5-30 giây cho một câu lệnh
4. **Nói rõ ràng**: Phát âm rõ ràng, tốc độ vừa phải

## Testing

### Test với Postman

1. Import collection từ file `VoiceAssistant.postman_collection.json` (nếu có)
2. Set environment variable `BASE_URL` = `http://localhost:8080`
3. Set `JWT_TOKEN` từ login endpoint
4. Test các endpoint theo thứ tự:
   - Health check
   - Transcribe
   - Extract intent
   - Complete booking

### Test với cURL

```bash
# Health check
curl http://localhost:8080/api/v1/voice-assistant/health

# Transcribe
curl -X POST http://localhost:8080/api/v1/voice-assistant/transcribe \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "audio=@test.mp3"

# Complete booking
curl -X POST http://localhost:8080/api/v1/voice-assistant/book \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "audio=@test.mp3" \
  -F "customerId=CUST001"
```

## Troubleshooting

### AssemblyAI API Key không hoạt động
- Kiểm tra API key có đúng không
- Kiểm tra còn credit/quota không
- Thử test trực tiếp trên AssemblyAI dashboard

### OpenAI không trích xuất đúng thông tin
- Kiểm tra model đang sử dụng (gpt-4 tốt hơn gpt-3.5)
- Kiểm tra credit OpenAI
- Xem log để debug AI response

### Transcription trả về "Không thể chuyển đổi"
- API key chưa được cấu hình
- Sử dụng fallback mode (giới hạn chức năng)

## Future Enhancements

- [ ] Hỗ trợ nhiều ngôn ngữ (English, Chinese, etc.)
- [ ] Tích hợp Text-to-Speech để phản hồi bằng giọng nói
- [ ] Hỗ trợ conversation flow (multi-turn dialog)
- [ ] Cải thiện intent extraction với custom trained models
- [ ] Thêm voice authentication/verification
- [ ] Real-time streaming transcription
- [ ] Voice command shortcuts

## Support

Liên hệ team phát triển nếu gặp vấn đề:
- Email: support@housekeeping.local
- GitHub Issues: [Link to repo]
