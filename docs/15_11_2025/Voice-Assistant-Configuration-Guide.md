# Voice Assistant - Hướng dẫn cấu hình và triển khai

## 1. Cài đặt Dependencies

### Gradle Dependencies (đã thêm vào build.gradle)

```gradle
// AI Voice Assistant dependencies
implementation 'org.springframework.ai:spring-ai-openai-spring-boot-starter:1.0.0-M3'
implementation 'com.assemblyai:assemblyai-java:1.2.0'
implementation 'com.squareup.okhttp3:okhttp:4.12.0'
```

### Repository Configuration

```gradle
repositories {
    mavenCentral()
    maven { url 'https://repo.spring.io/milestone' }
}
```

## 2. Cấu hình API Keys

### 2.1. AssemblyAI Setup

**Tại sao cần AssemblyAI?**
- Dịch vụ Speech-to-Text chuyên nghiệp
- Hỗ trợ tiếng Việt tốt
- Độ chính xác cao
- Free tier: 5 giờ/tháng

**Các bước lấy API Key:**

1. Truy cập: https://www.assemblyai.com/
2. Click "Sign Up" để đăng ký tài khoản
3. Xác nhận email
4. Đăng nhập vào Dashboard
5. Vào mục "API Keys" hoặc "Settings"
6. Copy API Key

**Lưu ý:**
- Free tier giới hạn 5 giờ transcription/tháng
- Nếu cần nhiều hơn, nâng cấp lên paid plan
- API key format: `xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx`

### 2.2. OpenAI Setup

**Tại sao cần OpenAI?**
- AI language model để phân tích ngữ nghĩa
- Trích xuất thông tin booking từ text
- Hiểu ngôn ngữ tự nhiên

**Các bước lấy API Key:**

1. Truy cập: https://platform.openai.com/
2. Sign up/Login
3. Vào "API Keys" section
4. Click "Create new secret key"
5. Đặt tên và copy key (chỉ hiện 1 lần!)
6. Lưu key an toàn

**Lưu ý:**
- Cần có credit (thẻ thanh toán)
- Giá: $0.002-0.03 per 1K tokens tùy model
- Khuyến nghị: GPT-4 cho độ chính xác cao
- Alternative: GPT-3.5-turbo rẻ hơn

### 2.3. Cấu hình trong application.yml

```yaml
# Voice Assistant Configuration
voice:
  assistant:
    assemblyai:
      api-key: ${ASSEMBLYAI_API_KEY:your_assemblyai_key_here}
    openai:
      api-key: ${OPENAI_API_KEY:your_openai_key_here}
      model: ${OPENAI_MODEL:gpt-4}
    temp-dir: ${VOICE_TEMP_DIR:${java.io.tmpdir}/voice-assistant}

spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY:your_openai_key_here}
      chat:
        options:
          model: ${OPENAI_MODEL:gpt-4}
          temperature: 0.7
```

### 2.4. Environment Variables

**Cách 1: Sử dụng Environment Variables (Khuyến nghị cho production)**

**Windows:**
```powershell
# PowerShell
$env:ASSEMBLYAI_API_KEY="your_actual_key_here"
$env:OPENAI_API_KEY="your_actual_key_here"
$env:OPENAI_MODEL="gpt-4"

# Command Prompt
set ASSEMBLYAI_API_KEY=your_actual_key_here
set OPENAI_API_KEY=your_actual_key_here
set OPENAI_MODEL=gpt-4
```

**Linux/Mac:**
```bash
export ASSEMBLYAI_API_KEY="your_actual_key_here"
export OPENAI_API_KEY="your_actual_key_here"
export OPENAI_MODEL="gpt-4"
```

**Cách 2: File .env (Development)**

Tạo file `.env` trong root project:
```
ASSEMBLYAI_API_KEY=your_actual_key_here
OPENAI_API_KEY=your_actual_key_here
OPENAI_MODEL=gpt-4
```

**Cách 3: IntelliJ IDEA Configuration**

1. Run > Edit Configurations
2. Chọn Spring Boot application
3. Environment variables section
4. Thêm:
   - `ASSEMBLYAI_API_KEY=xxx`
   - `OPENAI_API_KEY=xxx`
   - `OPENAI_MODEL=gpt-4`

## 3. Kiểm tra cấu hình

### 3.1. Test API Keys

**Test AssemblyAI:**
```bash
curl https://api.assemblyai.com/v2/transcript \
  -H "Authorization: YOUR_ASSEMBLYAI_KEY" \
  -H "Content-Type: application/json" \
  -d '{"audio_url":"https://github.com/AssemblyAI-Examples/audio-examples/raw/main/20230607_me_canadian_wildfires.mp3"}'
```

**Test OpenAI:**
```bash
curl https://api.openai.com/v1/chat/completions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_OPENAI_KEY" \
  -d '{
    "model": "gpt-4",
    "messages": [{"role": "user", "content": "Say hello"}]
  }'
```

### 3.2. Test Application

```bash
# Start application
./gradlew bootRun

# Test health endpoint
curl http://localhost:8080/api/v1/voice-assistant/health
```

Expected response:
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

## 4. Chi phí ước tính

