# WebSocket Test Cases - Chat System

## Overview
This document describes comprehensive test cases for the **Chat WebSocket System** including authentication, subscription validation, and real-time messaging functionality.  
**WebSocket Endpoint**: `ws://localhost:8080/ws/chat`  
**Topic Pattern**: `/topic/chatrooms/{chatRoomId}`  
**Test Date Context**: October 3, 2025

---

## Test Case Structure
Each test case includes:
- **Test Case ID**: Unique identifier for the test case.
- **Description**: Purpose of the test.
- **Preconditions**: Requirements before executing the test.
- **Input**: WebSocket connection details, headers, and messages.
- **Expected Output**: Expected WebSocket responses and events.
- **Status**: Connection status and error codes.

---

## WebSocket Authentication Requirements
- **Connection**: JWT token required in Authorization header
- **Subscription**: Participant validation for chat room access
- **Commands**: CONNECT, SUBSCRIBE, SEND, DISCONNECT
- **Security**: ChatWebSocketAuthChannelInterceptor validates all operations

---

## Test Scenarios Covered
1. **WebSocket Connection Authentication**
2. **Chat Room Subscription Validation**  
3. **Real-time Message Broadcasting**
4. **Message State Change Events**
5. **Authorization and Security**

---

## WebSocket Connection Authentication

### Test Case 1: Successful WebSocket Connection with Valid JWT
- **Test Case ID**: TC_SOCKET_001
- **Description**: Verify successful WebSocket connection with valid JWT token
- **Preconditions**:
  - Customer account exists with valid JWT token
  - WebSocket server is running
  - Chat system is initialized
- **Input**:
  - **WebSocket URL**: `ws://localhost:8080/ws/chat`
  - **Connection Methods**:
    1. **Authorization Header** (Recommended):
       ```
       WebSocket Headers:
       Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huX2RvZSIsImV4cCI6MTY5NzIwMDAwMH0.signature
       ```
    2. **Query Parameter** (Fallback for limited clients):
       ```
       WebSocket URL: ws://localhost:8080/ws/chat?token=eyJhbGciOiJIUzI1NiJ9...
       ```
  - **STOMP Command**:
    ```
    CONNECT
    Authorization:Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huX2RvZSIsImV4cCI6MTY5NzIwMDAwMH0.signature
    host:localhost:8080
    accept-version:1.1,1.0
    heart-beat:10000,10000

    ```
    *Note: Double newline required at end of STOMP frame*
- **Expected Output**:
  ```
  CONNECTED
  version:1.1
  heart-beat:10000,10000
  ```
- **Status**: Connection Established

### Test Case 2: WebSocket Connection with Invalid JWT
- **Test Case ID**: TC_SOCKET_002
- **Description**: Verify connection rejection with invalid JWT token
- **Preconditions**:
  - Invalid or expired JWT token provided
  - WebSocket server is running
- **Input**:
  - **WebSocket URL**: `ws://localhost:8080/ws/chat`
  - **Connection Methods**:
    1. **Authorization Header**:
       ```
       WebSocket Headers:
       Authorization: Bearer invalid_token_123
       ```
    2. **Query Parameter**:
       ```
       WebSocket URL: ws://localhost:8080/ws/chat?token=invalid_token_123
       ```
  - **STOMP Command**:
    ```
    CONNECT
    Authorization:Bearer invalid_token_123
    host:localhost:8080
    accept-version:1.1,1.0
    heart-beat:10000,10000

    ```
- **Expected Output**:
  ```
  ERROR
  message:Authentication failed: Invalid or expired JWT token
  ```
- **Status**: Connection Rejected

### Test Case 3: WebSocket Connection without Authorization Header
- **Test Case ID**: TC_SOCKET_003
- **Description**: Verify connection handling when no authorization header is provided
- **Preconditions**:
  - No authorization header in connection request
  - WebSocket server is running
- **Input**:
  - **WebSocket URL**: `ws://localhost:8080/ws/chat`
  - **STOMP Command**:
    ```
    CONNECT
    host:localhost:8080
    accept-version:1.1,1.0
    heart-beat:10000,10000

    ```
- **Expected Output**:
  ```
  ERROR
  message:Authentication failed: Authorization header is required
  ```
- **Status**: Connection Rejected

---

## Chat Room Subscription Validation

