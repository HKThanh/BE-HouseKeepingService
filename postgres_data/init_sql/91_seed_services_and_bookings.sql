-- Seed data: 91 Seed Services And Bookings

-- Khối II: Thêm dữ liệu cho Dịch vụ, Khuyến mãi và Đặt lịch
-- =================================================================================

-- XÓA DỮ LIỆU DỊCH VỤ CŨ ĐỂ CẬP NHẬT CẤU TRÚC MỚI
TRUNCATE TABLE service RESTART IDENTITY CASCADE;
-- TRUNCATE TABLE service_categories RESTART IDENTITY CASCADE; -- (Chạy nếu bảng đã tồn tại và có dữ liệu)

-- THÊM DỮ LIỆU MẪU CHO DANH MỤC VÀ DỊCH VỤ

-- Thêm các danh mục cha
INSERT INTO service_categories (category_name, description, icon_url) VALUES
('Dọn dẹp nhà', 'Các dịch vụ liên quan đến vệ sinh, làm sạch nhà cửa', 'https://res.cloudinary.com/dkzemgit8/image/upload/v1764171599/house_cleaning_nob-removebg-preview_amndgu.png'),
('Giặt ủi', 'Dịch vụ giặt sấy, ủi đồ chuyên nghiệp', 'https://res.cloudinary.com/dkzemgit8/image/upload/v1764171601/washing-removebg-preview_heihyo.png'),
('Việc nhà khác', 'Các dịch vụ tiện ích gia đình khác', 'https://res.cloudinary.com/dkzemgit8/image/upload/v1764171599/other_services-removebg-preview_fl88xu.png');

-- Thêm các dịch vụ con vào từng danh mục
-- Dữ liệu cho danh mục 'Dọn dẹp nhà' (category_id = 1)
INSERT INTO service (category_id, name, description, base_price, unit, estimated_duration_hours, recommended_staff, icon_url, is_active) VALUES
(1, 'Dọn dẹp theo giờ', 'Lau dọn, hút bụi, làm sạch các bề mặt cơ bản trong nhà. Phù hợp cho nhu cầu duy trì vệ sinh hàng tuần.', 50000, 'Giờ', 2.0, 1, 'https://res.cloudinary.com/dkzemgit8/image/upload/v1764171235/Cleaning_Clock-removebg-preview_o0oevs.png', TRUE),
(1, 'Tổng vệ sinh', 'Làm sạch sâu toàn diện, bao gồm các khu vực khó tiếp cận, trần nhà, lau cửa kính. Thích hợp cho nhà mới hoặc dọn dẹp theo mùa.', 100000, 'Gói', 2.0, 1, 'https://res.cloudinary.com/dkzemgit8/image/upload/v1764171599/house_cleaning_nob-removebg-preview_amndgu.png', TRUE),
(1, 'Vệ sinh Sofa - Nệm - Rèm', 'Giặt sạch và khử khuẩn Sofa, Nệm, Rèm cửa bằng máy móc chuyên dụng.', 300000, 'Gói', 3.0, 1, 'https://res.cloudinary.com/dkzemgit8/image/upload/v1764171601/sofa_bed-removebg-preview_uyom5g.png', TRUE),
(1, 'Vệ sinh máy lạnh', 'Bảo trì, làm sạch dàn nóng và dàn lạnh, bơm gas nếu cần.', 150000, 'Máy', 1.0, 1, 'https://res.cloudinary.com/dkzemgit8/image/upload/v1764171599/cooler-removebg-preview_trw5g2.png', TRUE);
-- Dữ liệu cho danh mục 'Giặt ủi' (category_id = 2)
INSERT INTO service (category_id, name, description, base_price, unit, estimated_duration_hours, recommended_staff, icon_url, is_active) VALUES
(2, 'Giặt sấy theo kg', 'Giặt và sấy khô quần áo thông thường, giao nhận tận nơi.', 30000, 'Kg', 24.0, 1, 'https://res.cloudinary.com/dkzemgit8/image/upload/v1764171600/shirt-removebg-preview_p8evky.png', TRUE),
(2, 'Giặt hấp cao cấp', 'Giặt khô cho các loại vải cao cấp như vest, áo dài, lụa.', 120000, 'Bộ', 48.0, 1, 'https://res.cloudinary.com/dkzemgit8/image/upload/v1764171601/vest_2-removebg-preview_olbnxs.png', TRUE);
-- Dữ liệu cho danh mục 'Việc nhà khác' (category_id = 3)
INSERT INTO service (category_id, name, description, base_price, unit, estimated_duration_hours, recommended_staff, icon_url, is_active) VALUES
(3, 'Nấu ăn gia đình', 'Đi chợ (chi phí thực phẩm tính riêng) và chuẩn bị bữa ăn cho gia đình theo thực đơn yêu cầu.', 60000, 'Giờ', 2.5, 1, 'https://res.cloudinary.com/dkzemgit8/image/upload/v1764171599/pan-removebg-preview_cpejtg.png', TRUE),
(3, 'Đi chợ hộ', 'Mua sắm và giao hàng tận nơi theo danh sách của bạn.', 40000, 'Lần', 1.0, 1, 'https://res.cloudinary.com/dkzemgit8/image/upload/v1764171600/shopping-removebg-preview_w5zvzl.png', TRUE);

-- Thêm các chương trình khuyến mãi
INSERT INTO promotions (promo_code, description, discount_type, discount_value, max_discount_amount, start_date, end_date, is_active) VALUES
('GIAM20K', 'Giảm giá 20,000đ cho mọi đơn hàng', 'FIXED_AMOUNT', 20000, NULL, '2025-11-01 00:00:00+07', '2025-12-30 23:59:59+07', TRUE),
('KHAITRUONG10', 'Giảm 10% mừng khai trương', 'PERCENTAGE', 10, 50000, '2025-11-01 00:00:00+07', '2025-12-31 23:59:59+07', TRUE);

-- Thêm các lịch đặt (bookings) mẫu
-- Một lịch đã HOÀN THÀNH của khách hàng 'John Doe'
-- Một lịch đã XÁC NHẬN của khách hàng 'Jane Smith Customer'
-- Thêm 5 booking PENDING chưa verify để test chức năng verify booking
INSERT INTO bookings (booking_id, customer_id, address_id, booking_code, booking_time, note, total_amount, status, promotion_id, is_verified) VALUES
('b0000001-0000-0000-0000-000000000001', 'c1000001-0000-0000-0000-000000000001', 'adrs0001-0000-0000-0000-000000000001', 'BK000001', '2025-08-20 09:00:00+07', 'Nhà có trẻ nhỏ, vui lòng lau dọn kỹ khu vực phòng khách.', 80000.00, 'COMPLETED', (SELECT promotion_id FROM promotions WHERE promo_code = 'GIAM20K'), true),
('b0000001-0000-0000-0000-000000000002', 'c1000001-0000-0000-0000-000000000003', 'adrs0001-0000-0000-0000-000000000003', 'BK000002', '2025-08-28 14:00:00+07', 'Vui lòng đến đúng giờ.', 90000.00, 'CONFIRMED', (SELECT promotion_id FROM promotions WHERE promo_code = 'KHAITRUONG10'), true),
-- 5 booking PENDING chưa verify (isVerified = false) để test
('b0000001-0000-0000-0000-000000000003', 'c1000001-0000-0000-0000-000000000004', 'adrs0001-0000-0000-0000-000000000009', 'BK000003', '2025-11-01 08:00:00+07', 'Cần vệ sinh tổng quát căn hộ 2 phòng ngủ.', 500000.00, 'PENDING', NULL, false),
('b0000001-0000-0000-0000-000000000004', 'c1000001-0000-0000-0000-000000000005', 'adrs0001-0000-0000-0000-000000000010', 'BK000004', '2025-11-02 10:00:00+07', 'Giặt ủi 10kg quần áo gia đình.', 300000.00, 'PENDING', NULL, false),
('b0000001-0000-0000-0000-000000000005', 'c1000001-0000-0000-0000-000000000006', 'adrs0001-0000-0000-0000-000000000011', 'BK000005', '2025-11-03 14:00:00+07', 'Vệ sinh sofa 3 chỗ và 2 ghế đơn.', 350000.00, 'PENDING', NULL, false),
('b0000001-0000-0000-0000-000000000006', 'c1000001-0000-0000-0000-000000000007', 'adrs0001-0000-0000-0000-000000000012', 'BK000006', '2025-11-04 09:30:00+07', 'Vệ sinh 2 máy lạnh trong phòng.', 400000.00, 'PENDING', NULL, false),
('b0000001-0000-0000-0000-000000000007', 'c1000001-0000-0000-0000-000000000008', 'adrs0001-0000-0000-0000-000000000013', 'BK000007', '2025-11-05 11:00:00+07', 'Nấu ăn cho gia đình 6 người, 2 bữa.', 320000.00, 'PENDING', NULL, false);

