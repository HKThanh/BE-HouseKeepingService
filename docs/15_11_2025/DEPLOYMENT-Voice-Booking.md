# Voice Booking Deployment Guide

## 📋 Pre-deployment Checklist

### 1. Environment Setup

- [ ] OpenAI API key obtained from https://platform.openai.com/api-keys
- [ ] Environment variables configured (see `voice-booking-config.env.example`)
- [ ] Database credentials ready

### 2. Database Migration

```bash
# Connect to PostgreSQL
psql -U postgres -d house_keeping

# Run migration
\i postgres_data/init_sql/13_voice_booking.sql

# Verify table created
\dt voice_booking_request
\d voice_booking_request
```

### 3. Dependencies

```bash
# Verify Gradle dependencies
./gradlew dependencies --configuration runtimeClasspath | grep -E "openai|soundlibs"

# Expected output:
# - com.theokanning.openai-gpt3-java:service:0.18.2
# - com.googlecode.soundlibs:jlayer:1.0.1.4
# - com.googlecode.soundlibs:mp3spi:1.9.5.4
```

### 4. Configuration Validation

```bash
# Check application.yml has whisper configuration
grep -A 20 "whisper:" src/main/resources/application.yml
```

---

## 🚀 Deployment Steps

### Step 1: Database Setup

```bash
# Production database
psql -h YOUR_DB_HOST -U YOUR_DB_USER -d YOUR_DB_NAME \
  -f postgres_data/init_sql/13_voice_booking.sql

# Verify
psql -h YOUR_DB_HOST -U YOUR_DB_USER -d YOUR_DB_NAME \
  -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'voice_booking_request';"
```

**Expected output**: `1`

---

### Step 2: Environment Variables

Create `.env` file or set system environment variables:

```bash
# Required
export OPENAI_API_KEY="sk-your-actual-api-key"

# Optional (with defaults)
export WHISPER_ENABLED=true
export WHISPER_MODEL=whisper-1
export WHISPER_TIMEOUT=30
export WHISPER_MAX_RETRIES=2
export WHISPER_ASYNC=false
export WHISPER_THREAD_POOL=3
```

Or add to your deployment platform:
- **Docker**: In `docker-compose.yml` environment section
- **Kubernetes**: In ConfigMap/Secret
- **Heroku**: Using `heroku config:set`
- **AWS**: In Elastic Beanstalk environment properties

---

### Step 3: Build Application

```bash
# Clean build
./gradlew clean build

# Or with tests skipped (if tests not yet implemented)
./gradlew clean build -x test

# Verify build artifact
ls -lh build/libs/house_keeping_service_BE-*.jar
```

---

### Step 4: Run Application

#### Development:

```bash
./gradlew bootRun
```

#### Production (with JAR):

```bash
java -jar build/libs/house_keeping_service_BE-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=production \
  --server.port=8080
```

#### With Docker:

```bash
# Build image
docker build -t house-keeping-service:latest .

# Run container
docker run -d \
  -p 8080:8080 \
  -e OPENAI_API_KEY="sk-your-api-key" \
  -e WHISPER_ENABLED=true \
  --name house-keeping-service \
  house-keeping-service:latest
```

---

### Step 5: Health Check

```bash
# 1. Check application started
curl http://localhost:8080/actuator/health

# 2. Check voice booking status
curl http://localhost:8080/api/v1/customer/bookings/voice/status

# Expected response:
# {
#   "success": true,
#   "voiceBookingEnabled": true,
#   "message": "Voice booking service is available"
# }
```

---

### Step 6: Verify Voice Booking Endpoint

```bash
# Get JWT token first (use your existing auth endpoint)
TOKEN=$(curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test_customer","password":"password"}' \
  | jq -r '.accessToken')

# Test voice booking with sample audio
curl -X POST http://localhost:8080/api/v1/customer/bookings/voice \
  -H "Authorization: Bearer $TOKEN" \
  -F "audio=@test_audio.mp3" \
  -F 'hints={"serviceId": 1}'

# Expected: 200 OK with voice booking response
```

---

## 🔍 Post-Deployment Verification

### 1. Database Check

```sql
-- Check if table exists and is accessible
SELECT 
    COUNT(*) as total_requests,
    status,
    COUNT(*) FILTER (WHERE created_at > NOW() - INTERVAL '1 hour') as last_hour
FROM voice_booking_request
GROUP BY status;
```

### 2. Logs Review

```bash
# Check for voice booking logs
tail -f logs/application.log | grep -i "voice"

# Look for:
# - "Processing voice booking for customer: ..."
# - "Whisper transcription completed in ...ms"
# - "Creating booking from voice request: ..."
```

### 3. Performance Monitoring

```bash
# Monitor Whisper API calls
tail -f logs/application.log | grep "Whisper"

# Check average processing time
psql -d house_keeping -c "
SELECT 
    AVG(processing_time_ms) as avg_processing_time,
    MIN(processing_time_ms) as min_time,
    MAX(processing_time_ms) as max_time
FROM voice_booking_request 
WHERE created_at > NOW() - INTERVAL '1 day';
"
```

---

## 🐛 Troubleshooting

### Issue 1: "Voice booking service is currently unavailable"

**Cause**: Whisper disabled or API key not configured

