# API Test Cases - Chat Module

## Overview
This document lists comprehensive manual test cases for the **Chat REST** and **Chat WebSocket** controllers. They allow customers and employees to fetch conversation history over HTTP and exchange messages in real time via STOMP over WebSocket.

**Base REST URL**: `/api/v1/chat`
**WebSocket STOMP Endpoint**: `/ws`

---

## Test Case Structure
Each test case includes:
- **Test Case ID**: Unique identifier.
- **Description**: Purpose of the test.
- **Preconditions**: Required data or authentication state before executing the test.
- **Input**: Request payload, headers, and parameters.
- **Expected Output**: Server response or observable behavior.
- **Status Code**: Expected HTTP response code (for REST cases) or STOMP frame status (for WebSocket cases).

---

## Authentication Requirements
- **REST Endpoints**: Require a valid JWT bearer token that resolves to an existing account. Customers and employees can only access conversations they participate in.
- **WebSocket**: Clients must complete the JWT-protected handshake (`Authorization: Bearer <token>`) and subscribe to `/topic/conversations/{conversationId}` they are members of.

---

## Seed Data References
Based on `06_seed_data.sql` and `07_chat.sql`:
- **Customer Account**: `john_doe` – `a1000001-0000-0000-0000-000000000001` (customer id `c1000001-...0001`).
- **Employee Account**: `emily_nguyen` – `a2000001-0000-0000-0000-000000000001` (employee id `e2000001-...0001`).
- **Sample Conversation**: Create manually between `john_doe` and `emily_nguyen` (e.g. `conversation_id = conv-chat-001`).
- **Sample Messages**: Insert at least one text and one file message per conversation for pagination and revocation tests.

---

## REST Endpoints Covered
1. **GET /conversations** – Fetch the authenticated user’s conversations.
2. **GET /conversations/{conversationId}/messages** – Fetch paginated messages within a conversation.

### Test Case R1: List Conversations Successfully
- **Test Case ID**: TC_CHAT_REST_001
- **Description**: Verify that an authenticated user receives their active conversations.
- **Preconditions**:
    - `john_doe` is logged in and participates in at least one conversation.
    - Conversation `conv-chat-001` exists with `last_message_at` populated.
- **Input**:
    - **Method**: `GET`
    - **URL**: `/api/v1/chat/conversations`
    - **Headers**:
      ```
      Authorization: Bearer <valid_customer_token_john_doe>
      ```
- **Expected Output**:
  ```json
  [
    {
      "conversationId": "conv-chat-001",
      "employee": {
        "employeeId": "e2000001-0000-0000-0000-000000000001",
        "fullName": "Emily Nguyen",
        "avatarUrl": "https://picsum.photos/200"
      },
      "customer": {
        "customerId": "c1000001-0000-0000-0000-000000000001",
        "fullName": "John Doe",
        "avatarUrl": "https://picsum.photos/200"
      },
      "lastMessagePreview": "Xin chào, tôi sẽ đến lúc 9h sáng.",
      "lastMessageAt": "2024-09-30T02:15:30Z",
      "unreadCount": 0
    }
  ]
  ```
- **Status Code**: `200 OK`

### Test Case R2: List Conversations Unauthorized
- **Test Case ID**: TC_CHAT_REST_002
- **Description**: Requests without authentication should be rejected.
- **Preconditions**: None.
- **Input**:
    - **Method**: `GET`
    - **URL**: `/api/v1/chat/conversations`
    - **Headers**: None
- **Expected Output**:
  ```json
  {
    "status": 401,
    "error": "Unauthorized",
    "message": "Full authentication is required to access this resource"
  }
  ```
- **Status Code**: `401 Unauthorized`

### Test Case R3: Paginate Conversation Messages (Default Page)
- **Test Case ID**: TC_CHAT_REST_003
- **Description**: Verify the default pagination (page 0, size 20) returns the newest messages first.
- **Preconditions**:
    - Conversation `conv-chat-001` contains at least 5 messages sorted by `sent_at`.
- **Input**:
    - **Method**: `GET`
    - **URL**: `/api/v1/chat/conversations/conv-chat-001/messages`
    - **Headers**:
      ```
      Authorization: Bearer <valid_customer_token_john_doe>
      ```
- **Expected Output**:
  ```json
  {
    "page": 0,
    "size": 20,
    "totalElements": 5,
    "totalPages": 1,
    "content": [
      {
        "messageId": "msg-005",
        "conversationId": "conv-chat-001",
        "senderAccountId": "a2000001-0000-0000-0000-000000000001",
        "messageType": "TEXT",
        "content": "Xin chào, tôi sẽ đến lúc 9h sáng.",
        "isRevoked": false,
        "sentAt": "2024-09-30T02:15:30Z"
      },
      {
        "messageId": "msg-004",
        "messageType": "FILE",
        "fileUrl": "https://cdn.example.com/work-order.pdf",
        "fileName": "work-order.pdf",
        "fileSize": 532481,
        "isRevoked": false,
        "sentAt": "2024-09-30T02:05:00Z"
      }
    ]
  }
  ```
- **Status Code**: `200 OK`

### Test Case R4: Paginate Conversation Messages (Custom Page & Size)
- **Test Case ID**: TC_CHAT_REST_004
- **Description**: Fetch an older page by setting `page` and `size` query parameters.
- **Preconditions**:
    - Conversation `conv-chat-001` contains ≥ 25 messages.
- **Input**:
    - **Method**: `GET`
    - **URL**: `/api/v1/chat/conversations/conv-chat-001/messages?page=1&size=10`
    - **Headers**:
      ```
      Authorization: Bearer <valid_employee_token_emily_nguyen>
      ```
