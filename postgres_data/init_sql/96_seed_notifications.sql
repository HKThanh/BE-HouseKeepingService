-- Seed data: 96 Seed Notifications

-- Sample Notifications
-- =============================================

-- Notifications for john_doe (Customer)
INSERT INTO notifications (notification_id, account_id, type, title, message, related_id, related_type, is_read, priority, action_url, created_at) VALUES
('ntf00001-0000-0000-0000-000000000001', 'a1000001-0000-0000-0000-000000000001', 'BOOKING_CREATED', 'Đặt lịch thành công', 'Booking HKS000001 của bạn đã được tạo thành công và đang chờ xác minh.', 'b0000001-0000-0000-0000-000000000001', 'BOOKING', true, 'NORMAL', '/bookings/b0000001-0000-0000-0000-000000000001', '2025-10-25 09:00:00+07'),
('ntf00002-0000-0000-0000-000000000001', 'a1000001-0000-0000-0000-000000000001', 'BOOKING_CONFIRMED', 'Booking đã được xác nhận', 'Booking HKS000001 của bạn đã được xác nhận. Nhân viên sẽ đến đúng giờ đã hẹn.', 'b0000001-0000-0000-0000-000000000001', 'BOOKING', false, 'HIGH', '/bookings/b0000001-0000-0000-0000-000000000001', '2025-10-26 10:30:00+07'),
('ntf00003-0000-0000-0000-000000000001', 'a1000001-0000-0000-0000-000000000001', 'PAYMENT_SUCCESS', 'Thanh toán thành công', 'Thanh toán của bạn đã được xử lý thành công. Số tiền: 300,000 VND', 'pay00001-0000-0000-0000-000000000001', 'PAYMENT', true, 'NORMAL', '/payments/pay00001-0000-0000-0000-000000000001', '2025-10-26 11:00:00+07');

-- Notifications for jane_smith (Employee)
INSERT INTO notifications (notification_id, account_id, type, title, message, related_id, related_type, is_read, priority, action_url, created_at) VALUES
('ntf00004-0000-0000-0000-000000000001', 'a1000001-0000-0000-0000-000000000002', 'ASSIGNMENT_CREATED', 'Bạn có công việc mới', 'Bạn đã được phân công làm việc cho booking HKS000002. Vui lòng xem chi tiết.', 'as000001-0000-0000-0000-000000000002', 'ASSIGNMENT', false, 'HIGH', '/assignments/as000001-0000-0000-0000-000000000002', '2025-10-27 08:00:00+07'),
('ntf00005-0000-0000-0000-000000000001', 'a1000001-0000-0000-0000-000000000002', 'REVIEW_RECEIVED', 'Bạn nhận được đánh giá mới', 'Bạn đã nhận được đánh giá ⭐⭐⭐⭐⭐. Cảm ơn bạn đã sử dụng dịch vụ!', 'rev00001-0000-0000-0000-000000000001', 'REVIEW', true, 'NORMAL', '/reviews/rev00001-0000-0000-0000-000000000001', '2025-10-28 14:30:00+07');

-- Notifications for mary_jones (Customer)
INSERT INTO notifications (notification_id, account_id, type, title, message, related_id, related_type, is_read, priority, action_url, created_at) VALUES
('ntf00006-0000-0000-0000-000000000001', 'a1000001-0000-0000-0000-000000000004', 'BOOKING_CREATED', 'Đặt lịch thành công', 'Booking HKS000003 của bạn đã được tạo thành công và đang chờ xác minh.', 'book0003-0000-0000-0000-000000000001', 'BOOKING', false, 'NORMAL', '/bookings/book0003-0000-0000-0000-000000000001', '2025-10-28 10:00:00+07'),
('ntf00007-0000-0000-0000-000000000001', 'a1000001-0000-0000-0000-000000000004', 'BOOKING_VERIFIED', 'Bài post được chấp nhận', 'Bài post HKS000003 của bạn đã được Admin chấp nhận. Nhân viên có thể nhận việc.', 'book0003-0000-0000-0000-000000000001', 'BOOKING', false, 'HIGH', '/bookings/book0003-0000-0000-0000-000000000001', '2025-10-28 11:00:00+07');

-- Notifications for bob_wilson (Employee)
INSERT INTO notifications (notification_id, account_id, type, title, message, related_id, related_type, is_read, priority, action_url, created_at) VALUES
('ntf00008-0000-0000-0000-000000000001', 'a1000001-0000-0000-0000-000000000005', 'ASSIGNMENT_CREATED', 'Bạn có công việc mới', 'Bạn đã được phân công làm việc cho booking HKS000001. Vui lòng xem chi tiết.', 'as000001-0000-0000-0000-000000000001', 'ASSIGNMENT', true, 'HIGH', '/assignments/as000001-0000-0000-0000-000000000001', '2025-10-26 09:00:00+07'),
('ntf00009-0000-0000-0000-000000000001', 'a1000001-0000-0000-0000-000000000005', 'SYSTEM_ANNOUNCEMENT', 'Cập nhật hệ thống', 'Hệ thống sẽ bảo trì vào 2:00 AM ngày 01/11/2025. Vui lòng hoàn thành công việc trước thời gian này.', NULL, 'SYSTEM', false, 'NORMAL', NULL, '2025-10-29 18:00:00+07');

-- Crisis notification example
INSERT INTO notifications (notification_id, account_id, type, title, message, related_id, related_type, is_read, priority, action_url, created_at) VALUES
('ntf00010-0000-0000-0000-000000000001', 'a1000001-0000-0000-0000-000000000001', 'ASSIGNMENT_CRISIS', 'KHẨN CẤP: Nhân viên hủy công việc', 'Nhân viên đã hủy công việc cho booking HKS000004. Lý do: Bị ốm đột xuất. Vui lòng liên hệ ngay để được hỗ trợ.', 'book0004-0000-0000-0000-000000000001', 'BOOKING', false, 'URGENT', '/bookings/book0004-0000-0000-0000-000000000001', '2025-10-30 08:30:00+07');

-- Promotion notification
INSERT INTO notifications (notification_id, account_id, type, title, message, related_id, related_type, is_read, priority, action_url, created_at) VALUES
('ntf00011-0000-0000-0000-000000000001', 'a1000001-0000-0000-0000-000000000006', 'PROMOTION_AVAILABLE', 'Ưu đãi đặc biệt dành cho bạn', 'Giảm giá 20% cho dịch vụ tổng vệ sinh. Áp dụng đến hết ngày 15/11/2025. Đặt lịch ngay!', 'promo001-0000-0000-0000-000000000001', 'PROMOTION', false, 'NORMAL', '/promotions', '2025-10-29 09:00:00+07');

-- =================================================================================
-- THÊM DỮ LIỆU MẪU CHO TÍNH NĂNG CHAT REAL-TIME
-- =================================================================================
