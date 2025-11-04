# Test Data Reference - Realistic Data from Database
**Source**: `postgres_data/init_sql/99_seed_datas.sql`  
**Last Updated**: November 1, 2025

---

## Table of Contents
1. [User Accounts](#user-accounts)
2. [Customer Profiles](#customer-profiles)
3. [Employee Profiles](#employee-profiles)
4. [Admin Profiles](#admin-profiles)
5. [Addresses](#addresses)
6. [Services](#services)
7. [Promotions](#promotions)
8. [Existing Bookings](#existing-bookings)
9. [Service Options](#service-options)

---

## User Accounts

### Customer Accounts
| Username | Account ID | Phone | Status | Email | Full Name |
|----------|------------|-------|--------|-------|-----------|
| john_doe | a1000001-0000-0000-0000-000000000001 | 0901234567 | ACTIVE | john.doe@example.com | John Doe |
| mary_jones | a1000001-0000-0000-0000-000000000004 | 0909876543 | INACTIVE | mary.jones@example.com | Mary Jones |
| nguyenvana | a1000001-0000-0000-0000-000000000006 | 0987654321 | ACTIVE | nguyenvanan@gmail.com | Nguyễn Văn An |
| tranthib | a1000001-0000-0000-0000-000000000007 | 0976543210 | ACTIVE | tranthibich@gmail.com | Trần Thị Bích |
| levanc | a1000001-0000-0000-0000-000000000008 | 0965432109 | ACTIVE | levancuong@gmail.com | Lê Văn Cường |
| phamthid | a1000001-0000-0000-0000-000000000009 | 0954321098 | ACTIVE | phamthidung@gmail.com | Phạm Thị Dung |
| hoangvane | a1000001-0000-0000-0000-000000000010 | 0943210987 | ACTIVE | hoangvanem@gmail.com | Hoàng Văn Em |

**Password for all accounts**: `password` (hashed: `$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK`)

### Employee Accounts
| Username | Account ID | Phone | Full Name | Employee ID |
|----------|------------|-------|-----------|-------------|
| jane_smith | a1000001-0000-0000-0000-000000000002 | 0912345678 | Jane Smith | e1000001-0000-0000-0000-000000000001 |
| bob_wilson | a1000001-0000-0000-0000-000000000005 | 0923456789 | Bob Wilson | e1000001-0000-0000-0000-000000000002 |
| tranvanl | a1000001-0000-0000-0000-000000000016 | 0887224321 | Trần Văn Long | e1000001-0000-0000-0000-000000000003 |
| nguyenthim | a1000001-0000-0000-0000-000000000017 | 0876223210 | Nguyễn Thị Mai | e1000001-0000-0000-0000-000000000004 |
| levann | a1000001-0000-0000-0000-000000000018 | 0865222109 | Lê Văn Nam | e1000001-0000-0000-0000-000000000005 |

### Admin Accounts
| Username | Account ID | Full Name | Department |
|----------|------------|-----------|------------|
| admin_1 | a1000001-0000-0000-0000-000000000003 | Admin One | Management |

---

## Customer Profiles

| Customer ID | Account ID | Full Name | Email | Gender | Birthdate |
|-------------|------------|-----------|-------|--------|-----------|
| c1000001-0000-0000-0000-000000000001 | a1000001-0000-0000-0000-000000000001 | John Doe | john.doe@example.com | Male | 2003-09-10 |
| c1000001-0000-0000-0000-000000000002 | a1000001-0000-0000-0000-000000000004 | Mary Jones | mary.jones@example.com | Female | 2003-01-19 |
| c1000001-0000-0000-0000-000000000003 | a1000001-0000-0000-0000-000000000002 | Jane Smith Customer | jane.smith.customer@example.com | Female | 2003-04-14 |
| c1000001-0000-0000-0000-000000000004 | a1000001-0000-0000-0000-000000000006 | Nguyễn Văn An | nguyenvanan@gmail.com | Male | 1995-03-15 |
| c1000001-0000-0000-0000-000000000005 | a1000001-0000-0000-0000-000000000007 | Trần Thị Bích | tranthibich@gmail.com | Female | 1998-07-22 |
| c1000001-0000-0000-0000-000000000006 | a1000001-0000-0000-0000-000000000008 | Lê Văn Cường | levancuong@gmail.com | Male | 1992-11-08 |
| c1000001-0000-0000-0000-000000000007 | a1000001-0000-0000-0000-000000000009 | Phạm Thị Dung | phamthidung@gmail.com | Female | 1996-05-30 |

---

## Employee Profiles

| Employee ID | Full Name | Email | Skills | Bio | Hired Date |
|-------------|-----------|-------|--------|-----|------------|
| e1000001-0000-0000-0000-000000000001 | Jane Smith | jane.smith@example.com | Cleaning, Organizing | Có kinh nghiệm dọn dẹp nhà cửa và sắp xếp đồ đạc. | 2024-01-15 |
| e1000001-0000-0000-0000-000000000002 | Bob Wilson | bob.wilson@examplefieldset.com | Deep Cleaning, Laundry | Chuyên gia giặt ủi và làm sạch sâu. | 2023-06-20 |
| e1000001-0000-0000-0000-000000000003 | Trần Văn Long | tranvanlong@gmail.com | Vệ sinh tổng quát, Lau dọn | Nhiều năm kinh nghiệm vệ sinh nhà cửa, tỉ mỉ và cẩn thận. | 2023-03-10 |
| e1000001-0000-0000-0000-000000000004 | Nguyễn Thị Mai | nguyenthimai@gmail.com | Giặt ủi, Nấu ăn | Chuyên về công việc gia đình, giặt ủi và nấu ăn ngon. | 2023-05-15 |
| e1000001-0000-0000-0000-000000000005 | Lê Văn Nam | levannam@gmail.com | Vệ sinh máy lạnh, Sửa chữa nhỏ | Có kỹ năng kỹ thuật, chuyên vệ sinh và bảo trì máy lạnh. | 2022-08-20 |

---

## Addresses

| Address ID | Customer ID | Full Address | Ward | City | Is Default |
|------------|-------------|--------------|------|------|------------|
| adrs0001-0000-0000-0000-000000000001 | c1000001-0000-0000-0000-000000000001 | 123 Lê Trọng Tấn, Phường Tây Thạnh, Thành phố Hồ Chí Minh | Phường Tây Thạnh | Thành phố Hồ Chí Minh | Yes |
| adrs0001-0000-0000-0000-000000000002 | c1000001-0000-0000-0000-000000000002 | 456 Lê Lợi, Phường Bến Thành, Thành phố Hồ Chí Minh | Phường Bến Thành | Thành phố Hồ Chí Minh | Yes |
| adrs0001-0000-0000-0000-000000000003 | c1000001-0000-0000-0000-000000000003 | 104 Lê Lợi, Phường Bến Nghé, Thành phố Hồ Chí Minh | Phường Bến Nghé | Thành phố Hồ Chí Minh | Yes |
| adrs0001-0000-0000-0000-000000000009 | c1000001-0000-0000-0000-000000000004 | 45 Nguyễn Huệ, Phường Bến Nghé, Thành phố Hồ Chí Minh | Phường Bến Nghé | Thành phố Hồ Chí Minh | Yes |
| adrs0001-0000-0000-0000-000000000010 | c1000001-0000-0000-0000-000000000005 | 128 Trần Hưng Đạo, Phường Cầu Kho, Thành phố Hồ Chí Minh | Phường Cầu Kho | Thành phố Hồ Chí Minh | Yes |
| adrs0001-0000-0000-0000-000000000011 | c1000001-0000-0000-0000-000000000006 | 234 Võ Văn Tần, Phường Võ Thị Sáu, Thành phố Hồ Chí Minh | Phường Võ Thị Sáu | Thành phố Hồ Chí Minh | Yes |

---

## Services

| Service ID | Category | Service Name | Description | Base Price (VND) | Unit | Duration (hrs) | Staff | Status |
|------------|----------|--------------|-------------|------------------|------|----------------|-------|--------|
| 1 | Dọn dẹp nhà | Dọn dẹp theo giờ | Lau dọn, hút bụi, làm sạch các bề mặt cơ bản trong nhà | 50,000 | Giờ | 2.0 | 1 | ACTIVE |
| 2 | Dọn dẹp nhà | Tổng vệ sinh | Làm sạch sâu toàn diện, bao gồm các khu vực khó tiếp cận | 100,000 | Gói | 2.0 | 1 | ACTIVE |
| 3 | Dọn dẹp nhà | Vệ sinh Sofa - Nệm - Rèm | Giặt sạch và khử khuẩn bằng máy móc chuyên dụng | 300,000 | Gói | 3.0 | 1 | ACTIVE |
| 4 | Dọn dẹp nhà | Vệ sinh máy lạnh | Bảo trì, làm sạch dàn nóng và dàn lạnh | 150,000 | Máy | 1.0 | 1 | ACTIVE |
| 5 | Giặt ủi | Giặt sấy theo kg | Giặt và sấy khô quần áo thông thường | 30,000 | Kg | 24.0 | 1 | ACTIVE |
| 6 | Giặt ủi | Giặt hấp cao cấp | Giặt khô cho các loại vải cao cấp | 120,000 | Bộ | 48.0 | 1 | ACTIVE |
| 7 | Việc nhà khác | Nấu ăn gia đình | Đi chợ và chuẩn bị bữa ăn cho gia đình | 60,000 | Giờ | 2.5 | 1 | ACTIVE |
| 8 | Việc nhà khác | Đi chợ hộ | Mua sắm và giao hàng tận nơi | 40,000 | Lần | 1.0 | 1 | ACTIVE |

---

## Promotions

| Promo Code | Description | Discount Type | Discount Value | Max Discount | Start Date | End Date | Status |
|------------|-------------|---------------|----------------|--------------|------------|----------|--------|
| GIAM20K | Giảm giá 20,000đ cho mọi đơn hàng | FIXED_AMOUNT | 20,000 | NULL | 2025-08-01 | 2025-09-30 | ACTIVE |
| KHAITRUONG10 | Giảm 10% mừng khai trương | PERCENTAGE | 10 | 50,000 | 2025-08-01 | 2025-08-31 | ACTIVE |

---

## Existing Bookings

### Verified Bookings
| Booking ID | Code | Customer | Booking Time | Total Amount | Status | Is Verified |
|------------|------|----------|--------------|--------------|--------|-------------|
| b0000001-0000-0000-0000-000000000001 | BK000001 | John Doe | 2025-08-20 09:00:00 | 80,000 | COMPLETED | Yes |
| b0000001-0000-0000-0000-000000000002 | BK000002 | Jane Smith Customer | 2025-08-28 14:00:00 | 90,000 | CONFIRMED | Yes |

### Unverified Bookings (Pending Verification)
| Booking ID | Code | Customer | Booking Time | Total Amount | Status | Is Verified |
|------------|------|----------|--------------|--------------|--------|-------------|
| b0000001-0000-0000-0000-000000000003 | BK000003 | Nguyễn Văn An | 2025-11-01 08:00:00 | 500,000 | PENDING | No |
| b0000001-0000-0000-0000-000000000004 | BK000004 | Trần Thị Bích | 2025-11-02 10:00:00 | 300,000 | PENDING | No |
| b0000001-0000-0000-0000-000000000005 | BK000005 | Lê Văn Cường | 2025-11-03 14:00:00 | 350,000 | PENDING | No |
| b0000001-0000-0000-0000-000000000006 | BK000006 | Phạm Thị Dung | 2025-11-04 09:30:00 | 400,000 | PENDING | No |
| b0000001-0000-0000-0000-000000000007 | BK000007 | Hoàng Văn Em | 2025-11-05 11:00:00 | 320,000 | PENDING | No |

---

## Service Options

### Service 1: Dọn dẹp theo giờ
**Option 1**: Bạn có yêu cầu thêm công việc nào? (MULTIPLE_CHOICE_CHECKBOX)
- Choice 1: Giặt chăn ga (+30,000 VND, +0.5 hrs)
- Choice 2: Rửa chén (+15,000 VND, +0.5 hrs)
- Choice 3: Lau cửa kính (+40,000 VND, +1.0 hr)

### Service 2: Tổng vệ sinh
**Option 1**: Loại hình nhà ở? (SINGLE_CHOICE_RADIO)
- Choice 1: Căn hộ
- Choice 2: Nhà phố

**Option 2**: Nhà bạn có mấy tầng (bao gồm trệt)? (QUANTITY_INPUT)
- Parent: Choice 2 (Nhà phố)

**Option 3**: Diện tích dọn dẹp? (SINGLE_CHOICE_DROPDOWN)
- Choice 1: Dưới 80m²
- Choice 2: Trên 80m²

**Pricing Rule**: Phụ thu nhà phố lớn (+250,000 VND, +1 staff, +2.0 hrs)
- Conditions: Nhà phố AND Trên 80m²

### Service 3: Vệ sinh Sofa - Nệm - Rèm
**Option 1**: Hạng mục cần vệ sinh? (SINGLE_CHOICE_RADIO)
- Choice 1: Sofa (base price)
- Choice 2: Nệm (+150,000 VND, +1.0 hr)
- Choice 3: Rèm (+100,000 VND, +1.0 hr)

### Service 4: Vệ sinh máy lạnh
**Option 1**: Loại máy lạnh? (SINGLE_CHOICE_DROPDOWN)
- Choice 1: Treo tường (base price)
- Choice 2: Âm trần/Cassette (+50,000 VND, +0.5 hrs)
- Choice 3: Tủ đứng (+50,000 VND, +0.5 hrs)

**Option 2**: Số lượng máy? (QUANTITY_INPUT)

### Service 5: Giặt sấy theo kg
**Option 1**: Có cần gấp quần áo sau khi giặt? (SINGLE_CHOICE_RADIO)
- Choice 1: Có (+10,000 VND, +1.0 hr)
- Choice 2: Không (base price)

### Service 6: Giặt hấp cao cấp
**Option 1**: Loại trang phục giặt hấp? (SINGLE_CHOICE_DROPDOWN)
- Choice 1: Vest
- Choice 2: Áo dài
- Choice 3: Đầm

### Service 7: Nấu ăn gia đình
**Option 1**: Số người ăn? (QUANTITY_INPUT)

---

## Sample Test Scenarios

### Scenario 1: Create Booking - Simple Service
**Customer**: john_doe (John Doe)  
**Service**: Dọn dẹp theo giờ (50,000 VND/hr)  
**Address**: 123 Lê Trọng Tấn, Phường Tây Thạnh  
**Quantity**: 2 hours  
**Total**: 100,000 VND

### Scenario 2: Create Booking - With Options
**Customer**: nguyenvana (Nguyễn Văn An)  
**Service**: Dọn dẹp theo giờ  
**Options**: Giặt chăn ga, Rửa chén  
**Base Price**: 50,000 VND  
**Option Adjustments**: +30,000 + 15,000 = 45,000 VND  
**Total**: 95,000 VND

### Scenario 3: Create Booking - Complex Service with Image
**Customer**: tranthib (Trần Thị Bích)  
**Service**: Tổng vệ sinh  
**Options**: Nhà phố, Trên 80m²  
**Base Price**: 100,000 VND  
**Pricing Rule Applied**: +250,000 VND (Phụ thu nhà phố lớn)  
**Image**: room_before.jpg (2.3 MB, image/jpeg)  
**Total**: 350,000 VND

### Scenario 4: Create Booking - With Promotion
**Customer**: levanc (Lê Văn Cường)  
**Service**: Vệ sinh Sofa - Nệm - Rèm (300,000 VND)  
**Promo Code**: GIAM20K  
**Discount**: -20,000 VND  
**Total**: 280,000 VND

### Scenario 5: Admin Verify Booking
**Admin**: admin_1  
**Booking**: BK000003 (Nguyễn Văn An)  
**Action**: Approve  
**Result**: Status changes from PENDING to AWAITING_EMPLOYEE, isVerified = true

### Scenario 6: Admin Reject Booking
**Admin**: admin_1  
**Booking**: BK000004 (Trần Thị Bích)  
**Action**: Reject  
**Reason**: "Thông tin dịch vụ không rõ ràng"  
**Result**: Status changes to CANCELLED, isVerified remains false

---

## Notes for Test Cases

### Authentication Tokens
- **Customer Token**: Use john_doe, nguyenvana, tranthib credentials
- **Employee Token**: Use jane_smith, tranvanl credentials  
- **Admin Token**: Use admin_1 credentials

### Image Upload Constraints
- **Allowed Types**: image/jpeg, image/png
- **Max Size**: 5 MB
- **Cloudinary URL Format**: `https://res.cloudinary.com/dkzemgit8/image/upload/v{timestamp}/{public_id}.{format}`

### Booking Time Constraints
- Must be in the future relative to current date (2025-11-01)
- Must be in format: ISO 8601 with timezone (e.g., 2025-11-05T09:00:00+07:00)

### Address Constraints
- Must provide either `addressId` (existing address) OR `newAddress` (new address data)
- Cannot provide both

### Service Calculation Logic
- **Base Total** = service.base_price × quantity
- **Option Adjustments** = sum of all selected option price adjustments
- **Promotion Discount** = apply promo code discount
- **Final Total** = Base Total + Option Adjustments - Promotion Discount

---

## Quick Reference IDs

### Common Test Customers
```
John Doe:      c1000001-0000-0000-0000-000000000001
Nguyễn Văn An: c1000001-0000-0000-0000-000000000004
Trần Thị Bích: c1000001-0000-0000-0000-000000000005
```

### Common Test Services
```
Dọn dẹp theo giờ:           1
Tổng vệ sinh:               2
Vệ sinh Sofa - Nệm - Rèm:   3
Vệ sinh máy lạnh:           4
```

### Common Test Addresses
```
123 Lê Trọng Tấn:  adrs0001-0000-0000-0000-000000000001
45 Nguyễn Huệ:     adrs0001-0000-0000-0000-000000000009
128 Trần Hưng Đạo: adrs0001-0000-0000-0000-000000000010
```

### Common Test Employees
```
Jane Smith:     e1000001-0000-0000-0000-000000000001
Trần Văn Long:  e1000001-0000-0000-0000-000000000003
```

### Unverified Bookings for Admin Testing
```
BK000003, BK000004, BK000005, BK000006, BK000007
```

---

## 🔌 WebSocket Chat Testing

### Fix Information

**Status:** ✅ Fixed on 03/11/2025

**Issue:** WebSocket chat tự động disconnect sau khi gửi tin nhắn hoặc sau thời gian ngắn idle

**Solution:** Đã thêm heartbeat configuration và timeout settings vào `WebSocketConfig.java`

### WebSocket Configuration

**Endpoint:** `/ws/chat` (with SockJS)  
**Protocol:** STOMP over SockJS  
**Heartbeat Interval:** 10 seconds (Simple Broker), 25 seconds (SockJS)  
**Message Size Limit:** 128 KB  
**Buffer Size:** 512 KB  

### Test Conversations

Để test WebSocket chat, bạn cần tạo conversations giữa customer và employee. Sử dụng API:

```bash
POST /api/v1/conversations
Headers:
  Authorization: Bearer YOUR_JWT_TOKEN
  Content-Type: application/json

Body:
{
  "customerId": "c1000001-0000-0000-0000-000000000001",
  "employeeId": "e1000001-0000-0000-0000-000000000001"
}
```

### WebSocket Test Tools

1. **Disconnect Monitor Tool:** `docs/websocket_disconnect_test.html`
   - Monitor connection uptime
   - Track disconnect events
   - Heartbeat verification

2. **Full Feature Test Tool:** `docs/websocket_realtime_test.html`
   - Send/receive text messages
   - Upload and send images
   - Load conversation history

### Test Accounts for Chat

**Customers:**
```
john_doe (a1000001-0000-0000-0000-000000000001)
nguyenvana (a1000001-0000-0000-0000-000000000006)
tranthib (a1000001-0000-0000-0000-000000000007)
```

**Employees:**
```
jane_smith (a1000001-0000-0000-0000-000000000002)
tranvanl (a1000001-0000-0000-0000-000000000016)
nguyenthim (a1000001-0000-0000-0000-000000000017)
```

**All passwords:** `123456`

### Quick WebSocket Test

```bash
# 1. Get JWT Token
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"john_doe","password":"123456"}'

# 2. Create Conversation
curl -X POST http://localhost:8080/api/v1/conversations \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"customerId":"c1000001-0000-0000-0000-000000000001","employeeId":"e1000001-0000-0000-0000-000000000001"}'

# 3. Open test tool and use the conversation ID
# File: docs/websocket_disconnect_test.html
```

### Expected Behavior (After Fix)

✅ **Connection stays alive** > 1 hour without disconnect  
✅ **Can send/receive** > 100 messages continuously  
✅ **Heartbeat working** - PING/PONG every 10 seconds  
✅ **No unexpected disconnects** - Disconnect count = 0  
✅ **Stable during idle** - No disconnect when no messages  

### Documentation

- **Fix Summary:** `docs/01_11_2025/fix/WEBSOCKET_DISCONNECT_FIX_SUMMARY.md`
- **Test Guide:** `docs/01_11_2025/fix/WEBSOCKET_DISCONNECT_TEST_GUIDE.md`
- **Analysis:** `docs/WEBSOCKET_DISCONNECT_ANALYSIS.md`
- **Quick Reference:** `docs/01_11_2025/fix/QUICK_REFERENCE.md`

---

**Last Updated:** November 3, 2025
