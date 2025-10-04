# API Test Cases - Chat Message Management

## Overview
This document describes comprehensive test cases for the **Chat Message Management** endpoints using realistic data from the housekeeping service database.  
**Base URL**: `/api/v1/chat/messages`  
**Test Date Context**: October 3, 2025

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
- **All endpoints**: Authentication through account validation
- **Content-Type**: `application/json`
- **Participant Validation**: Users must be participants in the chat room

---

## API Endpoints Covered
1. **POST /{chatRoomId}** - Send Message
2. **POST /{messageId}/reply** - Reply to Message
3. **DELETE /{messageId}** - Delete Message
4. **POST /{messageId}/recall** - Recall Message

---

## POST /{chatRoomId} - Send Message

### Test Case 1: Successful Message Send
- **Test Case ID**: TC_CHAT_001
- **Description**: Verify that a participant can successfully send a message to a chat room
- **Preconditions**:
  - Chat room exists with ID 'cr000001-0000-0000-0000-000000000001'
  - Customer account 'a1000001-0000-0000-0000-000000000001' is a participant
  - Assignment-based chat room between customer and employee
- **Input**:
  - **Path Parameter**: chatRoomId = "cr000001-0000-0000-0000-000000000001"
  - **Headers**: 
    ```
    Content-Type: application/json
    ```
  - **Request Body**:
    ```json
    {
      "senderAccountId": "a1000001-0000-0000-0000-000000000001",
      "messageText": "Hello, I have a question about the upcoming cleaning service.",
      "payloadType": null,
      "payloadData": null,
      "parentMessageId": null
    }
    ```
- **Expected Output**:
  ```json
  {
    "success": true,
    "message": "Gửi tin nhắn thành công",
    "data": {
      "chatMessageId": "cm000001-0000-0000-0000-000000000001",
      "chatRoomId": "cr000001-0000-0000-0000-000000000001",
      "senderAccountId": "a1000001-0000-0000-0000-000000000001",
      "messageText": "Hello, I have a question about the upcoming cleaning service.",
      "payloadType": null,
      "payloadData": null,
      "sentAt": "2025-10-03T10:00:00+07:00",
      "readAt": null,
      "readByAccountId": null,
      "reply": null,
      "deletion": null,
      "recall": null
    }
  }
  ```
- **Status Code**: 201 Created

### Test Case 2: Send Message with Payload Data
- **Test Case ID**: TC_CHAT_002
- **Description**: Verify that a participant can send a message with payload data (file/image)
- **Preconditions**:
  - Chat room exists with valid participant
  - Employee account is sending with image payload
- **Input**:
  - **Path Parameter**: chatRoomId = "cr000001-0000-0000-0000-000000000001"
  - **Request Body**:
    ```json
    {
      "senderAccountId": "a1000001-0000-0000-0000-000000000002",
      "messageText": "Here's the before photo",
      "payloadType": "IMAGE",
      "payloadData": "https://cloudinary.com/image123.jpg",
      "parentMessageId": null
    }
    ```
- **Expected Output**:
  ```json
  {
    "success": true,
    "message": "Gửi tin nhắn thành công",
    "data": {
      "chatMessageId": "cm000001-0000-0000-0000-000000000002",
      "chatRoomId": "cr000001-0000-0000-0000-000000000001",
      "senderAccountId": "a1000001-0000-0000-0000-000000000002",
      "messageText": "Here's the before photo",
      "payloadType": "IMAGE",
      "payloadData": "https://cloudinary.com/image123.jpg",
      "sentAt": "2025-10-03T10:05:00+07:00",
      "readAt": null,
      "readByAccountId": null,
      "reply": null,
      "deletion": null,
      "recall": null
    }
  }
  ```
- **Status Code**: 201 Created

### Test Case 3: Chat Room Not Found
- **Test Case ID**: TC_CHAT_003
- **Description**: Verify proper error handling when chat room doesn't exist
- **Preconditions**:
  - Chat room ID "NONEXISTENT" does not exist
  - Valid account attempting to send message
- **Input**:
  - **Path Parameter**: chatRoomId = "NONEXISTENT"
  - **Request Body**:
    ```json
    {
      "senderAccountId": "a1000001-0000-0000-0000-000000000001",
      "messageText": "Message to non-existent room",
      "payloadType": null,
      "payloadData": null,
      "parentMessageId": null
    }
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Không tìm thấy phòng chat: NONEXISTENT"
  }
  ```
- **Status Code**: 400 Bad Request

### Test Case 4: Sender Account Not Found
- **Test Case ID**: TC_CHAT_004
- **Description**: Verify proper error handling when sender account doesn't exist
- **Preconditions**:
  - Valid chat room exists
  - Account ID "NONEXISTENT" does not exist
