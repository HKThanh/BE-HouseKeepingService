# WebSocket Chat Testing Instructions

## Overview
This document provides step-by-step instructions to test the real-time chat functionality using WebSocket with STOMP protocol in your House Keeping Service application.

**WebSocket Endpoints:**
- Native WebSocket: `ws://localhost:8080/ws-native`
- SockJS Fallback: `ws://localhost:8080/ws`
- Message Destination: `/app/chat.send`
- Subscription Topic: `/topic/conversations/{conversationId}`

---

## Prerequisites

### 1. Server Setup
- Ensure your Spring Boot application is running on `localhost:8080`
- Verify WebSocket configuration is active
- Check that Redis is running (for session management)
- Ensure database has conversation and message tables

### 2. Authentication Requirements
- Valid JWT token from login endpoint
- User must be a participant in the conversation
- Supported roles: CUSTOMER, EMPLOYEE, ADMIN

---

## Testing Tools

### Option 1: Browser-based Testing (Recommended)
Create an HTML file with JavaScript to test WebSocket connections:

```html
<!DOCTYPE html>
<html>
<head>
    <title>WebSocket Chat Test</title>
    <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>
</head>
<body>
    <div id="messages"></div>
    <input type="text" id="messageInput" placeholder="Type message...">
    <button onclick="sendMessage()">Send</button>
    
    <script>
        // Your WebSocket testing code will go here
    </script>
</body>
</html>
```

### Option 2: Postman/Thunder Client
- Use WebSocket request type
- Set proper headers for authentication

### Option 3: Command Line Tools
- Use `wscat` or similar WebSocket CLI tools

---

## Step-by-Step Testing Instructions

### Step 1: Obtain Authentication Token

1. **Login via REST API:**
   ```bash
   POST http://localhost:8080/api/v1/auth/login
   Content-Type: application/json
   
   {
     "username": "your_username",
     "password": "your_password",
     "role": "CUSTOMER",
     "deviceType": "WEB"
   }
   ```

2. **Extract JWT Token:**
   - Copy the `accessToken` from the response
   - This token will be used for WebSocket authentication

### Step 2: Get Conversation ID

1. **Retrieve Your Conversations:**
   ```bash
   GET http://localhost:8080/api/v1/chat/conversations
   Authorization: Bearer YOUR_JWT_TOKEN
   ```

2. **Extract Conversation ID:**
   - Copy a `conversationId` from the response
   - Or create a new conversation if needed

### Step 3: Establish WebSocket Connection

#### Using JavaScript (Browser):

```javascript
// Step 3.1: Initialize connection
const token = 'YOUR_JWT_TOKEN_HERE';
const conversationId = 'YOUR_CONVERSATION_ID_HERE';

// Step 3.2: Connect using SockJS
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

// Step 3.3: Set authentication header
const headers = {
    'Authorization': 'Bearer ' + token
};

// Step 3.4: Connect to WebSocket
stompClient.connect(headers, function(frame) {
    console.log('Connected: ' + frame);
    
    // Step 3.5: Subscribe to conversation topic
    stompClient.subscribe('/topic/conversations/' + conversationId, function(message) {
        console.log('Received message:', JSON.parse(message.body));
        displayMessage(JSON.parse(message.body));
    });
    
    console.log('Subscribed to conversation:', conversationId);
}, function(error) {
    console.error('Connection error:', error);
});
```

#### Expected Result:
- Connection should establish successfully
- No authentication errors should occur
- Subscription to conversation topic should succeed

### Step 4: Send a Text Message

```javascript
function sendMessage() {
    const messageContent = document.getElementById('messageInput').value;
    
    if (messageContent.trim() === '') {
        alert('Please enter a message');
        return;
    }
    
    const messagePayload = {
        conversationId: conversationId,
        messageType: 'TEXT',  // Based on your MessageType enum
        content: messageContent,
        replyToMessageId: null,
        attachments: []
    };
    
    // Send message via WebSocket
    stompClient.send('/app/chat.send', {}, JSON.stringify(messagePayload));
    
    // Clear input
    document.getElementById('messageInput').value = '';
}

function displayMessage(message) {
    const messagesDiv = document.getElementById('messages');
    const messageElement = document.createElement('div');
    messageElement.innerHTML = `
        <strong>${message.sender.name}</strong> (${message.createdAt}): 
        ${message.content}
    `;
    messagesDiv.appendChild(messageElement);
}
```

#### Expected Result:
- Message should be sent successfully
- You should receive the message back via subscription
- Other participants should receive the message in real-time

### Step 5: Test Message with Reply

```javascript
function sendReplyMessage(replyToMessageId) {
    const messagePayload = {
        conversationId: conversationId,
        messageType: 'TEXT',
        content: 'This is a reply message',
        replyToMessageId: replyToMessageId,
        attachments: []
    };
    
    stompClient.send('/app/chat.send', {}, JSON.stringify(messagePayload));
}
```

#### Expected Result:
- Reply message should include reference to original message
- Reply structure should be properly formatted in response

### Step 6: Test Message with Attachments

```javascript
function sendMessageWithAttachment() {
    const messagePayload = {
        conversationId: conversationId,
        messageType: 'IMAGE', // or 'FILE'
        content: 'Check out this image',
        replyToMessageId: null,
        attachments: [{
            url: 'https://example.com/image.jpg',
            publicId: 'cloudinary_public_id',
            contentType: 'image/jpeg',
            size: 1024000
        }]
    };
    
    stompClient.send('/app/chat.send', {}, JSON.stringify(messagePayload));
}
```

