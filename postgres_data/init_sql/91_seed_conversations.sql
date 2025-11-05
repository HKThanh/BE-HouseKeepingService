-- =================================================================================
-- SEED DATA CHO CONVERSATIONS VÀ CHAT MESSAGES
-- =================================================================================
-- File này thêm dữ liệu mẫu cho chức năng chat conversation
-- Bao gồm test cases cho flag canChat

-- Tạo booking riêng cho Conversation 2
INSERT INTO bookings (booking_id, customer_id, address_id, booking_code, booking_time, note, total_amount, status, promotion_id, is_verified) VALUES
('b0000001-0000-0000-0000-000000000014', 'c1000001-0000-0000-0000-000000000001', 'adrs0001-0000-0000-0000-000000000001', 'BK000014', '2025-11-06 10:00:00+07', 'Vệ sinh nhà cửa định kỳ', 450000.00, 'PENDING', NULL, false);

INSERT INTO booking_details (booking_detail_id, booking_id, service_id, quantity, price_per_unit, sub_total) VALUES
('bd000001-0000-0000-0000-000000000014', 'b0000001-0000-0000-0000-000000000014', 2, 1, 450000, 450000);

-- Tạo booking riêng cho Conversation 3
INSERT INTO bookings (booking_id, customer_id, address_id, booking_code, booking_time, note, total_amount, status, promotion_id, is_verified) VALUES
('b0000001-0000-0000-0000-000000000015', 'c1000001-0000-0000-0000-000000000002', 'adrs0001-0000-0000-0000-000000000002', 'BK000015', '2025-11-07 09:00:00+07', 'Giặt ủi quần áo công sở', 200000.00, 'CONFIRMED', NULL, true);

INSERT INTO booking_details (booking_detail_id, booking_id, service_id, quantity, price_per_unit, sub_total) VALUES
('bd000001-0000-0000-0000-000000000015', 'b0000001-0000-0000-0000-000000000015', 6, 1, 200000, 200000);

-- Tạo booking riêng cho Conversation 6
INSERT INTO bookings (booking_id, customer_id, address_id, booking_code, booking_time, note, total_amount, status, promotion_id, is_verified) VALUES
('b0000001-0000-0000-0000-000000000016', 'c1000001-0000-0000-0000-000000000001', 'adrs0001-0000-0000-0000-000000000001', 'BK000016', '2025-10-25 14:00:00+07', 'Vệ sinh thảm', 300000.00, 'COMPLETED', NULL, true);

INSERT INTO booking_details (booking_detail_id, booking_id, service_id, quantity, price_per_unit, sub_total) VALUES
('bd000001-0000-0000-0000-000000000016', 'b0000001-0000-0000-0000-000000000016', 3, 1, 300000, 300000);

-- Tạo booking riêng cho Conversation 7
INSERT INTO bookings (booking_id, customer_id, address_id, booking_code, booking_time, note, total_amount, status, promotion_id, is_verified) VALUES
('b0000001-0000-0000-0000-000000000010', 'c1000001-0000-0000-0000-000000000004', 'adrs0001-0000-0000-0000-000000000009', 'BK000010', '2025-11-05 10:00:00+07', 'Vệ sinh tổng quát căn hộ', 500000.00, 'PENDING', NULL, false);

-- Thêm booking detail cho booking này
INSERT INTO booking_details (booking_detail_id, booking_id, service_id, quantity, price_per_unit, sub_total) VALUES
('bd000001-0000-0000-0000-000000000010', 'b0000001-0000-0000-0000-000000000010', 2, 1, 500000, 500000);

-- Tạo booking riêng cho Conversation 8
INSERT INTO bookings (booking_id, customer_id, address_id, booking_code, booking_time, note, total_amount, status, promotion_id, is_verified) VALUES
('b0000001-0000-0000-0000-000000000011', 'c1000001-0000-0000-0000-000000000005', 'adrs0001-0000-0000-0000-000000000010', 'BK000011', '2025-11-06 14:00:00+07', 'Giặt ủi áo dài cao cấp', 150000.00, 'PENDING', NULL, false);

