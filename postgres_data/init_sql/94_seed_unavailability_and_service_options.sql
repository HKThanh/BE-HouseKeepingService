-- Seed data: 94 Seed Unavailability And Service Options

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
INSERT INTO service_options (service_id, label, option_type, display_order)
VALUES (2, 'Loại hình nhà ở?', 'SINGLE_CHOICE_RADIO', 1);
INSERT INTO service_option_choices (option_id, label)
VALUES 
((SELECT option_id FROM service_options WHERE service_id = 2 AND label = 'Loại hình nhà ở?' LIMIT 1), 'Căn hộ'), 
((SELECT option_id FROM service_options WHERE service_id = 2 AND label = 'Loại hình nhà ở?' LIMIT 1), 'Nhà phố');

-- Câu hỏi 2 (PHỤ THUỘC): Số tầng (chỉ hiện khi chọn 'Nhà phố' - choice_id=2)
INSERT INTO service_options (service_id, label, option_type, display_order, parent_choice_id)
VALUES (2, 'Nhà bạn có mấy tầng (bao gồm trệt)?', 'QUANTITY_INPUT', 2, (SELECT choice_id FROM service_option_choices WHERE label = 'Nhà phố' LIMIT 1));

-- Câu hỏi 3: Diện tích
INSERT INTO service_options (service_id, label, option_type, display_order)
VALUES (2, 'Diện tích dọn dẹp?', 'SINGLE_CHOICE_DROPDOWN', 3);
INSERT INTO service_option_choices (option_id, label)
VALUES 
((SELECT option_id FROM service_options WHERE service_id = 2 AND label = 'Diện tích dọn dẹp?' LIMIT 1), 'Dưới 80m²'), 
((SELECT option_id FROM service_options WHERE service_id = 2 AND label = 'Diện tích dọn dẹp?' LIMIT 1), 'Trên 80m²');

-- Câu hỏi 2: Công việc thêm
INSERT INTO service_options (service_id, label, option_type, display_order)
VALUES (1, 'Bạn có yêu cầu thêm công việc nào?', 'MULTIPLE_CHOICE_CHECKBOX', 2);
INSERT INTO service_option_choices (option_id, label, display_order) VALUES
((SELECT option_id FROM service_options WHERE service_id = 1 AND label = 'Bạn có yêu cầu thêm công việc nào?' LIMIT 1), 'Giặt chăn ga', 1),
((SELECT option_id FROM service_options WHERE service_id = 1 AND label = 'Bạn có yêu cầu thêm công việc nào?' LIMIT 1), 'Rửa chén', 2),
((SELECT option_id FROM service_options WHERE service_id = 1 AND label = 'Bạn có yêu cầu thêm công việc nào?' LIMIT 1), 'Lau cửa kính', 3);

-- Câu hỏi cho dịch vụ 'Vệ sinh Sofa - Nệm - Rèm'
INSERT INTO service_options (service_id, label, option_type, display_order)
VALUES (3, 'Hạng mục cần vệ sinh?', 'SINGLE_CHOICE_RADIO', 1);
INSERT INTO service_option_choices (option_id, label, display_order) VALUES
((SELECT option_id FROM service_options WHERE service_id = 3 AND label = 'Hạng mục cần vệ sinh?' LIMIT 1), 'Sofa', 1),
((SELECT option_id FROM service_options WHERE service_id = 3 AND label = 'Hạng mục cần vệ sinh?' LIMIT 1), 'Nệm', 2),
((SELECT option_id FROM service_options WHERE service_id = 3 AND label = 'Hạng mục cần vệ sinh?' LIMIT 1), 'Rèm', 3);