### Test Case 4: Successful Subscription to Authorized Chat Room
- **Test Case ID**: TC_SOCKET_004
- **Description**: Verify successful subscription to chat room where user is a participant
- **Preconditions**:
  - Customer is authenticated via WebSocket CONNECT
  - Chat room exists with customer as participant
  - Assignment-based chat room between customer and employee
  - JWT token stored in session attributes during handshake
- **Input**:
  - **STOMP Command**:
    ```
    SUBSCRIBE
    id:sub-1
    destination:/topic/chatrooms/cr000001-0000-0000-0000-000000000001
    Authorization:Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huX2RvZSIsImV4cCI6MTY5NzIwMDAwMH0.signature

    ```
    *Note: Authorization header can be included in SUBSCRIBE for session persistence*
- **Expected Output**:
  ```
  No immediate response (subscription successful)
  Server logs: "Subscription validated for user: john_doe to chat room: cr000001-0000-0000-0000-000000000001"
  ```
- **Status**: Subscription Active

### Test Case 5: Subscription Denied for Non-Participant
- **Test Case ID**: TC_SOCKET_005
- **Description**: Verify subscription rejection when user is not a chat room participant
- **Preconditions**:
  - Customer is authenticated via WebSocket CONNECT
  - Chat room exists but customer is not a participant
  - Attempting to subscribe to another user's chat room
- **Input**:
  - **STOMP Command**:
    ```
    SUBSCRIBE
    id:sub-2
    destination:/topic/chatrooms/cr000002-0000-0000-0000-000000000001
    Authorization:Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huX2RvZSIsImV4cCI6MTY5NzIwMDAwMH0.signature

    ```
- **Expected Output**:
  ```
  ERROR
  message:Bạn không có quyền truy cập phòng chat này
  destination:/topic/chatrooms/cr000002-0000-0000-0000-000000000001
  ```
- **Status**: Subscription Rejected

### Test Case 6: Subscription to Non-Existent Chat Room
- **Test Case ID**: TC_SOCKET_006
- **Description**: Verify error handling when subscribing to non-existent chat room
- **Preconditions**:
  - Customer is authenticated via WebSocket CONNECT
  - Chat room ID does not exist in database
- **Input**:
  - **STOMP Command**:
    ```
    SUBSCRIBE
    id:sub-3
    destination:/topic/chatrooms/NONEXISTENT
    Authorization:Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huX2RvZSIsImV4cCI6MTY5NzIwMDAwMH0.signature

    ```
- **Expected Output**:
  ```
  ERROR
  message:Không tìm thấy phòng chat
  destination:/topic/chatrooms/NONEXISTENT
  ```
- **Status**: Subscription Rejected

### Test Case 7: Multiple Chat Room Subscriptions
- **Test Case ID**: TC_SOCKET_007
- **Description**: Verify user can subscribe to multiple authorized chat rooms simultaneously
- **Preconditions**:
  - Employee is authenticated via WebSocket CONNECT
  - Employee is participant in multiple chat rooms
  - Multiple assignments for the same employee
  - JWT token stored in session for user validation
- **Input**:
  - **STOMP Commands**:
    ```
    SUBSCRIBE
    id:sub-4
    destination:/topic/chatrooms/cr000001-0000-0000-0000-000000000001
    Authorization:Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJib2Jfd2lsc29uIiwiZXhwIjoxNjk3MjAwMDAwfQ.signature

    SUBSCRIBE
    id:sub-5
    destination:/topic/chatrooms/cr000003-0000-0000-0000-000000000001
    Authorization:Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJib2Jfd2lsc29uIiwiZXhwIjoxNjk3MjAwMDAwfQ.signature

    ```
- **Expected Output**:
  ```
  No immediate responses (both subscriptions successful)
  Server logs: "Subscription validated for user: bob_wilson to chat room: cr000001..."
  Server logs: "Subscription validated for user: bob_wilson to chat room: cr000003..."
  ```
- **Status**: Multiple Subscriptions Active

---

## Real-time Message Broadcasting

### Test Case 8: Message Send Event Broadcasting
- **Test Case ID**: TC_SOCKET_008
- **Description**: Verify real-time message broadcasting when new message is sent
- **Preconditions**:
  - Customer (John Doe) and employee (Bob Wilson) both subscribed to same chat room
  - Customer sends message via REST API
  - Both participants connected via WebSocket