-- Thêm booking detail cho Conversation 8
INSERT INTO booking_details (booking_detail_id, booking_id, service_id, quantity, price_per_unit, sub_total) VALUES
('bd000001-0000-0000-0000-000000000011', 'b0000001-0000-0000-0000-000000000011', 6, 1, 150000, 150000);

-- Tạo booking riêng cho Conversation 9
INSERT INTO bookings (booking_id, customer_id, address_id, booking_code, booking_time, note, total_amount, status, promotion_id, is_verified) VALUES
('b0000001-0000-0000-0000-000000000012', 'c1000001-0000-0000-0000-000000000006', 'adrs0001-0000-0000-0000-000000000011', 'BK000012', '2025-11-07 09:00:00+07', 'Vệ sinh sofa da chuyên dụng', 200000.00, 'CONFIRMED', NULL, true);

-- Thêm booking detail cho Conversation 9
INSERT INTO booking_details (booking_detail_id, booking_id, service_id, quantity, price_per_unit, sub_total) VALUES
('bd000001-0000-0000-0000-000000000012', 'b0000001-0000-0000-0000-000000000012', 3, 1, 200000, 200000);

-- Tạo booking riêng cho Conversation 10
INSERT INTO bookings (booking_id, customer_id, address_id, booking_code, booking_time, note, total_amount, status, promotion_id, is_verified) VALUES
('b0000001-0000-0000-0000-000000000013', 'c1000001-0000-0000-0000-000000000007', 'adrs0001-0000-0000-0000-000000000012', 'BK000013', '2025-11-08 10:30:00+07', 'Vệ sinh máy lạnh 2 cái', 100000.00, 'PENDING', NULL, false);

-- Thêm booking detail cho Conversation 10
INSERT INTO booking_details (booking_detail_id, booking_id, service_id, quantity, price_per_unit, sub_total) VALUES
('bd000001-0000-0000-0000-000000000013', 'b0000001-0000-0000-0000-000000000013', 4, 2, 50000, 100000);

-- Thêm conversations mẫu
-- Conversation 12: Customer 'John Doe' và Employee 'Trần Văn Long' - test unread count
INSERT INTO conversations (conversation_id, customer_id, employee_id, booking_id, last_message, last_message_time, is_active, created_at, updated_at) VALUES
('conv0012-0000-0000-0000-000000000012', 'c1000001-0000-0000-0000-000000000001', 'e1000001-0000-0000-0000-000000000003', 'b0000001-0000-0000-0000-000000000014', 'Vâng, tôi đã sẵn sàng', '2025-11-01 10:00:00+07', true, '2025-11-01 09:00:00+07', '2025-11-01 10:00:00+07');

-- Conversation 13: Customer 'Jane Smith' và Employee 'Trần Văn Long' - test unread count
INSERT INTO conversations (conversation_id, customer_id, employee_id, booking_id, last_message, last_message_time, is_active, created_at, updated_at) VALUES
('conv0013-0000-0000-0000-000000000013', 'c1000001-0000-0000-0000-000000000002', 'e1000001-0000-0000-0000-000000000003', 'b0000001-0000-0000-0000-000000000015', 'Cảm ơn bạn!', '2025-11-01 11:30:00+07', true, '2025-11-01 11:00:00+07', '2025-11-01 11:30:00+07');

-- Conversation 14: Conversation không có unread messages
INSERT INTO conversations (conversation_id, customer_id, employee_id, booking_id, last_message, last_message_time, is_active, created_at, updated_at) VALUES
('conv0014-0000-0000-0000-000000000014', 'c1000001-0000-0000-0000-000000000001', 'e1000001-0000-0000-0000-000000000004', 'b0000001-0000-0000-0000-000000000016', 'Đã nhận được!', '2025-10-25 14:00:00+07', true, '2025-10-25 13:00:00+07', '2025-10-25 14:00:00+07');

-- Conversation 7: Customer 'Nguyễn Văn An' và Employee 'Trần Văn Long' - có booking PENDING đang chờ verify, canChat = true
INSERT INTO conversations (conversation_id, customer_id, employee_id, booking_id, last_message, last_message_time, is_active, created_at, updated_at) VALUES
('conv0007-0000-0000-0000-000000000007', 'c1000001-0000-0000-0000-000000000004', 'e1000001-0000-0000-0000-000000000003', 'b0000001-0000-0000-0000-000000000010', 'Tôi sẽ đến đúng giờ nhé!', '2025-11-01 07:45:00+07', true, '2025-11-01 07:30:00+07', '2025-11-01 07:45:00+07');

