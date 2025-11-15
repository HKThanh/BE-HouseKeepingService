# 🎤 Voice Assistant - Quick Reference Card

## ⚡ Quick Start (3 Steps)

### 1️⃣ Get API Keys
```
AssemblyAI: https://www.assemblyai.com/ (Free: 5h/month)
OpenAI: https://platform.openai.com/ (Needs credit)
```

### 2️⃣ Set Environment Variables
```powershell
# Windows
$env:ASSEMBLYAI_API_KEY="your_key_here"
$env:OPENAI_API_KEY="your_key_here"
```

### 3️⃣ Run & Test
```bash
./gradlew bootRun
curl http://localhost:8080/api/v1/voice-assistant/health
```

---

## 🔌 API Endpoints

| Endpoint | Method | Auth | Purpose |
|----------|--------|------|---------|
| `/health` | GET | ❌ | Check status |
| `/transcribe` | POST | ✅ | Voice → Text |
| `/extract-intent` | POST | ✅ | Text → Intent |
| `/book` | POST | ✅ | Voice → Booking |

**Base URL:** `http://localhost:8080/api/v1/voice-assistant`

---

## 📝 Example Voice Commands

```
✅ "Tôi muốn đặt dịch vụ vệ sinh nhà cửa vào ngày mai lúc 9 giờ sáng"
✅ "Đặt lịch giặt là vào thứ 7 tuần sau"
✅ "Tôi cần người giúp việc dọn dẹp nhà vào chiều mai"
```

---

## 🧪 Test Commands

### cURL
```bash
# Health check
curl http://localhost:8080/api/v1/voice-assistant/health

# Voice booking
curl -X POST http://localhost:8080/api/v1/voice-assistant/book \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "audio=@recording.mp3" \
  -F "customerId=CUST001"
```

### HTML Tester
```
Open: docs/15_11_2025/voice-assistant-tester.html
```

---

## 📁 Files Created

```
✅ VoiceAssistantController.java
✅ VoiceAssistantService.java
✅ VoiceAssistantServiceImpl.java
✅ 6 DTO files
✅ 4 Documentation files
✅ HTML tester tool
✅ Updated build.gradle
✅ Updated application.yml
```

---

## 💰 Cost Estimate

**1000 bookings/month:**
- AssemblyAI: $180
- OpenAI: $5
- **Total: ~$185/month**

**Free tier:** ~150 bookings/month

---

## 🔧 Configuration Files

### build.gradle
```gradle
implementation 'org.springframework.ai:spring-ai-openai-spring-boot-starter:1.0.0-M3'
implementation 'com.assemblyai:assemblyai-java:1.2.0'
implementation 'com.squareup.okhttp3:okhttp:4.12.0'
```

### application.yml
```yaml
voice:
  assistant:
    assemblyai:
      api-key: ${ASSEMBLYAI_API_KEY:}
    openai:
      api-key: ${OPENAI_API_KEY:}
```

---

## ⚠️ Troubleshooting

| Problem | Solution |
|---------|----------|
| API key not configured | Set environment variables |
| Rate limit exceeded | Upgrade API plan |
| Poor transcription | Improve audio quality |
| OpenAI timeout | Check internet / Use gpt-3.5 |

---

## 📚 Documentation

| File | Description |
|------|-------------|
| `README.md` | Quick start guide |
| `Voice-Assistant-API-Documentation.md` | Full API docs (25+ pages) |
| `Voice-Assistant-Configuration-Guide.md` | Setup & config guide |
| `voice-assistant-tester.html` | Interactive test tool |
| `IMPLEMENTATION-SUMMARY.md` | Complete implementation report |

**Location:** `docs/15_11_2025/`

---

## 🚀 Next Steps

- [ ] Configure API keys
- [ ] Test with HTML tool
- [ ] Review documentation
- [ ] Plan production deployment
- [ ] Set up monitoring

---

## 📞 Need Help?

**Read docs:** `docs/15_11_2025/`  
**Check logs:** Console output for debugging  
**Test health:** `curl localhost:8080/api/v1/voice-assistant/health`

---

**Version:** 1.0.0 | **Date:** 14/11/2025 | **Status:** ✅ Complete