- **Input**:
  - **REST API Call**: POST /api/v1/chat/messages/cr000001-0000-0000-0000-000000000001
  - **Request Body**:
    ```json
    {
      "senderAccountId": "a1000001-0000-0000-0000-000000000001",
      "messageText": "Hello, when will you arrive?",
      "payloadType": null,
      "payloadData": null,
      "parentMessageId": null
    }
    ```
- **Expected WebSocket Output** (to both subscribers):
  ```
  MESSAGE
  destination:/topic/chatrooms/cr000001-0000-0000-0000-000000000001
  content-type:application/json
  
  {
    "eventType": "CREATED",
    "message": {
      "chatMessageId": "cm000001-0000-0000-0000-000000000001",
      "chatRoomId": "cr000001-0000-0000-0000-000000000001",
      "senderAccountId": "a1000001-0000-0000-0000-000000000001",
      "messageText": "Hello, when will you arrive?",
      "payloadType": null,
      "payloadData": null,
      "sentAt": "2025-08-20T08:45:00+07:00",
      "readAt": "2025-08-20T08:50:00+07:00",
      "readByAccountId": "a1000001-0000-0000-0000-000000000005",
      "reply": null,
      "deletion": null,
      "recall": null
    }
  }
  ```
- **Status**: Event Broadcasted Successfully

### Test Case 9: Message Reply Event Broadcasting
- **Test Case ID**: TC_SOCKET_009
- **Description**: Verify real-time broadcasting when replying to a message
- **Preconditions**:
  - Both participants subscribed to chat room
  - Original message exists (cm000001-0000-0000-0000-000000000001)
  - Employee (Bob Wilson) replies via REST API
- **Input**:
  - **REST API Call**: POST /api/v1/chat/messages/cm000001-0000-0000-0000-000000000001/reply
  - **Request Body**:
    ```json
    {
      "senderAccountId": "a1000001-0000-0000-0000-000000000005",
      "messageText": "I will be there in 30 minutes",
      "payloadType": null,
      "payloadData": null
    }
    ```
- **Expected WebSocket Output** (to both subscribers):
  ```
  MESSAGE
  destination:/topic/chatrooms/cr000001-0000-0000-0000-000000000001
  content-type:application/json
  
  {
    "eventType": "CREATED",
    "message": {
      "chatMessageId": "cm000001-0000-0000-0000-000000000002",
      "chatRoomId": "cr000001-0000-0000-0000-000000000001",
      "senderAccountId": "a1000001-0000-0000-0000-000000000005",
      "messageText": "I will be there in 30 minutes",
      "payloadType": null,
      "payloadData": null,
      "sentAt": "2025-08-20T08:52:00+07:00",
      "readAt": "2025-08-20T08:55:00+07:00",
      "readByAccountId": "a1000001-0000-0000-0000-000000000001",
      "reply": {
        "messageId": "cm000001-0000-0000-0000-000000000001",
        "senderAccountId": "a1000001-0000-0000-0000-000000000001",
        "messageText": "Hello, when will you arrive?"
      },
      "deletion": null,
      "recall": null
    }
  }
  ```
- **Status**: Reply Event Broadcasted Successfully

### Test Case 10: Message with Payload Broadcasting
- **Test Case ID**: TC_SOCKET_010
- **Description**: Verify broadcasting of messages with media payload (images, files)
- **Preconditions**:
  - Both participants subscribed to chat room
  - Employee (Bob Wilson) sends message with image payload
- **Input**:
  - **REST API Call**: POST /api/v1/chat/messages/cr000001-0000-0000-0000-000000000001
  - **Request Body**:
    ```json
    {
      "senderAccountId": "a1000001-0000-0000-0000-000000000005",
      "messageText": "Here is the before photo",
      "payloadType": "IMAGE",
      "payloadData": "https://cloudinary.com/cleaning-before.jpg",
      "parentMessageId": null
    }
    ```