-- Thêm chi tiết dịch vụ cho các lịch đặt
INSERT INTO booking_details (booking_detail_id, booking_id, service_id, quantity, price_per_unit, sub_total) VALUES
('bd000001-0000-0000-0000-000000000001', 'b0000001-0000-0000-0000-000000000001', (SELECT service_id FROM service WHERE name = 'Tổng vệ sinh'), 1, 100000.00, 100000.00),
('bd000001-0000-0000-0000-000000000002', 'b0000001-0000-0000-0000-000000000002', (SELECT service_id FROM service WHERE name = 'Dọn dẹp theo giờ'), 2, 50000.00, 100000.00),
-- Chi tiết cho 5 booking PENDING chưa verify
('bd000001-0000-0000-0000-000000000003', 'b0000001-0000-0000-0000-000000000003', (SELECT service_id FROM service WHERE name = 'Tổng vệ sinh'), 1, 500000.00, 500000.00),
('bd000001-0000-0000-0000-000000000004', 'b0000001-0000-0000-0000-000000000004', (SELECT service_id FROM service WHERE name = 'Giặt sấy theo kg'), 10, 30000.00, 300000.00),
('bd000001-0000-0000-0000-000000000005', 'b0000001-0000-0000-0000-000000000005', (SELECT service_id FROM service WHERE name = 'Vệ sinh Sofa - Nệm - Rèm'), 1, 350000.00, 350000.00),
('bd000001-0000-0000-0000-000000000006', 'b0000001-0000-0000-0000-000000000006', (SELECT service_id FROM service WHERE name = 'Vệ sinh máy lạnh'), 2, 200000.00, 400000.00),
('bd000001-0000-0000-0000-000000000007', 'b0000001-0000-0000-0000-000000000007', (SELECT service_id FROM service WHERE name = 'Nấu ăn gia đình'), 4, 80000.00, 320000.00);

-- Phân công nhân viên cho các lịch đặt
INSERT INTO assignments (assignment_id, booking_detail_id, employee_id, status, check_in_time, check_out_time) VALUES
('as000001-0000-0000-0000-000000000001', 'bd000001-0000-0000-0000-000000000001', 'e1000001-0000-0000-0000-000000000002', 'COMPLETED', '2025-08-20 09:00:00+07', '2025-08-20 13:00:00+07'),
('as000001-0000-0000-0000-000000000002', 'bd000001-0000-0000-0000-000000000002', 'e1000001-0000-0000-0000-000000000001', 'ASSIGNED', NULL, NULL);

-- =================================================================================
-- THÊM DỮ LIỆU TEST CHO CHỨC NĂNG ASSIGNMENT (GET, ACCEPT, CANCEL)
-- =================================================================================

-- Thêm các booking mới với thời gian trong tương lai để test
-- Booking 8: Có assignment PENDING (test accept assignment)
INSERT INTO bookings (booking_id, customer_id, address_id, booking_code, booking_time, note, total_amount, status, promotion_id, is_verified) VALUES
('b0000001-0000-0000-0000-000000000008', 'c1000001-0000-0000-0000-000000000001', 'adrs0001-0000-0000-0000-000000000001', 'BK000008', '2025-11-15 09:00:00+07', 'Test assignment PENDING - Dọn dẹp căn hộ', 200000.00, 'PENDING', NULL, true);

-- Booking 9: Có assignment PENDING (test cancel trước 30 phút)
INSERT INTO bookings (booking_id, customer_id, address_id, booking_code, booking_time, note, total_amount, status, promotion_id, is_verified) VALUES
('b0000001-0000-0000-0000-000000000009', 'c1000001-0000-0000-0000-000000000003', 'adrs0001-0000-0000-0000-000000000003', 'BK000009', '2025-11-20 14:00:00+07', 'Test cancel assignment - Giặt ủi', 150000.00, 'PENDING', NULL, true);

-- Booking 10: Có assignment ASSIGNED (test cancel assignment đã assigned)
INSERT INTO bookings (booking_id, customer_id, address_id, booking_code, booking_time, note, total_amount, status, promotion_id, is_verified) VALUES
('b0000001-0000-0000-0000-000000000010', 'c1000001-0000-0000-0000-000000000004', 'adrs0001-0000-0000-0000-000000000009', 'BK000010', '2025-11-18 10:00:00+07', 'Test assignment ASSIGNED - Vệ sinh máy lạnh', 300000.00, 'CONFIRMED', NULL, true);

-- Booking 11: Có nhiều assignment PENDING (test accept nhiều assignment)
INSERT INTO bookings (booking_id, customer_id, address_id, booking_code, booking_time, note, total_amount, status, promotion_id, is_verified) VALUES
('b0000001-0000-0000-0000-000000000011', 'c1000001-0000-0000-0000-000000000005', 'adrs0001-0000-0000-0000-000000000010', 'BK000011', '2025-11-25 08:00:00+07', 'Test nhiều assignments - Tổng vệ sinh căn hộ lớn', 800000.00, 'PENDING', NULL, true);

-- Booking 12: Assignment IN_PROGRESS (test get assignment đang làm việc)
INSERT INTO bookings (booking_id, customer_id, address_id, booking_code, booking_time, note, total_amount, status, promotion_id, is_verified) VALUES
('b0000001-0000-0000-0000-000000000012', 'c1000001-0000-0000-0000-000000000006', 'adrs0001-0000-0000-0000-000000000011', 'BK000012', '2025-11-07 08:00:00+07', 'Test assignment IN_PROGRESS', 250000.00, 'IN_PROGRESS', NULL, true);

-- Booking 13: Assignment gần thời gian bắt đầu (test cancel không được do < 30 phút)
INSERT INTO bookings (booking_id, customer_id, address_id, booking_code, booking_time, note, total_amount, status, promotion_id, is_verified) VALUES
('b0000001-0000-0000-0000-000000000013', 'c1000001-0000-0000-0000-000000000007', 'adrs0001-0000-0000-0000-000000000012', 'BK000013', '2025-11-07 14:30:00+07', 'Test cancel gần giờ - không được phép', 180000.00, 'PENDING', NULL, true);

-- Chi tiết dịch vụ cho các booking test
INSERT INTO booking_details (booking_detail_id, booking_id, service_id, quantity, price_per_unit, sub_total) VALUES
-- Booking 8: 1 dịch vụ dọn dẹp theo giờ
('bd000001-0000-0000-0000-000000000008', 'b0000001-0000-0000-0000-000000000008', (SELECT service_id FROM service WHERE name = 'Dọn dẹp theo giờ'), 4, 50000.00, 200000.00),
-- Booking 9: 1 dịch vụ giặt sấy
('bd000001-0000-0000-0000-000000000009', 'b0000001-0000-0000-0000-000000000009', (SELECT service_id FROM service WHERE name = 'Giặt sấy theo kg'), 5, 30000.00, 150000.00),
-- Booking 10: 2 máy lạnh
('bd000001-0000-0000-0000-000000000010', 'b0000001-0000-0000-0000-000000000010', (SELECT service_id FROM service WHERE name = 'Vệ sinh máy lạnh'), 2, 150000.00, 300000.00),
-- Booking 11: Tổng vệ sinh + Vệ sinh sofa (cần nhiều nhân viên)
('bd000001-0000-0000-0000-000000000011', 'b0000001-0000-0000-0000-000000000011', (SELECT service_id FROM service WHERE name = 'Tổng vệ sinh'), 2, 100000.00, 200000.00),
('bd000001-0000-0000-0000-000000000012', 'b0000001-0000-0000-0000-000000000011', (SELECT service_id FROM service WHERE name = 'Vệ sinh Sofa - Nệm - Rèm'), 2, 300000.00, 600000.00),
-- Booking 12: Dọn dẹp theo giờ
('bd000001-0000-0000-0000-000000000013', 'b0000001-0000-0000-0000-000000000012', (SELECT service_id FROM service WHERE name = 'Dọn dẹp theo giờ'), 5, 50000.00, 250000.00),
-- Booking 13: Giặt hấp cao cấp
('bd000001-0000-0000-0000-000000000014', 'b0000001-0000-0000-0000-000000000013', (SELECT service_id FROM service WHERE name = 'Giặt hấp cao cấp'), 1, 120000.00, 120000.00);