-- Conversation 8: Customer 'Trần Thị Bích' và Employee 'Nguyễn Thị Mai' - có booking PENDING, canChat = true
INSERT INTO conversations (conversation_id, customer_id, employee_id, booking_id, last_message, last_message_time, is_active, created_at, updated_at) VALUES
('conv0008-0000-0000-0000-000000000008', 'c1000001-0000-0000-0000-000000000005', 'e1000001-0000-0000-0000-000000000004', 'b0000001-0000-0000-0000-000000000011', 'Bạn có nhận giặt áo dài không?', '2025-11-02 09:30:00+07', true, '2025-11-02 09:15:00+07', '2025-11-02 09:30:00+07');

-- Conversation 9: Customer 'Lê Văn Cường' và Employee 'Lê Văn Nam' - có booking CONFIRMED, canChat = true
INSERT INTO conversations (conversation_id, customer_id, employee_id, booking_id, last_message, last_message_time, is_active, created_at, updated_at) VALUES
('conv0009-0000-0000-0000-000000000009', 'c1000001-0000-0000-0000-000000000006', 'e1000001-0000-0000-0000-000000000005', 'b0000001-0000-0000-0000-000000000012', 'Sofa nhà em màu trắng, cần làm sạch kỹ.', '2025-11-03 13:50:00+07', true, '2025-11-03 13:40:00+07', '2025-11-03 13:50:00+07');

-- Conversation 10: Customer 'Phạm Thị Dung' và Employee 'Phạm Văn Ơn' - có booking PENDING, canChat = true
INSERT INTO conversations (conversation_id, customer_id, employee_id, booking_id, last_message, last_message_time, is_active, created_at, updated_at) VALUES
('conv0010-0000-0000-0000-000000000010', 'c1000001-0000-0000-0000-000000000007', 'e1000001-0000-0000-0000-000000000006', 'b0000001-0000-0000-0000-000000000013', 'Vâng, em sẽ mang đủ dụng cụ.', '2025-11-04 09:20:00+07', true, '2025-11-04 09:10:00+07', '2025-11-04 09:20:00+07');

-- Thêm chat messages mẫu cho các conversations
-- Messages cho Conversation 12 (TC 1.1 - Customer có 3 tin nhắn chưa đọc)
INSERT INTO chat_messages (message_id, conversation_id, sender_id, content, is_read, created_at) VALUES
('msg00200-0000-0000-0000-000000000001', 'conv0012-0000-0000-0000-000000000012', 'a1000001-0000-0000-0000-000000000016', 'Chào bạn, tôi đã nhận được yêu cầu của bạn', false, '2025-11-01 09:15:00+07'),
('msg00200-0000-0000-0000-000000000002', 'conv0012-0000-0000-0000-000000000012', 'a1000001-0000-0000-0000-000000000016', 'Tôi sẽ đến vào lúc 10:00', false, '2025-11-01 09:30:00+07'),
('msg00200-0000-0000-0000-000000000003', 'conv0012-0000-0000-0000-000000000012', 'a1000001-0000-0000-0000-000000000016', 'Bạn chuẩn bị sẵn nhé!', false, '2025-11-01 09:45:00+07'),
('msg00200-0000-0000-0000-000000000004', 'conv0012-0000-0000-0000-000000000012', 'a1000001-0000-0000-0000-000000000001', 'Vâng, tôi đã sẵn sàng', true, '2025-11-01 10:00:00+07');