- **Input**:
  - **Path Parameter**: chatRoomId = "cr000001-0000-0000-0000-000000000001"
  - **Request Body**:
    ```json
    {
      "senderAccountId": "NONEXISTENT",
      "messageText": "Message from non-existent account",
      "payloadType": null,
      "payloadData": null,
      "parentMessageId": null
    }
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Không tìm thấy tài khoản gửi: NONEXISTENT"
  }
  ```
- **Status Code**: 400 Bad Request

### Test Case 5: Non-Participant Sending Message
- **Test Case ID**: TC_CHAT_005
- **Description**: Verify that only chat room participants can send messages
- **Preconditions**:
  - Chat room exists between customer A and employee B
  - Customer C (not a participant) attempts to send message
- **Input**:
  - **Path Parameter**: chatRoomId = "cr000001-0000-0000-0000-000000000001"
  - **Request Body**:
    ```json
    {
      "senderAccountId": "a1000001-0000-0000-0000-000000000003",
      "messageText": "Unauthorized message attempt",
      "payloadType": null,
      "payloadData": null,
      "parentMessageId": null
    }
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Tài khoản không thuộc phòng chat này"
  }
  ```
- **Status Code**: 400 Bad Request

### Test Case 6: Invalid Message Content
- **Test Case ID**: TC_CHAT_006
- **Description**: Verify validation when neither message text nor payload data is provided
- **Preconditions**:
  - Valid chat room and participant
  - Empty message content
- **Input**:
  - **Path Parameter**: chatRoomId = "cr000001-0000-0000-0000-000000000001"
  - **Request Body**:
    ```json
    {
      "senderAccountId": "a1000001-0000-0000-0000-000000000001",
      "messageText": "",
      "payloadType": null,
      "payloadData": "",
      "parentMessageId": null
    }
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Message text or payload data must be provided"
  }
  ```
- **Status Code**: 400 Bad Request

### Test Case 7: Message Text Too Long
- **Test Case ID**: TC_CHAT_007
- **Description**: Verify validation when message text exceeds maximum length
- **Preconditions**:
  - Valid chat room and participant
  - Message text longer than 5000 characters
- **Input**:
  - **Path Parameter**: chatRoomId = "cr000001-0000-0000-0000-000000000001"
  - **Request Body**:
    ```json
    {
      "senderAccountId": "a1000001-0000-0000-0000-000000000001",
      "messageText": "A".repeat(5001),
      "payloadType": null,
      "payloadData": null,
      "parentMessageId": null
    }
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Message text must be less than 5000 characters"
  }
  ```
- **Status Code**: 400 Bad Request

---

## POST /{messageId}/reply - Reply to Message

### Test Case 8: Successful Message Reply
- **Test Case ID**: TC_CHAT_008
- **Description**: Verify that a participant can successfully reply to an existing message
- **Preconditions**:
  - Original message exists with ID 'cm000001-0000-0000-0000-000000000001'
  - Employee replying to customer's message
  - Both are participants in the same chat room
- **Input**:
  - **Path Parameter**: messageId = "cm000001-0000-0000-0000-000000000001"
  - **Request Body**:
    ```json
    {
      "senderAccountId": "a1000001-0000-0000-0000-000000000002",
      "messageText": "Hi! I'll be there at 2 PM as scheduled. Is there anything specific you'd like me to focus on?",
      "payloadType": null,
      "payloadData": null
    }
    ```
- **Expected Output**:
  ```json
  {
    "success": true,
    "message": "Trả lời tin nhắn thành công",
    "data": {
      "chatMessageId": "cm000001-0000-0000-0000-000000000003",
      "chatRoomId": "cr000001-0000-0000-0000-000000000001",
      "senderAccountId": "a1000001-0000-0000-0000-000000000002",
      "messageText": "Hi! I'll be there at 2 PM as scheduled. Is there anything specific you'd like me to focus on?",
      "payloadType": null,
      "payloadData": null,
      "sentAt": "2025-10-03T10:10:00+07:00",
      "readAt": null,
      "readByAccountId": null,
      "reply": {
        "messageId": "cm000001-0000-0000-0000-000000000001",
        "senderAccountId": "a1000001-0000-0000-0000-000000000001",
        "messageText": "Hello, I have a question about the upcoming cleaning service."
      },
      "deletion": null,
      "recall": null
    }
  }
  ```
- **Status Code**: 201 Created

