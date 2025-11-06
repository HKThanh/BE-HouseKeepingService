-- Seed data: 92 Seed Reviews

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