-- Messages cho Conversation 13 (TC 1.2 - Employee có 7 tin nhắn chưa đọc)
INSERT INTO chat_messages (message_id, conversation_id, sender_id, content, is_read, created_at) VALUES
('msg00300-0000-0000-0000-000000000001', 'conv0013-0000-0000-0000-000000000013', 'a1000001-0000-0000-0000-000000000002', 'Bạn có thể đến sớm hơn không?', false, '2025-11-01 11:00:00+07'),
('msg00300-0000-0000-0000-000000000002', 'conv0013-0000-0000-0000-000000000013', 'a1000001-0000-0000-0000-000000000002', 'Tôi cần gấp lắm', false, '2025-11-01 11:05:00+07'),
('msg00300-0000-0000-0000-000000000003', 'conv0013-0000-0000-0000-000000000013', 'a1000001-0000-0000-0000-000000000002', 'Làm ơn phản hồi giúp tôi', false, '2025-11-01 11:10:00+07'),
('msg00300-0000-0000-0000-000000000004', 'conv0013-0000-0000-0000-000000000013', 'a1000001-0000-0000-0000-000000000002', 'Tôi đang chờ bạn', false, '2025-11-01 11:15:00+07'),
('msg00300-0000-0000-0000-000000000005', 'conv0013-0000-0000-0000-000000000013', 'a1000001-0000-0000-0000-000000000002', 'Bạn đã thấy tin nhắn chưa?', false, '2025-11-01 11:20:00+07'),
('msg00300-0000-0000-0000-000000000006', 'conv0013-0000-0000-0000-000000000013', 'a1000001-0000-0000-0000-000000000002', 'Vui lòng trả lời tôi', false, '2025-11-01 11:25:00+07'),
('msg00300-0000-0000-0000-000000000007', 'conv0013-0000-0000-0000-000000000013', 'a1000001-0000-0000-0000-000000000002', 'Cảm ơn bạn!', false, '2025-11-01 11:30:00+07'),
('msg00300-0000-0000-0000-000000000008', 'conv0013-0000-0000-0000-000000000013', 'a1000001-0000-0000-0000-000000000016', 'Xin lỗi, tôi vừa thấy', true, '2025-11-01 11:35:00+07');

-- Messages cho Conversation 14 (TC 1.3 - Không có tin nhắn chưa đọc)
INSERT INTO chat_messages (message_id, conversation_id, sender_id, content, is_read, created_at) VALUES
('msg00600-0000-0000-0000-000000000001', 'conv0014-0000-0000-0000-000000000014', 'a1000001-0000-0000-0000-000000000001', 'Xin chào!', true, '2025-10-25 13:30:00+07'),
('msg00600-0000-0000-0000-000000000002', 'conv0014-0000-0000-0000-000000000014', 'a1000001-0000-0000-0000-000000000017', 'Chào bạn!', true, '2025-10-25 13:35:00+07'),
('msg00600-0000-0000-0000-000000000003', 'conv0014-0000-0000-0000-000000000014', 'a1000001-0000-0000-0000-000000000001', 'Đã nhận được!', true, '2025-10-25 14:00:00+07');

-- Messages cho Conversation 7 (PENDING booking)
INSERT INTO chat_messages (message_id, conversation_id, sender_id, content, is_read, created_at) VALUES
('msg00100-0000-0000-0000-000000000007', 'conv0007-0000-0000-0000-000000000007', 'a1000001-0000-0000-0000-000000000006', 'Chào anh, căn hộ của em cần vệ sinh tổng quát.', true, '2025-11-01 07:30:00+07'),
('msg00100-0000-0000-0000-000000000008', 'conv0007-0000-0000-0000-000000000007', 'a1000001-0000-0000-0000-000000000016', 'Vâng, tôi sẽ đến đúng giờ nhé!', true, '2025-11-01 07:45:00+07');

-- Messages cho Conversation 8 (PENDING booking)
INSERT INTO chat_messages (message_id, conversation_id, sender_id, content, is_read, created_at) VALUES
('msg00100-0000-0000-0000-000000000009', 'conv0008-0000-0000-0000-000000000008', 'a1000001-0000-0000-0000-000000000007', 'Bạn có nhận giặt áo dài không?', true, '2025-11-02 09:30:00+07'),
('msg00100-0000-0000-0000-000000000010', 'conv0008-0000-0000-0000-000000000008', 'a1000001-0000-0000-0000-000000000017', 'Có ạ, chị gửi cho em nhé!', false, '2025-11-02 09:32:00+07');