### Test Case 9: Reply to Non-Existent Message
- **Test Case ID**: TC_CHAT_009
- **Description**: Verify proper error handling when replying to a non-existent message
- **Preconditions**:
  - Message ID "NONEXISTENT" does not exist
  - Valid account attempting to reply
- **Input**:
  - **Path Parameter**: messageId = "NONEXISTENT"
  - **Request Body**:
    ```json
    {
      "senderAccountId": "a1000001-0000-0000-0000-000000000002",
      "messageText": "Reply to non-existent message",
      "payloadType": null,
      "payloadData": null
    }
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Không tìm thấy tin nhắn để trả lời: NONEXISTENT"
  }
  ```
- **Status Code**: 400 Bad Request

### Test Case 10: Reply to Deleted Message
- **Test Case ID**: TC_CHAT_010
- **Description**: Verify that users cannot reply to deleted messages
- **Preconditions**:
  - Original message exists but has been deleted
  - Participant attempts to reply to deleted message
- **Input**:
  - **Path Parameter**: messageId = "cm000001-0000-0000-0000-000000000004"
  - **Request Body**:
    ```json
    {
      "senderAccountId": "a1000001-0000-0000-0000-000000000002",
      "messageText": "Reply to deleted message",
      "payloadType": null,
      "payloadData": null
    }
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Không thể trả lời tin nhắn đã bị xóa"
  }
  ```
- **Status Code**: 400 Bad Request

---

## DELETE /{messageId} - Delete Message

### Test Case 11: Successful Message Deletion by Sender
- **Test Case ID**: TC_CHAT_011
- **Description**: Verify that a participant can delete their own message
- **Preconditions**:
  - Message exists with ID 'cm000001-0000-0000-0000-000000000001'
  - Message sender is requesting deletion
  - Message has not been deleted before
- **Input**:
  - **Path Parameter**: messageId = "cm000001-0000-0000-0000-000000000001"
  - **Request Body**:
    ```json
    {
      "accountId": "a1000001-0000-0000-0000-000000000001"
    }
    ```
- **Expected Output**:
  ```json
  {
    "success": true,
    "message": "Xóa tin nhắn thành công",
    "data": {
      "chatMessageId": "cm000001-0000-0000-0000-000000000001",
      "chatRoomId": "cr000001-0000-0000-0000-000000000001",
      "senderAccountId": "a1000001-0000-0000-0000-000000000001",
      "messageText": "Hello, I have a question about the upcoming cleaning service.",
      "payloadType": null,
      "payloadData": null,
      "sentAt": "2025-10-03T10:00:00+07:00",
      "readAt": null,
      "readByAccountId": null,
      "reply": null,
      "deletion": {
        "deletedAt": "2025-10-03T10:15:00+07:00",
        "deletedByAccountId": "a1000001-0000-0000-0000-000000000001"
      },
      "recall": null
    }
  }
  ```
- **Status Code**: 200 OK

### Test Case 12: Successful Message Deletion by Other Participant
- **Test Case ID**: TC_CHAT_012
- **Description**: Verify that any chat room participant can delete messages
- **Preconditions**:
  - Message exists sent by customer
  - Employee participant is requesting deletion
  - Message has not been deleted before
- **Input**:
  - **Path Parameter**: messageId = "cm000001-0000-0000-0000-000000000002"
  - **Request Body**:
    ```json
    {
      "accountId": "a1000001-0000-0000-0000-000000000002"
    }
    ```
- **Expected Output**:
  ```json
  {
    "success": true,
    "message": "Xóa tin nhắn thành công",
    "data": {
      "chatMessageId": "cm000001-0000-0000-0000-000000000002",
      "chatRoomId": "cr000001-0000-0000-0000-000000000001",
      "senderAccountId": "a1000001-0000-0000-0000-000000000001",
      "messageText": "Another message to delete",
      "payloadType": null,
      "payloadData": null,
      "sentAt": "2025-10-03T10:05:00+07:00",
      "readAt": null,
      "readByAccountId": null,
      "reply": null,
      "deletion": {
        "deletedAt": "2025-10-03T10:20:00+07:00",
        "deletedByAccountId": "a1000001-0000-0000-0000-000000000002"
      },
      "recall": null
    }
  }
  ```
- **Status Code**: 200 OK

### Test Case 13: Delete Non-Existent Message
- **Test Case ID**: TC_CHAT_013
- **Description**: Verify proper error handling when trying to delete a non-existent message
- **Preconditions**:
  - Message ID "NONEXISTENT" does not exist
  - Valid account attempting deletion
- **Input**:
  - **Path Parameter**: messageId = "NONEXISTENT"
  - **Request Body**:
    ```json
    {
      "accountId": "a1000001-0000-0000-0000-000000000001"
    }
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Không tìm thấy tin nhắn: NONEXISTENT"
  }
  ```
