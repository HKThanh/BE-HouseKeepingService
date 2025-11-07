-- Seed data: 91 Seed Services And Bookings

-- Khối II: Thêm dữ liệu cho Dịch vụ, Khuyến mãi và Đặt lịch
-- =================================================================================

-- XÓA DỮ LIỆU DỊCH VỤ CŨ ĐỂ CẬP NHẬT CẤU TRÚC MỚI
TRUNCATE TABLE service RESTART IDENTITY CASCADE;
-- TRUNCATE TABLE service_categories RESTART IDENTITY CASCADE; -- (Chạy nếu bảng đã tồn tại và có dữ liệu)

-- THÊM DỮ LIỆU MẪU CHO DANH MỤC VÀ DỊCH VỤ

-- Thêm các danh mục cha
INSERT INTO service_categories (category_name, description, icon_url) VALUES
('Dọn dẹp nhà', 'Các dịch vụ liên quan đến vệ sinh, làm sạch nhà cửa', 'https://res.cloudinary.com/dkzemgit8/image/upload/v1757599581/house_cleaning_nob_umewqf.png'),
('Giặt ủi', 'Dịch vụ giặt sấy, ủi đồ chuyên nghiệp', 'https://res.cloudinary.com/dkzemgit8/image/upload/v1757599717/washing_nz3cbw.png'),
('Việc nhà khác', 'Các dịch vụ tiện ích gia đình khác', 'https://res.cloudinary.com/dkzemgit8/image/upload/v1757599722/other_services_ozqdxk.png');

-- Thêm các dịch vụ con vào từng danh mục
-- Dữ liệu cho danh mục 'Dọn dẹp nhà' (category_id = 1)
INSERT INTO service (category_id, name, description, base_price, unit, estimated_duration_hours, recommended_staff, icon_url, is_active) VALUES
(1, 'Dọn dẹp theo giờ', 'Lau dọn, hút bụi, làm sạch các bề mặt cơ bản trong nhà. Phù hợp cho nhu cầu duy trì vệ sinh hàng tuần.', 50000, 'Giờ', 2.0, 1, 'https://res.cloudinary.com/dkzemgit8/image/upload/v1757599899/Cleaning_Clock_z29juh.png', TRUE),
(1, 'Tổng vệ sinh', 'Làm sạch sâu toàn diện, bao gồm các khu vực khó tiếp cận, trần nhà, lau cửa kính. Thích hợp cho nhà mới hoặc dọn dẹp theo mùa.', 100000, 'Gói', 2.0, 1, 'https://res.cloudinary.com/dkzemgit8/image/upload/v1757599581/house_cleaning_nob_umewqf.png', TRUE),
(1, 'Vệ sinh Sofa - Nệm - Rèm', 'Giặt sạch và khử khuẩn Sofa, Nệm, Rèm cửa bằng máy móc chuyên dụng.', 300000, 'Gói', 3.0, 1, 'https://res.cloudinary.com/dkzemgit8/image/upload/v1757600057/sofa_bed_vkkjz8.png', TRUE),
(1, 'Vệ sinh máy lạnh', 'Bảo trì, làm sạch dàn nóng và dàn lạnh, bơm gas nếu cần.', 150000, 'Máy', 1.0, 1, 'https://res.cloudinary.com/dkzemgit8/image/upload/v1757600733/cooler_rnyppn.png', TRUE);
-- Dữ liệu cho danh mục 'Giặt ủi' (category_id = 2)
INSERT INTO service (category_id, name, description, base_price, unit, estimated_duration_hours, recommended_staff, icon_url, is_active) VALUES
(2, 'Giặt sấy theo kg', 'Giặt và sấy khô quần áo thông thường, giao nhận tận nơi.', 30000, 'Kg', 24.0, 1, 'https://res.cloudinary.com/dkzemgit8/image/upload/v1757601210/shirt_nmee0d.png', TRUE),
(2, 'Giặt hấp cao cấp', 'Giặt khô cho các loại vải cao cấp như vest, áo dài, lụa.', 120000, 'Bộ', 48.0, 1, 'https://res.cloudinary.com/dkzemgit8/image/upload/v1757601414/vest_2_kfigzg.png', TRUE);
-- Dữ liệu cho danh mục 'Việc nhà khác' (category_id = 3)
INSERT INTO service (category_id, name, description, base_price, unit, estimated_duration_hours, recommended_staff, icon_url, is_active) VALUES
(3, 'Nấu ăn gia đình', 'Đi chợ (chi phí thực phẩm tính riêng) và chuẩn bị bữa ăn cho gia đình theo thực đơn yêu cầu.', 60000, 'Giờ', 2.5, 1, 'https://res.cloudinary.com/dkzemgit8/image/upload/v1757601546/pan_ysmoql.png', TRUE),
(3, 'Đi chợ hộ', 'Mua sắm và giao hàng tận nơi theo danh sách của bạn.', 40000, 'Lần', 1.0, 1, 'https://res.cloudinary.com/dkzemgit8/image/upload/v1757601712/shopping_etf5iz.png', TRUE);

-- Thêm các chương trình khuyến mãi
INSERT INTO promotions (promo_code, description, discount_type, discount_value, max_discount_amount, start_date, end_date, is_active) VALUES
('GIAM20K', 'Giảm giá 20,000đ cho mọi đơn hàng', 'FIXED_AMOUNT', 20000, NULL, '2025-08-01 00:00:00+07', '2025-09-30 23:59:59+07', TRUE),
('KHAITRUONG10', 'Giảm 10% mừng khai trương', 'PERCENTAGE', 10, 50000, '2025-08-01 00:00:00+07', '2025-08-31 23:59:59+07', TRUE);

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
-- =================================================================================