-- Câu hỏi cho dịch vụ 'Vệ sinh máy lạnh'
INSERT INTO service_options (service_id, label, option_type, display_order)
VALUES (4, 'Loại máy lạnh?', 'SINGLE_CHOICE_DROPDOWN', 1);
INSERT INTO service_option_choices (option_id, label, display_order) VALUES
((SELECT option_id FROM service_options WHERE service_id = 4 AND label = 'Loại máy lạnh?' LIMIT 1), 'Treo tường', 1),
((SELECT option_id FROM service_options WHERE service_id = 4 AND label = 'Loại máy lạnh?' LIMIT 1), 'Âm trần/Cassette', 2),
((SELECT option_id FROM service_options WHERE service_id = 4 AND label = 'Loại máy lạnh?' LIMIT 1), 'Tủ đứng', 3);
INSERT INTO service_options (service_id, label, option_type, display_order)
VALUES (4, 'Số lượng máy?', 'QUANTITY_INPUT', 2);

-- Câu hỏi cho dịch vụ 'Giặt sấy theo kg'
INSERT INTO service_options (service_id, label, option_type, display_order)
VALUES (5, 'Có cần gấp quần áo sau khi giặt?', 'SINGLE_CHOICE_RADIO', 1);
INSERT INTO service_option_choices (option_id, label, display_order) VALUES
((SELECT option_id FROM service_options WHERE service_id = 5 AND label = 'Có cần gấp quần áo sau khi giặt?' LIMIT 1), 'Có', 1),
((SELECT option_id FROM service_options WHERE service_id = 5 AND label = 'Có cần gấp quần áo sau khi giặt?' LIMIT 1), 'Không', 2);

-- Câu hỏi cho dịch vụ 'Giặt hấp cao cấp'
INSERT INTO service_options (service_id, label, option_type, display_order)
VALUES (6, 'Loại trang phục giặt hấp?', 'SINGLE_CHOICE_DROPDOWN', 1);
INSERT INTO service_option_choices (option_id, label, display_order) VALUES
((SELECT option_id FROM service_options WHERE service_id = 6 AND label = 'Loại trang phục giặt hấp?' LIMIT 1), 'Vest', 1),
((SELECT option_id FROM service_options WHERE service_id = 6 AND label = 'Loại trang phục giặt hấp?' LIMIT 1), 'Áo dài', 2),
((SELECT option_id FROM service_options WHERE service_id = 6 AND label = 'Loại trang phục giặt hấp?' LIMIT 1), 'Đầm', 3);

-- Câu hỏi cho dịch vụ 'Nấu ăn gia đình'
INSERT INTO service_options (service_id, label, option_type, display_order)
VALUES (7, 'Số người ăn?', 'QUANTITY_INPUT', 1);
INSERT INTO service_options (service_id, label, option_type, display_order)
VALUES (7, 'Bạn có cần chúng tôi mua nguyên liệu?', 'SINGLE_CHOICE_RADIO', 2);
INSERT INTO service_option_choices (option_id, label, display_order) VALUES
((SELECT option_id FROM service_options WHERE service_id = 7 AND label = 'Bạn có cần chúng tôi mua nguyên liệu?' LIMIT 1), 'Có', 1),
((SELECT option_id FROM service_options WHERE service_id = 7 AND label = 'Bạn có cần chúng tôi mua nguyên liệu?' LIMIT 1), 'Không', 2);

INSERT INTO pricing_rules (service_id, rule_name, condition_logic, priority, price_adjustment, staff_adjustment, duration_adjustment_hours) VALUES
(2, 'Phụ thu nhà phố lớn', 'ALL', 10, 250000, 1, 2.0),
(1, 'Giặt chăn ga', 'ALL', 5, 30000, 0, 0.5),
(1, 'Rửa chén', 'ALL', 5, 15000, 0, 0.5),
(1, 'Lau cửa kính', 'ALL', 5, 40000, 0, 1.0),
(3, 'Vệ sinh nệm', 'ALL', 5, 150000, 0, 1.0),
(3, 'Vệ sinh rèm', 'ALL', 5, 100000, 0, 1.0),
(4, 'Máy lạnh âm trần', 'ALL', 5, 50000, 0, 0.5),
(5, 'Gấp quần áo', 'ALL', 5, 10000, 0, 1.0),
(7, 'Mua nguyên liệu nấu ăn', 'ALL', 5, 40000, 0, 0.5);

