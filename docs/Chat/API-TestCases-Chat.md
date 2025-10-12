# API Test Cases - Chat Management

## Overview
This document describes the comprehensive test cases for the **Chat Management** endpoints of the Chat API.  
The endpoints allow users to manage conversations and messages including creation, retrieval, sending messages, and conversation management with robust validation and error handling.  
**Base URL**: `/api/v1/chat`

---

## Test Case Structure
Each test case includes:
- **Test Case ID**: Unique identifier for the test case.
- **Description**: Purpose of the test.
- **Preconditions**: Requirements before executing the test.
- **Input**: Request data or headers.
- **Expected Output**: Expected response based on the API specification.
- **Status Code**: HTTP status code expected.

---

## Authentication Requirements
All endpoints require:
- **Authorization Header**: `Bearer <valid_token>`
- **Content-Type**: `application/json` (for POST requests)
- **Role Requirements**: 
  - Chat endpoints: CUSTOMER, EMPLOYEE, or ADMIN role required
  - Admin endpoints: ADMIN role required

---

## Test Cases

### Test Case 1: Create New Conversation (Customer-Employee)
- **Test Case ID**: TC_CHAT_001
- **Description**: Verify that a customer can create a new conversation with an employee.
- **Preconditions**:
  - Customer is authenticated with valid token
  - Employee exists in the system
  - No existing conversation between customer and employee
- **Input**:
  ```json
  {
    "participantId": "employee-uuid-here",
    "participantType": "EMPLOYEE",
    "conversationType": "CUSTOMER_EMPLOYEE"
  }
  ```
- **Expected Output**:
  ```json
  {
    "success": true,
    "message": "Cuộc hội thoại đã được tạo thành công",
    "data": {
      "conversationId": "conv-uuid-here",
      "participants": [
        {
          "userId": "customer-uuid-here",
          "userType": "CUSTOMER",
          "name": "John Doe",
          "avatar": "avatar-url"
        },
        {
          "userId": "employee-uuid-here",
          "userType": "EMPLOYEE",
          "name": "Jane Smith",
          "avatar": "avatar-url"
        }
      ],
      "conversationType": "CUSTOMER_EMPLOYEE",
      "createdAt": "2025-10-11T10:30:00Z",
      "lastMessage": null
    }
  }
  ```
- **Status Code**: `201 Created`

### Test Case 2: Get User Conversations
- **Test Case ID**: TC_CHAT_002
- **Description**: Verify that a user can retrieve all their conversations with pagination.
- **Preconditions**:
  - User is authenticated with valid token
  - User has existing conversations
- **Input**:
  ```
  GET /api/v1/chat/conversations?page=0&size=10&sort=lastMessageAt,desc
  ```
- **Expected Output**:
  ```json
  {
    "success": true,
    "message": "Danh sách cuộc hội thoại",
    "data": {
      "content": [
        {
          "conversationId": "conv-uuid-1",
          "participants": [
            {
              "userId": "other-user-uuid",
              "userType": "EMPLOYEE",
              "name": "Jane Smith",
              "avatar": "avatar-url"
            }
          ],
          "conversationType": "CUSTOMER_EMPLOYEE",
          "lastMessage": {
            "messageId": "msg-uuid-1",
            "content": "Xin chào, tôi cần hỗ trợ",
            "messageType": "TEXT",
            "senderId": "customer-uuid",
            "senderName": "John Doe",
            "sentAt": "2025-10-11T10:30:00Z",
            "isRead": true
          },
          "unreadCount": 0,
          "lastMessageAt": "2025-10-11T10:30:00Z",
          "createdAt": "2025-10-11T09:00:00Z"
        }
      ],
      "totalElements": 5,
      "totalPages": 1,
      "currentPage": 0,
      "pageSize": 10
    }
  }
  ```
- **Status Code**: `200 OK`

### Test Case 3: Send Text Message
- **Test Case ID**: TC_CHAT_003
- **Description**: Verify that a user can send a text message in a conversation.
- **Preconditions**:
  - User is authenticated and is participant in the conversation
  - Conversation exists and is active
- **Input**:
  ```json
  {
    "conversationId": "conv-uuid-here",
    "content": "Xin chào, tôi cần hỗ trợ về dịch vụ dọn dẹp",
    "messageType": "TEXT"
  }
  ```
