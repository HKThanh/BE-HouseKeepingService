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

-- Các đánh giá bổ sung cho các booking đã hoàn thành (54-56)
INSERT INTO review (booking_id, customer_id, employee_id, comment) VALUES
('b0000001-0000-0000-0000-000000000054', 'c1000001-0000-0000-0000-000000000001', 'e1000001-0000-0000-0000-000000000001', 'Nhân viên dọn dẹp tỉ mỉ, hoàn thành đúng hẹn.'),
('b0000001-0000-0000-0000-000000000055', 'c1000001-0000-0000-0000-000000000003', 'e1000001-0000-0000-0000-000000000002', 'Dịch vụ vệ sinh máy lạnh tốt, tư vấn nhiệt tình.'),
('b0000001-0000-0000-0000-000000000056', 'c1000001-0000-0000-0000-000000000004', 'e1000001-0000-0000-0000-000000000003', 'Tổng vệ sinh ổn nhưng cần chú ý hơn phần bếp.');

INSERT INTO review_details (review_id, criteria_id, rating) VALUES
(2, (SELECT criteria_id FROM review_criteria WHERE criteria_name = 'Thái độ'), 4.8),
(2, (SELECT criteria_id FROM review_criteria WHERE criteria_name = 'Đúng giờ'), 4.7),
(2, (SELECT criteria_id FROM review_criteria WHERE criteria_name = 'Chất lượng công việc'), 4.9),
(3, (SELECT criteria_id FROM review_criteria WHERE criteria_name = 'Thái độ'), 4.5),
(3, (SELECT criteria_id FROM review_criteria WHERE criteria_name = 'Đúng giờ'), 4.6),
(3, (SELECT criteria_id FROM review_criteria WHERE criteria_name = 'Chất lượng công việc'), 4.4),
(4, (SELECT criteria_id FROM review_criteria WHERE criteria_name = 'Thái độ'), 3.8),
(4, (SELECT criteria_id FROM review_criteria WHERE criteria_name = 'Đúng giờ'), 4.0),
(4, (SELECT criteria_id FROM review_criteria WHERE criteria_name = 'Chất lượng công việc'), 3.9);

-- =================================================================================
-- TỰ ĐỘNG TẠO REVIEW CHO TẤT CẢ NHÂN VIÊN (2-4 REVIEW/NGƯỜI)
-- =================================================================================
WITH completed_assignments AS (
    SELECT
        a.employee_id,
        b.booking_id,
        b.customer_id,
        b.booking_time,
        ROW_NUMBER() OVER (PARTITION BY a.employee_id ORDER BY b.booking_time) AS rn
    FROM assignments a
    JOIN booking_details bd ON a.booking_detail_id = bd.booking_detail_id
    JOIN bookings b ON bd.booking_id = b.booking_id
    WHERE a.status = 'COMPLETED'
),
review_limits AS (
    SELECT
        e.employee_id,
        2 + (ascii(substring(e.employee_id FROM 1 FOR 1)) % 3) AS review_limit
    FROM employee e
),
prioritized_assignments AS (
    SELECT ca.*
    FROM completed_assignments ca
    JOIN review_limits rl ON rl.employee_id = ca.employee_id
    WHERE ca.rn <= rl.review_limit
),
existing_reviews AS (
    SELECT employee_id, booking_id
    FROM review
),
selected_reviews AS (
    SELECT
        pa.employee_id,
        pa.booking_id,
        pa.customer_id,
        pa.booking_time,
        pa.rn,
        format('Đánh giá tự động #%s cho nhân viên %s', pa.rn, pa.employee_id) AS comment
    FROM prioritized_assignments pa
    LEFT JOIN existing_reviews er
        ON er.employee_id = pa.employee_id
       AND er.booking_id = pa.booking_id
    WHERE er.employee_id IS NULL
),
inserted_reviews AS (
    INSERT INTO review (booking_id, customer_id, employee_id, comment)
    SELECT booking_id, customer_id, employee_id, comment
    FROM selected_reviews
    RETURNING review_id, booking_id, customer_id, employee_id
),
detailed_reviews AS (
    SELECT
        ir.review_id,
        sr.employee_id,
        sr.rn
    FROM inserted_reviews ir
    JOIN selected_reviews sr
        ON sr.booking_id = ir.booking_id
       AND sr.employee_id = ir.employee_id
       AND sr.customer_id = ir.customer_id
)
INSERT INTO review_details (review_id, criteria_id, rating)
SELECT
    dr.review_id,
    rc.criteria_id,
    ROUND(
        LEAST(
            5.0,
            3.5 + (((ascii(substring(dr.employee_id FROM 1 FOR 1)) + dr.rn + rc.criteria_id) % 4) * 0.4)
        ),
        1
    )
FROM detailed_reviews dr
CROSS JOIN review_criteria rc;

-- Cập nhật rating của nhân viên dựa trên điểm trung bình mới
WITH employee_avg AS (
    SELECT r.employee_id, AVG(rd.rating) AS avg_rating
    FROM review r
    JOIN review_details rd ON rd.review_id = r.review_id
    GROUP BY r.employee_id
)
UPDATE employee e
SET rating = CASE
    WHEN ea.avg_rating >= 4.5 THEN 'HIGHEST'
    WHEN ea.avg_rating >= 4.0 THEN 'HIGH'
    WHEN ea.avg_rating >= 3.0 THEN 'MEDIUM'
    WHEN ea.avg_rating >= 2.0 THEN 'LOW'
    ELSE 'LOWEST'
END
FROM employee_avg ea
WHERE e.employee_id = ea.employee_id;