-- Gán điều kiện cho các quy tắc trên
-- Phụ thu nhà phố lớn: yêu cầu nhà phố và diện tích trên 80m²
INSERT INTO rule_conditions (rule_id, choice_id)
VALUES (
(SELECT rule_id FROM pricing_rules WHERE rule_name = 'Phụ thu nhà phố lớn'),
(SELECT choice_id FROM service_option_choices 
 WHERE option_id = (SELECT option_id FROM service_options WHERE service_id = 2 AND label = 'Loại hình nhà ở?' LIMIT 1) 
 AND label = 'Nhà phố' LIMIT 1)
);

INSERT INTO rule_conditions (rule_id, choice_id)
VALUES (
(SELECT rule_id FROM pricing_rules WHERE rule_name = 'Phụ thu nhà phố lớn'),
(SELECT choice_id FROM service_option_choices 
 WHERE option_id = (SELECT option_id FROM service_options WHERE service_id = 2 AND label = 'Diện tích dọn dẹp?' LIMIT 1) 
 AND label = 'Trên 80m²' LIMIT 1)
);

-- Giặt chăn ga
INSERT INTO rule_conditions (rule_id, choice_id)
VALUES (
(SELECT rule_id FROM pricing_rules WHERE rule_name = 'Giặt chăn ga'),
(SELECT choice_id FROM service_option_choices 
 WHERE option_id = (SELECT option_id FROM service_options WHERE service_id = 1 AND label = 'Bạn có yêu cầu thêm công việc nào?' LIMIT 1) 
 AND label = 'Giặt chăn ga' LIMIT 1)
);

-- Rửa chén
INSERT INTO rule_conditions (rule_id, choice_id)
VALUES (
(SELECT rule_id FROM pricing_rules WHERE rule_name = 'Rửa chén'),
(SELECT choice_id FROM service_option_choices 
 WHERE option_id = (SELECT option_id FROM service_options WHERE service_id = 1 AND label = 'Bạn có yêu cầu thêm công việc nào?' LIMIT 1) 
 AND label = 'Rửa chén' LIMIT 1)
);

-- Lau cửa kính
INSERT INTO rule_conditions (rule_id, choice_id)
VALUES (
(SELECT rule_id FROM pricing_rules WHERE rule_name = 'Lau cửa kính'),
(SELECT choice_id FROM service_option_choices 
 WHERE option_id = (SELECT option_id FROM service_options WHERE service_id = 1 AND label = 'Bạn có yêu cầu thêm công việc nào?' LIMIT 1) 
 AND label = 'Lau cửa kính' LIMIT 1)
);

-- Vệ sinh nệm
INSERT INTO rule_conditions (rule_id, choice_id)
VALUES (
(SELECT rule_id FROM pricing_rules WHERE rule_name = 'Vệ sinh nệm'),
(SELECT choice_id FROM service_option_choices 
 WHERE option_id = (SELECT option_id FROM service_options WHERE service_id = 3 AND label = 'Hạng mục cần vệ sinh?' LIMIT 1) 
 AND label = 'Nệm' LIMIT 1)
);

-- Vệ sinh rèm
INSERT INTO rule_conditions (rule_id, choice_id)
VALUES (
(SELECT rule_id FROM pricing_rules WHERE rule_name = 'Vệ sinh rèm'),
(SELECT choice_id FROM service_option_choices 
 WHERE option_id = (SELECT option_id FROM service_options WHERE service_id = 3 AND label = 'Hạng mục cần vệ sinh?' LIMIT 1) 
 AND label = 'Rèm' LIMIT 1)
);

-- Máy lạnh âm trần
INSERT INTO rule_conditions (rule_id, choice_id)
VALUES (
(SELECT rule_id FROM pricing_rules WHERE rule_name = 'Máy lạnh âm trần'),
(SELECT choice_id FROM service_option_choices 
 WHERE option_id = (SELECT option_id FROM service_options WHERE service_id = 4 AND label = 'Loại máy lạnh?' LIMIT 1) 
 AND label = 'Âm trần/Cassette' LIMIT 1)
);