- **Expected Output**:
  ```json
  {
    "success": true,
    "message": "Tin nhắn đã được gửi thành công",
    "data": {
      "messageId": "msg-uuid-here",
      "conversationId": "conv-uuid-here",
      "content": "Xin chào, tôi cần hỗ trợ về dịch vụ dọn dẹp",
      "messageType": "TEXT",
      "senderId": "user-uuid-here",
      "senderName": "John Doe",
      "senderAvatar": "avatar-url",
      "sentAt": "2025-10-11T10:30:00Z",
      "isEdited": false,
      "replyToMessageId": null
    }
  }
  ```
- **Status Code**: `201 Created`

### Test Case 4: Send Image Message
- **Test Case ID**: TC_CHAT_004
- **Description**: Verify that a user can send an image message in a conversation.
- **Preconditions**:
  - User is authenticated and is participant in the conversation
  - Conversation exists and is active
  - Valid image file is provided
- **Input**:
  ```json
  {
    "conversationId": "conv-uuid-here",
    "content": "https://example.com/images/problem.jpg",
    "messageType": "IMAGE",
    "metadata": {
      "fileName": "problem.jpg",
      "fileSize": 1024000,
      "mimeType": "image/jpeg"
    }
  }
  ```
- **Expected Output**:
  ```json
  {
    "success": true,
    "message": "Hình ảnh đã được gửi thành công",
    "data": {
      "messageId": "msg-uuid-here",
      "conversationId": "conv-uuid-here",
      "content": "https://example.com/images/problem.jpg",
      "messageType": "IMAGE",
      "senderId": "user-uuid-here",
      "senderName": "John Doe",
      "senderAvatar": "avatar-url",
      "sentAt": "2025-10-11T10:30:00Z",
      "metadata": {
        "fileName": "problem.jpg",
        "fileSize": 1024000,
        "mimeType": "image/jpeg",
        "thumbnailUrl": "https://example.com/thumbnails/problem_thumb.jpg"
      }
    }
  }
  ```
- **Status Code**: `201 Created`

### Test Case 5: Get Conversation Messages
- **Test Case ID**: TC_CHAT_005
- **Description**: Verify that a user can retrieve messages from a conversation with pagination.
- **Preconditions**:
  - User is authenticated and is participant in the conversation
  - Conversation exists with messages
- **Input**:
  ```
  GET /api/v1/chat/conversations/{conversationId}/messages?page=0&size=20&sort=sentAt,desc
  ```
- **Expected Output**:
  ```json
  {
    "success": true,
    "message": "Danh sách tin nhắn",
    "data": {
      "content": [
        {
          "messageId": "msg-uuid-2",
          "content": "Cảm ơn bạn, chúng tôi sẽ hỗ trợ bạn ngay",
          "messageType": "TEXT",
          "senderId": "employee-uuid",
          "senderName": "Jane Smith",
          "senderAvatar": "avatar-url",
          "sentAt": "2025-10-11T10:35:00Z",
          "isEdited": false,
          "replyToMessageId": "msg-uuid-1"
        },
        {
          "messageId": "msg-uuid-1",
          "content": "Xin chào, tôi cần hỗ trợ về dịch vụ dọn dẹp",
          "messageType": "TEXT",
          "senderId": "customer-uuid",
          "senderName": "John Doe",
          "senderAvatar": "avatar-url",
          "sentAt": "2025-10-11T10:30:00Z",
          "isEdited": false,
          "replyToMessageId": null
        }
      ],
      "totalElements": 2,
      "totalPages": 1,
      "currentPage": 0,
      "pageSize": 20
    }
  }
  ```
- **Status Code**: `200 OK`

### Test Case 6: Mark Messages as Read
- **Test Case ID**: TC_CHAT_006
- **Description**: Verify that a user can mark messages as read in a conversation.
- **Preconditions**:
  - User is authenticated and is participant in the conversation
  - Conversation has unread messages
- **Input**:
  ```json
  {
    "conversationId": "conv-uuid-here",
    "lastReadMessageId": "msg-uuid-here"
  }
  ```
- **Expected Output**:
  ```json
  {
    "success": true,
    "message": "Tin nhắn đã được đánh dấu là đã đọc",
    "data": {
      "conversationId": "conv-uuid-here",
      "unreadCount": 0,
      "lastReadMessageId": "msg-uuid-here",
      "lastReadAt": "2025-10-11T10:40:00Z"
    }
  }
  ```
- **Status Code**: `200 OK`

### Test Case 7: Reply to Message
- **Test Case ID**: TC_CHAT_007
- **Description**: Verify that a user can reply to a specific message in a conversation.
- **Preconditions**:
  - User is authenticated and is participant in the conversation
  - Original message exists