- **Expected WebSocket Output** (to both subscribers):
  ```
  MESSAGE
  destination:/topic/chatrooms/cr000001-0000-0000-0000-000000000001
  content-type:application/json
  
  {
    "eventType": "CREATED",
    "message": {
      "chatMessageId": "cm000001-0000-0000-0000-000000000003",
      "chatRoomId": "cr000001-0000-0000-0000-000000000001",
      "senderAccountId": "a1000001-0000-0000-0000-000000000005",
      "messageText": "Here is the before photo",
      "payloadType": "IMAGE",
      "payloadData": "https://cloudinary.com/cleaning-before.jpg",
      "sentAt": "2025-08-20T09:15:00+07:00",
      "readAt": "2025-08-20T09:20:00+07:00",
      "readByAccountId": "a1000001-0000-0000-0000-000000000001",
      "reply": null,
      "deletion": null,
      "recall": null
    }
  }
  ```
- **Status**: Media Message Broadcasted Successfully

---

## Message State Change Events

### Test Case 11: Message Deletion Event Broadcasting
- **Test Case ID**: TC_SOCKET_011
- **Description**: Verify real-time broadcasting when message is deleted
- **Preconditions**:
  - Both participants subscribed to chat room
  - Message exists in chat room (cm000001-0000-0000-0000-000000000005)
  - Employee (Bob Wilson) deletes message via REST API
- **Input**:
  - **REST API Call**: DELETE /api/v1/chat/messages/cm000001-0000-0000-0000-000000000005
  - **Request Body**:
    ```json
    {
      "accountId": "a1000001-0000-0000-0000-000000000005"
    }
    ```
- **Expected WebSocket Output** (to both subscribers):
  ```
  MESSAGE
  destination:/topic/chatrooms/cr000001-0000-0000-0000-000000000001
  content-type:application/json
  
  {
    "eventType": "DELETED",
    "message": {
      "chatMessageId": "cm000001-0000-0000-0000-000000000005",
      "chatRoomId": "cr000001-0000-0000-0000-000000000001",
      "senderAccountId": "a1000001-0000-0000-0000-000000000005",
      "messageText": "Work completed! Here is the after photo",
      "payloadType": "IMAGE",
      "payloadData": "https://cloudinary.com/cleaning-after.jpg",
      "sentAt": "2025-08-20T12:45:00+07:00",
      "readAt": "2025-08-20T12:50:00+07:00",
      "readByAccountId": "a1000001-0000-0000-0000-000000000001",
      "reply": null,
      "deletion": {
        "deletedAt": "2025-08-20T14:45:00+07:00",
        "deletedByAccountId": "a1000001-0000-0000-0000-000000000005"
      },
      "recall": null
    }
  }
  ```
- **Status**: Deletion Event Broadcasted Successfully

### Test Case 12: Message Recall Event Broadcasting
- **Test Case ID**: TC_SOCKET_012
- **Description**: Verify real-time broadcasting when message is recalled by sender
- **Preconditions**:
  - Both participants subscribed to chat room cr000003 (Mary Jones + Bob Wilson)
  - Message exists sent less than 10 minutes ago (cm000003-0000-0000-0000-000000000001)
  - Sender (Bob Wilson) recalls message via REST API
- **Input**:
  - **REST API Call**: POST /api/v1/chat/messages/cm000003-0000-0000-0000-000000000001/recall
  - **Request Body**:
    ```json
    {
      "accountId": "a1000001-0000-0000-0000-000000000005"
    }
    ```
- **Expected WebSocket Output** (to both subscribers):
  ```
  MESSAGE
  destination:/topic/chatrooms/cr000003-0000-0000-0000-000000000001
  content-type:application/json
  
  {
    "eventType": "RECALLED",
    "message": {
      "chatMessageId": "cm000003-0000-0000-0000-000000000001",
      "chatRoomId": "cr000003-0000-0000-0000-000000000001",
      "senderAccountId": "a1000001-0000-0000-0000-000000000005",
      "messageText": "I made a mistake in the time. Let me correct that.",
      "payloadType": null,
      "payloadData": null,
      "sentAt": "2025-10-03T14:40:00+07:00",
      "readAt": null,
      "readByAccountId": null,
      "reply": null,
      "deletion": null,
      "recall": {
        "recalledAt": "2025-10-03T14:47:00+07:00"
      }
    }
  }
  ```
- **Status**: Recall Event Broadcasted Successfully

---

## Authorization and Security

### Test Case 13: Subscription After Authentication Expiry
- **Test Case ID**: TC_SOCKET_013
- **Description**: Verify behavior when JWT token expires during active WebSocket session
- **Preconditions**:
  - User connected with valid JWT token
  - JWT token expires while connection is active
  - User attempts new subscription