-- Phân công assignments cho các booking test
INSERT INTO assignments (assignment_id, booking_detail_id, employee_id, status, check_in_time, check_out_time) VALUES
-- Booking 8: 1 assignment PENDING (test accept)
('as000001-0000-0000-0000-000000000003', 'bd000001-0000-0000-0000-000000000008', 'e1000001-0000-0000-0000-000000000001', 'PENDING', NULL, NULL),

-- Booking 9: 1 assignment PENDING (test cancel - thời gian xa)
('as000001-0000-0000-0000-000000000004', 'bd000001-0000-0000-0000-000000000009', 'e1000001-0000-0000-0000-000000000002', 'PENDING', NULL, NULL),

-- Booking 10: 1 assignment ASSIGNED (test cancel assigned)
('as000001-0000-0000-0000-000000000005', 'bd000001-0000-0000-0000-000000000010', 'e1000001-0000-0000-0000-000000000001', 'ASSIGNED', NULL, NULL),

-- Booking 11: 3 assignments PENDING (test accept nhiều assignments)
('as000001-0000-0000-0000-000000000006', 'bd000001-0000-0000-0000-000000000011', 'e1000001-0000-0000-0000-000000000001', 'PENDING', NULL, NULL),
('as000001-0000-0000-0000-000000000007', 'bd000001-0000-0000-0000-000000000011', 'e1000001-0000-0000-0000-000000000002', 'PENDING', NULL, NULL),
('as000001-0000-0000-0000-000000000008', 'bd000001-0000-0000-0000-000000000012', 'e1000001-0000-0000-0000-000000000003', 'PENDING', NULL, NULL),

-- Booking 12: 1 assignment IN_PROGRESS (đang làm việc)
('as000001-0000-0000-0000-000000000009', 'bd000001-0000-0000-0000-000000000013', 'e1000001-0000-0000-0000-000000000001', 'IN_PROGRESS', '2025-11-07 08:00:00+07', NULL),

-- Booking 13: 1 assignment PENDING gần giờ (test cancel fail do < 30 phút)
('as000001-0000-0000-0000-000000000010', 'bd000001-0000-0000-0000-000000000014', 'e1000001-0000-0000-0000-000000000002', 'PENDING', NULL, NULL);

-- =================================================================================
-- THÊM DỮ LIỆU TEST CHO CHỨC NĂNG CHECK-IN ASSIGNMENT (18h30-21h hôm nay)
-- =================================================================================

-- Booking 29-38: 10 booking vào khung giờ 18:30-21:00 ngày 07/11/2025 để test check-in
INSERT INTO bookings (booking_id, customer_id, address_id, booking_code, booking_time, note, total_amount, status, promotion_id, is_verified) VALUES
('b0000001-0000-0000-0000-000000000029', 'c1000001-0000-0000-0000-000000000001', 'adrs0001-0000-0000-0000-000000000001', 'BK000029', '2025-11-07 18:30:00+07', 'Test check-in 18h30 - Dọn dẹp căn hộ', 200000.00, 'CONFIRMED', NULL, true),
('b0000001-0000-0000-0000-000000000030', 'c1000001-0000-0000-0000-000000000003', 'adrs0001-0000-0000-0000-000000000003', 'BK000030', '2025-11-07 18:45:00+07', 'Test check-in 18h45 - Tổng vệ sinh', 500000.00, 'CONFIRMED', NULL, true),
('b0000001-0000-0000-0000-000000000031', 'c1000001-0000-0000-0000-000000000004', 'adrs0001-0000-0000-0000-000000000009', 'BK000031', '2025-11-07 19:00:00+07', 'Test check-in 19h - Vệ sinh máy lạnh', 300000.00, 'CONFIRMED', NULL, true),
('b0000001-0000-0000-0000-000000000032', 'c1000001-0000-0000-0000-000000000005', 'adrs0001-0000-0000-0000-000000000010', 'BK000032', '2025-11-07 19:15:00+07', 'Test check-in 19h15 - Giặt ủi', 240000.00, 'CONFIRMED', NULL, true),
('b0000001-0000-0000-0000-000000000033', 'c1000001-0000-0000-0000-000000000006', 'adrs0001-0000-0000-0000-000000000011', 'BK000033', '2025-11-07 19:30:00+07', 'Test check-in 19h30 - Vệ sinh sofa', 300000.00, 'CONFIRMED', NULL, true),
('b0000001-0000-0000-0000-000000000034', 'c1000001-0000-0000-0000-000000000007', 'adrs0001-0000-0000-0000-000000000012', 'BK000034', '2025-11-07 19:45:00+07', 'Test check-in 19h45 - Nấu ăn', 240000.00, 'CONFIRMED', NULL, true),
('b0000001-0000-0000-0000-000000000035', 'c1000001-0000-0000-0000-000000000008', 'adrs0001-0000-0000-0000-000000000013', 'BK000035', '2025-11-07 20:00:00+07', 'Test check-in 20h - Dọn dẹp theo giờ', 150000.00, 'CONFIRMED', NULL, true),
('b0000001-0000-0000-0000-000000000036', 'c1000001-0000-0000-0000-000000000001', 'adrs0001-0000-0000-0000-000000000001', 'BK000036', '2025-11-07 20:15:00+07', 'Test check-in 20h15 - Giặt hấp cao cấp', 360000.00, 'CONFIRMED', NULL, true),
('b0000001-0000-0000-0000-000000000037', 'c1000001-0000-0000-0000-000000000003', 'adrs0001-0000-0000-0000-000000000003', 'BK000037', '2025-11-07 20:30:00+07', 'Test check-in 20h30 - Đi chợ hộ + Nấu ăn', 280000.00, 'CONFIRMED', NULL, true),
('b0000001-0000-0000-0000-000000000038', 'c1000001-0000-0000-0000-000000000004', 'adrs0001-0000-0000-0000-000000000009', 'BK000038', '2025-11-07 21:00:00+07', 'Test check-in 21h - Tổng vệ sinh + Vệ sinh máy lạnh', 550000.00, 'CONFIRMED', NULL, true);

-- =================================================================================
-- THÊM DỮ LIỆU TEST CHO CHỨC NĂNG CHECK-IN ASSIGNMENT (17h-18h hôm nay)
-- =================================================================================

-- Booking 39-43: 5 booking vào khung giờ 17:00-18:00 ngày 07/11/2025 để test check-in
INSERT INTO bookings (booking_id, customer_id, address_id, booking_code, booking_time, note, total_amount, status, promotion_id, is_verified) VALUES
('b0000001-0000-0000-0000-000000000039', 'c1000001-0000-0000-0000-000000000001', 'adrs0001-0000-0000-0000-000000000001', 'BK000039', '2025-11-07 17:00:00+07', 'Test check-in 17h - Dọn dẹp căn hộ', 150000.00, 'CONFIRMED', NULL, true),
('b0000001-0000-0000-0000-000000000040', 'c1000001-0000-0000-0000-000000000003', 'adrs0001-0000-0000-0000-000000000003', 'BK000040', '2025-11-07 17:15:00+07', 'Test check-in 17h15 - Vệ sinh máy lạnh', 300000.00, 'CONFIRMED', NULL, true),
('b0000001-0000-0000-0000-000000000041', 'c1000001-0000-0000-0000-000000000004', 'adrs0001-0000-0000-0000-000000000009', 'BK000041', '2025-11-07 17:30:00+07', 'Test check-in 17h30 - Giặt ủi', 180000.00, 'CONFIRMED', NULL, true),
('b0000001-0000-0000-0000-000000000042', 'c1000001-0000-0000-0000-000000000005', 'adrs0001-0000-0000-0000-000000000010', 'BK000042', '2025-11-07 17:45:00+07', 'Test check-in 17h45 - Tổng vệ sinh', 200000.00, 'CONFIRMED', NULL, true),
('b0000001-0000-0000-0000-000000000043', 'c1000001-0000-0000-0000-000000000006', 'adrs0001-0000-0000-0000-000000000011', 'BK000043', '2025-11-07 18:00:00+07', 'Test check-in 18h - Vệ sinh sofa', 300000.00, 'CONFIRMED', NULL, true);

-- =================================================================================
-- THÊM DỮ LIỆU TEST CHO CHỨC NĂNG CHECK-IN ASSIGNMENT (21h-22h hôm nay)
-- =================================================================================