#### Expected Result:
- Message with attachment should be processed
- Attachment information should be included in response

### Step 7: Test Multiple Users (Real-time Testing)

1. **Open Multiple Browser Tabs/Windows:**
   - Login with different user accounts
   - Connect to the same conversation
   - Send messages from different users

2. **Verify Real-time Delivery:**
   - Messages sent from one user should appear instantly in other users' windows
   - No page refresh should be required

#### Expected Result:
- All connected users should receive messages in real-time
- Message order should be preserved
- No message duplication should occur

### Step 8: Test Error Scenarios

#### Test 8.1: Invalid Authentication
```javascript
// Connect without token or with invalid token
const invalidHeaders = {
    'Authorization': 'Bearer invalid_token'
};

stompClient.connect(invalidHeaders, function(frame) {
    console.log('Should not reach here');
}, function(error) {
    console.log('Expected authentication error:', error);
});
```

#### Test 8.2: Unauthorized Conversation Access
```javascript
// Try to subscribe to conversation user is not part of
stompClient.subscribe('/topic/conversations/unauthorized-conv-id', function(message) {
    console.log('Should not receive messages');
});
```

#### Test 8.3: Invalid Message Format
```javascript
// Send message with missing required fields
const invalidPayload = {
    conversationId: conversationId,
    // Missing messageType
    content: 'Test message'
};

stompClient.send('/app/chat.send', {}, JSON.stringify(invalidPayload));
```

#### Expected Results:
- Invalid authentication should fail connection
- Unauthorized subscription should be rejected
- Invalid messages should be rejected with proper error handling

---

## Validation Checklist

### ✅ Connection Testing
- [ ] WebSocket connection establishes successfully
- [ ] JWT authentication works correctly
- [ ] Subscription to conversation topic succeeds
- [ ] Connection handles network interruptions gracefully

### ✅ Message Testing
- [ ] Text messages send and receive correctly
- [ ] Messages appear in real-time for all participants
- [ ] Message order is preserved
- [ ] Reply messages work correctly
- [ ] Attachment messages work correctly

### ✅ Security Testing
- [ ] Invalid tokens are rejected
- [ ] Unauthorized conversation access is blocked
- [ ] Only conversation participants receive messages
- [ ] Message content is properly validated

### ✅ Error Handling
- [ ] Connection errors are handled gracefully
- [ ] Invalid message formats are rejected
- [ ] Network disconnections are handled properly
- [ ] Reconnection works after disconnection

### ✅ Performance Testing
- [ ] Multiple concurrent connections work
- [ ] High message volume is handled correctly
- [ ] Memory usage remains stable
- [ ] No message loss occurs under load

---

## Troubleshooting Common Issues

### Issue 1: Connection Fails
**Symptoms:** WebSocket connection cannot be established
**Solutions:**
- Check if server is running on correct port
- Verify CORS configuration allows your origin
- Ensure JWT token is valid and not expired
- Check network firewall settings

### Issue 2: Authentication Errors
**Symptoms:** "Thiếu thông tin xác thực" or "Token không hợp lệ"
**Solutions:**
- Verify JWT token format in Authorization header
- Check token expiration time
- Ensure user account exists and is active
- Try refreshing the token

### Issue 3: Subscription Fails
**Symptoms:** "Không có quyền truy cập cuộc hội thoại này"
**Solutions:**
- Verify user is participant in the conversation
- Check conversation ID is correct
- Ensure conversation exists in database
- Verify user permissions

### Issue 4: Messages Not Received
**Symptoms:** Messages sent but not received by other users
**Solutions:**
- Check if all users are subscribed to correct topic
- Verify WebSocket connections are active
- Check server logs for errors
- Ensure message format is correct

### Issue 5: Browser Compatibility
**Symptoms:** WebSocket not working in certain browsers
**Solutions:**
- Use SockJS fallback endpoint (`/ws` instead of `/ws-native`)
- Check browser WebSocket support
- Update browser to latest version
- Test with different browsers

---

## Advanced Testing Scenarios

### Scenario 1: Load Testing
```javascript
// Create multiple connections and send messages rapidly
for (let i = 0; i < 10; i++) {
    setTimeout(() => {
        sendMessage(`Load test message ${i}`);
    }, i * 100);
}
```

### Scenario 2: Connection Recovery
```javascript
// Simulate network disconnection
stompClient.disconnect();

// Reconnect after delay
setTimeout(() => {
    stompClient.connect(headers, onConnected, onError);
}, 5000);
```

### Scenario 3: Large Message Testing
```javascript
// Send large text message
const largeContent = 'A'.repeat(2000); // Test message size limits
sendMessageWithContent(largeContent);
```

---

## Monitoring and Logging

### Server-side Monitoring
- Check server logs for WebSocket connections
- Monitor memory usage during high load
- Track message delivery statistics
- Watch for authentication failures

### Client-side Monitoring
```javascript
// Add comprehensive logging
stompClient.debug = function(str) {
    console.log('STOMP: ' + str);
};

// Monitor connection state
socket.onopen = function() {
    console.log('WebSocket connection opened');
};

socket.onclose = function(event) {
    console.log('WebSocket connection closed:', event.code, event.reason);
};

socket.onerror = function(error) {
    console.error('WebSocket error:', error);
};
```

---

## Notes
- All WebSocket communication uses STOMP protocol over SockJS
- Authentication is required for all WebSocket operations
- Message delivery is guaranteed within the same conversation
- File attachments should be uploaded separately via REST API before sending message
- Maximum message size and attachment limits should be tested
- Connection timeout is handled automatically by the framework