- **Input**:
  - **STOMP Command** (after token expiry):
    ```
    SUBSCRIBE
    id:sub-6
    destination:/topic/chatrooms/cr000001-0000-0000-0000-000000000001
    ```
- **Expected Output**:
  ```
  ERROR
  message:Invalid JWT token
  destination:/topic/chatrooms/cr000001-0000-0000-0000-000000000001
  ```
- **Status**: Subscription Rejected, Re-authentication Required

### Test Case 14: Cross-Chat-Room Event Isolation
- **Test Case ID**: TC_SOCKET_014
- **Description**: Verify that WebSocket events are isolated to specific chat rooms
- **Preconditions**:
  - John Doe (customer) subscribed to chat room cr000001 (with Bob Wilson)
  - Jane Smith subscribed to chat room cr000002 (customer+employee roles)
  - Message sent in chat room cr000001
- **Input**:
  - **John Doe Subscription**: `/topic/chatrooms/cr000001-0000-0000-0000-000000000001`
  - **Jane Smith Subscription**: `/topic/chatrooms/cr000002-0000-0000-0000-000000000001`
  - **Message Sent**: To chat room cr000001 by Bob Wilson (a1000001-0000-0000-0000-000000000005)
- **Expected Output**:
  - **John Doe receives**: Message event for chat room cr000001
  - **Jane Smith receives**: No message event (isolated)
- **Status**: Event Isolation Working Correctly

### Test Case 15: Concurrent Participants Real-time Updates
- **Test Case ID**: TC_SOCKET_015
- **Description**: Verify simultaneous real-time updates to multiple participants
- **Preconditions**:
  - John Doe (customer) and Bob Wilson (employee) both subscribed to chat room cr000001
  - Both participants sending messages simultaneously
  - Network latency simulation
- **Input**:
  - **John Doe Message**: "I'm running 10 minutes late"
  - **Bob Wilson Message**: "No problem, I'll wait"
  - **Both sent within 1 second**
- **Expected Output**:
  - Both participants receive both messages
  - Message order preserved by server timestamp
  - No message loss or duplication
- **Status**: Concurrent Updates Handled Successfully

### Test Case 16: WebSocket Connection Cleanup on Disconnect
- **Test Case ID**: TC_SOCKET_016
- **Description**: Verify proper cleanup when WebSocket connection is terminated
- **Preconditions**:
  - User connected and subscribed to chat rooms
  - Network disconnection occurs
  - User attempts to reconnect
- **Input**:
  - **Action**: Force disconnect WebSocket connection
  - **Action**: Attempt reconnection with same credentials
- **Expected Output**:
  - Previous subscriptions cleaned up
  - New connection established successfully
  - Can resubscribe to authorized chat rooms
- **Status**: Connection Cleanup Successful

---

## Chat Room Integration

### Test Case 17: Automatic Chat Room Creation on Assignment
- **Test Case ID**: TC_SOCKET_017
- **Description**: Verify WebSocket readiness when chat room is auto-created during assignment acceptance
- **Preconditions**:
  - Test Employee (e1000001-0000-0000-0000-000000000003) accepts booking assignment
  - Chat room is automatically created between Test Customer and Test Employee
  - Customer and employee accounts exist
- **Input**:
  - **REST API Call**: POST /api/v1/employee/booking-details/{detailId}/accept
  - **WebSocket Connections**: Both Test Customer (a1000001-0000-0000-0000-000000000006) and Test Employee (a1000001-0000-0000-0000-000000000007) connect
- **Expected Output**:
  - Chat room created successfully (cr000004 or cr000005)
  - Both participants can subscribe to the new chat room
  - WebSocket topic is immediately available
- **Status**: Auto-Creation Integration Working

### Test Case 18: Chat Room Participant Validation
- **Test Case ID**: TC_SOCKET_018
- **Description**: Verify participant validation using assignment-based access control
- **Preconditions**:
  - Assignment exists between John Doe (customer) and Bob Wilson (employee)
  - Chat room cr000001 created based on assignment as000001-0000-0000-0000-000000000001
  - Mary Jones (different customer) attempts access
- **Input**:
  - **Valid Participants**: John Doe (a1000001-0000-0000-0000-000000000001), Bob Wilson (a1000001-0000-0000-0000-000000000005)
  - **Invalid Participant**: Mary Jones (a1000001-0000-0000-0000-000000000004) not in this assignment