-- Booking 44-53: 10 booking vào khung giờ 21:00-22:00 ngày 07/11/2025 để test check-in
INSERT INTO bookings (booking_id, customer_id, address_id, booking_code, booking_time, note, total_amount, status, promotion_id, is_verified) VALUES
('b0000001-0000-0000-0000-000000000044', 'c1000001-0000-0000-0000-000000000001', 'adrs0001-0000-0000-0000-000000000001', 'BK000044', '2025-11-07 21:00:00+07', 'Test check-in 21h - Dọn dẹp theo giờ', 200000.00, 'CONFIRMED', NULL, true),
('b0000001-0000-0000-0000-000000000045', 'c1000001-0000-0000-0000-000000000003', 'adrs0001-0000-0000-0000-000000000003', 'BK000045', '2025-11-07 21:15:00+07', 'Test check-in 21h15 - Nấu ăn gia đình', 180000.00, 'CONFIRMED', NULL, true),
('b0000001-0000-0000-0000-000000000046', 'c1000001-0000-0000-0000-000000000004', 'adrs0001-0000-0000-0000-000000000009', 'BK000046', '2025-11-07 21:30:00+07', 'Test check-in 21h30 - Vệ sinh máy lạnh', 300000.00, 'CONFIRMED', NULL, true),
('b0000001-0000-0000-0000-000000000047', 'c1000001-0000-0000-0000-000000000005', 'adrs0001-0000-0000-0000-000000000010', 'BK000047', '2025-11-07 21:45:00+07', 'Test check-in 21h45 - Giặt sấy', 150000.00, 'CONFIRMED', NULL, true),
('b0000001-0000-0000-0000-000000000048', 'c1000001-0000-0000-0000-000000000006', 'adrs0001-0000-0000-0000-000000000011', 'BK000048', '2025-11-07 22:00:00+07', 'Test check-in 22h - Tổng vệ sinh', 200000.00, 'CONFIRMED', NULL, true),
('b0000001-0000-0000-0000-000000000049', 'c1000001-0000-0000-0000-000000000007', 'adrs0001-0000-0000-0000-000000000012', 'BK000049', '2025-11-07 21:05:00+07', 'Test check-in 21h05 - Vệ sinh sofa', 300000.00, 'CONFIRMED', NULL, true),
('b0000001-0000-0000-0000-000000000050', 'c1000001-0000-0000-0000-000000000008', 'adrs0001-0000-0000-0000-000000000013', 'BK000050', '2025-11-07 21:20:00+07', 'Test check-in 21h20 - Đi chợ hộ', 80000.00, 'CONFIRMED', NULL, true),
('b0000001-0000-0000-0000-000000000051', 'c1000001-0000-0000-0000-000000000001', 'adrs0001-0000-0000-0000-000000000001', 'BK000051', '2025-11-07 21:35:00+07', 'Test check-in 21h35 - Giặt hấp cao cấp', 240000.00, 'CONFIRMED', NULL, true),
('b0000001-0000-0000-0000-000000000052', 'c1000001-0000-0000-0000-000000000003', 'adrs0001-0000-0000-0000-000000000003', 'BK000052', '2025-11-07 21:50:00+07', 'Test check-in 21h50 - Dọn dẹp theo giờ', 150000.00, 'CONFIRMED', NULL, true),
('b0000001-0000-0000-0000-000000000053', 'c1000001-0000-0000-0000-000000000004', 'adrs0001-0000-0000-0000-000000000009', 'BK000053', '2025-11-07 21:55:00+07', 'Test check-in 21h55 - Vệ sinh máy lạnh', 300000.00, 'CONFIRMED', NULL, true);

-- =================================================================================
-- THÊM DỮ LIỆU BOOKING ĐÃ HOÀN THÀNH (COMPLETED) THÁNG 11 VÀ THÁNG 12/2025
-- =================================================================================

-- Booking 54-63: 10 booking đã hoàn thành trong tháng 11 và 12/2025
INSERT INTO bookings (booking_id, customer_id, address_id, booking_code, booking_time, note, total_amount, status, promotion_id, is_verified) VALUES
('b0000001-0000-0000-0000-000000000054', 'c1000001-0000-0000-0000-000000000001', 'adrs0001-0000-0000-0000-000000000001', 'BK000054', '2025-11-01 09:00:00+07', 'Booking hoàn thành - Dọn dẹp căn hộ', 200000.00, 'COMPLETED', NULL, true),
('b0000001-0000-0000-0000-000000000055', 'c1000001-0000-0000-0000-000000000003', 'adrs0001-0000-0000-0000-000000000003', 'BK000055', '2025-11-02 14:00:00+07', 'Booking hoàn thành - Vệ sinh máy lạnh', 450000.00, 'COMPLETED', NULL, true),
('b0000001-0000-0000-0000-000000000056', 'c1000001-0000-0000-0000-000000000004', 'adrs0001-0000-0000-0000-000000000009', 'BK000056', '2025-11-03 10:00:00+07', 'Booking hoàn thành - Tổng vệ sinh', 500000.00, 'COMPLETED', NULL, true),
('b0000001-0000-0000-0000-000000000057', 'c1000001-0000-0000-0000-000000000005', 'adrs0001-0000-0000-0000-000000000010', 'BK000057', '2025-11-04 15:30:00+07', 'Booking hoàn thành - Giặt sấy', 240000.00, 'COMPLETED', NULL, true),
('b0000001-0000-0000-0000-000000000058', 'c1000001-0000-0000-0000-000000000006', 'adrs0001-0000-0000-0000-000000000011', 'BK000058', '2025-11-05 08:00:00+07', 'Booking hoàn thành - Vệ sinh sofa', 300000.00, 'COMPLETED', NULL, true),
('b0000001-0000-0000-0000-000000000059', 'c1000001-0000-0000-0000-000000000007', 'adrs0001-0000-0000-0000-000000000012', 'BK000059', '2025-11-06 11:00:00+07', 'Booking hoàn thành - Nấu ăn gia đình', 180000.00, 'COMPLETED', NULL, true),
('b0000001-0000-0000-0000-000000000060', 'c1000001-0000-0000-0000-000000000008', 'adrs0001-0000-0000-0000-000000000013', 'BK000060', '2025-12-01 09:30:00+07', 'Booking hoàn thành - Dọn dẹp theo giờ', 150000.00, 'COMPLETED', NULL, true),
('b0000001-0000-0000-0000-000000000061', 'c1000001-0000-0000-0000-000000000001', 'adrs0001-0000-0000-0000-000000000001', 'BK000061', '2025-12-02 13:00:00+07', 'Booking hoàn thành - Giặt hấp cao cấp', 360000.00, 'COMPLETED', NULL, true),
('b0000001-0000-0000-0000-000000000062', 'c1000001-0000-0000-0000-000000000003', 'adrs0001-0000-0000-0000-000000000003', 'BK000062', '2025-12-03 10:00:00+07', 'Booking hoàn thành - Vệ sinh máy lạnh + Tổng vệ sinh', 650000.00, 'COMPLETED', NULL, true),
('b0000001-0000-0000-0000-000000000063', 'c1000001-0000-0000-0000-000000000004', 'adrs0001-0000-0000-0000-000000000009', 'BK000063', '2025-12-04 14:30:00+07', 'Booking hoàn thành - Đi chợ hộ + Nấu ăn', 220000.00, 'COMPLETED', NULL, true);