- **Input**:
  ```json
  {
    "conversationId": "conv-uuid-here",
    "content": "Tôi cần dịch vụ dọn dẹp nhà cửa vào cuối tuần",
    "messageType": "TEXT",
    "replyToMessageId": "msg-uuid-original"
  }
  ```
- **Expected Output**:
  ```json
  {
    "success": true,
    "message": "Phản hồi đã được gửi thành công",
    "data": {
      "messageId": "msg-uuid-reply",
      "conversationId": "conv-uuid-here",
      "content": "Tôi cần dịch vụ dọn dẹp nhà cửa vào cuối tuần",
      "messageType": "TEXT",
      "senderId": "user-uuid-here",
      "senderName": "John Doe",
      "sentAt": "2025-10-11T10:45:00Z",
      "replyToMessageId": "msg-uuid-original",
      "replyToMessage": {
        "messageId": "msg-uuid-original",
        "content": "Bạn cần dịch vụ gì?",
        "senderName": "Jane Smith",
        "sentAt": "2025-10-11T10:30:00Z"
      }
    }
  }
  ```
- **Status Code**: `201 Created`

### Test Case 8: Search Messages
- **Test Case ID**: TC_CHAT_008
- **Description**: Verify that a user can search for messages within their conversations.
- **Preconditions**:
  - User is authenticated
  - User has conversations with messages
- **Input**:
  ```
  GET /api/v1/chat/messages/search?query=dọn dẹp&page=0&size=10
  ```
- **Expected Output**:
  ```json
  {
    "success": true,
    "message": "Kết quả tìm kiếm tin nhắn",
    "data": {
      "content": [
        {
          "messageId": "msg-uuid-1",
          "conversationId": "conv-uuid-1",
          "content": "Tôi cần hỗ trợ về dịch vụ **dọn dẹp**",
          "messageType": "TEXT",
          "senderId": "customer-uuid",
          "senderName": "John Doe",
          "sentAt": "2025-10-11T10:30:00Z",
          "conversationInfo": {
            "otherParticipant": {
              "name": "Jane Smith",
              "userType": "EMPLOYEE"
            }
          }
        }
      ],
      "totalElements": 1,
      "totalPages": 1,
      "currentPage": 0,
      "pageSize": 10
    }
  }
  ```
- **Status Code**: `200 OK`

---

## Error Test Cases

### Test Case 9: Send Message to Non-existent Conversation
- **Test Case ID**: TC_CHAT_ERROR_001
- **Description**: Verify proper error handling when sending message to non-existent conversation.
- **Input**:
  ```json
  {
    "conversationId": "non-existent-uuid",
    "content": "Test message",
    "messageType": "TEXT"
  }
  ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Cuộc hội thoại không tồn tại",
    "errorCode": "CONVERSATION_NOT_FOUND"
  }
  ```
- **Status Code**: `404 Not Found`

### Test Case 10: Unauthorized Access to Conversation
- **Test Case ID**: TC_CHAT_ERROR_002
- **Description**: Verify proper error handling when user tries to access conversation they're not part of.
- **Input**:
  ```
  GET /api/v1/chat/conversations/{other-user-conversation-id}/messages
  ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Bạn không có quyền truy cập cuộc hội thoại này",
    "errorCode": "CONVERSATION_ACCESS_DENIED"
  }
  ```
- **Status Code**: `403 Forbidden`

### Test Case 11: Send Empty Message
- **Test Case ID**: TC_CHAT_ERROR_003
- **Description**: Verify proper validation for empty message content.
- **Input**:
  ```json
  {
    "conversationId": "conv-uuid-here",
    "content": "",
    "messageType": "TEXT"
  }
  ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Nội dung tin nhắn không được để trống",
    "errorCode": "INVALID_MESSAGE_CONTENT"
  }
  ```
- **Status Code**: `400 Bad Request`

---

## Notes
- All timestamps are in ISO 8601 format (UTC)
- Message content supports basic text and media URLs
- File uploads should be handled through separate file upload endpoints
- Conversation types: CUSTOMER_EMPLOYEE, CUSTOMER_ADMIN, EMPLOYEE_ADMIN
- Message types: TEXT, IMAGE, FILE, SYSTEM, BOOKING_REFERENCE
- Maximum message length: 2000 characters
- Supported image formats: JPG, PNG, GIF (max 10MB)
- Real-time notifications are handled via WebSocket (see real-time documentation)