- **Expected Output**:
  - Valid participants can subscribe successfully
  - Invalid participant receives access denied error
- **Status**: Assignment-Based Access Control Working

---

## Performance and Reliability

### Test Case 19: High-Frequency Message Broadcasting
- **Test Case ID**: TC_SOCKET_019
- **Description**: Verify WebSocket performance under high message frequency
- **Preconditions**:
  - Multiple chat rooms active
  - High volume of messages being sent
  - Multiple subscribers per chat room
- **Input**:
  - **Scenario**: 100 messages sent within 10 seconds across 10 chat rooms
  - **Subscribers**: 2-3 per chat room
- **Expected Output**:
  - All messages broadcasted successfully
  - No message loss or delay > 2 seconds
  - WebSocket connections remain stable
- **Status**: High-Volume Performance Acceptable

### Test Case 20: WebSocket Heartbeat and Keep-Alive
- **Test Case ID**: TC_SOCKET_020
- **Description**: Verify WebSocket heartbeat mechanism maintains connections
- **Preconditions**:
  - WebSocket connection established
  - Heartbeat configured (10 seconds)
  - No message activity for extended period
- **Input**:
  - **Action**: Maintain connection for 5 minutes without sending messages
  - **Heartbeat**: Every 10 seconds
- **Expected Output**:
  - Connection remains active
  - Heartbeat frames exchanged regularly
  - Can send/receive messages after idle period
- **Status**: Heartbeat Mechanism Working

---

## Error Handling and Edge Cases

### Test Case 21: Malformed WebSocket Messages
- **Test Case ID**: TC_SOCKET_021
- **Description**: Verify handling of malformed or invalid WebSocket messages
- **Preconditions**:
  - Valid WebSocket connection established
  - User attempts to send malformed STOMP commands
- **Input**:
  - **Malformed Command**:
    ```
    INVALID_COMMAND
    destination:/topic/chatrooms/
    malformed:data
    ```
- **Expected Output**:
  ```
  ERROR
  message:Invalid STOMP command
  ```
- **Status**: Malformed Messages Handled Gracefully

### Test Case 22: Database Integration WebSocket Events
- **Test Case ID**: TC_SOCKET_022
- **Description**: Verify WebSocket integration with actual database transactions
- **Preconditions**:
  - Real database with chat_rooms and chat_messages tables from 02_chat_init.sql
  - Assignment data from 01_housekeeping_service.sql (existing assignments as000001-0000-0000-0000-000000000001 and as000001-0000-0000-0000-000000000002)
  - WebSocket connected participants
- **Input**:
  - **Database**: Real assignment and account data
  - **Chat Rooms**: Assignment-based between actual customers and employees
  - **Messages**: Persistent storage with real foreign keys
  - **Test Data**: Complete message history in cr000001, cr000002, cr000003
- **Expected Output**:
  - WebSocket events reflect actual database state
  - Message persistence works correctly with foreign key constraints
  - Participant validation uses real assignment data from assignments table
  - Chat room access control validates against actual customer_account_id and employee_account_id
- **Status**: Database Integration Working

---

## Test Data Reference (Based on 02_chat_init.sql)

### Chat Room Mapping for Test Cases:
- **cr000001-...-001**: John Doe (john_doe, a1000001-...-001) + Bob Wilson (bob_wilson, a1000001-...-005) 
  - Assignment: as000001-0000-0000-0000-000000000001 (COMPLETED)
  - Service: Tổng vệ sinh (Total cleaning)
  - Complete message history with examples of all message types

- **cr000002-...-001**: Jane Smith (jane_smith, a1000001-...-002) Customer + Employee roles
  - Assignment: as000001-0000-0000-0000-000000000002 (ASSIGNED) 
  - Service: Dọn dẹp theo giờ (Hourly cleaning)
  - Current assignment for dual-role testing

- **cr000003-...-001**: Mary Jones (mary_jones, a1000001-...-004) + Bob Wilson (bob_wilson, a1000001-...-005)
  - Assignment: as000003-0000-0000-0000-000000000001 (ASSIGNED)
  - Service: Vệ sinh máy lạnh (Air conditioner cleaning)
  - Fresh assignment for real-time testing scenarios

