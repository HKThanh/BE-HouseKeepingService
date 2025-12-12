-- Seed data: 95 Seed Additional Bookings And Payments

-- Insert additional addresses for more booking locations
INSERT INTO address (address_id, customer_id, full_address, ward, city, latitude, longitude, is_default) VALUES
('adrs0001-0000-0000-0000-000000000004', 'c1000001-0000-0000-0000-000000000001', '789 Nguyễn Văn Cừ, Phường Vĩnh Tân, Thành phố Hồ Chí Minh', 'Phường Vĩnh Tân', 'Thành phố Hồ Chí Minh', 10.7594, 106.6822, false),
('adrs0001-0000-0000-0000-000000000005', 'c1000001-0000-0000-0000-000000000002', '321 Phan Văn Trị, Phường Bình Cơ, Thành phố Hồ Chí Minh', 'Phường Bình Cơ', 'Thành phố Hồ Chí Minh', 10.8011, 106.7067, false),
('adrs0001-0000-0000-0000-000000000006', 'c1000001-0000-0000-0000-000000000003', '567 Lý Thường Kiệt, Phường Tân Hiệp, Thành phố Hồ Chí Minh', 'Phường Tân Hiệp', 'Thành phố Hồ Chí Minh', 10.7993, 106.6554, false),
('adrs0001-0000-0000-0000-000000000007', 'c1000001-0000-0000-0000-000000000001', '432 Võ Văn Tần, Phường Dĩ An, Thành phố Hồ Chí Minh', 'Phường Dĩ An', 'Thành phố Hồ Chí Minh', 10.7756, 106.6914, false),
('adrs0001-0000-0000-0000-000000000008', 'c1000001-0000-0000-0000-000000000002', '876 Cách Mạng Tháng 8, Phường Tân Đông Hiệp, Thành phố Hồ Chí Minh', 'Phường Tân Đông Hiệp', 'Thành phố Hồ Chí Minh', 10.7854, 106.6533, false);

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

-- =============================================
-- Verified awaiting-employee bookings for recommendation testing
-- =============================================

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
    is_verified,
    title
) VALUES
('book0014-0000-0000-0000-000000000001', 'c1000001-0000-0000-0000-000000000001', 'adrs0001-0000-0000-0000-000000000004', 'HKSVA014', '2025-11-10 08:00:00+07', 'Ưu tiên phòng khách và ban công', 420000, 'AWAITING_EMPLOYEE', NULL, true, 'Dọn dẹp căn hộ phố đi bộ Nguyễn Huệ'),
('book0015-0000-0000-0000-000000000001', 'c1000001-0000-0000-0000-000000000002', 'adrs0001-0000-0000-0000-000000000005', 'HKSVA015', '2025-11-10 13:30:00+07', 'Cần xử lý bụi mịn và cửa kính', 380000, 'AWAITING_EMPLOYEE', NULL, true, 'Vệ sinh căn hộ Bình Lợi'),
('book0016-0000-0000-0000-000000000001', 'c1000001-0000-0000-0000-000000000003', 'adrs0001-0000-0000-0000-000000000006', 'HKSVA016', '2025-11-11 09:00:00+07', 'Lau chùi nội thất gỗ và rèm', 560000, 'AWAITING_EMPLOYEE', NULL, true, 'Chăm sóc nhà phố Tân Sơn Nhất'),
('book0017-0000-0000-0000-000000000001', 'c1000001-0000-0000-0000-000000000004', 'adrs0001-0000-0000-0000-000000000009', 'HKSVA017', '2025-11-11 15:00:00+07', 'Chuẩn bị đón khách cuối tuần', 460000, 'AWAITING_EMPLOYEE', NULL, true, 'Dọn nhà chung cư Quận 1'),
('book0018-0000-0000-0000-000000000001', 'c1000001-0000-0000-0000-000000000005', 'adrs0001-0000-0000-0000-000000000010', 'HKSVA018', '2025-11-12 08:30:00+07', 'Giặt thảm phòng ngủ và sofa', 520000, 'AWAITING_EMPLOYEE', NULL, true, 'Vệ sinh cao cấp Phường Cầu Kho'),
('book0019-0000-0000-0000-000000000001', 'c1000001-0000-0000-0000-000000000006', 'adrs0001-0000-0000-0000-000000000011', 'HKSVA019', '2025-11-12 14:00:00+07', 'Khử khuẩn đồ chơi trẻ em', 340000, 'AWAITING_EMPLOYEE', NULL, true, 'Dịch vụ vệ sinh gia đình trẻ nhỏ'),
('book0020-0000-0000-0000-000000000001', 'c1000001-0000-0000-0000-000000000007', 'adrs0001-0000-0000-0000-000000000012', 'HKSVA020', '2025-11-13 09:30:00+07', 'Lau máy lạnh và quạt trần', 480000, 'AWAITING_EMPLOYEE', NULL, true, 'Combo vệ sinh thiết bị làm mát'),
('book0021-0000-0000-0000-000000000001', 'c1000001-0000-0000-0000-000000000008', 'adrs0001-0000-0000-0000-000000000013', 'HKSVA021', '2025-11-13 16:00:00+07', 'Dọn bếp và khu vực ăn uống', 360000, 'AWAITING_EMPLOYEE', NULL, true, 'Làm sạch căn bếp ấm cúng'),
('book0022-0000-0000-0000-000000000001', 'c1000001-0000-0000-0000-000000000009', 'adrs0001-0000-0000-0000-000000000014', 'HKSVA022', '2025-11-14 10:30:00+07', 'Tổng vệ sinh nhà trước sự kiện', 610000, 'AWAITING_EMPLOYEE', NULL, true, 'Chuẩn bị nhà cho tiệc gia đình'),
('book0023-0000-0000-0000-000000000001', 'c1000001-0000-0000-0000-000000000010', 'adrs0001-0000-0000-0000-000000000015', 'HKSVA023', '2025-11-14 18:00:00+07', 'Ưu tiên phòng ngủ master', 390000, 'AWAITING_EMPLOYEE', NULL, true, 'Dọn phòng nghỉ cao cấp');