- **Expected Output**:
  ```json
  {
    "page": 1,
    "size": 10,
    "totalElements": 25,
    "totalPages": 3,
    "content": [
      {
        "messageId": "msg-015",
        "messageType": "TEXT",
        "content": "Khách hàng đã xác nhận.",
        "isRevoked": false,
        "sentAt": "2024-09-28T08:45:00Z"
      }
    ]
  }
  ```
- **Status Code**: `200 OK`

### Test Case R5: Access Messages Without Membership
- **Test Case ID**: TC_CHAT_REST_005
- **Description**: Verify that users who are not participants cannot view messages.
- **Preconditions**:
    - Conversation `conv-chat-001` exists.
    - Account `other_employee` is authenticated but not part of the conversation.
- **Input**:
    - **Method**: `GET`
    - **URL**: `/api/v1/chat/conversations/conv-chat-001/messages`
    - **Headers**:
      ```
      Authorization: Bearer <valid_token_other_employee>
      ```
- **Expected Output**:
  ```json
  {
    "status": 403,
    "error": "Forbidden",
    "message": "Người dùng không có quyền truy cập cuộc hội thoại này"
  }
  ```
- **Status Code**: `403 Forbidden`

---

## WebSocket Message Mappings Covered
1. **/app/chat/send** – Send a new message to a conversation.
2. **/app/chat/recall** – Recall (soft delete) a previously sent message.

### Test Case W1: Send Text Message Successfully
- **Test Case ID**: TC_CHAT_WS_001
- **Description**: Ensure authenticated clients can send text messages and receive broadcast updates.
- **Preconditions**:
    - `john_doe` is connected to `/ws` with a valid token and subscribed to `/topic/conversations/conv-chat-001`.
- **Input**:
    - **STOMP SEND Destination**: `/app/chat/send`
    - **Frame Body**:
      ```json
      {
        "conversationId": "conv-chat-001",
        "messageType": "TEXT",
        "content": "Xin chào, tôi có thể xác nhận lại lịch hẹn không?"
      }
      ```
- **Expected Output**:
    - Subscription receives a `MESSAGE` frame on `/topic/conversations/conv-chat-001` with payload:
      ```json
      {
        "messageId": "msg-101",
        "conversationId": "conv-chat-001",
        "senderAccountId": "a1000001-0000-0000-0000-000000000001",
        "messageType": "TEXT",
        "content": "Xin chào, tôi có thể xác nhận lại lịch hẹn không?",
        "isRevoked": false,
        "sentAt": "2024-10-01T03:00:00Z"
      }
      ```
- **Status Code**: STOMP `MESSAGE` frame delivered.

### Test Case W2: Send File Message with Metadata
- **Test Case ID**: TC_CHAT_WS_002
- **Description**: Verify file messages propagate file metadata.
- **Preconditions**:
    - `emily_nguyen` is connected and subscribed to the conversation topic.
- **Input**:
    - **STOMP SEND Destination**: `/app/chat/send`
    - **Frame Body**:
      ```json
      {
        "conversationId": "conv-chat-001",
        "messageType": "FILE",
        "fileUrl": "https://cdn.example.com/checklist.pdf",
        "fileName": "checklist.pdf",
        "fileSize": 24576
      }
      ```
- **Expected Output**:
    - `/topic/conversations/conv-chat-001` receives payload with the same metadata and `isRevoked = false`.
- **Status Code**: STOMP `MESSAGE` frame delivered.

### Test Case W3: Recall Message Successfully
- **Test Case ID**: TC_CHAT_WS_003
- **Description**: Ensure the sender can recall a previously sent message and subscribers get the updated flag.
- **Preconditions**:
    - Message `msg-101` belongs to `john_doe` within `conv-chat-001`.
- **Input**:
    - **STOMP SEND Destination**: `/app/chat/recall`
    - **Frame Body**:
      ```json
      {
        "messageId": "msg-101"
      }
      ```
- **Expected Output**:
  ```json
  {
    "messageId": "msg-101",
    "conversationId": "conv-chat-001",
    "senderAccountId": "a1000001-0000-0000-0000-000000000001",
    "messageType": "TEXT",
    "content": null,
    "isRevoked": true,
    "revokedAt": "2024-10-01T03:05:00Z"
  }
  ```
- **Status Code**: STOMP `MESSAGE` frame delivered.

### Test Case W4: Recall Message Without Ownership
- **Test Case ID**: TC_CHAT_WS_004
- **Description**: Users cannot revoke messages sent by other accounts.
- **Preconditions**:
    - Message `msg-104` was sent by `emily_nguyen`.
    - `john_doe` attempts to recall it.
- **Input**:
    - **Destination**: `/app/chat/recall`
    - **Frame Body**:
      ```json
      {
        "messageId": "msg-104"
      }
      ```
- **Expected Output**:
    - Client receives STOMP `ERROR` frame with message `"Bạn không thể thu hồi tin nhắn này"`.
- **Status Code**: STOMP `ERROR`

### Test Case W5: Send Message Without Authentication
- **Test Case ID**: TC_CHAT_WS_005
- **Description**: Anonymous sessions are rejected during handshake.
- **Preconditions**: Client omits the `Authorization` header during the `/ws` handshake.
- **Input**:
    - **Handshake Request Headers**: none
- **Expected Output**:
    - Server terminates the handshake with HTTP `401` before STOMP session is established.
- **Status Code**: `401 Unauthorized`

---

## Notes
- Ensure the WebSocket client reconnects after token refresh to maintain session integrity.
- Verify revoked messages remain in the database with `is_revoked = true` and that REST responses mask `content` for revoked entries.
- When simulating file uploads, ensure URLs are accessible or mocked through a storage emulator.