- **cr000004-...-001**: Test Customer (test_customer, a1000001-...-006) + Test Employee (test_employee, a1000001-...-007)
  - Assignment: as000004-0000-0000-0000-000000000001 (ASSIGNED)
  - Service: Đi chợ hộ (Shopping service)
  - Performance testing scenarios

- **cr000005-...-001**: John Doe (john_doe, a1000001-...-001) + Test Employee (test_employee, a1000001-...-007)
  - Assignment: as000005-0000-0000-0000-000000000001 (ASSIGNED)
  - Service: Nấu ăn gia đình (Family cooking)
  - Additional performance testing

### Account IDs for WebSocket Authentication:
- **a1000001-...-001**: john_doe (Customer) - Password: test123
- **a1000001-...-002**: jane_smith (Customer + Employee) - Password: test123  
- **a1000001-...-004**: mary_jones (Customer) - Password: test123
- **a1000001-...-005**: bob_wilson (Employee) - Password: test123
- **a1000001-...-006**: test_customer (Customer) - Password: test123
- **a1000001-...-007**: test_employee (Employee) - Password: test123

### Message State Examples:
- **cm000001-...-001**: "Hello, when will you arrive?" - Normal message with read receipt
- **cm000001-...-002**: "I will be there in 30 minutes" - Reply message with parent reference
- **cm000001-...-003**: "Here is the before photo" - Message with IMAGE payload  
- **cm000001-...-005**: "Work completed! Here is the after photo" - Deleted message (shows deletion timestamp)
- **cm000003-...-001**: "I made a mistake in the time. Let me correct that." - Recalled message (shows recall timestamp)
- **cm000003-...-002**: "Thank you for accepting the job!" - Old message (cannot be recalled, >10 minutes)

### Database Integration:
- All chat rooms linked to real assignments with valid foreign key constraints
- Assignment participants determine chat room access control
- Message persistence with proper timestamps and state tracking
- Booking details reference actual services from the housekeeping system
- Real customer and employee profiles with proper role assignments

---

## Notes
- **Test Environment**: WebSocket testing requires specialized tools like WebSocket King, Artillery, or custom JavaScript clients
- **Security**: All WebSocket operations require valid JWT authentication
- **Real-time Guarantees**: Events should be delivered within 2 seconds under normal network conditions  
- **Chat Room Access**: Based on assignment relationships from the housekeeping service

### Authentication Methods (Updated October 5, 2025):
1. **Authorization Header** (Primary method):
   - Set in WebSocket handshake headers: `Authorization: Bearer <JWT_TOKEN>`
   - Automatically stored in session attributes during handshake
   - Recommended for all WebSocket clients that support custom headers

2. **Query Parameter** (Fallback method):
   - URL format: `ws://localhost:8080/ws/chat?token=<JWT_TOKEN>`
   - Useful for clients with limited header support
   - Token automatically extracted and stored in session attributes

3. **STOMP Frame Headers** (Session persistence):
   - Include `Authorization:Bearer <JWT_TOKEN>` in CONNECT and SUBSCRIBE frames
   - Helps maintain user context across STOMP commands
   - Prevents "missing user information" errors during subscription

### Session Management:
- JWT tokens are stored in WebSocket session attributes during handshake
- User authentication state persists across multiple STOMP commands
- Session attributes include: `jwt_token`, `AUTHENTICATED_USER`, `USERNAME`
- Enhanced logging shows session ID, user info, and command processing

### Error Handling:
- Detailed error messages for authentication failures
- Graceful handling of incomplete frames and malformed STOMP commands
- Server logs provide comprehensive debugging information
- WebSocket connections remain stable during authentication errors

### Event Types**: 
  - `CREATED`: New message sent
  - `DELETED`: Message deleted by participant  
  - `RECALLED`: Message recalled by sender within time window
- **Scalability**: System designed to handle multiple concurrent chat rooms and participants
- **Error Recovery**: WebSocket connections auto-reconnect on network issues
- **Message Ordering**: Server timestamp ensures proper message sequencing
- **Broadcasting Scope**: Events only sent to participants of the specific chat room

### Testing Recommendations:
1. Test native WebSocket connection first with simple echo
2. Use proper STOMP frame format with double newlines
3. Include Authorization in both handshake and STOMP frames
4. Monitor server logs for detailed debugging information
5. Test with multiple clients for real-time broadcasting verification