-- Chi tiết dịch vụ cho các booking test check-in
INSERT INTO booking_details (booking_detail_id, booking_id, service_id, quantity, price_per_unit, sub_total) VALUES
-- Booking 29: Dọn dẹp theo giờ (18h30)
('bd000001-0000-0000-0000-000000000029', 'b0000001-0000-0000-0000-000000000029', (SELECT service_id FROM service WHERE name = 'Dọn dẹp theo giờ'), 4, 50000.00, 200000.00),
-- Booking 30: Tổng vệ sinh (18h45)
('bd000001-0000-0000-0000-000000000030', 'b0000001-0000-0000-0000-000000000030', (SELECT service_id FROM service WHERE name = 'Tổng vệ sinh'), 5, 100000.00, 500000.00),
-- Booking 31: Vệ sinh máy lạnh (19h)
('bd000001-0000-0000-0000-000000000031', 'b0000001-0000-0000-0000-000000000031', (SELECT service_id FROM service WHERE name = 'Vệ sinh máy lạnh'), 2, 150000.00, 300000.00),
-- Booking 32: Giặt sấy (19h15)
('bd000001-0000-0000-0000-000000000032', 'b0000001-0000-0000-0000-000000000032', (SELECT service_id FROM service WHERE name = 'Giặt sấy theo kg'), 8, 30000.00, 240000.00),
-- Booking 33: Vệ sinh sofa (19h30)
('bd000001-0000-0000-0000-000000000033', 'b0000001-0000-0000-0000-000000000033', (SELECT service_id FROM service WHERE name = 'Vệ sinh Sofa - Nệm - Rèm'), 1, 300000.00, 300000.00),
-- Booking 34: Nấu ăn (19h45)
('bd000001-0000-0000-0000-000000000034', 'b0000001-0000-0000-0000-000000000034', (SELECT service_id FROM service WHERE name = 'Nấu ăn gia đình'), 4, 60000.00, 240000.00),
-- Booking 35: Dọn dẹp theo giờ (20h)
('bd000001-0000-0000-0000-000000000035', 'b0000001-0000-0000-0000-000000000035', (SELECT service_id FROM service WHERE name = 'Dọn dẹp theo giờ'), 3, 50000.00, 150000.00),
-- Booking 36: Giặt hấp cao cấp (20h15)
('bd000001-0000-0000-0000-000000000036', 'b0000001-0000-0000-0000-000000000036', (SELECT service_id FROM service WHERE name = 'Giặt hấp cao cấp'), 3, 120000.00, 360000.00),
-- Booking 37: Đi chợ hộ + Nấu ăn (20h30)
('bd000001-0000-0000-0000-000000000037', 'b0000001-0000-0000-0000-000000000037', (SELECT service_id FROM service WHERE name = 'Đi chợ hộ'), 2, 40000.00, 80000.00),
('bd000001-0000-0000-0000-000000000038', 'b0000001-0000-0000-0000-000000000037', (SELECT service_id FROM service WHERE name = 'Nấu ăn gia đình'), 4, 50000.00, 200000.00),
-- Booking 38: Tổng vệ sinh + Vệ sinh máy lạnh (21h)
('bd000001-0000-0000-0000-000000000039', 'b0000001-0000-0000-0000-000000000038', (SELECT service_id FROM service WHERE name = 'Tổng vệ sinh'), 2, 100000.00, 200000.00),
('bd000001-0000-0000-0000-000000000040', 'b0000001-0000-0000-0000-000000000038', (SELECT service_id FROM service WHERE name = 'Vệ sinh máy lạnh'), 2, 175000.00, 350000.00),
-- Booking 39: Dọn dẹp theo giờ (17h)
('bd000001-0000-0000-0000-000000000041', 'b0000001-0000-0000-0000-000000000039', (SELECT service_id FROM service WHERE name = 'Dọn dẹp theo giờ'), 3, 50000.00, 150000.00),
-- Booking 40: Vệ sinh máy lạnh (17h15)
('bd000001-0000-0000-0000-000000000042', 'b0000001-0000-0000-0000-000000000040', (SELECT service_id FROM service WHERE name = 'Vệ sinh máy lạnh'), 2, 150000.00, 300000.00),
-- Booking 41: Giặt sấy (17h30)
('bd000001-0000-0000-0000-000000000043', 'b0000001-0000-0000-0000-000000000041', (SELECT service_id FROM service WHERE name = 'Giặt sấy theo kg'), 6, 30000.00, 180000.00),
-- Booking 42: Tổng vệ sinh (17h45)
('bd000001-0000-0000-0000-000000000044', 'b0000001-0000-0000-0000-000000000042', (SELECT service_id FROM service WHERE name = 'Tổng vệ sinh'), 2, 100000.00, 200000.00),
-- Booking 43: Vệ sinh sofa (18h)
('bd000001-0000-0000-0000-000000000045', 'b0000001-0000-0000-0000-000000000043', (SELECT service_id FROM service WHERE name = 'Vệ sinh Sofa - Nệm - Rèm'), 1, 300000.00, 300000.00),
-- Booking 44: Dọn dẹp theo giờ (21h)
('bd000001-0000-0000-0000-000000000046', 'b0000001-0000-0000-0000-000000000044', (SELECT service_id FROM service WHERE name = 'Dọn dẹp theo giờ'), 4, 50000.00, 200000.00),
-- Booking 45: Nấu ăn (21h15)
('bd000001-0000-0000-0000-000000000047', 'b0000001-0000-0000-0000-000000000045', (SELECT service_id FROM service WHERE name = 'Nấu ăn gia đình'), 3, 60000.00, 180000.00),
-- Booking 46: Vệ sinh máy lạnh (21h30)
('bd000001-0000-0000-0000-000000000048', 'b0000001-0000-0000-0000-000000000046', (SELECT service_id FROM service WHERE name = 'Vệ sinh máy lạnh'), 2, 150000.00, 300000.00),
-- Booking 47: Giặt sấy (21h45)
('bd000001-0000-0000-0000-000000000049', 'b0000001-0000-0000-0000-000000000047', (SELECT service_id FROM service WHERE name = 'Giặt sấy theo kg'), 5, 30000.00, 150000.00),
-- Booking 48: Tổng vệ sinh (22h)
('bd000001-0000-0000-0000-000000000050', 'b0000001-0000-0000-0000-000000000048', (SELECT service_id FROM service WHERE name = 'Tổng vệ sinh'), 2, 100000.00, 200000.00),
-- Booking 49: Vệ sinh sofa (21h05)
('bd000001-0000-0000-0000-000000000051', 'b0000001-0000-0000-0000-000000000049', (SELECT service_id FROM service WHERE name = 'Vệ sinh Sofa - Nệm - Rèm'), 1, 300000.00, 300000.00),
-- Booking 50: Đi chợ hộ (21h20)
('bd000001-0000-0000-0000-000000000052', 'b0000001-0000-0000-0000-000000000050', (SELECT service_id FROM service WHERE name = 'Đi chợ hộ'), 2, 40000.00, 80000.00),
-- Booking 51: Giặt hấp cao cấp (21h35)
('bd000001-0000-0000-0000-000000000053', 'b0000001-0000-0000-0000-000000000051', (SELECT service_id FROM service WHERE name = 'Giặt hấp cao cấp'), 2, 120000.00, 240000.00),
-- Booking 52: Dọn dẹp theo giờ (21h50)
('bd000001-0000-0000-0000-000000000054', 'b0000001-0000-0000-0000-000000000052', (SELECT service_id FROM service WHERE name = 'Dọn dẹp theo giờ'), 3, 50000.00, 150000.00),
-- Booking 53: Vệ sinh máy lạnh (21h55)
('bd000001-0000-0000-0000-000000000055', 'b0000001-0000-0000-0000-000000000053', (SELECT service_id FROM service WHERE name = 'Vệ sinh máy lạnh'), 2, 150000.00, 300000.00),
-- Booking 54: Dọn dẹp theo giờ (Tháng 11 - COMPLETED)
('bd000001-0000-0000-0000-000000000056', 'b0000001-0000-0000-0000-000000000054', (SELECT service_id FROM service WHERE name = 'Dọn dẹp theo giờ'), 4, 50000.00, 200000.00),
-- Booking 55: Vệ sinh máy lạnh (Tháng 11 - COMPLETED)
('bd000001-0000-0000-0000-000000000057', 'b0000001-0000-0000-0000-000000000055', (SELECT service_id FROM service WHERE name = 'Vệ sinh máy lạnh'), 3, 150000.00, 450000.00),
-- Booking 56: Tổng vệ sinh (Tháng 11 - COMPLETED)
('bd000001-0000-0000-0000-000000000058', 'b0000001-0000-0000-0000-000000000056', (SELECT service_id FROM service WHERE name = 'Tổng vệ sinh'), 5, 100000.00, 500000.00),
-- Booking 57: Giặt sấy (Tháng 11 - COMPLETED)
('bd000001-0000-0000-0000-000000000059', 'b0000001-0000-0000-0000-000000000057', (SELECT service_id FROM service WHERE name = 'Giặt sấy theo kg'), 8, 30000.00, 240000.00),
-- Booking 58: Vệ sinh sofa (Tháng 11 - COMPLETED)
('bd000001-0000-0000-0000-000000000060', 'b0000001-0000-0000-0000-000000000058', (SELECT service_id FROM service WHERE name = 'Vệ sinh Sofa - Nệm - Rèm'), 1, 300000.00, 300000.00),
-- Booking 59: Nấu ăn gia đình (Tháng 11 - COMPLETED)
('bd000001-0000-0000-0000-000000000061', 'b0000001-0000-0000-0000-000000000059', (SELECT service_id FROM service WHERE name = 'Nấu ăn gia đình'), 3, 60000.00, 180000.00),
-- Booking 60: Dọn dẹp theo giờ (Tháng 12 - COMPLETED)
('bd000001-0000-0000-0000-000000000062', 'b0000001-0000-0000-0000-000000000060', (SELECT service_id FROM service WHERE name = 'Dọn dẹp theo giờ'), 3, 50000.00, 150000.00),
-- Booking 61: Giặt hấp cao cấp (Tháng 12 - COMPLETED)
('bd000001-0000-0000-0000-000000000063', 'b0000001-0000-0000-0000-000000000061', (SELECT service_id FROM service WHERE name = 'Giặt hấp cao cấp'), 3, 120000.00, 360000.00),
-- Booking 62: Vệ sinh máy lạnh + Tổng vệ sinh (Tháng 12 - COMPLETED)
('bd000001-0000-0000-0000-000000000064', 'b0000001-0000-0000-0000-000000000062', (SELECT service_id FROM service WHERE name = 'Vệ sinh máy lạnh'), 2, 150000.00, 300000.00),
('bd000001-0000-0000-0000-000000000065', 'b0000001-0000-0000-0000-000000000062', (SELECT service_id FROM service WHERE name = 'Tổng vệ sinh'), 3, 100000.00, 300000.00),
-- Booking 63: Đi chợ hộ + Nấu ăn (Tháng 12 - COMPLETED)
('bd000001-0000-0000-0000-000000000066', 'b0000001-0000-0000-0000-000000000063', (SELECT service_id FROM service WHERE name = 'Đi chợ hộ'), 2, 40000.00, 80000.00),
('bd000001-0000-0000-0000-000000000067', 'b0000001-0000-0000-0000-000000000063', (SELECT service_id FROM service WHERE name = 'Nấu ăn gia đình'), 3, 60000.00, 180000.00);