-- Messages cho Conversation 9 (PENDING booking)
INSERT INTO chat_messages (message_id, conversation_id, sender_id, content, is_read, created_at) VALUES
('msg00100-0000-0000-0000-000000000011', 'conv0009-0000-0000-0000-000000000009', 'a1000001-0000-0000-0000-000000000008', 'Sofa nhà em màu trắng, cần làm sạch kỹ.', true, '2025-11-03 13:50:00+07'),
('msg00100-0000-0000-0000-000000000012', 'conv0009-0000-0000-0000-000000000009', 'a1000001-0000-0000-0000-000000000018', 'Vâng anh, em sẽ vệ sinh kỹ lưỡng.', false, '2025-11-03 13:52:00+07');

-- Messages cho Conversation 10 (PENDING booking)
INSERT INTO chat_messages (message_id, conversation_id, sender_id, content, is_read, created_at) VALUES
('msg00100-0000-0000-0000-000000000013', 'conv0010-0000-0000-0000-000000000010', 'a1000001-0000-0000-0000-000000000009', 'Bạn mang dụng cụ vệ sinh máy lạnh theo không?', true, '2025-11-04 09:10:00+07'),
('msg00100-0000-0000-0000-000000000014', 'conv0010-0000-0000-0000-000000000010', 'a1000001-0000-0000-0000-000000000019', 'Vâng, em sẽ mang đủ dụng cụ.', true, '2025-11-04 09:20:00+07');

-- =================================================================================
-- TEST CASE ĐẶC BIỆT: Conversation với booking COMPLETED nhưng có booking mới
-- =================================================================================

-- Thêm booking COMPLETED cho Customer 'Hoàng Văn Em' và Employee 'Hoàng Thị Phương'
INSERT INTO bookings (booking_id, customer_id, address_id, booking_code, booking_time, note, total_amount, status, promotion_id, is_verified) VALUES
('b0000001-0000-0000-0000-000000000008', 'c1000001-0000-0000-0000-000000000008', 'adrs0001-0000-0000-0000-000000000013', 'BK000008', '2025-10-15 09:00:00+07', 'Vệ sinh tổng quát lần 1', 500000.00, 'COMPLETED', NULL, true);

-- Thêm booking detail cho booking COMPLETED
INSERT INTO booking_details (booking_detail_id, booking_id, service_id, quantity, price_per_unit, sub_total) VALUES
('bd000001-0000-0000-0000-000000000008', 'b0000001-0000-0000-0000-000000000008', 2, 1, 500000, 500000);

-- Thêm assignment cho booking COMPLETED
INSERT INTO assignments (assignment_id, booking_detail_id, employee_id, status, check_in_time, check_out_time) VALUES
('asgn0001-0000-0000-0000-000000000008', 'bd000001-0000-0000-0000-000000000008', 'e1000001-0000-0000-0000-000000000007', 'COMPLETED', '2025-10-15 09:00:00+07', '2025-10-15 11:30:00+07');

-- Thêm booking MỚI (CONFIRMED) cho cùng customer và employee
INSERT INTO bookings (booking_id, customer_id, address_id, booking_code, booking_time, note, total_amount, status, promotion_id, is_verified) VALUES
('b0000001-0000-0000-0000-000000000009', 'c1000001-0000-0000-0000-000000000008', 'adrs0001-0000-0000-0000-000000000013', 'BK000009', '2025-11-10 14:00:00+07', 'Vệ sinh tổng quát lần 2', 500000.00, 'CONFIRMED', NULL, true);

-- Thêm booking detail cho booking mới
INSERT INTO booking_details (booking_detail_id, booking_id, service_id, quantity, price_per_unit, sub_total) VALUES
('bd000001-0000-0000-0000-000000000009', 'b0000001-0000-0000-0000-000000000009', 2, 1, 500000, 500000);

-- Thêm assignment cho booking mới
INSERT INTO assignments (assignment_id, booking_detail_id, employee_id, status) VALUES
('asgn0001-0000-0000-0000-000000000009', 'bd000001-0000-0000-0000-000000000009', 'e1000001-0000-0000-0000-000000000007', 'ASSIGNED');