- **Status Code**: 400 Bad Request

### Test Case 14: Delete Message by Non-Participant
- **Test Case ID**: TC_CHAT_014
- **Description**: Verify that only chat room participants can delete messages
- **Preconditions**:
  - Message exists in chat room
  - Non-participant account attempts deletion
- **Input**:
  - **Path Parameter**: messageId = "cm000001-0000-0000-0000-000000000001"
  - **Request Body**:
    ```json
    {
      "accountId": "a1000001-0000-0000-0000-000000000003"
    }
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Tài khoản không thuộc phòng chat này"
  }
  ```
- **Status Code**: 400 Bad Request

### Test Case 15: Delete Already Deleted Message
- **Test Case ID**: TC_CHAT_015
- **Description**: Verify behavior when trying to delete an already deleted message
- **Preconditions**:
  - Message has already been deleted
  - Participant attempts to delete again
- **Input**:
  - **Path Parameter**: messageId = "cm000001-0000-0000-0000-000000000005"
  - **Request Body**:
    ```json
    {
      "accountId": "a1000001-0000-0000-0000-000000000001"
    }
    ```
- **Expected Output**:
  ```json
  {
    "success": true,
    "message": "Xóa tin nhắn thành công",
    "data": {
      "chatMessageId": "cm000001-0000-0000-0000-000000000005",
      "chatRoomId": "cr000001-0000-0000-0000-000000000001",
      "senderAccountId": "a1000001-0000-0000-0000-000000000001",
      "messageText": "Already deleted message",
      "payloadType": null,
      "payloadData": null,
      "sentAt": "2025-10-03T09:00:00+07:00",
      "readAt": null,
      "readByAccountId": null,
      "reply": null,
      "deletion": {
        "deletedAt": "2025-10-03T09:30:00+07:00",
        "deletedByAccountId": "a1000001-0000-0000-0000-000000000002"
      },
      "recall": null
    }
  }
  ```
- **Status Code**: 200 OK

---

## POST /{messageId}/recall - Recall Message

### Test Case 16: Successful Message Recall Within Time Window
- **Test Case ID**: TC_CHAT_016
- **Description**: Verify that a sender can recall their message within the allowed time window (10 minutes)
- **Preconditions**:
  - Message exists sent by the requester
  - Message was sent less than 10 minutes ago
  - Message has not been recalled or deleted
- **Input**:
  - **Path Parameter**: messageId = "cm000001-0000-0000-0000-000000000006"
  - **Request Body**:
    ```json
    {
      "accountId": "a1000001-0000-0000-0000-000000000001"
    }
    ```
- **Expected Output**:
  ```json
  {
    "success": true,
    "message": "Thu hồi tin nhắn thành công",
    "data": {
      "chatMessageId": "cm000001-0000-0000-0000-000000000006",
      "chatRoomId": "cr000001-0000-0000-0000-000000000001",
      "senderAccountId": "a1000001-0000-0000-0000-000000000001",
      "messageText": "Message to be recalled",
      "payloadType": null,
      "payloadData": null,
      "sentAt": "2025-10-03T10:20:00+07:00",
      "readAt": null,
      "readByAccountId": null,
      "reply": null,
      "deletion": null,
      "recall": {
        "recalledAt": "2025-10-03T10:25:00+07:00"
      }
    }
  }
  ```
- **Status Code**: 200 OK

### Test Case 17: Recall Message by Non-Sender
- **Test Case ID**: TC_CHAT_017
- **Description**: Verify that only the message sender can recall their own message
- **Preconditions**:
  - Message exists sent by customer
  - Employee (not the sender) attempts to recall
- **Input**:
  - **Path Parameter**: messageId = "cm000001-0000-0000-0000-000000000007"
  - **Request Body**:
    ```json
    {
      "accountId": "a1000001-0000-0000-0000-000000000002"
    }
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Chỉ người gửi mới có thể thu hồi tin nhắn"
  }
  ```
- **Status Code**: 400 Bad Request

### Test Case 18: Recall Message Outside Time Window
- **Test Case ID**: TC_CHAT_018
- **Description**: Verify that messages cannot be recalled after the time window (10 minutes)
- **Preconditions**:
  - Message exists sent by the requester
  - Message was sent more than 10 minutes ago
  - Message has not been recalled or deleted
- **Input**:
  - **Path Parameter**: messageId = "cm000001-0000-0000-0000-000000000008"
  - **Request Body**:
    ```json
    {
      "accountId": "a1000001-0000-0000-0000-000000000001"
    }
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Tin nhắn đã quá thời gian cho phép để thu hồi"
  }
  ```