### AssemblyAI Pricing
- **Free Tier**: 5 hours/month
- **Pay-as-you-go**: $0.00025/second (~$0.90/hour)
- **Pro Plan**: $50/month (100 hours + features)

### OpenAI Pricing (GPT-4)
- **Input**: $0.03 per 1K tokens
- **Output**: $0.06 per 1K tokens
- Ước tính: ~$0.005 per booking request

**Ví dụ: 1000 bookings/tháng**
- AssemblyAI: ~200 hours → $180
- OpenAI: 1000 * $0.005 → $5
- **Tổng: ~$185/month**

## 5. Alternative Options (Nếu không có budget)

### 5.1. Sử dụng Free Alternatives

**Speech-to-Text:**
- Google Speech-to-Text API (Free tier: 60 minutes/month)
- Mozilla DeepSpeech (Open source, offline)
- Vosk (Open source, offline)

**NLP/Intent Extraction:**
- Ollama + LLaMA 2 (Free, self-hosted)
- Google Gemini API (Free tier available)
- Hugging Face models (Free, self-hosted)

### 5.2. Fallback Mode

Service đã implement fallback mode khi không có API key:
- Keyword-based service detection
- Simple time parsing
- Limited functionality nhưng vẫn hoạt động cơ bản

## 6. Production Deployment

### 6.1. Docker Configuration

**Dockerfile đã có sẵn**, thêm environment variables:

```dockerfile
ENV ASSEMBLYAI_API_KEY=your_key
ENV OPENAI_API_KEY=your_key
ENV OPENAI_MODEL=gpt-4
```

### 6.2. Docker Compose

Thêm vào `docker-compose.yml`:

```yaml
services:
  backend:
    environment:
      - ASSEMBLYAI_API_KEY=${ASSEMBLYAI_API_KEY}
      - OPENAI_API_KEY=${OPENAI_API_KEY}
      - OPENAI_MODEL=${OPENAI_MODEL:-gpt-4}
      - VOICE_TEMP_DIR=/tmp/voice-assistant
    volumes:
      - voice-temp:/tmp/voice-assistant

volumes:
  voice-temp:
```

### 6.3. Kubernetes Secrets

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: voice-assistant-secrets
type: Opaque
stringData:
  ASSEMBLYAI_API_KEY: your_actual_key
  OPENAI_API_KEY: your_actual_key
```

## 7. Security Best Practices

1. **Không commit API keys** vào Git
2. **Sử dụng environment variables** cho production
3. **Rotate keys định kỳ** (3-6 tháng)
4. **Monitor API usage** để phát hiện leak
5. **Set rate limits** trên backend
6. **Implement request validation**

## 8. Monitoring và Logging

### 8.1. Log Levels

```yaml
logging:
  level:
    iuh.house_keeping_service_be.services.VoiceAssistantService: DEBUG
```

### 8.2. Metrics to Monitor

- Transcription success rate
- Average processing time
- API costs
- Error rates
- User adoption

### 8.3. Alerts

Setup alerts cho:
- API quota exceeded
- High error rate (>10%)
- Slow response time (>5s)
- API key expiration

## 9. Troubleshooting

### Issue: "API key not configured"
**Solution:** Set environment variables correctly

### Issue: "AssemblyAI rate limit exceeded"
**Solution:** 
- Upgrade plan
- Implement caching
- Use batch processing

### Issue: "OpenAI timeout"
**Solution:**
- Check internet connection
- Increase timeout settings
- Switch to faster model (gpt-3.5-turbo)

### Issue: "Poor transcription quality"
**Solution:**
- Improve audio quality
- Use noise cancellation
- Speak clearly and slowly

## 10. Testing Guide

### Unit Tests
```bash
./gradlew test --tests *VoiceAssistant*
```

### Integration Tests
```bash
./gradlew test --tests *VoiceAssistantIntegrationTest
```

### Manual Testing
Sử dụng HTML test page: `voice-assistant-tester.html`

## 11. Maintenance

### Regular Tasks
- [ ] Monitor API usage monthly
- [ ] Review and optimize prompts
- [ ] Update AI models when available
- [ ] Clean up temp files
- [ ] Analyze user feedback

### Quarterly Review
- [ ] Cost analysis
- [ ] Performance optimization
- [ ] Feature enhancement
- [ ] Security audit

## 12. Support Resources

- **AssemblyAI Docs:** https://www.assemblyai.com/docs
- **OpenAI Docs:** https://platform.openai.com/docs
- **Spring AI:** https://docs.spring.io/spring-ai/reference/
- **Project Issues:** [Your GitHub Issues]

## 13. Quick Start Checklist

- [ ] Đăng ký AssemblyAI account
- [ ] Lấy AssemblyAI API key
- [ ] Đăng ký OpenAI account
- [ ] Lấy OpenAI API key
- [ ] Thêm credit vào OpenAI account
- [ ] Set environment variables
- [ ] Build project: `./gradlew build`
- [ ] Run application: `./gradlew bootRun`
- [ ] Test health endpoint
- [ ] Test với sample audio file
- [ ] Check logs for errors
- [ ] Monitor API usage

## Liên hệ

Nếu gặp khó khăn trong quá trình setup, liên hệ:
- Email: dev-team@housekeeping.local
- Slack: #voice-assistant-support
