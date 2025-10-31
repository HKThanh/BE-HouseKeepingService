# Chat Real-time Feature

## Mô tả

Chức năng chat real-time cho phép khách hàng và nhân viên giao tiếp với nhau trong thời gian thực. Hỗ trợ gửi tin nhắn dạng text và hình ảnh.

## Các thành phần chính

### 1. Models

#### Conversation
- `conversationId`: ID của cuộc trò chuyện
- `customer`: Khách hàng tham gia
- `employee`: Nhân viên tham gia (có thể null)
- `booking`: Booking liên quan (có thể null)
- `lastMessage`: Tin nhắn cuối cùng
- `lastMessageTime`: Thời gian tin nhắn cuối
- `isActive`: Trạng thái active
- `createdAt`, `updatedAt`: Thời gian tạo và cập nhật

#### ChatMessage
- `messageId`: ID của tin nhắn
- `conversation`: Cuộc trò chuyện
- `sender`: Người gửi (Account)
- `messageType`: Loại tin nhắn (TEXT hoặc IMAGE)
- `content`: Nội dung text
- `imageUrl`: URL hình ảnh (nếu là IMAGE)
- `isRead`: Trạng thái đã đọc
- `createdAt`: Thời gian tạo

### 2. API Endpoints

#### Conversations

**Tạo cuộc trò chuyện mới**
```
POST /api/v1/conversations
Body: {
  "customerId": "string",
  "employeeId": "string",  // optional
  "bookingId": "string"    // optional
}
```

**Lấy thông tin cuộc trò chuyện**
```
GET /api/v1/conversations/{conversationId}
```

**Lấy danh sách cuộc trò chuyện của user**
```
GET /api/v1/conversations/account/{accountId}?page=0&size=20
```

**Tìm hoặc tạo cuộc trò chuyện giữa customer và employee**
```
GET /api/v1/conversations/get-or-create?customerId={customerId}&employeeId={employeeId}
```

**Lấy cuộc trò chuyện theo booking**
```
GET /api/v1/conversations/booking/{bookingId}
```

**Xóa cuộc trò chuyện (soft delete)**
```
DELETE /api/v1/conversations/{conversationId}
```

#### Chat Messages

**Gửi tin nhắn text**
```
POST /api/v1/messages/send/text
Params:
  - conversationId: string
  - senderId: string
  - content: string
```

**Gửi tin nhắn hình ảnh**
```
POST /api/v1/messages/send/image
Form-data:
  - conversationId: string
  - senderId: string
  - imageFile: file
  - caption: string (optional)
```

**Lấy tin nhắn theo cuộc trò chuyện (phân trang)**
```
GET /api/v1/messages/conversation/{conversationId}?page=0&size=50
```

**Lấy tất cả tin nhắn theo cuộc trò chuyện**
```
GET /api/v1/messages/conversation/{conversationId}/all
```

**Đếm số tin nhắn chưa đọc**
```
GET /api/v1/messages/conversation/{conversationId}/unread-count?accountId={accountId}
```

**Đánh dấu tin nhắn đã đọc**
```
PUT /api/v1/messages/conversation/{conversationId}/mark-read?accountId={accountId}
```

### 3. WebSocket

#### Kết nối WebSocket

**Endpoint**: `/ws/chat`

#### Subscribe để nhận tin nhắn real-time

```javascript
// Subscribe to a specific conversation
stompClient.subscribe('/topic/conversation/{conversationId}', function(message) {
    const chatMessage = JSON.parse(message.body);
    // Handle received message
    console.log(chatMessage);
});
```

#### Gửi tin nhắn qua WebSocket

```javascript
stompClient.send("/app/chat.send", {}, JSON.stringify({
    messageId: "uuid",
    conversationId: "conversation-id",
    senderId: "sender-id",
    senderName: "Sender Name",
    messageType: "TEXT", // or "IMAGE"
    content: "Message content",
    imageUrl: null, // or image URL
    timestamp: "2025-10-31T10:00:00"
}));
```

## Ví dụ sử dụng với JavaScript Client

### 1. Kết nối WebSocket

```javascript
// Import SockJS and STOMP
const socket = new SockJS('http://localhost:8080/ws/chat');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    console.log('Connected: ' + frame);
    
    // Subscribe to conversation
    stompClient.subscribe('/topic/conversation/' + conversationId, function(message) {
        const chatMessage = JSON.parse(message.body);
        displayMessage(chatMessage);
    });
});
```

### 2. Gửi tin nhắn text qua REST API

```javascript
async function sendTextMessage(conversationId, senderId, content) {
    const formData = new URLSearchParams();
    formData.append('conversationId', conversationId);
    formData.append('senderId', senderId);
    formData.append('content', content);

    const response = await fetch('/api/v1/messages/send/text', {
        method: 'POST',
        headers: {
            'Authorization': 'Bearer ' + token,
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: formData
    });
    
    return await response.json();
}
```

### 3. Gửi tin nhắn hình ảnh qua REST API

```javascript
async function sendImageMessage(conversationId, senderId, imageFile) {
    const formData = new FormData();
    formData.append('conversationId', conversationId);
    formData.append('senderId', senderId);
    formData.append('imageFile', imageFile);

    const response = await fetch('/api/v1/messages/send/image', {
        method: 'POST',
        headers: {
            'Authorization': 'Bearer ' + token
        },
        body: formData
    });
    
    return await response.json();
}
```

### 4. Lấy lịch sử tin nhắn

```javascript
async function getMessages(conversationId, page = 0, size = 50) {
    const response = await fetch(
        `/api/v1/messages/conversation/${conversationId}?page=${page}&size=${size}`,
        {
            headers: {
                'Authorization': 'Bearer ' + token
            }
        }
    );
    
    return await response.json();
}
```

### 5. Đánh dấu tin nhắn đã đọc

```javascript
async function markAsRead(conversationId, accountId) {
    const response = await fetch(
        `/api/v1/messages/conversation/${conversationId}/mark-read?accountId=${accountId}`,
        {
            method: 'PUT',
            headers: {
                'Authorization': 'Bearer ' + token
            }
        }
    );
    
    return await response.json();
}
```

## Cấu hình

### Database Migration

File migration SQL đã được tạo tại: `src/main/resources/db/migration/V1__Create_Chat_Tables.sql`

Để chạy migration, sử dụng Flyway hoặc thực thi trực tiếp SQL script.

### Cloudinary Configuration

Thêm cấu hình sau vào environment variables:

```
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret
CLOUDINARY_CHAT_FOLDER=chat_images
```

## Security

- Tất cả các endpoint yêu cầu authentication (trừ WebSocket endpoint)
- Cần có role: CUSTOMER, EMPLOYEE hoặc ADMIN
- WebSocket endpoint `/ws/**` được cho phép public access

## Lưu ý

1. Tin nhắn được gửi qua REST API sẽ tự động broadcast qua WebSocket
2. Hình ảnh được upload lên Cloudinary và lưu URL trong database
3. Conversation có thể liên kết với Booking để chat về booking cụ thể
4. Hỗ trợ phân trang cho lịch sử tin nhắn
5. Có chức năng đếm tin nhắn chưa đọc và đánh dấu đã đọc