-- Thêm conversation cho test case này - canChat = true vì có booking mới
INSERT INTO conversations (conversation_id, customer_id, employee_id, booking_id, last_message, last_message_time, is_active, created_at, updated_at) VALUES
('conv0011-0000-0000-0000-000000000011', 'c1000001-0000-0000-0000-000000000008', 'e1000001-0000-0000-0000-000000000007', 'b0000001-0000-0000-0000-000000000009', 'Hẹn gặp lại chị lần sau!', '2025-11-05 10:00:00+07', true, '2025-10-15 08:30:00+07', '2025-11-05 10:00:00+07');

-- Thêm messages cho conversation này
INSERT INTO chat_messages (message_id, conversation_id, sender_id, content, is_read, created_at) VALUES
('msg00100-0000-0000-0000-000000000015', 'conv0011-0000-0000-0000-000000000011', 'a1000001-0000-0000-0000-000000000010', 'Lần trước bạn làm rất tốt, lần này tôi book lại nhé!', true, '2025-11-05 09:50:00+07'),
('msg00100-0000-0000-0000-000000000016', 'conv0011-0000-0000-0000-000000000011', 'a1000001-0000-0000-0000-000000000020', 'Cảm ơn chị đã tin tưởng. Em sẽ cố gắng!', true, '2025-11-05 09:55:00+07'),
('msg00100-0000-0000-0000-000000000017', 'conv0011-0000-0000-0000-000000000011', 'a1000001-0000-0000-0000-000000000010', 'Hẹn gặp lại chị lần sau!', true, '2025-11-05 10:00:00+07');

-- =================================================================================
-- NOTES VỀ TEST CASES
-- =================================================================================
-- Conversation 1 (trong 90_seed_datas.sql): canChat = FALSE (booking COMPLETED, không có booking mới)
--   - Customer: John Doe (c1000001-0000-0000-0000-000000000001)
--   - Employee: Jane Smith (e1000001-0000-0000-0000-000000000001)
--   - Booking: b0000001-0000-0000-0000-000000000001 (COMPLETED)
--
-- Conversation 12: Test TC 1.1 - Customer có 3 tin nhắn chưa đọc
--   - Customer: John Doe (c1000001-0000-0000-0000-000000000001) - receiverId
--   - Employee: Trần Văn Long (e1000001-0000-0000-0000-000000000003) - sender
--   - Unread messages: 3 (từ employee gửi cho customer)
--   - Booking: b0000001-0000-0000-0000-000000000014 (PENDING)
--
-- Conversation 13: Test TC 1.2 - Employee có 7 tin nhắn chưa đọc
--   - Customer: Jane Smith (c1000001-0000-0000-0000-000000000002) - sender
--   - Employee: Trần Văn Long (e1000001-0000-0000-0000-000000000003) - receiverId
--   - Unread messages: 7 (từ customer gửi cho employee)
--   - Booking: b0000001-0000-0000-0000-000000000015 (CONFIRMED)
--
-- Conversation 14: Test TC 1.3 - Không có tin nhắn chưa đọc
--   - Customer: John Doe (c1000001-0000-0000-0000-000000000001)
--   - Employee: Nguyễn Thị Mai (e1000001-0000-0000-0000-000000000004)
--   - Unread messages: 0 (tất cả đã đọc)
--   - Booking: b0000001-0000-0000-0000-000000000016 (COMPLETED)
--
-- Conversation 7-10: canChat = TRUE (booking PENDING/CONFIRMED đang active)
--   - Các conversation này có booking chưa hoàn thành
--
-- Conversation 11: canChat = TRUE (có booking COMPLETED nhưng có booking mới CONFIRMED)
--   - Customer: Hoàng Văn Em (c1000001-0000-0000-0000-000000000008)
--   - Employee: Hoàng Thị Phương (e1000001-0000-0000-0000-000000000007)
--   - Booking cũ: b0000001-0000-0000-0000-000000000008 (COMPLETED)
--   - Booking mới: b0000001-0000-0000-0000-000000000009 (CONFIRMED)
--
-- TỔNG QUAN UNREAD COUNT:
-- - Customer John Doe (c1000001-0000-0000-0000-000000000001): 
--     + Conv 2: 3 unread messages
--     + Total: 3 unread messages
-- - Employee Trần Văn Long (e1000001-0000-0000-0000-000000000003):
--     + Conv 3: 7 unread messages
--     + Total: 7 unread messages
-- =================================================================================
