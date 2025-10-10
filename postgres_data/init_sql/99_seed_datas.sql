-- Thêm các vai trò mặc định
INSERT INTO roles (role_id, role_name) VALUES (1, 'CUSTOMER'), (2, 'EMPLOYEE'), (3, 'ADMIN');

INSERT INTO account (account_id, username, password, phone_number, status, is_phone_verified) VALUES
('a1000001-0000-0000-0000-000000000001', 'john_doe', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0901234567', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000002', 'jane_smith', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0912345678', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000003', 'admin_1', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0900000001', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000004', 'mary_jones', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0909876543', 'INACTIVE', false),
('a1000001-0000-0000-0000-000000000005', 'bob_wilson', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0923456789', 'ACTIVE', true);

-- Gán vai trò cho các tài khoản
INSERT INTO account_roles (account_id, role_id) VALUES
('a1000001-0000-0000-0000-000000000001', 1), -- john_doe là CUSTOMER
('a1000001-0000-0000-0000-000000000002', 2), -- jane_smith là EMPLOYEE
('a1000001-0000-0000-0000-000000000002', 1), -- jane_smith cũng là CUSTOMER
('a1000001-0000-0000-0000-000000000003', 3), -- admin_1 là ADMIN
('a1000001-0000-0000-0000-000000000004', 1), -- mary_jones là CUSTOMER
('a1000001-0000-0000-0000-000000000005', 2); -- bob_wilson là EMPLOYEE

-- Thêm hồ sơ khách hàng (customer)
INSERT INTO customer (customer_id, account_id, avatar, full_name, is_male, email, birthdate) VALUES
('c1000001-0000-0000-0000-000000000001', 'a1000001-0000-0000-0000-000000000001', 'https://picsum.photos/200', 'John Doe', TRUE, 'john.doe@example.com', '2003-09-10'),
('c1000001-0000-0000-0000-000000000002', 'a1000001-0000-0000-0000-000000000004', 'https://picsum.photos/200', 'Mary Jones', FALSE, 'mary.jones@example.com', '2003-01-19'),
('c1000001-0000-0000-0000-000000000003', 'a1000001-0000-0000-0000-000000000002', 'https://picsum.photos/200', 'Jane Smith Customer', FALSE, 'jane.smith.customer@example.com', '2003-04-14');

-- Thêm hồ sơ nhân viên (employee)
INSERT INTO employee (employee_id, account_id, avatar, full_name, is_male, email, birthdate, hired_date, skills, bio) VALUES
('e1000001-0000-0000-0000-000000000001', 'a1000001-0000-0000-0000-000000000002', 'https://picsum.photos/200', 'Jane Smith', FALSE, 'jane.smith@example.com', '2003-04-14', '2024-01-15', ARRAY['Cleaning', 'Organizing'], 'Có kinh nghiệm dọn dẹp nhà cửa và sắp xếp đồ đạc.'),
('e1000001-0000-0000-0000-000000000002', 'a1000001-0000-0000-0000-000000000005', 'https://picsum.photos/200', 'Bob Wilson', TRUE, 'bob.wilson@examplefieldset.com', '2003-08-10', '2023-06-20', ARRAY['Deep Cleaning', 'Laundry'], 'Chuyên gia giặt ủi và làm sạch sâu.');

-- Thêm hồ sơ quản trị viên (admin_profile)
INSERT INTO admin_profile (admin_profile_id, account_id, full_name, is_male, department, contact_info, birthdate, hire_date) VALUES
('ad100001-0000-0000-0000-000000000001', 'a1000001-0000-0000-0000-000000000003', 'Admin One', TRUE, 'Management', 'admin1@example.com', '1988-09-10', '2023-03-01');

-- Thêm địa chỉ cho khách hàng
INSERT INTO address (address_id, customer_id, full_address, ward, city, latitude, longitude, is_default) VALUES
('adrs0001-0000-0000-0000-000000000001', 'c1000001-0000-0000-0000-000000000001', '123 Lê Trọng Tấn, Phường Tây Thạnh, TP. Hồ Chí Minh', 'Phường Tây Thạnh', 'TP. Hồ Chí Minh', 10.7943, 106.6256, true),
('adrs0001-0000-0000-0000-000000000002', 'c1000001-0000-0000-0000-000000000002', '456 Lê Lợi, Phường Sài Gòn, TP. Hồ Chí Minh', 'Phường Sài Gòn', 'TP. Hồ Chí Minh', 10.7769, 106.7009, true),
('adrs0001-0000-0000-0000-000000000003', 'c1000001-0000-0000-0000-000000000003', '104 Lê Lợi, Phường Hạnh Thông, TP. Hồ Chí Minh', 'Phường Hạnh Thông', 'TP. Hồ Chí Minh', 10.8142, 106.6938, true);

-- Thêm khu vực làm việc cho nhân viên
INSERT INTO employee_working_zones (employee_id, ward, city) VALUES
('e1000001-0000-0000-0000-000000000001', 'Phường Tây Thạnh', 'TP. Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000001', 'Phường Bảy Hiền', 'TP. Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000002', 'Phường Hạnh Thông', 'TP. Hồ Chí Minh');

-- Thêm các chức năng mẫu vào bảng `features`
INSERT INTO features (feature_name, description, module) VALUES
-- Chức năng của Customer
('booking.create', 'Tạo một lịch đặt mới', 'Booking'),
('booking.view.history', 'Xem lịch sử đặt lịch của bản thân', 'Booking'),
('booking.cancel', 'Hủy một lịch đặt', 'Booking'),
('review.create', 'Viết đánh giá cho nhân viên', 'Review'),
('profile.customer.edit', 'Chỉnh sửa hồ sơ cá nhân', 'Account'),
('service.view', 'Xem danh sách và chi tiết dịch vụ', 'Service'),
-- Chức năng của Employee
('booking.view.available', 'Xem các lịch đặt mới có sẵn', 'Booking'),
('booking.accept', 'Chấp nhận một lịch đặt', 'Booking'),
('booking.view.assigned', 'Xem các lịch đã nhận', 'Booking'),
('profile.employee.edit', 'Chỉnh sửa hồ sơ nhân viên', 'Account'),
-- Chức năng của Admin
('admin.dashboard.view', 'Xem bảng điều khiển tổng quan', 'Admin'),
('admin.user.manage', 'Quản lý tất cả người dùng', 'Admin'),
('admin.permission.manage', 'Quản lý và phân quyền', 'Admin');

-- Gán quyền mặc định cho các vai trò
INSERT INTO role_features (role_id, feature_id, is_enabled) VALUES
-- Quyền của CUSTOMER (role_id = 1)
(1, (SELECT feature_id FROM features WHERE feature_name = 'booking.create'), true),
(1, (SELECT feature_id FROM features WHERE feature_name = 'booking.view.history'), true),
(1, (SELECT feature_id FROM features WHERE feature_name = 'booking.cancel'), true),
(1, (SELECT feature_id FROM features WHERE feature_name = 'review.create'), true),
(1, (SELECT feature_id FROM features WHERE feature_name = 'profile.customer.edit'), true),
(1, (SELECT feature_id FROM features WHERE feature_name = 'service.view'), true),

-- Quyền của EMPLOYEE (role_id = 2)
(2, (SELECT feature_id FROM features WHERE feature_name = 'booking.view.available'), true),
(2, (SELECT feature_id FROM features WHERE feature_name = 'booking.accept'), true),
(2, (SELECT feature_id FROM features WHERE feature_name = 'booking.view.assigned'), true),
(2, (SELECT feature_id FROM features WHERE feature_name = 'profile.employee.edit'), true),

-- Quyền của ADMIN (role_id = 3)
-- Admin có tất cả các quyền trên và thêm các quyền quản trị
(3, (SELECT feature_id FROM features WHERE feature_name = 'admin.dashboard.view'), true),
(3, (SELECT feature_id FROM features WHERE feature_name = 'admin.user.manage'), true),
(3, (SELECT feature_id FROM features WHERE feature_name = 'admin.permission.manage'), true),

-- Quyền của CUSTOMER
(3, (SELECT feature_id FROM features WHERE feature_name = 'booking.create'), true),
(3, (SELECT feature_id FROM features WHERE feature_name = 'booking.view.history'), true),
(3, (SELECT feature_id FROM features WHERE feature_name = 'booking.cancel'), true),
(3, (SELECT feature_id FROM features WHERE feature_name = 'review.create'), true),
(3, (SELECT feature_id FROM features WHERE feature_name = 'profile.customer.edit'), true),
(3, (SELECT feature_id FROM features WHERE feature_name = 'service.view'), true),

-- Quyền của EMPLOYEE
(3, (SELECT feature_id FROM features WHERE feature_name = 'booking.view.available'), true),
(3, (SELECT feature_id FROM features WHERE feature_name = 'booking.accept'), true),
(3, (SELECT feature_id FROM features WHERE feature_name = 'booking.view.assigned'), true),
(3, (SELECT feature_id FROM features WHERE feature_name = 'profile.employee.edit'), true);

-- =================================================================================
-- THÊM DỮ LIỆU MẪU (TIẾNG VIỆT) VÀO CÁC KHỐI CÒN LẠI
-- =================================================================================

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

-- Thêm 2 lịch đặt (bookings) mẫu
-- Một lịch đã HOÀN THÀNH của khách hàng 'John Doe'
-- Một lịch đã XÁC NHẬN của khách hàng 'Jane Smith Customer'
INSERT INTO bookings (booking_id, customer_id, address_id, booking_code, booking_time, note, total_amount, status, promotion_id) VALUES
('b0000001-0000-0000-0000-000000000001', 'c1000001-0000-0000-0000-000000000001', 'adrs0001-0000-0000-0000-000000000001', 'BK000001', '2025-08-20 09:00:00+07', 'Nhà có trẻ nhỏ, vui lòng lau dọn kỹ khu vực phòng khách.', 80000.00, 'COMPLETED', (SELECT promotion_id FROM promotions WHERE promo_code = 'GIAM20K')),
('b0000001-0000-0000-0000-000000000002', 'c1000001-0000-0000-0000-000000000003', 'adrs0001-0000-0000-0000-000000000003', 'BK000002', '2025-08-28 14:00:00+07', 'Vui lòng đến đúng giờ.', 90000.00, 'CONFIRMED', (SELECT promotion_id FROM promotions WHERE promo_code = 'KHAITRUONG10'));

-- Thêm chi tiết dịch vụ cho các lịch đặt
INSERT INTO booking_details (booking_detail_id, booking_id, service_id, quantity, price_per_unit, sub_total) VALUES
('bd000001-0000-0000-0000-000000000001', 'b0000001-0000-0000-0000-000000000001', (SELECT service_id FROM service WHERE name = 'Tổng vệ sinh'), 1, 100000.00, 100000.00),
('bd000001-0000-0000-0000-000000000002', 'b0000001-0000-0000-0000-000000000002', (SELECT service_id FROM service WHERE name = 'Dọn dẹp theo giờ'), 2, 50000.00, 100000.00);

-- Phân công nhân viên cho các lịch đặt
INSERT INTO assignments (assignment_id, booking_detail_id, employee_id, status, check_in_time, check_out_time) VALUES
('as000001-0000-0000-0000-000000000001', 'bd000001-0000-0000-0000-000000000001', 'e1000001-0000-0000-0000-000000000002', 'COMPLETED', '2025-08-20 09:00:00+07', '2025-08-20 13:00:00+07'),
('as000001-0000-0000-0000-000000000002', 'bd000001-0000-0000-0000-000000000002', 'e1000001-0000-0000-0000-000000000001', 'ASSIGNED', NULL, NULL);

-- Khối IV: Thêm dữ liệu cho Thanh toán và Đánh giá
-- =================================================================================

-- Thêm các tiêu chí đánh giá
INSERT INTO review_criteria (criteria_name) VALUES
('Thái độ'),
('Đúng giờ'),
('Chất lượng công việc');

-- Thêm một đánh giá cho lịch đặt đã hoàn thành
INSERT INTO review ( booking_id, customer_id, employee_id, comment) VALUES
('b0000001-0000-0000-0000-000000000001', 'c1000001-0000-0000-0000-000000000001', 'e1000001-0000-0000-0000-000000000002', 'Bạn nhân viên làm việc rất chuyên nghiệp và sạch sẽ. Rất hài lòng!');

-- Thêm chi tiết đánh giá theo từng tiêu chí
INSERT INTO review_details (review_id, criteria_id, rating) VALUES
(1, (SELECT criteria_id FROM review_criteria WHERE criteria_name = 'Thái độ'), 5.0),
(1, (SELECT criteria_id FROM review_criteria WHERE criteria_name = 'Đúng giờ'), 5.0),
(1, (SELECT criteria_id FROM review_criteria WHERE criteria_name = 'Chất lượng công việc'), 4.5);


-- Khối III: Thêm dữ liệu cho Checklist và Media
-- =================================================================================

-- Thêm một mẫu checklist cho dịch vụ 'Tổng vệ sinh'
INSERT INTO checklist_templates (service_id, name, description) VALUES
((SELECT service_id FROM service WHERE name = 'Tổng vệ sinh'), 'Checklist Tổng Vệ Sinh Cơ Bản', 'Các đầu việc cần làm cho gói tổng vệ sinh');

-- Thêm các mục con cho mẫu checklist
INSERT INTO checklist_template_items (template_id, item_description, item_order) VALUES
(1, 'Quét và lau sàn tất cả các phòng', 1),
(1, 'Hút bụi thảm, sofa', 2),
(1, 'Lau bụi bề mặt tủ, bàn, ghế', 3),
(1, 'Vệ sinh toàn bộ khu vực nhà vệ sinh', 4),
(1, 'Lau cửa kính mặt trong', 5),
(1, 'Thu gom và đổ rác', 6);

-- Thêm các mục checklist vào lịch đặt đã hoàn thành (giả sử được copy từ template)
INSERT INTO booking_checklist_items (booking_id, item_description, is_completed, completed_at, employee_id) VALUES
('b0000001-0000-0000-0000-000000000001', 'Quét và lau sàn tất cả các phòng', true, '2025-08-20 10:00:00+07', 'e1000001-0000-0000-0000-000000000002'),
('b0000001-0000-0000-0000-000000000001', 'Hút bụi thảm, sofa', true, '2025-08-20 10:30:00+07', 'e1000001-0000-0000-0000-000000000002'),
('b0000001-0000-0000-0000-000000000001', 'Lau bụi bề mặt tủ, bàn, ghế', true, '2025-08-20 11:00:00+07', 'e1000001-0000-0000-0000-000000000002');


-- Thêm media (ảnh Trước & Sau) cho lịch đặt đã hoàn thành
INSERT INTO booking_media (booking_id, assignment_id, media_url, media_type) VALUES
('b0000001-0000-0000-0000-000000000001', 'as000001-0000-0000-0000-000000000001', 'https://example.com/images/before_job1.jpg', 'BEFORE'),
('b0000001-0000-0000-0000-000000000001', 'as000001-0000-0000-0000-000000000001', 'https://example.com/images/after_job1.jpg', 'AFTER');

-- =================================================================================
-- THÊM DỮ LIỆU MẪU VÀO BẢNG LỊCH BẬN CỦA NHÂN VIÊN
-- =================================================================================

INSERT INTO employee_unavailability (employee_id, start_time, end_time, reason, is_approved) VALUES
-- Nhân viên 'Jane Smith' (id: e1000001-...-0001) có lịch cá nhân vào buổi sáng
('e1000001-0000-0000-0000-000000000001', '2025-08-28 09:00:00+07', '2025-08-28 11:00:00+07', 'Lịch cá nhân', true),

-- Nhân viên 'Jane Smith' (id: e1000001-...-0001) bận đi học vào buổi tối
('e1000001-0000-0000-0000-000000000001', '2025-08-29 18:00:00+07', '2025-08-29 20:00:00+07', 'Lớp học buổi tối', true),

-- Nhân viên 'Bob Wilson' (id: e1000001-...-0002) đăng ký nghỉ phép 3 ngày
('e1000001-0000-0000-0000-000000000002', '2025-09-01 00:00:00+07', '2025-09-03 23:59:59+07', 'Nghỉ phép', true);

-- Tổng vệ sinh có các câu hỏi tùy chọn
-- Câu hỏi 1: Loại nhà
INSERT INTO service_options (service_id, label, option_type, display_order) VALUES (2, 'Loại hình nhà ở?', 'SINGLE_CHOICE_RADIO', 1);
INSERT INTO service_option_choices (option_id, label) VALUES (1, 'Căn hộ'), (1, 'Nhà phố');

-- Câu hỏi 2 (PHỤ THUỘC): Số tầng (chỉ hiện khi chọn 'Nhà phố' - choice_id=2)
INSERT INTO service_options (service_id, label, option_type, display_order, parent_choice_id) VALUES (2, 'Nhà bạn có mấy tầng (bao gồm trệt)?', 'QUANTITY_INPUT', 2, 2);

-- Câu hỏi 3: Diện tích
INSERT INTO service_options (service_id, label, option_type, display_order) VALUES (2, 'Diện tích dọn dẹp?', 'SINGLE_CHOICE_DROPDOWN', 3);
INSERT INTO service_option_choices (option_id, label) VALUES (3, 'Dưới 80m²'), (3, 'Trên 80m²');

-- Câu hỏi cho dịch vụ 'Dọn dẹp theo giờ'
-- Câu hỏi 1: Số phòng ngủ
INSERT INTO service_options (service_id, label, option_type, display_order)
VALUES (1, 'Số phòng ngủ cần dọn?', 'QUANTITY_INPUT', 1);

-- Câu hỏi 2: Công việc thêm
INSERT INTO service_options (service_id, label, option_type, display_order)
VALUES (1, 'Bạn có yêu cầu thêm công việc nào?', 'MULTIPLE_CHOICE_CHECKBOX', 2);
INSERT INTO service_option_choices (option_id, label, display_order) VALUES
(5, 'Giặt chăn ga', 1),
(5, 'Rửa chén', 2),
(5, 'Lau cửa kính', 3);

-- Câu hỏi cho dịch vụ 'Vệ sinh Sofa - Nệm - Rèm'
INSERT INTO service_options (service_id, label, option_type, display_order)
VALUES (3, 'Hạng mục cần vệ sinh?', 'SINGLE_CHOICE_RADIO', 1);
INSERT INTO service_option_choices (option_id, label, display_order) VALUES
(6, 'Sofa', 1),
(6, 'Nệm', 2),
(6, 'Rèm', 3);

-- Câu hỏi cho dịch vụ 'Vệ sinh máy lạnh'
INSERT INTO service_options (service_id, label, option_type, display_order)
VALUES (4, 'Loại máy lạnh?', 'SINGLE_CHOICE_DROPDOWN', 1);
INSERT INTO service_option_choices (option_id, label, display_order) VALUES
(7, 'Treo tường', 1),
(7, 'Âm trần/Cassette', 2),
(7, 'Tủ đứng', 3);
INSERT INTO service_options (service_id, label, option_type, display_order)
VALUES (4, 'Số lượng máy?', 'QUANTITY_INPUT', 2);

-- Câu hỏi cho dịch vụ 'Giặt sấy theo kg'
INSERT INTO service_options (service_id, label, option_type, display_order)
VALUES (5, 'Có cần gấp quần áo sau khi giặt?', 'SINGLE_CHOICE_RADIO', 1);
INSERT INTO service_option_choices (option_id, label, display_order) VALUES
(9, 'Có', 1),
(9, 'Không', 2);

-- Câu hỏi cho dịch vụ 'Giặt hấp cao cấp'
INSERT INTO service_options (service_id, label, option_type, display_order)
VALUES (6, 'Loại trang phục giặt hấp?', 'SINGLE_CHOICE_DROPDOWN', 1);
INSERT INTO service_option_choices (option_id, label, display_order) VALUES
(10, 'Vest', 1),
(10, 'Áo dài', 2),
(10, 'Đầm', 3);

-- Câu hỏi cho dịch vụ 'Nấu ăn gia đình'
INSERT INTO service_options (service_id, label, option_type, display_order)
VALUES (7, 'Số người ăn?', 'QUANTITY_INPUT', 1);
INSERT INTO service_options (service_id, label, option_type, display_order)
VALUES (7, 'Bạn có cần chúng tôi mua nguyên liệu?', 'SINGLE_CHOICE_RADIO', 2);
INSERT INTO service_option_choices (option_id, label, display_order) VALUES
(12, 'Có', 1),
(12, 'Không', 2);
INSERT INTO service_options (service_id, label, option_type, display_order, parent_choice_id)
VALUES (7, 'Ngân sách thực phẩm (VNĐ)?', 'QUANTITY_INPUT', 3, 19);

-- Câu hỏi cho dịch vụ 'Đi chợ hộ'
INSERT INTO service_options (service_id, label, option_type, display_order)
VALUES (8, 'Thời gian giao hàng mong muốn?', 'SINGLE_CHOICE_DROPDOWN', 1);
INSERT INTO service_option_choices (option_id, label, display_order) VALUES
(14, 'Trong ngày', 1),
(14, 'Ngày hôm sau', 2);

INSERT INTO pricing_rules (service_id, rule_name, condition_logic, priority, price_adjustment, staff_adjustment, duration_adjustment_hours) VALUES
(2, 'Phụ thu nhà phố lớn', 'ALL', 10, 250000, 1, 2.0),
(1, 'Giặt chăn ga', 'ALL', 5, 30000, 0, 0.5),
(1, 'Rửa chén', 'ALL', 5, 15000, 0, 0.5),
(1, 'Lau cửa kính', 'ALL', 5, 40000, 0, 1.0),
(3, 'Vệ sinh nệm', 'ALL', 5, 150000, 0, 1.0),
(3, 'Vệ sinh rèm', 'ALL', 5, 100000, 0, 1.0),
(4, 'Máy lạnh âm trần', 'ALL', 5, 50000, 0, 0.5),
(5, 'Gấp quần áo', 'ALL', 5, 10000, 0, 1.0),
(7, 'Mua nguyên liệu nấu ăn', 'ALL', 5, 30000, 0, 1.0);

-- Gán điều kiện cho các quy tắc trên
-- Phụ thu nhà phố lớn: yêu cầu nhà phố và diện tích trên 80m²
INSERT INTO rule_conditions (rule_id, choice_id)
VALUES (
(SELECT rule_id FROM pricing_rules WHERE rule_name = 'Phụ thu nhà phố lớn'),
(SELECT choice_id FROM service_option_choices WHERE option_id = 1 AND label = 'Nhà phố')
);

INSERT INTO rule_conditions (rule_id, choice_id)
VALUES (
(SELECT rule_id FROM pricing_rules WHERE rule_name = 'Phụ thu nhà phố lớn'),
(SELECT choice_id FROM service_option_choices WHERE option_id = 3 AND label = 'Trên 80m²')
);

-- Giặt chăn ga
INSERT INTO rule_conditions (rule_id, choice_id)
VALUES (
(SELECT rule_id FROM pricing_rules WHERE rule_name = 'Giặt chăn ga'),
(SELECT choice_id FROM service_option_choices WHERE option_id = 5 AND label = 'Giặt chăn ga')
);

-- Rửa chén
INSERT INTO rule_conditions (rule_id, choice_id)
VALUES (
(SELECT rule_id FROM pricing_rules WHERE rule_name = 'Rửa chén'),
(SELECT choice_id FROM service_option_choices WHERE option_id = 5 AND label = 'Rửa chén')
);

-- Lau cửa kính
INSERT INTO rule_conditions (rule_id, choice_id)
VALUES (
(SELECT rule_id FROM pricing_rules WHERE rule_name = 'Lau cửa kính'),
(SELECT choice_id FROM service_option_choices WHERE option_id = 5 AND label = 'Lau cửa kính')
);

-- Vệ sinh nệm
INSERT INTO rule_conditions (rule_id, choice_id)
VALUES (
(SELECT rule_id FROM pricing_rules WHERE rule_name = 'Vệ sinh nệm'),
(SELECT choice_id FROM service_option_choices WHERE option_id = 6 AND label = 'Nệm')
);

-- Vệ sinh rèm
INSERT INTO rule_conditions (rule_id, choice_id)
VALUES (
(SELECT rule_id FROM pricing_rules WHERE rule_name = 'Vệ sinh rèm'),
(SELECT choice_id FROM service_option_choices WHERE option_id = 6 AND label = 'Rèm')
);

-- Máy lạnh âm trần
INSERT INTO rule_conditions (rule_id, choice_id)
VALUES (
(SELECT rule_id FROM pricing_rules WHERE rule_name = 'Máy lạnh âm trần'),
(SELECT choice_id FROM service_option_choices WHERE option_id = 7 AND label = 'Âm trần/Cassette')
);

-- Gấp quần áo
INSERT INTO rule_conditions (rule_id, choice_id)
VALUES (
(SELECT rule_id FROM pricing_rules WHERE rule_name = 'Gấp quần áo'),
(SELECT choice_id FROM service_option_choices WHERE option_id = 9 AND label = 'Có')
);

-- Mua nguyên liệu nấu ăn
INSERT INTO rule_conditions (rule_id, choice_id)
VALUES (
(SELECT rule_id FROM pricing_rules WHERE rule_name = 'Mua nguyên liệu nấu ăn'),
(SELECT choice_id FROM service_option_choices WHERE option_id = 12 AND label = 'Có')
);

INSERT INTO payment_methods (method_code, method_name, is_active) VALUES
('CASH', 'Thanh toán tiền mặt', TRUE),
('MOMO', 'Ví điện tử Momo', TRUE),
('VNPAY', 'Cổng thanh toán VNPAY', TRUE),
('BANK_TRANSFER', 'Chuyển khoản ngân hàng', TRUE);

-- Add more bookings with their corresponding booking details and assignments
-- Each booking will have exactly 1 booking detail

-- Booking 3: Mary Jones - Pending status
INSERT INTO bookings (booking_id, customer_id, address_id, booking_code, booking_time, note, total_amount, status) VALUES
('b0000001-0000-0000-0000-000000000003', 'c1000001-0000-0000-0000-000000000002', 'adrs0001-0000-0000-0000-000000000002', 'BK000003', '2025-08-30 10:00:00+07', 'Cần vệ sinh máy lạnh trong phòng ngủ.', 150000.00, 'PENDING');

INSERT INTO booking_details (booking_detail_id, booking_id, service_id, quantity, price_per_unit, sub_total) VALUES
('bd000001-0000-0000-0000-000000000003', 'b0000001-0000-0000-0000-000000000003', (SELECT service_id FROM service WHERE name = 'Vệ sinh máy lạnh'), 1, 150000.00, 150000.00);

-- Booking 4: John Doe - Confirmed status with assignment
INSERT INTO bookings (booking_id, customer_id, address_id, booking_code, booking_time, note, total_amount, status) VALUES
('b0000001-0000-0000-0000-000000000004', 'c1000001-0000-0000-0000-000000000001', 'adrs0001-0000-0000-0000-000000000001', 'BK000004', '2025-09-01 08:00:00+07', 'Giặt vest cho buổi họp quan trọng.', 120000.00, 'CONFIRMED');

INSERT INTO booking_details (booking_detail_id, booking_id, service_id, quantity, price_per_unit, sub_total) VALUES
('bd000001-0000-0000-0000-000000000004', 'b0000001-0000-0000-0000-000000000004', (SELECT service_id FROM service WHERE name = 'Giặt hấp cao cấp'), 1, 120000.00, 120000.00);

INSERT INTO assignments (assignment_id, booking_detail_id, employee_id, status) VALUES
('as000001-0000-0000-0000-000000000003', 'bd000001-0000-0000-0000-000000000004', 'e1000001-0000-0000-0000-000000000002', 'ASSIGNED');

-- Booking 5: Jane Smith Customer - In Progress
INSERT INTO bookings (booking_id, customer_id, address_id, booking_code, booking_time, note, total_amount, status) VALUES
('b0000001-0000-0000-0000-000000000005', 'c1000001-0000-0000-0000-000000000003', 'adrs0001-0000-0000-0000-000000000003', 'BK000005', '2025-08-25 15:00:00+07', 'Nấu cơm tối cho gia đình 4 người.', 150000.00, 'IN_PROGRESS');

INSERT INTO booking_details (booking_detail_id, booking_id, service_id, quantity, price_per_unit, sub_total) VALUES
('bd000001-0000-0000-0000-000000000005', 'b0000001-0000-0000-0000-000000000005', (SELECT service_id FROM service WHERE name = 'Nấu ăn gia đình'), 2, 60000.00, 120000.00);

INSERT INTO assignments (assignment_id, booking_detail_id, employee_id, status, check_in_time) VALUES
('as000001-0000-0000-0000-000000000004', 'bd000001-0000-0000-0000-000000000005', 'e1000001-0000-0000-0000-000000000001', 'IN_PROGRESS', '2025-08-25 15:00:00+07');

-- Booking 6: Mary Jones - Cancelled
INSERT INTO bookings (booking_id, customer_id, address_id, booking_code, booking_time, note, total_amount, status) VALUES
('b0000001-0000-0000-0000-000000000006', 'c1000001-0000-0000-0000-000000000002', 'adrs0001-0000-0000-0000-000000000002', 'BK000006', '2025-08-22 14:00:00+07', 'Hủy do thay đổi lịch trình.', 60000.00, 'CANCELLED');

INSERT INTO booking_details (booking_detail_id, booking_id, service_id, quantity, price_per_unit, sub_total) VALUES
('bd000001-0000-0000-0000-000000000006', 'b0000001-0000-0000-0000-000000000006', (SELECT service_id FROM service WHERE name = 'Giặt sấy theo kg'), 2, 30000.00, 60000.00);

-- Booking 7: John Doe - Awaiting Employee (no assignment yet)
INSERT INTO bookings (booking_id, customer_id, address_id, booking_code, booking_time, note, total_amount, status, promotion_id) VALUES
('b0000001-0000-0000-0000-000000000007', 'c1000001-0000-0000-0000-000000000001', 'adrs0001-0000-0000-0000-000000000001', 'BK000007', '2025-09-05 09:00:00+07', 'Vệ sinh sofa phòng khách.', 270000.00, 'AWAITING_EMPLOYEE', (SELECT promotion_id FROM promotions WHERE promo_code = 'GIAM20K'));

INSERT INTO booking_details (booking_detail_id, booking_id, service_id, quantity, price_per_unit, sub_total) VALUES
('bd000001-0000-0000-0000-000000000007', 'b0000001-0000-0000-0000-000000000007', (SELECT service_id FROM service WHERE name = 'Vệ sinh Sofa - Nệm - Rèm'), 1, 300000.00, 300000.00);

-- Booking 8: Jane Smith Customer - Completed with assignment
INSERT INTO bookings (booking_id, customer_id, address_id, booking_code, booking_time, note, total_amount, status) VALUES
('b0000001-0000-0000-0000-000000000008', 'c1000001-0000-0000-0000-000000000003', 'adrs0001-0000-0000-0000-000000000003', 'BK000008', '2025-08-18 11:00:00+07', 'Đi chợ mua thực phẩm cho tuần.', 40000.00, 'COMPLETED');

INSERT INTO booking_details (booking_detail_id, booking_id, service_id, quantity, price_per_unit, sub_total) VALUES
('bd000001-0000-0000-0000-000000000008', 'b0000001-0000-0000-0000-000000000008', (SELECT service_id FROM service WHERE name = 'Đi chợ hộ'), 1, 40000.00, 40000.00);

INSERT INTO assignments (assignment_id, booking_detail_id, employee_id, status, check_in_time, check_out_time) VALUES
('as000001-0000-0000-0000-000000000005', 'bd000001-0000-0000-0000-000000000008', 'e1000001-0000-0000-0000-000000000001', 'COMPLETED', '2025-08-18 11:00:00+07', '2025-08-18 12:30:00+07');

-- Booking 9: Mary Jones - Awaiting Employee
INSERT INTO bookings (booking_id, customer_id, address_id, booking_code, booking_time, note, total_amount, status) VALUES
('b0000001-0000-0000-0000-000000000009', 'c1000001-0000-0000-0000-000000000002', 'adrs0001-0000-0000-0000-000000000002', 'BK000009', '2025-09-03 16:00:00+07', 'Dọn dẹp nhà cửa 3 giờ.', 150000.00, 'AWAITING_EMPLOYEE');

INSERT INTO booking_details (booking_detail_id, booking_id, service_id, quantity, price_per_unit, sub_total) VALUES
('bd000001-0000-0000-0000-000000000009', 'b0000001-0000-0000-0000-000000000009', (SELECT service_id FROM service WHERE name = 'Dọn dẹp theo giờ'), 3, 50000.00, 150000.00);

-- Booking 10: John Doe - Confirmed
INSERT INTO bookings (booking_id, customer_id, address_id, booking_code, booking_time, note, total_amount, status, promotion_id) VALUES
('b0000001-0000-0000-0000-000000000010', 'c1000001-0000-0000-0000-000000000001', 'adrs0001-0000-0000-0000-000000000001', 'BK000010', '2025-09-02 13:00:00+07', 'Tổng vệ sinh nhà phố 2 tầng.', 630000.00, 'CONFIRMED', (SELECT promotion_id FROM promotions WHERE promo_code = 'KHAITRUONG10'));

INSERT INTO booking_details (booking_detail_id, booking_id, service_id, quantity, price_per_unit, sub_total, selected_choice_ids) VALUES
('bd000001-0000-0000-0000-000000000010', 'b0000001-0000-0000-0000-000000000010', (SELECT service_id FROM service WHERE name = 'Tổng vệ sinh'), 1, 700000.00, 700000.00, '2,4'); -- Nhà phố, Trên 80m²

INSERT INTO assignments (assignment_id, booking_detail_id, employee_id, status) VALUES
('as000001-0000-0000-0000-000000000006', 'bd000001-0000-0000-0000-000000000010', 'e1000001-0000-0000-0000-000000000002', 'ASSIGNED'),
('as000001-0000-0000-0000-000000000007', 'bd000001-0000-0000-0000-000000000010', 'e1000001-0000-0000-0000-000000000001', 'ASSIGNED');

-- Add corresponding payments for the new bookings
INSERT INTO payments (payment_id, booking_id, amount, method_id, payment_status, transaction_code, paid_at) VALUES
('pay00001-0000-0000-0000-000000000003', 'b0000001-0000-0000-0000-000000000004', 120000.00, (SELECT method_id FROM payment_methods WHERE method_code = 'CASH'), 'PAID', NULL, '2025-09-01 08:30:00+07'),
('pay00001-0000-0000-0000-000000000004', 'b0000001-0000-0000-0000-000000000005', 150000.00, (SELECT method_id FROM payment_methods WHERE method_code = 'MOMO'), 'PENDING', NULL, NULL),
('pay00001-0000-0000-0000-000000000005', 'b0000001-0000-0000-0000-000000000008', 40000.00, (SELECT method_id FROM payment_methods WHERE method_code = 'CASH'), 'PAID', NULL, '2025-08-18 12:30:00+07'),
('pay00001-0000-0000-0000-000000000001', 'b0000001-0000-0000-0000-000000000001', 80000.00, (SELECT method_id FROM payment_methods WHERE method_code = 'VNPAY'), 'PAID', 'VNP123456789', '2025-08-20 13:05:00+07'),
('pay00001-0000-0000-0000-000000000002', 'b0000001-0000-0000-0000-000000000002', 90000.00, (SELECT method_id FROM payment_methods WHERE method_code = 'MOMO'), 'PENDING', NULL, NULL),
('pay00001-0000-0000-0000-000000000006', 'b0000001-0000-0000-0000-000000000010', 630000.00, (SELECT method_id FROM payment_methods WHERE method_code = 'VNPAY'), 'PENDING', NULL, NULL);

-- Add reviews for completed bookings
INSERT INTO review (booking_id, customer_id, employee_id, comment) VALUES
('b0000001-0000-0000-0000-000000000008', 'c1000001-0000-0000-0000-000000000003', 'e1000001-0000-0000-0000-000000000001', 'Nhân viên mua đúng yêu cầu và giao hàng nhanh chóng.');

INSERT INTO review_details (review_id, criteria_id, rating) VALUES
(2, (SELECT criteria_id FROM review_criteria WHERE criteria_name = 'Thái độ'), 4.5),
(2, (SELECT criteria_id FROM review_criteria WHERE criteria_name = 'Đúng giờ'), 5.0),
(2, (SELECT criteria_id FROM review_criteria WHERE criteria_name = 'Chất lượng công việc'), 4.0);

UPDATE service SET base_price = 60000 WHERE name = 'Dọn dẹp theo giờ';
UPDATE service SET base_price = 500000 WHERE name = 'Tổng vệ sinh';
UPDATE service SET base_price = 350000 WHERE name = 'Vệ sinh Sofa - Nệm - Rèm';
UPDATE service SET base_price = 200000 WHERE name = 'Vệ sinh máy lạnh';
UPDATE service SET base_price = 25000 WHERE name = 'Giặt sấy theo kg';
UPDATE service SET base_price = 150000 WHERE name = 'Giặt hấp cao cấp';
UPDATE service SET base_price = 80000 WHERE name = 'Nấu ăn gia đình';
UPDATE service SET base_price = 50000 WHERE name = 'Đi chợ hộ';

-- Update pricing rules to reflect realistic market adjustments
UPDATE pricing_rules SET price_adjustment = 200000 WHERE rule_name = 'Phụ thu nhà phố lớn';
UPDATE pricing_rules SET price_adjustment = 25000 WHERE rule_name = 'Giặt chăn ga';
UPDATE pricing_rules SET price_adjustment = 20000 WHERE rule_name = 'Rửa chén';
UPDATE pricing_rules SET price_adjustment = 35000 WHERE rule_name = 'Lau cửa kính';
UPDATE pricing_rules SET price_adjustment = 100000 WHERE rule_name = 'Vệ sinh nệm';
UPDATE pricing_rules SET price_adjustment = 80000 WHERE rule_name = 'Vệ sinh rèm';
UPDATE pricing_rules SET price_adjustment = 100000 WHERE rule_name = 'Máy lạnh âm trần';
UPDATE pricing_rules SET price_adjustment = 15000 WHERE rule_name = 'Gấp quần áo';
UPDATE pricing_rules SET price_adjustment = 40000 WHERE rule_name = 'Mua nguyên liệu nấu ăn';

-- Update existing booking details to reflect new pricing
UPDATE booking_details SET
price_per_unit = 500000,
sub_total = 500000
WHERE booking_detail_id = 'bd000001-0000-0000-0000-000000000001';

UPDATE booking_details SET
price_per_unit = 60000,
sub_total = 120000
WHERE booking_detail_id = 'bd000001-0000-0000-0000-000000000002';

UPDATE booking_details SET
price_per_unit = 200000,
sub_total = 200000
WHERE booking_detail_id = 'bd000001-0000-0000-0000-000000000003';

UPDATE booking_details SET
price_per_unit = 150000,
sub_total = 150000
WHERE booking_detail_id = 'bd000001-0000-0000-0000-000000000004';

UPDATE booking_details SET
price_per_unit = 80000,
sub_total = 160000
WHERE booking_detail_id = 'bd000001-0000-0000-0000-000000000005';

UPDATE booking_details SET
price_per_unit = 25000,
sub_total = 50000
WHERE booking_detail_id = 'bd000001-0000-0000-0000-000000000006';

UPDATE booking_details SET
price_per_unit = 350000,
sub_total = 350000
WHERE booking_detail_id = 'bd000001-0000-0000-0000-000000000007';

UPDATE booking_details SET
price_per_unit = 50000,
sub_total = 50000
WHERE booking_detail_id = 'bd000001-0000-0000-0000-000000000008';

UPDATE booking_details SET
price_per_unit = 60000,
sub_total = 180000
WHERE booking_detail_id = 'bd000001-0000-0000-0000-000000000009';

UPDATE booking_details SET
price_per_unit = 700000,
sub_total = 700000
WHERE booking_detail_id = 'bd000001-0000-0000-0000-000000000010';

-- Update booking total amounts accordingly
UPDATE bookings SET total_amount = 480000 WHERE booking_id = 'b0000001-0000-0000-0000-000000000001'; -- 500k - 20k promotion
UPDATE bookings SET total_amount = 108000 WHERE booking_id = 'b0000001-0000-0000-0000-000000000002'; -- 120k - 10% promotion (max 12k)
UPDATE bookings SET total_amount = 200000 WHERE booking_id = 'b0000001-0000-0000-0000-000000000003';
UPDATE bookings SET total_amount = 150000 WHERE booking_id = 'b0000001-0000-0000-0000-000000000004';
UPDATE bookings SET total_amount = 200000 WHERE booking_id = 'b0000001-0000-0000-0000-000000000005'; -- 160k + 40k for buying ingredients
UPDATE bookings SET total_amount = 50000 WHERE booking_id = 'b0000001-0000-0000-0000-000000000006';
UPDATE bookings SET total_amount = 330000 WHERE booking_id = 'b0000001-0000-0000-0000-000000000007'; -- 350k - 20k promotion
UPDATE bookings SET total_amount = 50000 WHERE booking_id = 'b0000001-0000-0000-0000-000000000008';
UPDATE bookings SET total_amount = 180000 WHERE booking_id = 'b0000001-0000-0000-0000-000000000009';
UPDATE bookings SET total_amount = 630000 WHERE booking_id = 'b0000001-0000-0000-0000-000000000010'; -- 700k - 10% (70k discount)

-- Update payment amounts to match new booking totals
UPDATE payments SET amount = 480000 WHERE payment_id = 'pay00001-0000-0000-0000-000000000001';
UPDATE payments SET amount = 108000 WHERE payment_id = 'pay00001-0000-0000-0000-000000000002';
UPDATE payments SET amount = 150000 WHERE payment_id = 'pay00001-0000-0000-0000-000000000003';
UPDATE payments SET amount = 200000 WHERE payment_id = 'pay00001-0000-0000-0000-000000000004';
UPDATE payments SET amount = 50000 WHERE payment_id = 'pay00001-0000-0000-0000-000000000005';
UPDATE payments SET amount = 630000 WHERE payment_id = 'pay00001-0000-0000-0000-000000000006';

-- Add new pricing rules for quantity-based services
INSERT INTO pricing_rules (service_id, rule_name, condition_logic, priority, price_adjustment, staff_adjustment, duration_adjustment_hours) VALUES
                        ((SELECT service_id FROM service WHERE name = 'Vệ sinh máy lạnh'), 'Phụ thu máy tủ đứng', 'ALL', 8, 150000, 0, 1.0),
                        ((SELECT service_id FROM service WHERE name = 'Giặt hấp cao cấp'), 'Phụ thu áo dài', 'ALL', 5, 50000, 0, 0.5),
                        ((SELECT service_id FROM service WHERE name = 'Giặt hấp cao cấp'), 'Phụ thu đầm dạ hội', 'ALL', 5, 100000, 0, 1.0);

-- Add rule conditions for new pricing rules
INSERT INTO rule_conditions (rule_id, choice_id) VALUES
((SELECT rule_id FROM pricing_rules WHERE rule_name = 'Phụ thu máy tủ đứng'),
(SELECT choice_id FROM service_option_choices WHERE option_id = 7 AND label = 'Tủ đứng'));

INSERT INTO rule_conditions (rule_id, choice_id) VALUES
((SELECT rule_id FROM pricing_rules WHERE rule_name = 'Phụ thu áo dài'),
(SELECT choice_id FROM service_option_choices WHERE option_id = 10 AND label = 'Áo dài'));

INSERT INTO rule_conditions (rule_id, choice_id) VALUES
((SELECT rule_id FROM pricing_rules WHERE rule_name = 'Phụ thu đầm dạ hội'),
(SELECT choice_id FROM service_option_choices WHERE option_id = 10 AND label = 'Đầm'));

-- Insert additional addresses for more booking locations
INSERT INTO address (address_id, customer_id, full_address, ward, city, latitude, longitude, is_default) VALUES
('adrs0001-0000-0000-0000-000000000004', 'c1000001-0000-0000-0000-000000000001', '789 Nguyễn Văn Cừ, Phường Chợ Quán, TP. Hồ Chí Minh', 'Phường Chợ Quán', 'TP. Hồ Chí Minh', 10.7594, 106.6822, false),
('adrs0001-0000-0000-0000-000000000005', 'c1000001-0000-0000-0000-000000000002', '321 Phan Văn Trị, Phường Bình Lợi Trung, TP. Hồ Chí Minh', 'Phường Bình Lợi Trung', 'TP. Hồ Chí Minh', 10.8011, 106.7067, false),
('adrs0001-0000-0000-0000-000000000006', 'c1000001-0000-0000-0000-000000000003', '567 Lý Thường Kiệt, Phường Tân Sơn Nhất, TP. Hồ Chí Minh', 'Phường Tân Sơn Nhất', 'TP. Hồ Chí Minh', 10.7993, 106.6554, false),
('adrs0001-0000-0000-0000-000000000007', 'c1000001-0000-0000-0000-000000000001', '432 Võ Văn Tần, Phường Bàn Cờ, TP. Hồ Chí Minh', 'Phường Bàn Cờ', 'TP. Hồ Chí Minh', 10.7756, 106.6914, false),
('adrs0001-0000-0000-0000-000000000008', 'c1000001-0000-0000-0000-000000000002', '876 Cách Mạng Tháng 8, Phường Tân Sơn Nhất, TP. Hồ Chí Minh', 'Phường Tân Sơn Nhất', 'TP. Hồ Chí Minh', 10.7854, 106.6533, false);

-- Insert 10 additional bookings
INSERT INTO bookings (booking_id, customer_id, address_id, booking_code, booking_time, note, total_amount, status) VALUES
('book0004-0000-0000-0000-000000000001', 'c1000001-0000-0000-0000-000000000001', 'adrs0001-0000-0000-0000-000000000004', 'HKS000004', '2025-10-05 08:00:00+07', 'Cần dọn dẹp tổng quát, chú ý khu vực bếp', 450000, 'AWAITING_EMPLOYEE'),
('book0005-0000-0000-0000-000000000001', 'c1000001-0000-0000-0000-000000000002', 'adrs0001-0000-0000-0000-000000000005', 'HKS000005', '2025-10-05 14:00:00+07', 'Ưu tiên dọn phòng khách và phòng ngủ', 350000, 'AWAITING_EMPLOYEE'),
('book0006-0000-0000-0000-000000000001', 'c1000001-0000-0000-0000-000000000003', 'adrs0001-0000-0000-0000-000000000006', 'HKS000006', '2025-10-06 09:30:00+07', 'Cần giặt rèm cửa và thảm', 600000, 'AWAITING_EMPLOYEE'),
('book0007-0000-0000-0000-000000000001', 'c1000001-0000-0000-0000-000000000001', 'adrs0001-0000-0000-0000-000000000007', 'HKS000007', '2025-10-06 16:00:00+07', 'Dọn dẹp sau tiệc, nhiều rác cần dọn', 500000, 'CONFIRMED'),
('book0008-0000-0000-0000-000000000001', 'c1000001-0000-0000-0000-000000000002', 'adrs0001-0000-0000-0000-000000000008', 'HKS000008', '2025-10-07 10:00:00+07', 'Vệ sinh tổng quát hàng tuần', 400000, 'AWAITING_EMPLOYEE'),
('book0009-0000-0000-0000-000000000001', 'c1000001-0000-0000-0000-000000000001', 'adrs0001-0000-0000-0000-000000000001', 'HKS000009', '2025-10-07 15:30:00+07', 'Cần dọn nhà trước khi có khách', 300000, 'AWAITING_EMPLOYEE'),
('book0010-0000-0000-0000-000000000001', 'c1000001-0000-0000-0000-000000000003', 'adrs0001-0000-0000-0000-000000000003', 'HKS000010', '2025-10-08 08:30:00+07', 'Lau kính cửa sổ và ban công', 250000, 'AWAITING_EMPLOYEE'),
('book0011-0000-0000-0000-000000000001', 'c1000001-0000-0000-0000-000000000002', 'adrs0001-0000-0000-0000-000000000002', 'HKS000011', '2025-10-08 13:00:00+07', 'Dọn dẹp và sắp xếp tủ quần áo', 350000, 'AWAITING_EMPLOYEE'),
('book0012-0000-0000-0000-000000000001', 'c1000001-0000-0000-0000-000000000001', 'adrs0001-0000-0000-0000-000000000004', 'HKS000012', '2025-10-09 11:00:00+07', 'Vệ sinh máy lạnh và quạt trần', 550000, 'AWAITING_EMPLOYEE'),
('book0013-0000-0000-0000-000000000001', 'c1000001-0000-0000-0000-000000000003', 'adrs0001-0000-0000-0000-000000000006', 'HKS000013', '2025-10-09 17:00:00+07', 'Dọn dẹp sau khi sửa chữa nhà', 700000, 'PENDING');

-- Insert booking details for the new bookings (assuming service_id 1, 2, 3 exist)
INSERT INTO booking_details (booking_detail_id, booking_id, service_id, quantity, price_per_unit, sub_total) VALUES
-- Booking HKS000004 - General Cleaning
('bd000004-0000-0000-0000-000000000001', 'book0004-0000-0000-0000-000000000001', 1, 1, 450000, 450000),

-- Booking HKS000005 - Room Cleaning
('bd000005-0000-0000-0000-000000000001', 'book0005-0000-0000-0000-000000000001', 1, 1, 350000, 350000),

-- Booking HKS000006 - Deep Cleaning with Laundry
('bd000006-0000-0000-0000-000000000001', 'book0006-0000-0000-0000-000000000001', 2, 1, 400000, 400000),
('bd000006-0000-0000-0000-000000000002', 'book0006-0000-0000-0000-000000000001', 3, 1, 200000, 200000),

-- Booking HKS000007 - Post-party Cleaning
('bd000007-0000-0000-0000-000000000001', 'book0007-0000-0000-0000-000000000001', 2, 1, 500000, 500000),

-- Booking HKS000008 - Weekly Cleaning
('bd000008-0000-0000-0000-000000000001', 'book0008-0000-0000-0000-000000000001', 1, 1, 400000, 400000),

-- Booking HKS000009 - Quick Cleaning
('bd000009-0000-0000-0000-000000000001', 'book0009-0000-0000-0000-000000000001', 1, 1, 300000, 300000),

-- Booking HKS000010 - Window Cleaning
('bd000010-0000-0000-0000-000000000001', 'book0010-0000-0000-0000-000000000001', 1, 1, 250000, 250000),

-- Booking HKS000011 - Organizing Service
('bd000011-0000-0000-0000-000000000001', 'book0011-0000-0000-0000-000000000001', 1, 1, 350000, 350000),

-- Booking HKS000012 - Appliance Cleaning
('bd000012-0000-0000-0000-000000000001', 'book0012-0000-0000-0000-000000000001', 2, 1, 550000, 550000),

-- Booking HKS000013 - Post-renovation Cleaning
('bd000013-0000-0000-0000-000000000001', 'book0013-0000-0000-0000-000000000001', 2, 2, 350000, 700000);

-- Insert assignments for some bookings (only for CONFIRMED and IN_PROGRESS bookings)
INSERT INTO assignments (assignment_id, booking_detail_id, employee_id, status, check_in_time) VALUES
('assgn004-0000-0000-0000-000000000001', 'bd000007-0000-0000-0000-000000000001', 'e1000001-0000-0000-0000-000000000001', 'ASSIGNED', NULL);

-- Insert payments for some bookings
INSERT INTO payments (payment_id, booking_id, amount, payment_status, transaction_code, paid_at) VALUES
('pay00004-0000-0000-0000-000000000001', 'book0007-0000-0000-0000-000000000001', 500000, 'PAID', 'TXN20240926001', '2024-09-26 15:30:00+07'),
('pay00005-0000-0000-0000-000000000001', 'book0013-0000-0000-0000-000000000001', 700000, 'PENDING', NULL, NULL);

-- Insert employee unavailability (some employees might be on leave)
INSERT INTO employee_unavailability (unavailability_id, employee_id, start_time, end_time, reason, is_approved) VALUES
('unavl001-0000-0000-0000-000000000001', 'e1000001-0000-0000-0000-000000000002', '2024-09-25 00:00:00+07', '2024-09-25 23:59:59+07', 'Nghỉ phép cá nhân', true),
('unavl002-0000-0000-0000-000000000001', 'e1000001-0000-0000-0000-000000000001', '2024-09-30 14:00:00+07', '2024-09-30 18:00:00+07', 'Khám bệnh định kỳ', true);