**Solution**:
```bash
# Check environment variable
echo $OPENAI_API_KEY

# Verify in application
curl http://localhost:8080/api/v1/customer/bookings/voice/status

# Set API key
export OPENAI_API_KEY="sk-your-api-key"

# Restart application
```

---

### Issue 2: "Whisper API timeout"

**Cause**: Network issues or audio too large

**Solution**:
```yaml
# Increase timeout in application.yml
whisper:
  timeout-seconds: 60  # Increase from 30 to 60
  max-retries: 3       # Increase retries
```

---

### Issue 3: "Failed to transcribe audio"

**Cause**: Invalid audio format or corrupted file

**Solution**:
```bash
# Validate audio file
file test_audio.mp3
# Should show: MPEG ADTS, layer III

# Check file size
ls -lh test_audio.mp3
# Should be < 5MB

# Test with known good audio
curl -X POST http://localhost:8080/api/v1/customer/bookings/voice \
  -H "Authorization: Bearer $TOKEN" \
  -F "audio=@valid_sample.mp3"
```

---

### Issue 4: Database Connection Issues

**Solution**:
```bash
# Test database connection
psql -h YOUR_DB_HOST -U YOUR_DB_USER -d YOUR_DB_NAME -c "\dt voice_booking_request"

# Check application.yml database settings
cat src/main/resources/application.yml | grep -A 5 "datasource:"

# Verify user has permissions
psql -d house_keeping -c "GRANT ALL ON voice_booking_request TO your_user;"
```

---

## 📊 Monitoring & Metrics

### 1. Usage Metrics

```sql
-- Daily voice booking stats
SELECT 
    DATE(created_at) as date,
    COUNT(*) as total_requests,
    COUNT(*) FILTER (WHERE status = 'COMPLETED') as completed,
    COUNT(*) FILTER (WHERE status = 'PARTIAL') as partial,
    COUNT(*) FILTER (WHERE status = 'FAILED') as failed,
    AVG(processing_time_ms) as avg_processing_time
FROM voice_booking_request
WHERE created_at > NOW() - INTERVAL '7 days'
GROUP BY DATE(created_at)
ORDER BY date DESC;
```

### 2. Cost Tracking

```sql
-- Estimate Whisper API costs (based on audio duration)
SELECT 
    DATE(created_at) as date,
    COUNT(*) as requests,
    SUM(audio_duration_seconds) / 60.0 as total_minutes,
    (SUM(audio_duration_seconds) / 60.0) * 0.006 as estimated_cost_usd
FROM voice_booking_request
WHERE created_at > NOW() - INTERVAL '30 days'
GROUP BY DATE(created_at)
ORDER BY date DESC;
```

### 3. Error Analysis

```sql
-- Failed requests analysis
SELECT 
    error_message,
    COUNT(*) as occurrences,
    MAX(created_at) as last_occurrence
FROM voice_booking_request
WHERE status = 'FAILED'
    AND created_at > NOW() - INTERVAL '7 days'
GROUP BY error_message
ORDER BY occurrences DESC;
```

---

## 🔐 Security Checklist

- [ ] OpenAI API key stored securely (environment variable, not in code)
- [ ] JWT authentication enabled for voice booking endpoints
- [ ] CORS configured properly for frontend origin
- [ ] Rate limiting implemented (consider adding)
- [ ] Audio file validation enabled (size, duration, format)
- [ ] Customer can only access their own voice booking requests
- [ ] HTTPS enabled in production

---

## 📈 Scaling Considerations

### Horizontal Scaling

```yaml
# For multiple instances, consider:
whisper:
  processing:
    async-enabled: true  # Enable async processing
    thread-pool-size: 5  # Adjust based on load
```

### Database Optimization

```sql
-- Add indexes if not exists (already in migration)
CREATE INDEX IF NOT EXISTS idx_voice_booking_customer ON voice_booking_request(customer_id);
CREATE INDEX IF NOT EXISTS idx_voice_booking_status ON voice_booking_request(status);
CREATE INDEX IF NOT EXISTS idx_voice_booking_created ON voice_booking_request(created_at DESC);

-- Analyze table for query optimization
ANALYZE voice_booking_request;
```

### Cost Optimization

Consider implementing:
1. **Whisper.cpp** - Local processing (no API costs)
2. **Caching** - Cache similar transcripts
3. **Audio compression** - Reduce file size before sending
4. **Batching** - Process multiple requests together

---

## 📞 Support & Rollback

### Rollback Plan

If voice booking causes issues:

```bash
# 1. Disable feature immediately
export WHISPER_ENABLED=false

# 2. Restart application
systemctl restart house-keeping-service

# 3. Verify disabled
curl http://localhost:8080/api/v1/customer/bookings/voice/status
# Should show: "voiceBookingEnabled": false
```

### Getting Help

- **Logs location**: `/var/log/house-keeping-service/`
- **OpenAI status**: https://status.openai.com/
- **Contact**: support@housekeeping.com

---

## ✅ Deployment Complete

After successful deployment:

1. ✅ Voice booking endpoint is live
2. ✅ Database table created
3. ✅ Whisper API integrated
4. ✅ Monitoring in place
5. ✅ Documentation updated

**Next Steps**:
- Monitor usage and costs
- Gather user feedback
- Implement improvements
- Add unit tests
- Consider offline Whisper.cpp

---

**Deployed by**: Backend Team  
**Deployment Date**: _____________  
**Environment**: _____________  
**Version**: 1.0