-- Assignments cho các booking check-in test (18h30-21h hôm nay)
INSERT INTO assignments (assignment_id, booking_detail_id, employee_id, status, check_in_time, check_out_time) VALUES
-- Booking 29 (18h30): 1 assignment ASSIGNED - sẵn sàng check-in
('as000001-0000-0000-0000-000000000023', 'bd000001-0000-0000-0000-000000000029', 'e1000001-0000-0000-0000-000000000001', 'ASSIGNED', NULL, NULL),
-- Booking 30 (18h45): 1 assignment ASSIGNED - sẵn sàng check-in
('as000001-0000-0000-0000-000000000024', 'bd000001-0000-0000-0000-000000000030', 'e1000001-0000-0000-0000-000000000002', 'ASSIGNED', NULL, NULL),
-- Booking 31 (19h): 1 assignment ASSIGNED - sẵn sàng check-in
('as000001-0000-0000-0000-000000000025', 'bd000001-0000-0000-0000-000000000031', 'e1000001-0000-0000-0000-000000000003', 'ASSIGNED', NULL, NULL),
-- Booking 32 (19h15): 1 assignment ASSIGNED - sẵn sàng check-in
('as000001-0000-0000-0000-000000000026', 'bd000001-0000-0000-0000-000000000032', 'e1000001-0000-0000-0000-000000000001', 'ASSIGNED', NULL, NULL),
-- Booking 33 (19h30): 1 assignment ASSIGNED - sẵn sàng check-in
('as000001-0000-0000-0000-000000000027', 'bd000001-0000-0000-0000-000000000033', 'e1000001-0000-0000-0000-000000000002', 'ASSIGNED', NULL, NULL),
-- Booking 34 (19h45): 1 assignment ASSIGNED - sẵn sàng check-in
('as000001-0000-0000-0000-000000000028', 'bd000001-0000-0000-0000-000000000034', 'e1000001-0000-0000-0000-000000000003', 'ASSIGNED', NULL, NULL),
-- Booking 35 (20h): 1 assignment ASSIGNED - sẵn sàng check-in
('as000001-0000-0000-0000-000000000029', 'bd000001-0000-0000-0000-000000000035', 'e1000001-0000-0000-0000-000000000001', 'ASSIGNED', NULL, NULL),
-- Booking 36 (20h15): 1 assignment ASSIGNED - sẵn sàng check-in
('as000001-0000-0000-0000-000000000030', 'bd000001-0000-0000-0000-000000000036', 'e1000001-0000-0000-0000-000000000002', 'ASSIGNED', NULL, NULL),
-- Booking 37 (20h30): 2 assignments ASSIGNED - sẵn sàng check-in
('as000001-0000-0000-0000-000000000031', 'bd000001-0000-0000-0000-000000000037', 'e1000001-0000-0000-0000-000000000003', 'ASSIGNED', NULL, NULL),
('as000001-0000-0000-0000-000000000032', 'bd000001-0000-0000-0000-000000000038', 'e1000001-0000-0000-0000-000000000001', 'ASSIGNED', NULL, NULL),
-- Booking 38 (21h): 2 assignments ASSIGNED - sẵn sàng check-in
('as000001-0000-0000-0000-000000000033', 'bd000001-0000-0000-0000-000000000039', 'e1000001-0000-0000-0000-000000000002', 'ASSIGNED', NULL, NULL),
('as000001-0000-0000-0000-000000000034', 'bd000001-0000-0000-0000-000000000040', 'e1000001-0000-0000-0000-000000000003', 'ASSIGNED', NULL, NULL),
-- Booking 39 (17h): 1 assignment ASSIGNED - sẵn sàng check-in
('as000001-0000-0000-0000-000000000035', 'bd000001-0000-0000-0000-000000000041', 'e1000001-0000-0000-0000-000000000001', 'ASSIGNED', NULL, NULL),
-- Booking 40 (17h15): 1 assignment ASSIGNED - sẵn sàng check-in
('as000001-0000-0000-0000-000000000036', 'bd000001-0000-0000-0000-000000000042', 'e1000001-0000-0000-0000-000000000002', 'ASSIGNED', NULL, NULL),
-- Booking 41 (17h30): 1 assignment ASSIGNED - sẵn sàng check-in
('as000001-0000-0000-0000-000000000037', 'bd000001-0000-0000-0000-000000000043', 'e1000001-0000-0000-0000-000000000003', 'ASSIGNED', NULL, NULL),
-- Booking 42 (17h45): 1 assignment ASSIGNED - sẵn sàng check-in
('as000001-0000-0000-0000-000000000038', 'bd000001-0000-0000-0000-000000000044', 'e1000001-0000-0000-0000-000000000001', 'ASSIGNED', NULL, NULL),
-- Booking 43 (18h): 1 assignment ASSIGNED - sẵn sàng check-in
('as000001-0000-0000-0000-000000000039', 'bd000001-0000-0000-0000-000000000045', 'e1000001-0000-0000-0000-000000000002', 'ASSIGNED', NULL, NULL),
-- Booking 44 (21h): 1 assignment ASSIGNED - sẵn sàng check-in
('as000001-0000-0000-0000-000000000040', 'bd000001-0000-0000-0000-000000000046', 'e1000001-0000-0000-0000-000000000003', 'ASSIGNED', NULL, NULL),
-- Booking 45 (21h15): 1 assignment ASSIGNED - sẵn sàng check-in
('as000001-0000-0000-0000-000000000041', 'bd000001-0000-0000-0000-000000000047', 'e1000001-0000-0000-0000-000000000001', 'ASSIGNED', NULL, NULL),
-- Booking 46 (21h30): 1 assignment ASSIGNED - sẵn sàng check-in
('as000001-0000-0000-0000-000000000042', 'bd000001-0000-0000-0000-000000000048', 'e1000001-0000-0000-0000-000000000002', 'ASSIGNED', NULL, NULL),
-- Booking 47 (21h45): 1 assignment ASSIGNED - sẵn sàng check-in
('as000001-0000-0000-0000-000000000043', 'bd000001-0000-0000-0000-000000000049', 'e1000001-0000-0000-0000-000000000003', 'ASSIGNED', NULL, NULL),
-- Booking 48 (22h): 1 assignment ASSIGNED - sẵn sàng check-in
('as000001-0000-0000-0000-000000000044', 'bd000001-0000-0000-0000-000000000050', 'e1000001-0000-0000-0000-000000000001', 'ASSIGNED', NULL, NULL),
-- Booking 49 (21h05): 1 assignment ASSIGNED - sẵn sàng check-in
('as000001-0000-0000-0000-000000000045', 'bd000001-0000-0000-0000-000000000051', 'e1000001-0000-0000-0000-000000000002', 'ASSIGNED', NULL, NULL),
-- Booking 50 (21h20): 1 assignment ASSIGNED - sẵn sàng check-in
('as000001-0000-0000-0000-000000000046', 'bd000001-0000-0000-0000-000000000052', 'e1000001-0000-0000-0000-000000000003', 'ASSIGNED', NULL, NULL),
-- Booking 51 (21h35): 1 assignment ASSIGNED - sẵn sàng check-in
('as000001-0000-0000-0000-000000000047', 'bd000001-0000-0000-0000-000000000053', 'e1000001-0000-0000-0000-000000000001', 'ASSIGNED', NULL, NULL),
-- Booking 52 (21h50): 1 assignment ASSIGNED - sẵn sàng check-in
('as000001-0000-0000-0000-000000000048', 'bd000001-0000-0000-0000-000000000054', 'e1000001-0000-0000-0000-000000000002', 'ASSIGNED', NULL, NULL),
-- Booking 53 (21h55): 1 assignment ASSIGNED - sẵn sàng check-in
('as000001-0000-0000-0000-000000000049', 'bd000001-0000-0000-0000-000000000055', 'e1000001-0000-0000-0000-000000000003', 'ASSIGNED', NULL, NULL),
-- Booking 54 (Tháng 11 - COMPLETED): 1 assignment COMPLETED
('as000001-0000-0000-0000-000000000050', 'bd000001-0000-0000-0000-000000000056', 'e1000001-0000-0000-0000-000000000001', 'COMPLETED', '2025-11-01 09:00:00+07', '2025-11-01 13:00:00+07'),
-- Booking 55 (Tháng 11 - COMPLETED): 1 assignment COMPLETED
('as000001-0000-0000-0000-000000000051', 'bd000001-0000-0000-0000-000000000057', 'e1000001-0000-0000-0000-000000000002', 'COMPLETED', '2025-11-02 14:00:00+07', '2025-11-02 17:00:00+07'),
-- Booking 56 (Tháng 11 - COMPLETED): 1 assignment COMPLETED
('as000001-0000-0000-0000-000000000052', 'bd000001-0000-0000-0000-000000000058', 'e1000001-0000-0000-0000-000000000003', 'COMPLETED', '2025-11-03 10:00:00+07', '2025-11-03 15:00:00+07'),
-- Booking 57 (Tháng 11 - COMPLETED): 1 assignment COMPLETED
('as000001-0000-0000-0000-000000000053', 'bd000001-0000-0000-0000-000000000059', 'e1000001-0000-0000-0000-000000000001', 'COMPLETED', '2025-11-04 15:30:00+07', '2025-11-04 19:30:00+07'),
-- Booking 58 (Tháng 11 - COMPLETED): 1 assignment COMPLETED
('as000001-0000-0000-0000-000000000054', 'bd000001-0000-0000-0000-000000000060', 'e1000001-0000-0000-0000-000000000002', 'COMPLETED', '2025-11-05 08:00:00+07', '2025-11-05 11:00:00+07'),
-- Booking 59 (Tháng 11 - COMPLETED): 1 assignment COMPLETED
('as000001-0000-0000-0000-000000000055', 'bd000001-0000-0000-0000-000000000061', 'e1000001-0000-0000-0000-000000000003', 'COMPLETED', '2025-11-06 11:00:00+07', '2025-11-06 13:30:00+07'),
-- Booking 60 (Tháng 12 - COMPLETED): 1 assignment COMPLETED
('as000001-0000-0000-0000-000000000056', 'bd000001-0000-0000-0000-000000000062', 'e1000001-0000-0000-0000-000000000001', 'COMPLETED', '2025-12-01 09:30:00+07', '2025-12-01 12:30:00+07'),
-- Booking 61 (Tháng 12 - COMPLETED): 1 assignment COMPLETED
('as000001-0000-0000-0000-000000000057', 'bd000001-0000-0000-0000-000000000063', 'e1000001-0000-0000-0000-000000000002', 'COMPLETED', '2025-12-02 13:00:00+07', '2025-12-02 16:00:00+07'),
-- Booking 62 (Tháng 12 - COMPLETED): 2 assignments COMPLETED
('as000001-0000-0000-0000-000000000058', 'bd000001-0000-0000-0000-000000000064', 'e1000001-0000-0000-0000-000000000003', 'COMPLETED', '2025-12-03 10:00:00+07', '2025-12-03 12:00:00+07'),
('as000001-0000-0000-0000-000000000059', 'bd000001-0000-0000-0000-000000000065', 'e1000001-0000-0000-0000-000000000001', 'COMPLETED', '2025-12-03 10:00:00+07', '2025-12-03 13:00:00+07'),
-- Booking 63 (Tháng 12 - COMPLETED): 2 assignments COMPLETED
('as000001-0000-0000-0000-000000000060', 'bd000001-0000-0000-0000-000000000066', 'e1000001-0000-0000-0000-000000000002', 'COMPLETED', '2025-12-04 14:30:00+07', '2025-12-04 15:30:00+07'),
('as000001-0000-0000-0000-000000000061', 'bd000001-0000-0000-0000-000000000067', 'e1000001-0000-0000-0000-000000000003', 'COMPLETED', '2025-12-04 14:30:00+07', '2025-12-04 17:00:00+07');

