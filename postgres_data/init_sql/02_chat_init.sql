CREATE TABLE IF NOT EXISTS chat_rooms (
    chat_room_id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
    assignment_id VARCHAR(36) NOT NULL REFERENCES assignments(assignment_id) ON DELETE CASCADE,
    customer_account_id VARCHAR(36) NOT NULL REFERENCES account(account_id) ON DELETE CASCADE,
    employee_account_id VARCHAR(36) NOT NULL REFERENCES account(account_id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_message_at TIMESTAMP WITHOUT TIME ZONE
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_chat_rooms_assignment_id ON chat_rooms (assignment_id);
CREATE INDEX IF NOT EXISTS idx_chat_rooms_customer ON chat_rooms (customer_account_id);
CREATE INDEX IF NOT EXISTS idx_chat_rooms_employee ON chat_rooms (employee_account_id);

CREATE TABLE IF NOT EXISTS chat_messages (
    chat_message_id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
    chat_room_id VARCHAR(36) NOT NULL REFERENCES chat_rooms(chat_room_id) ON DELETE CASCADE,
    sender_account_id VARCHAR(36) NOT NULL REFERENCES account(account_id) ON DELETE CASCADE,
    message_text TEXT,
    payload_type VARCHAR(255),
    payload_data TEXT,
    sent_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP WITH TIME ZONE,
    read_by_account_id VARCHAR(36),
    parent_message_id VARCHAR(36) REFERENCES chat_messages(chat_message_id) ON DELETE SET NULL,
    deleted_at TIMESTAMP WITH TIME ZONE,
    deleted_by_account_id VARCHAR(36),
    recalled_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_chat_messages_room_id ON chat_messages (chat_room_id);
CREATE INDEX IF NOT EXISTS idx_chat_messages_sender ON chat_messages (sender_account_id);
CREATE INDEX IF NOT EXISTS idx_chat_messages_parent ON chat_messages (parent_message_id);
CREATE INDEX IF NOT EXISTS idx_chat_messages_deleted_at ON chat_messages (deleted_at);

-- =================================================================================
-- CHAT SYSTEM TEST DATA FOR WEBSOCKET TESTING
-- Based on Socket-Test.md test cases
-- =================================================================================

-- Create chat rooms based on existing assignments for WebSocket testing
-- These chat rooms correspond to the test scenarios in Socket-Test.md

-- Chat room 1: For completed assignment (John Doe customer + Bob Wilson employee)
INSERT INTO chat_rooms (chat_room_id, assignment_id, customer_account_id, employee_account_id, created_at, updated_at, last_message_at) VALUES
('cr000001-0000-0000-0000-000000000001', 'as000001-0000-0000-0000-000000000001', 'a1000001-0000-0000-0000-000000000001', 'a1000001-0000-0000-0000-000000000005', '2025-08-20 08:30:00+07', '2025-08-20 14:45:00+07', '2025-08-20 14:45:00+07');

-- Chat room 2: For assigned but not started assignment (Jane Smith customer + Jane Smith employee)
INSERT INTO chat_rooms (chat_room_id, assignment_id, customer_account_id, employee_account_id, created_at, updated_at, last_message_at) VALUES
('cr000002-0000-0000-0000-000000000001', 'as000001-0000-0000-0000-000000000002', 'a1000001-0000-0000-0000-000000000002', 'a1000001-0000-0000-0000-000000000002', '2025-08-28 13:30:00+07', '2025-08-28 14:35:00+07', '2025-08-28 14:35:00+07');

-- Chat room 3: For employee Bob Wilson with different customer (Mary Jones)
-- Create a new booking detail for Mary Jones (customer) to avoid conflicts
INSERT INTO booking_details (booking_detail_id, booking_id, service_id, quantity, price_per_unit, sub_total) VALUES
('bd000003-0000-0000-0000-000000000001', 'b0000001-0000-0000-0000-000000000001', (SELECT service_id FROM service WHERE name = 'Vệ sinh máy lạnh'), 1, 150000.00, 150000.00);

-- Create assignment for this new booking detail with Bob Wilson
INSERT INTO assignments (assignment_id, booking_detail_id, employee_id, status, check_in_time, check_out_time) VALUES
('as000003-0000-0000-0000-000000000001', 'bd000003-0000-0000-0000-000000000001', 'e1000001-0000-0000-0000-000000000002', 'ASSIGNED', NULL, NULL);

INSERT INTO chat_rooms (chat_room_id, assignment_id, customer_account_id, employee_account_id, created_at, updated_at, last_message_at) VALUES
('cr000003-0000-0000-0000-000000000001', 'as000003-0000-0000-0000-000000000001', 'a1000001-0000-0000-0000-000000000004', 'a1000001-0000-0000-0000-000000000005', '2025-10-01 10:00:00+07', '2025-10-01 10:00:00+07', NULL);

-- =================================================================================
-- CHAT MESSAGES FOR WEBSOCKET TEST SCENARIOS
-- =================================================================================

-- Messages for Chat Room 1 (John Doe + Bob Wilson) - Completed assignment
-- Initial conversation from completed booking
INSERT INTO chat_messages (chat_message_id, chat_room_id, sender_account_id, message_text, payload_type, payload_data, sent_at, read_at, read_by_account_id, parent_message_id, deleted_at, deleted_by_account_id, recalled_at) VALUES
-- Customer's initial message
('cm000001-0000-0000-0000-000000000001', 'cr000001-0000-0000-0000-000000000001', 'a1000001-0000-0000-0000-000000000001', 'Hello, when will you arrive?', NULL, NULL, '2025-08-20 08:45:00+07', '2025-08-20 08:50:00+07', 'a1000001-0000-0000-0000-000000000005', NULL, NULL, NULL, NULL),

-- Employee's reply
('cm000001-0000-0000-0000-000000000002', 'cr000001-0000-0000-0000-000000000001', 'a1000001-0000-0000-0000-000000000005', 'I will be there in 30 minutes', NULL, NULL, '2025-08-20 08:52:00+07', '2025-08-20 08:55:00+07', 'a1000001-0000-0000-0000-000000000001', 'cm000001-0000-0000-0000-000000000001', NULL, NULL, NULL),

-- Employee sends before photo (with payload)
('cm000001-0000-0000-0000-000000000003', 'cr000001-0000-0000-0000-000000000001', 'a1000001-0000-0000-0000-000000000005', 'Here is the before photo', 'IMAGE', 'https://cloudinary.com/cleaning-before.jpg', '2025-08-20 09:15:00+07', '2025-08-20 09:20:00+07', 'a1000001-0000-0000-0000-000000000001', NULL, NULL, NULL, NULL),

-- Customer acknowledgment
('cm000001-0000-0000-0000-000000000004', 'cr000001-0000-0000-0000-000000000001', 'a1000001-0000-0000-0000-000000000001', 'Thank you for the update!', NULL, NULL, '2025-08-20 09:25:00+07', '2025-08-20 09:30:00+07', 'a1000001-0000-0000-0000-000000000005', NULL, NULL, NULL, NULL),

-- Employee completion message (this will be deleted in test case)
('cm000001-0000-0000-0000-000000000005', 'cr000001-0000-0000-0000-000000000001', 'a1000001-0000-0000-0000-000000000005', 'Work completed! Here is the after photo', 'IMAGE', 'https://cloudinary.com/cleaning-after.jpg', '2025-08-20 12:45:00+07', '2025-08-20 12:50:00+07', 'a1000001-0000-0000-0000-000000000001', NULL, '2025-08-20 14:45:00+07', 'a1000001-0000-0000-0000-000000000005', NULL),

-- Customer final message
('cm000001-0000-0000-0000-000000000006', 'cr000001-0000-0000-0000-000000000001', 'a1000001-0000-0000-0000-000000000001', 'Perfect! Thank you very much', NULL, NULL, '2025-08-20 13:00:00+07', '2025-08-20 13:05:00+07', 'a1000001-0000-0000-0000-000000000005', NULL, NULL, NULL, NULL);

-- Messages for Chat Room 2 (Jane Smith customer + Jane Smith employee)
-- Recent conversation for ongoing assignment
INSERT INTO chat_messages (chat_message_id, chat_room_id, sender_account_id, message_text, payload_type, payload_data, sent_at, read_at, read_by_account_id, parent_message_id, deleted_at, deleted_by_account_id, recalled_at) VALUES
-- Customer (Jane Smith) as customer profile messaging
('cm000002-0000-0000-0000-000000000001', 'cr000002-0000-0000-0000-000000000001', 'a1000001-0000-0000-0000-000000000002', 'Hi, I will be at home around 2 PM tomorrow. Is that okay?', NULL, NULL, '2025-08-28 14:00:00+07', '2025-08-28 14:05:00+07', 'a1000001-0000-0000-0000-000000000002', NULL, NULL, NULL, NULL),

-- Employee (same Jane Smith account) replying
('cm000002-0000-0000-0000-000000000002', 'cr000002-0000-0000-0000-000000000001', 'a1000001-0000-0000-0000-000000000002', 'Yes, that works perfectly for me. See you tomorrow!', NULL, NULL, '2025-08-28 14:35:00+07', NULL, NULL, 'cm000002-0000-0000-0000-000000000001', NULL, NULL, NULL);

-- Messages for Chat Room 3 (Mary Jones + Bob Wilson) - Fresh assignment
-- No messages yet - will be used for real-time testing scenarios

-- =================================================================================
-- ADDITIONAL TEST DATA FOR WEBSOCKET EDGE CASES
-- =================================================================================

-- Recent message that can be recalled (sent less than 10 minutes ago)
-- For Test Case 12: Message Recall Event Broadcasting
INSERT INTO chat_messages (chat_message_id, chat_room_id, sender_account_id, message_text, payload_type, payload_data, sent_at, read_at, read_by_account_id, parent_message_id, deleted_at, deleted_by_account_id, recalled_at) VALUES
('cm000003-0000-0000-0000-000000000001', 'cr000003-0000-0000-0000-000000000001', 'a1000001-0000-0000-0000-000000000005', 'I made a mistake in the time. Let me correct that.', NULL, NULL, '2025-10-03 14:40:00+07', NULL, NULL, NULL, NULL, NULL, '2025-10-03 14:47:00+07');

-- Message that was sent more than 10 minutes ago (cannot be recalled)
INSERT INTO chat_messages (chat_message_id, chat_room_id, sender_account_id, message_text, payload_type, payload_data, sent_at, read_at, read_by_account_id, parent_message_id, deleted_at, deleted_by_account_id, recalled_at) VALUES
('cm000003-0000-0000-0000-000000000002', 'cr000003-0000-0000-0000-000000000001', 'a1000001-0000-0000-0000-000000000004', 'Thank you for accepting the job!', NULL, NULL, '2025-10-03 14:15:00+07', '2025-10-03 14:20:00+07', 'a1000001-0000-0000-0000-000000000005', NULL, NULL, NULL, NULL);

-- =================================================================================
-- WEBSOCKET TEST ACCOUNTS FOR AUTHENTICATION SCENARIOS
-- =================================================================================

-- Additional test accounts for WebSocket authentication testing
-- Account with expired token simulation (will be handled by JWT expiry)
INSERT INTO account (account_id, username, password, phone_number, status, is_phone_verified) VALUES
('a1000001-0000-0000-0000-000000000006', 'test_customer', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0999888777', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000007', 'test_employee', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0999888666', 'ACTIVE', true);

-- Assign roles to test accounts
INSERT INTO account_roles (account_id, role_id) VALUES
('a1000001-0000-0000-0000-000000000006', 1), -- test_customer is CUSTOMER
('a1000001-0000-0000-0000-000000000007', 2); -- test_employee is EMPLOYEE

-- Create customer and employee profiles for test accounts
INSERT INTO customer (customer_id, account_id, avatar, full_name, is_male, email, birthdate) VALUES
('c1000001-0000-0000-0000-000000000004', 'a1000001-0000-0000-0000-000000000006', 'https://picsum.photos/200', 'Test Customer', TRUE, 'test.customer@example.com', '2000-01-01');

INSERT INTO employee (employee_id, account_id, avatar, full_name, is_male, email, birthdate, hired_date, skills, bio) VALUES
('e1000001-0000-0000-0000-000000000003', 'a1000001-0000-0000-0000-000000000007', 'https://picsum.photos/200', 'Test Employee', FALSE, 'test.employee@example.com', '1995-05-15', '2024-01-01', ARRAY['Testing', 'WebSocket'], 'Test employee for WebSocket scenarios.');

-- =================================================================================
-- PERFORMANCE TEST DATA FOR HIGH-FREQUENCY SCENARIOS
-- =================================================================================

-- Create additional booking details and assignments for performance testing (Test Case 19)
-- New booking detail for test customer
INSERT INTO booking_details (booking_detail_id, booking_id, service_id, quantity, price_per_unit, sub_total) VALUES
('bd000004-0000-0000-0000-000000000001', 'b0000001-0000-0000-0000-000000000002', (SELECT service_id FROM service WHERE name = 'Đi chợ hộ'), 1, 40000.00, 40000.00);

-- Assignment for test customer + test employee
INSERT INTO assignments (assignment_id, booking_detail_id, employee_id, status, check_in_time, check_out_time) VALUES
('as000004-0000-0000-0000-000000000001', 'bd000004-0000-0000-0000-000000000001', 'e1000001-0000-0000-0000-000000000003', 'ASSIGNED', NULL, NULL);

-- Create another booking detail for John Doe with test employee
INSERT INTO booking_details (booking_detail_id, booking_id, service_id, quantity, price_per_unit, sub_total) VALUES
('bd000005-0000-0000-0000-000000000001', 'b0000001-0000-0000-0000-000000000001', (SELECT service_id FROM service WHERE name = 'Nấu ăn gia đình'), 2, 60000.00, 120000.00);

-- Assignment for John Doe + test employee
INSERT INTO assignments (assignment_id, booking_detail_id, employee_id, status, check_in_time, check_out_time) VALUES
('as000005-0000-0000-0000-000000000001', 'bd000005-0000-0000-0000-000000000001', 'e1000001-0000-0000-0000-000000000003', 'ASSIGNED', NULL, NULL);

-- Chat rooms for high-volume message testing
INSERT INTO chat_rooms (chat_room_id, assignment_id, customer_account_id, employee_account_id, created_at, updated_at, last_message_at) VALUES
('cr000004-0000-0000-0000-000000000001', 'as000004-0000-0000-0000-000000000001', 'a1000001-0000-0000-0000-000000000006', 'a1000001-0000-0000-0000-000000000007', '2025-10-03 10:00:00+07', '2025-10-03 10:00:00+07', NULL),
('cr000005-0000-0000-0000-000000000001', 'as000005-0000-0000-0000-000000000001', 'a1000001-0000-0000-0000-000000000001', 'a1000001-0000-0000-0000-000000000007', '2025-10-03 10:00:00+07', '2025-10-03 10:00:00+07', NULL);

-- =================================================================================
-- UPDATE last_message_at TIMESTAMPS
-- =================================================================================

-- Update last_message_at for chat rooms based on latest messages
UPDATE chat_rooms SET last_message_at = '2025-08-20 13:00:00+07' WHERE chat_room_id = 'cr000001-0000-0000-0000-000000000001';
UPDATE chat_rooms SET last_message_at = '2025-08-28 14:35:00+07' WHERE chat_room_id = 'cr000002-0000-0000-0000-000000000001';
UPDATE chat_rooms SET last_message_at = '2025-10-03 14:47:00+07' WHERE chat_room_id = 'cr000003-0000-0000-0000-000000000001';

-- =================================================================================
-- NOTES FOR WEBSOCKET TESTING
-- =================================================================================
-- 
-- Chat Room Mapping for Test Cases:
-- - cr000001-...-001: John Doe (customer) + Bob Wilson (employee) - Completed assignment with message history
-- - cr000002-...-001: Jane Smith (customer+employee roles) - Current assignment  
-- - cr000003-...-001: Mary Jones (customer) + Bob Wilson (employee) - Fresh assignment for real-time testing
-- - cr000004-...-001: Test Customer + Test Employee - Performance testing
-- - cr000005-...-001: John Doe + Test Employee - Additional performance testing
--
-- Account IDs for WebSocket Authentication:
-- - a1000001-...-001: john_doe (Customer)
-- - a1000001-...-002: jane_smith (Customer + Employee)  
-- - a1000001-...-004: mary_jones (Customer)
-- - a1000001-...-005: bob_wilson (Employee)
-- - a1000001-...-006: test_customer (Customer)
-- - a1000001-...-007: test_employee (Employee)
--
-- Message State Examples:
-- - cm000001-...-001: Normal message with reply
-- - cm000001-...-003: Message with IMAGE payload  
-- - cm000001-...-005: Deleted message (shows deletion timestamp)
-- - cm000003-...-001: Recalled message (shows recall timestamp)
-- - cm000003-...-002: Old message (cannot be recalled)
--
-- This data supports all test scenarios from Socket-Test.md including:
-- - WebSocket authentication with valid/invalid JWT
-- - Chat room subscription authorization based on assignment participants
-- - Real-time message broadcasting (CREATED, DELETED, RECALLED events)
-- - Cross-chat-room isolation testing
-- - Message payload testing (text + IMAGE)
-- - Reply message threading
-- - Performance testing with multiple chat rooms
-- - Database integration with real assignment relationships