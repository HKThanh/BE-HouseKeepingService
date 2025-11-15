# Voice Assistant - Trợ lý AI Đặt lịch bằng Giọng nói

## 📋 Tóm tắt

Tính năng cho phép khách hàng đặt lịch dịch vụ giúp việc nhà bằng giọng nói. Hệ thống tự động chuyển đổi giọng nói thành văn bản, phân tích ý định và tạo booking.

## 🚀 Quick Start

### 1. Cài đặt Dependencies

```bash
./gradlew build
```

Dependencies đã được thêm vào `build.gradle`:
- Spring AI OpenAI
- AssemblyAI Java SDK
- OkHttp

### 2. Cấu hình API Keys

**Lấy API Keys:**
- AssemblyAI: https://www.assemblyai.com/ (Free: 5h/month)
- OpenAI: https://platform.openai.com/ (Cần credit)

**Set Environment Variables:**

```powershell
# Windows PowerShell
$env:ASSEMBLYAI_API_KEY="your_key_here"
$env:OPENAI_API_KEY="your_key_here"
$env:OPENAI_MODEL="gpt-4"
```

```bash
# Linux/Mac
export ASSEMBLYAI_API_KEY="your_key_here"
export OPENAI_API_KEY="your_key_here"
export OPENAI_MODEL="gpt-4"
```

### 3. Chạy Application

```bash
./gradlew bootRun
```

### 4. Test API

**Health Check:**
```bash
curl http://localhost:8080/api/v1/voice-assistant/health
```

**Test với HTML:**
Mở file `docs/15_11_2025/voice-assistant-tester.html` trong trình duyệt.

## 📚 Tài liệu chi tiết

- **[API Documentation](./Voice-Assistant-API-Documentation.md)**: Chi tiết về các endpoints, request/response formats
- **[Configuration Guide](./Voice-Assistant-Configuration-Guide.md)**: Hướng dẫn cấu hình đầy đủ, troubleshooting
- **[HTML Tester](./voice-assistant-tester.html)**: Tool test giao diện web

## 🎯 Tính năng chính

### 1. Voice to Text (Speech Recognition)
- Chuyển đổi audio thành text
- Hỗ trợ tiếng Việt
- API: `POST /api/v1/voice-assistant/transcribe`

### 2. Intent Extraction
- Phân tích text để trích xuất thông tin booking
- Sử dụng AI (GPT-4)
- API: `POST /api/v1/voice-assistant/extract-intent`

### 3. Complete Voice Booking
- Quy trình hoàn chỉnh: transcribe → extract → create booking
- API: `POST /api/v1/voice-assistant/book`

## 🔧 Kiến trúc

```
[Audio Input] 
    ↓
[VoiceAssistantController]
    ↓
[VoiceAssistantService]
    ├─→ [AssemblyAI] → Speech-to-Text
    ├─→ [OpenAI GPT-4] → Intent Extraction
    └─→ [BookingService] → Create Booking
```

## 📝 API Endpoints

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| GET | `/api/v1/voice-assistant/health` | Kiểm tra trạng thái |
| POST | `/api/v1/voice-assistant/transcribe` | Chuyển giọng nói sang text |
| POST | `/api/v1/voice-assistant/extract-intent` | Trích xuất intent từ text |
| POST | `/api/v1/voice-assistant/book` | Đặt lịch hoàn chỉnh |

## 🎤 Ví dụ câu lệnh

```
✅ "Tôi muốn đặt dịch vụ vệ sinh nhà cửa vào ngày mai lúc 9 giờ sáng"
✅ "Đặt lịch giặt là vào thứ 7 tuần sau"
✅ "Tôi cần người giúp việc dọn dẹp nhà vào chiều mai"
✅ "Đặt dịch vụ nấu ăn vào tối thứ 6"
```

## 💰 Chi phí ước tính

**AssemblyAI:**
- Free: 5 hours/month
- Paid: ~$0.90/hour

**OpenAI GPT-4:**
- ~$0.005 per booking request

**Ví dụ: 1000 bookings/tháng**
- AssemblyAI: ~$180
- OpenAI: ~$5
- **Tổng: ~$185/month**

## 🔐 Security

- Yêu cầu JWT authentication
- Chỉ CUSTOMER và ADMIN có quyền
- Tự động xóa file audio sau xử lý
- Không lưu trữ recordings

## 🧪 Testing

### Manual Test với cURL

```bash
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

### Browser Test

Mở `voice-assistant-tester.html` và:
1. Nhập JWT token
2. Nhập Customer ID
3. Ghi âm hoặc upload file
4. Click "Complete Voice Booking"

## 📊 Monitoring

```yaml
logging:
  level:
    iuh.house_keeping_service_be.services.VoiceAssistantService: DEBUG
```

Metrics quan trọng:
- Transcription success rate
- Average processing time (~2-5s)
- API costs
- Error rates

## ⚠️ Troubleshooting

| Vấn đề | Giải pháp |
|--------|-----------|
| API key not configured | Set environment variables |
| Rate limit exceeded | Upgrade plan hoặc implement caching |
| Poor transcription | Cải thiện audio quality |
| OpenAI timeout | Check internet, switch to gpt-3.5-turbo |

## 🔄 Alternative Options

Nếu không có budget cho API:

**Speech-to-Text:**
- Google Speech-to-Text (60 min/month free)
- Mozilla DeepSpeech (offline, open source)
- Vosk (offline, open source)

**NLP:**
- Ollama + LLaMA 2 (free, self-hosted)
- Google Gemini (free tier)
- Hugging Face models (free)

## 📦 Files Created

```
src/main/java/iuh/house_keeping_service_be/
├── controllers/
│   └── VoiceAssistantController.java
├── services/VoiceAssistantService/
│   ├── VoiceAssistantService.java
│   └── impl/VoiceAssistantServiceImpl.java
└── dtos/VoiceAssistant/
    ├── request/VoiceBookingRequest.java
    ├── response/
    │   ├── VoiceBookingResponse.java
    │   ├── VoiceTranscriptionResponse.java
    │   └── VoiceBookingIntent.java
    └── internal/
        ├── VoiceProcessingResult.java
        └── BookingIntentExtractionResult.java

docs/15_11_2025/
├── Voice-Assistant-API-Documentation.md
├── Voice-Assistant-Configuration-Guide.md
├── voice-assistant-tester.html
└── README.md

build.gradle (updated)
src/main/resources/application.yml (updated)
```

## 🚀 Next Steps

1. **Cấu hình API Keys** (bắt buộc)
   ```bash
   export ASSEMBLYAI_API_KEY="..."
   export OPENAI_API_KEY="..."
   ```

2. **Build & Run**
   ```bash
   ./gradlew bootRun
   ```

3. **Test Health**
   ```bash
   curl http://localhost:8080/api/v1/voice-assistant/health
   ```

4. **Test với HTML Tester**
   - Mở `voice-assistant-tester.html`
   - Login để lấy JWT token
   - Test voice booking

5. **Production Deployment**
   - Set environment variables trong Docker/K8s
   - Monitor API usage
   - Set up alerts

## 📞 Support

- Email: dev-team@housekeeping.local
- Docs: Xem các file trong `docs/15_11_2025/`
- Issues: [GitHub Issues]

## ✨ Features Roadmap

- [ ] Multi-language support (English, Chinese)
- [ ] Text-to-Speech response
- [ ] Real-time streaming transcription
- [ ] Voice authentication
- [ ] Conversation flow (multi-turn)
- [ ] Custom trained models

---

**Tạo bởi:** Development Team  
**Ngày:** 15/11/2025  
**Version:** 1.0.0