-- =================================================================================
-- TEST SCENARIOS SUMMARY
-- =================================================================================
-- 
-- 1. GET ASSIGNMENTS (Employee e1000001-0000-0000-0000-000000000001):
--    - PENDING: as000001-0000-0000-0000-000000000003, as000001-0000-0000-0000-000000000006
--    - ASSIGNED: as000001-0000-0000-0000-000000000005, as000001-0000-0000-0000-000000000002
--    - IN_PROGRESS: as000001-0000-0000-0000-000000000009
--    - COMPLETED: as000001-0000-0000-0000-000000000001
--
-- 2. ACCEPT ASSIGNMENT:
--    ✅ Success: as000001-0000-0000-0000-000000000003 (PENDING → ASSIGNED)
--    ❌ Fail: as000001-0000-0000-0000-000000000005 (đã ASSIGNED rồi)
--
-- 3. CANCEL ASSIGNMENT:
--    ✅ Success (PENDING, > 30 min): as000001-0000-0000-0000-000000000004
--    ✅ Success (ASSIGNED, > 30 min): as000001-0000-0000-0000-000000000005
--    ❌ Fail (< 30 min): as000001-0000-0000-0000-000000000010
--    ❌ Fail (IN_PROGRESS): as000001-0000-0000-0000-000000000009
--
-- 4. FILTER BY STATUS:
--    status=PENDING: 4 assignments
--    status=ASSIGNED: 2 assignments
--    status=IN_PROGRESS: 1 assignment
--    status=COMPLETED: 1 assignment
--
-- 5. CHECK-IN ASSIGNMENTS (18h30-21h ngày 07/11/2025):
--    Employee e1000001-0000-0000-0000-000000000001:
--      - 18h30: as000001-0000-0000-0000-000000000023 (BK000029 - Dọn dẹp theo giờ)
--      - 19h15: as000001-0000-0000-0000-000000000026 (BK000032 - Giặt ủi)
--      - 20h00: as000001-0000-0000-0000-000000000029 (BK000035 - Dọn dẹp theo giờ)
--      - 20h30: as000001-0000-0000-0000-000000000032 (BK000037 - Nấu ăn)
--    
--    Employee e1000001-0000-0000-0000-000000000002:
--      - 18h45: as000001-0000-0000-0000-000000000024 (BK000030 - Tổng vệ sinh)
--      - 19h30: as000001-0000-0000-0000-000000000027 (BK000033 - Vệ sinh sofa)
--      - 20h15: as000001-0000-0000-0000-000000000030 (BK000036 - Giặt hấp cao cấp)
--      - 21h00: as000001-0000-0000-0000-000000000033 (BK000038 - Tổng vệ sinh)
--    
--    Employee e1000001-0000-0000-0000-000000000003:
--      - 19h00: as000001-0000-0000-0000-000000000025 (BK000031 - Vệ sinh máy lạnh)
--      - 19h45: as000001-0000-0000-0000-000000000028 (BK000034 - Nấu ăn)
--      - 20h30: as000001-0000-0000-0000-000000000031 (BK000037 - Đi chợ hộ)
--      - 21h00: as000001-0000-0000-0000-000000000034 (BK000038 - Vệ sinh máy lạnh)
--
--    Tổng cộng: 10 bookings, 14 assignments ASSIGNED (sẵn sàng check-in)
--
-- 6. CHECK-IN ASSIGNMENTS (17h-18h ngày 07/11/2025):
--    Employee e1000001-0000-0000-0000-000000000001:
--      - 17h00: as000001-0000-0000-0000-000000000035 (BK000039 - Dọn dẹp theo giờ)
--      - 17h45: as000001-0000-0000-0000-000000000038 (BK000042 - Tổng vệ sinh)
--    
--    Employee e1000001-0000-0000-0000-000000000002:
--      - 17h15: as000001-0000-0000-0000-000000000036 (BK000040 - Vệ sinh máy lạnh)
--      - 18h00: as000001-0000-0000-0000-000000000039 (BK000043 - Vệ sinh sofa)
--    
--    Employee e1000001-0000-0000-0000-000000000003:
--      - 17h30: as000001-0000-0000-0000-000000000037 (BK000041 - Giặt ủi)
--
--    Tổng cộng: 5 bookings, 5 assignments ASSIGNED (sẵn sàng check-in)
--
-- 7. CHECK-IN ASSIGNMENTS (21h-22h ngày 07/11/2025):
--    Employee e1000001-0000-0000-0000-000000000001:
--      - 21h15: as000001-0000-0000-0000-000000000041 (BK000045 - Nấu ăn gia đình)
--      - 21h35: as000001-0000-0000-0000-000000000047 (BK000051 - Giặt hấp cao cấp)
--      - 22h00: as000001-0000-0000-0000-000000000044 (BK000048 - Tổng vệ sinh)
--    
--    Employee e1000001-0000-0000-0000-000000000002:
--      - 21h05: as000001-0000-0000-0000-000000000045 (BK000049 - Vệ sinh sofa)
--      - 21h30: as000001-0000-0000-0000-000000000042 (BK000046 - Vệ sinh máy lạnh)
--      - 21h50: as000001-0000-0000-0000-000000000048 (BK000052 - Dọn dẹp theo giờ)
--    
--    Employee e1000001-0000-0000-0000-000000000003:
--      - 21h00: as000001-0000-0000-0000-000000000040 (BK000044 - Dọn dẹp theo giờ)
--      - 21h20: as000001-0000-0000-0000-000000000046 (BK000050 - Đi chợ hộ)
--      - 21h45: as000001-0000-0000-0000-000000000043 (BK000047 - Giặt sấy)
--      - 21h55: as000001-0000-0000-0000-000000000049 (BK000053 - Vệ sinh máy lạnh)
--
--    Tổng cộng: 10 bookings, 10 assignments ASSIGNED (sẵn sàng check-in)
--
-- 8. COMPLETED BOOKINGS (Tháng 11 và Tháng 12/2025):
--    Tháng 11/2025:
--      - BK000054 (01/11): Dọn dẹp theo giờ - 200,000đ (Employee 1)
--      - BK000055 (02/11): Vệ sinh máy lạnh - 450,000đ (Employee 2)
--      - BK000056 (03/11): Tổng vệ sinh - 500,000đ (Employee 3)
--      - BK000057 (04/11): Giặt sấy - 240,000đ (Employee 1)
--      - BK000058 (05/11): Vệ sinh sofa - 300,000đ (Employee 2)
--      - BK000059 (06/11): Nấu ăn gia đình - 180,000đ (Employee 3)
--    
--    Tháng 12/2025:
--      - BK000060 (01/12): Dọn dẹp theo giờ - 150,000đ (Employee 1)
--      - BK000061 (02/12): Giặt hấp cao cấp - 360,000đ (Employee 2)
--      - BK000062 (03/12): Vệ sinh máy lạnh + Tổng vệ sinh - 650,000đ (Employee 3, 1)
--      - BK000063 (04/12): Đi chợ hộ + Nấu ăn - 220,000đ (Employee 2, 3)
--
--    Tổng cộng: 10 bookings COMPLETED, 12 assignments COMPLETED
--    Tổng doanh thu: 3,250,000đ
-- =================================================================================