- **Status Code**: 400 Bad Request

### Test Case 19: Recall Already Recalled Message
- **Test Case ID**: TC_CHAT_019
- **Description**: Verify behavior when trying to recall an already recalled message
- **Preconditions**:
  - Message has already been recalled by sender
  - Sender attempts to recall again
- **Input**:
  - **Path Parameter**: messageId = "cm000001-0000-0000-0000-000000000009"
  - **Request Body**:
    ```json
    {
      "accountId": "a1000001-0000-0000-0000-000000000001"
    }
    ```
- **Expected Output**:
  ```json
  {
    "success": true,
    "message": "Thu hồi tin nhắn thành công",
    "data": {
      "chatMessageId": "cm000001-0000-0000-0000-000000000009",
      "chatRoomId": "cr000001-0000-0000-0000-000000000001",
      "senderAccountId": "a1000001-0000-0000-0000-000000000001",
      "messageText": "Already recalled message",
      "payloadType": null,
      "payloadData": null,
      "sentAt": "2025-10-03T10:00:00+07:00",
      "readAt": null,
      "readByAccountId": null,
      "reply": null,
      "deletion": null,
      "recall": {
        "recalledAt": "2025-10-03T10:05:00+07:00"
      }
    }
  }
  ```
- **Status Code**: 200 OK

### Test Case 20: Recall Deleted Message
- **Test Case ID**: TC_CHAT_020
- **Description**: Verify that deleted messages cannot be recalled
- **Preconditions**:
  - Message exists sent by the requester
  - Message has been deleted
  - Sender attempts to recall deleted message
- **Input**:
  - **Path Parameter**: messageId = "cm000001-0000-0000-0000-000000000010"
  - **Request Body**:
    ```json
    {
      "accountId": "a1000001-0000-0000-0000-000000000001"
    }
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Tin nhắn đã bị xóa và không thể thu hồi"
  }
  ```
- **Status Code**: 400 Bad Request

### Test Case 21: Recall Non-Existent Message
- **Test Case ID**: TC_CHAT_021
- **Description**: Verify proper error handling when trying to recall a non-existent message
- **Preconditions**:
  - Message ID "NONEXISTENT" does not exist
  - Valid account attempting recall
- **Input**:
  - **Path Parameter**: messageId = "NONEXISTENT"
  - **Request Body**:
    ```json
    {
      "accountId": "a1000001-0000-0000-0000-000000000001"
    }
    ```
- **Expected Output**:
  ```json
  {
    "success": false,
    "message": "Không tìm thấy tin nhắn: NONEXISTENT"
  }
  ```
- **Status Code**: 400 Bad Request

---

## Database Integration Test Scenarios

### Test Case 22: Real Database Integration
- **Test Case ID**: TC_CHAT_022
- **Description**: Verify integration with actual database data from chat initialization SQL
- **Covered Data**:
  - **Chat Rooms**: Assignment-based chat rooms between customers and employees
  - **Accounts**: Customer (a1000001-0000-0000-0000-000000000001), Employee (a1000001-0000-0000-0000-000000000002)
  - **Assignments**: Real assignment IDs linking customers to employees
  - **Messages**: Various message types including text, images, and replies
  - **Timestamps**: Proper timezone handling for message operations
- **Validation Points**:
  - Chat room participant validation works correctly
  - Message threading (replies) maintains proper relationships
  - Time window validation for recall operations
  - WebSocket events are published correctly
  - Last message timestamp updates chat room state

---

## Notes
- **Test Date Context**: All test cases assume current date is October 3, 2025
- **Real Data Integration**: Uses actual IDs and data structure from 02_chat_init.sql
- **WebSocket Integration**: 
  - Events published to `/topic/chatrooms/{chatRoomId}`
  - Event types: CREATED, DELETED, RECALLED
  - Real-time updates for chat participants
- **Business Logic Validation**:
  - 10-minute time window for message recall
  - Only participants can interact with chat room messages
  - Message threading supports nested replies
  - Soft deletion preserves message history
  - Payload support for media files and structured data
- **Security Features**:
  - Participant validation prevents unauthorized access
  - Account existence verification
  - Message ownership validation for recalls
  - Cross-chat-room message protection
- **Performance Considerations**:
  - Message indexing by chat room and sender
  - Efficient parent message resolution
  - Proper cascade deletion handling
- **Message States**:
  - **Normal**: Active message visible to participants
  - **Deleted**: Soft deleted with deletion info
  - **Recalled**: Recalled by sender within time window
  - **Reply**: Message with parent message reference