-- Gấp quần áo
INSERT INTO rule_conditions (rule_id, choice_id)
VALUES (
(SELECT rule_id FROM pricing_rules WHERE rule_name = 'Gấp quần áo'),
(SELECT choice_id FROM service_option_choices 
 WHERE option_id = (SELECT option_id FROM service_options WHERE service_id = 5 AND label = 'Có cần gấp quần áo sau khi giặt?' LIMIT 1) 
 AND label = 'Có' LIMIT 1)
);

-- Mua nguyên liệu nấu ăn
INSERT INTO rule_conditions (rule_id, choice_id)
VALUES (
(SELECT rule_id FROM pricing_rules WHERE rule_name = 'Mua nguyên liệu nấu ăn'),
(SELECT choice_id FROM service_option_choices 
 WHERE option_id = (SELECT option_id FROM service_options WHERE service_id = 7 AND label = 'Bạn có cần chúng tôi mua nguyên liệu?' LIMIT 1) 
 AND label = 'Có' LIMIT 1)
);

INSERT INTO payment_methods (method_code, method_name, is_active) VALUES
('CASH', 'Thanh toán tiền mặt', TRUE),
('MOMO', 'Ví điện tử Momo', TRUE),
('VNPAY', 'Cổng thanh toán VNPAY', TRUE),
('BANK_TRANSFER', 'Chuyển khoản ngân hàng', TRUE);

-- Add corresponding payments for the bookings
INSERT INTO payments (payment_id, booking_id, amount, method_id, payment_status, transaction_code, paid_at) VALUES
('pay00001-0000-0000-0000-000000000001', 'b0000001-0000-0000-0000-000000000001', 80000.00, (SELECT method_id FROM payment_methods WHERE method_code = 'VNPAY'), 'PAID', 'VNP123456789', '2025-08-20 13:05:00+07'),
('pay00001-0000-0000-0000-000000000002', 'b0000001-0000-0000-0000-000000000002', 90000.00, (SELECT method_id FROM payment_methods WHERE method_code = 'MOMO'), 'PENDING', NULL, NULL),
('pay00001-0000-0000-0000-000000000003', 'b0000001-0000-0000-0000-000000000004', 150000.00, (SELECT method_id FROM payment_methods WHERE method_code = 'CASH'), 'PAID', NULL, '2025-09-01 08:30:00+07'),
('pay00001-0000-0000-0000-000000000004', 'b0000001-0000-0000-0000-000000000003', 200000.00, (SELECT method_id FROM payment_methods WHERE method_code = 'BANK_TRANSFER'), 'PENDING', 'BFT20250901001', NULL),
('pay00001-0000-0000-0000-000000000005', 'b0000001-0000-0000-0000-000000000006', 50000.00, (SELECT method_id FROM payment_methods WHERE method_code = 'CASH'), 'PENDING', NULL, NULL),
('pay00001-0000-0000-0000-000000000006', 'b0000001-0000-0000-0000-000000000010', 630000.00, (SELECT method_id FROM payment_methods WHERE method_code = 'VNPAY'), 'PENDING', NULL, NULL);

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
(SELECT choice_id FROM service_option_choices 
 WHERE option_id = (SELECT option_id FROM service_options WHERE service_id = 4 AND label = 'Loại máy lạnh?' LIMIT 1) 
 AND label = 'Tủ đứng' LIMIT 1));

INSERT INTO rule_conditions (rule_id, choice_id) VALUES
((SELECT rule_id FROM pricing_rules WHERE rule_name = 'Phụ thu áo dài'),
(SELECT choice_id FROM service_option_choices 
 WHERE option_id = (SELECT option_id FROM service_options WHERE service_id = 6 AND label = 'Loại trang phục giặt hấp?' LIMIT 1) 
 AND label = 'Áo dài' LIMIT 1));

INSERT INTO rule_conditions (rule_id, choice_id) VALUES
((SELECT rule_id FROM pricing_rules WHERE rule_name = 'Phụ thu đầm dạ hội'),
(SELECT choice_id FROM service_option_choices 
 WHERE option_id = (SELECT option_id FROM service_options WHERE service_id = 6 AND label = 'Loại trang phục giặt hấp?' LIMIT 1) 
 AND label = 'Đầm' LIMIT 1));