-- Insert image URLs into booking_image_urls table
INSERT INTO booking_image_urls (booking_id, image_url) VALUES
('book0014-0000-0000-0000-000000000001', 'https://picsum.photos/seed/picsum/200/300'),
('book0015-0000-0000-0000-000000000001', 'https://picsum.photos/seed/picsum/200/300'),
('book0016-0000-0000-0000-000000000001', 'https://picsum.photos/seed/picsum/200/300'),
('book0017-0000-0000-0000-000000000001', 'https://picsum.photos/seed/picsum/200/300'),
('book0018-0000-0000-0000-000000000001', 'https://picsum.photos/seed/picsum/200/300'),
('book0019-0000-0000-0000-000000000001', 'https://picsum.photos/seed/picsum/200/300'),
('book0020-0000-0000-0000-000000000001', 'https://picsum.photos/seed/picsum/200/300'),
('book0021-0000-0000-0000-000000000001', 'https://picsum.photos/seed/picsum/200/300'),
('book0022-0000-0000-0000-000000000001', 'https://picsum.photos/seed/picsum/200/300'),
('book0023-0000-0000-0000-000000000001', 'https://picsum.photos/seed/picsum/200/300');

INSERT INTO booking_details (booking_detail_id, booking_id, service_id, quantity, price_per_unit, sub_total) VALUES
('bdva014-0000-0000-0000-000000000001', 'book0014-0000-0000-0000-000000000001', (SELECT service_id FROM service WHERE name = 'Dọn dẹp theo giờ'), 4, 105000, 420000),
('bdva015-0000-0000-0000-000000000001', 'book0015-0000-0000-0000-000000000001', (SELECT service_id FROM service WHERE name = 'Tổng vệ sinh'), 1, 380000, 380000),
('bdva016-0000-0000-0000-000000000001', 'book0016-0000-0000-0000-000000000001', (SELECT service_id FROM service WHERE name = 'Vệ sinh Sofa - Nệm - Rèm'), 1, 560000, 560000),
('bdva017-0000-0000-0000-000000000001', 'book0017-0000-0000-0000-000000000001', (SELECT service_id FROM service WHERE name = 'Dọn dẹp theo giờ'), 4, 115000, 460000),
('bdva018-0000-0000-0000-000000000001', 'book0018-0000-0000-0000-000000000001', (SELECT service_id FROM service WHERE name = 'Giặt sấy theo kg'), 10, 52000, 520000),
('bdva019-0000-0000-0000-000000000001', 'book0019-0000-0000-0000-000000000001', (SELECT service_id FROM service WHERE name = 'Tổng vệ sinh'), 1, 340000, 340000),
('bdva020-0000-0000-0000-000000000001', 'book0020-0000-0000-0000-000000000001', (SELECT service_id FROM service WHERE name = 'Vệ sinh máy lạnh'), 3, 160000, 480000),
('bdva021-0000-0000-0000-000000000001', 'book0021-0000-0000-0000-000000000001', (SELECT service_id FROM service WHERE name = 'Nấu ăn gia đình'), 4, 90000, 360000),
('bdva022-0000-0000-0000-000000000001', 'book0022-0000-0000-0000-000000000001', (SELECT service_id FROM service WHERE name = 'Tổng vệ sinh'), 1, 610000, 610000),
('bdva023-0000-0000-0000-000000000001', 'book0023-0000-0000-0000-000000000001', (SELECT service_id FROM service WHERE name = 'Dọn dẹp theo giờ'), 3, 130000, 390000);

-- Booking phù hợp với khu vực Phường Tây Thạnh (employee e1000001-...-000000000001)
INSERT INTO bookings (booking_id, customer_id, address_id, booking_code, booking_time, note, total_amount, status, promotion_id, is_verified, title) VALUES
('book0024-0000-0000-0000-000000000001', 'c1000001-0000-0000-0000-000000000001', 'adrs0001-0000-0000-0000-000000000001', 'HKSVA024', '2025-11-12 09:00:00+07', 'Vệ sinh tổng quát nhà phố Tây Thạnh', 450000, 'AWAITING_EMPLOYEE', NULL, true, 'Tổng vệ sinh nhà phố Tây Thạnh');

INSERT INTO booking_image_urls (booking_id, image_url) VALUES
('book0024-0000-0000-0000-000000000001', 'https://picsum.photos/seed/picsum/200/300');

INSERT INTO booking_details (booking_detail_id, booking_id, service_id, quantity, price_per_unit, sub_total) VALUES
('bdva024-0000-0000-0000-000000000001', 'book0024-0000-0000-0000-000000000001', (SELECT service_id FROM service WHERE name = 'Tổng vệ sinh'), 1, 450000, 450000);

-- =============================================