-- =================================================================================
-- TỰ ĐỘNG TẠO BOOKING/ASSIGNMENT COMPLETED CHO TOÀN BỘ TÀI KHOẢN EMPLOYEE
-- =================================================================================
WITH employee_with_role AS (
    SELECT e.employee_id,
           e.account_id,
           ROW_NUMBER() OVER (ORDER BY e.employee_id) AS rn
    FROM employee e
    JOIN account_roles ar ON ar.account_id = e.account_id
    WHERE ar.role_id = 2
),
assignment_targets AS (
    SELECT employee_id,
           rn,
           10 + ((rn - 1) % 11) AS assignment_count
    FROM employee_with_role
),
expanded_employee_assignments AS (
    SELECT
        at.employee_id,
        at.rn,
        series.seq,
        ROW_NUMBER() OVER (ORDER BY at.employee_id, series.seq) AS global_seq
    FROM assignment_targets at
    CROSS JOIN LATERAL generate_series(1, at.assignment_count) AS series(seq)
),
customer_catalog AS (
    SELECT customer_id,
           ROW_NUMBER() OVER (ORDER BY customer_id) AS idx
    FROM customer
),
customer_total AS (
    SELECT COUNT(*) AS total FROM customer_catalog
),
service_catalog AS (
    SELECT service_id,
           base_price,
           ROW_NUMBER() OVER (ORDER BY service_id) AS idx
    FROM service
),
service_total AS (
    SELECT COUNT(*) AS total FROM service_catalog
),
default_customer_address AS (
    SELECT address_id, customer_id
    FROM (
        SELECT address_id,
               customer_id,
               ROW_NUMBER() OVER (PARTITION BY customer_id ORDER BY is_default DESC, address_id) AS priority_rank
        FROM address
    ) ranked_addresses
    WHERE priority_rank = 1
),
auto_assignment_payload AS (
    SELECT
        base.employee_id,
        base.customer_id,
        base.address_id,
        base.service_id,
        base.price_per_unit,
        base.quantity,
        base.sub_total,
        base.booking_time,
        format('%s-%s-%s-%s-%s',
            substring(base.booking_hash, 1, 8),
            substring(base.booking_hash, 9, 4),
            substring(base.booking_hash, 13, 4),
            substring(base.booking_hash, 17, 4),
            substring(base.booking_hash, 21, 12)
        ) AS booking_id,
        format('%s-%s-%s-%s-%s',
            substring(base.booking_detail_hash, 1, 8),
            substring(base.booking_detail_hash, 9, 4),
            substring(base.booking_detail_hash, 13, 4),
            substring(base.booking_detail_hash, 17, 4),
            substring(base.booking_detail_hash, 21, 12)
        ) AS booking_detail_id,
        format('%s-%s-%s-%s-%s',
            substring(base.assignment_hash, 1, 8),
            substring(base.assignment_hash, 9, 4),
            substring(base.assignment_hash, 13, 4),
            substring(base.assignment_hash, 17, 4),
            substring(base.assignment_hash, 21, 12)
        ) AS assignment_id,
        base.booking_code,
        base.note
    FROM (
        SELECT
            exp.employee_id,
            cust.customer_id,
            COALESCE(addr.address_id, 'adrs0001-0000-0000-0000-000000000001') AS address_id,
            svc.service_id,
            svc.base_price AS price_per_unit,
            ((exp.seq + exp.rn - 1) % 4) + 1 AS quantity,
            svc.base_price * (((exp.seq + exp.rn - 1) % 4) + 1) AS sub_total,
            ('2025-01-01 08:00:00+07'::timestamptz
                + ((exp.global_seq - 1) * INTERVAL '1 day')
                + (((exp.seq % 5) * 2) * INTERVAL '1 hour')) AS booking_time,
            md5(concat('booking-', exp.global_seq::text)) AS booking_hash,
            md5(concat('booking-detail-', exp.global_seq::text)) AS booking_detail_hash,
            md5(concat('assignment-', exp.global_seq::text)) AS assignment_hash,
            concat('BKE', to_char((exp.global_seq % 1000000)::INT, 'FM000000')) AS booking_code,
            'Auto generated completed assignment #' || exp.seq || ' for ' || exp.employee_id AS note
        FROM expanded_employee_assignments exp
        CROSS JOIN customer_total ct
        CROSS JOIN service_total st
        JOIN customer_catalog cust
            ON cust.idx = ((exp.global_seq - 1) % GREATEST(ct.total, 1)) + 1
        LEFT JOIN default_customer_address addr
            ON addr.customer_id = cust.customer_id
        JOIN service_catalog svc
            ON svc.idx = ((exp.global_seq - 1) % GREATEST(st.total, 1)) + 1
    ) base
),
bookings_insert AS (
    INSERT INTO bookings (
        booking_id,
        customer_id,
        address_id,
        booking_code,
        booking_time,
        note,
        total_amount,
        status,
        promotion_id,
        is_verified
    )
    SELECT
        booking_id,
        customer_id,
        address_id,
        booking_code,
        booking_time,
        note,
        sub_total,
        'COMPLETED',
        NULL,
        true
    FROM auto_assignment_payload
    RETURNING booking_id
),
booking_details_insert AS (
    INSERT INTO booking_details (
        booking_detail_id,
        booking_id,
        service_id,
        quantity,
        price_per_unit,
        sub_total
    )
    SELECT
        booking_detail_id,
        booking_id,
        service_id,
        quantity,
        price_per_unit,
        sub_total
    FROM auto_assignment_payload
    RETURNING booking_detail_id
)
INSERT INTO assignments (
    assignment_id,
    booking_detail_id,
    employee_id,
    status,
    check_in_time,
    check_out_time
)
SELECT
    assignment_id,
    booking_detail_id,
    employee_id,
    'COMPLETED',
    booking_time,
    booking_time + INTERVAL '2 hours'
FROM auto_assignment_payload;

-- =================================================================================
