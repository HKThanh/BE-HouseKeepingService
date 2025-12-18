-- Seed data: A01 - Dữ liệu assignments ASSIGNED để test check-in
-- Cho employee: dangthir1 (e1000001-0000-0000-0000-000000000033) và jane_smith (e1000001-0000-0000-0000-000000000001)
-- Thời gian: 09h15 ngày 16/12/2025 đến 02h00 sáng ngày 17/12/2025

-- Lưu ý: Schema dùng VARCHAR(36) cho các ID (không cast ::uuid)

-- Tạo slot chung để bookings / booking_details / assignments luôn khớp nhau
DROP TABLE IF EXISTS a01_slots;
CREATE TEMP TABLE a01_slots AS
SELECT
	row_number() OVER (ORDER BY ts) AS i,
	ts
FROM generate_series(
	'2025-12-19 07:30:00+07'::timestamptz,
	'2025-12-19 11:00:00+07'::timestamptz,
	interval '15 minutes'
) ts;

-- =================================================================================
-- THÊM BOOKINGS VÀ ASSIGNMENTS ĐỂ TEST CHECK-IN
-- =================================================================================

-- Các bookings từ 09:15 ngày 16/12/2025 đến 02:00 ngày 17/12/2025 (mỗi 15 phút)
WITH slots AS (
	SELECT
		i,
		ts
	FROM a01_slots
),
generated AS (
	SELECT
		i,
		ts AS booking_time,
		(
			'b0000001-0000-0000-0000-' || lpad((100000 + i)::text, 12, '0')
		) AS booking_id,
		(
			'bd000001-0000-0000-0000-' || lpad((100000 + i)::text, 12, '0')
		) AS booking_detail_id,
		(
			'as000001-0000-0000-0000-' || lpad((100000 + i)::text, 12, '0')
		) AS assignment_id,
		('BKCI' || lpad(i::text, 5, '0')) AS booking_code,

		-- Cycle customers/addresses from existing known IDs
		CASE (i % 8)
			WHEN 1 THEN 'c1000001-0000-0000-0000-000000000001'
			WHEN 2 THEN 'c1000001-0000-0000-0000-000000000003'
			WHEN 3 THEN 'c1000001-0000-0000-0000-000000000004'
			WHEN 4 THEN 'c1000001-0000-0000-0000-000000000005'
			WHEN 5 THEN 'c1000001-0000-0000-0000-000000000006'
			WHEN 6 THEN 'c1000001-0000-0000-0000-000000000007'
			WHEN 7 THEN 'c1000001-0000-0000-0000-000000000008'
			ELSE       'c1000001-0000-0000-0000-000000000001'
		END AS customer_id,
		CASE (i % 8)
			WHEN 1 THEN 'adrs0001-0000-0000-0000-000000000001'
			WHEN 2 THEN 'adrs0001-0000-0000-0000-000000000003'
			WHEN 3 THEN 'adrs0001-0000-0000-0000-000000000009'
			WHEN 4 THEN 'adrs0001-0000-0000-0000-000000000010'
			WHEN 5 THEN 'adrs0001-0000-0000-0000-000000000011'
			WHEN 6 THEN 'adrs0001-0000-0000-0000-000000000012'
			WHEN 7 THEN 'adrs0001-0000-0000-0000-000000000013'
			ELSE       'adrs0001-0000-0000-0000-000000000001'
		END AS address_id,

		-- Rotate services; keep totals consistent (1 booking_detail per booking)
		CASE ((i - 1) % 6)
			WHEN 0 THEN 'Dọn dẹp theo giờ'
			WHEN 1 THEN 'Vệ sinh máy lạnh'
			WHEN 2 THEN 'Tổng vệ sinh'
			WHEN 3 THEN 'Giặt sấy theo kg'
			WHEN 4 THEN 'Vệ sinh Sofa - Nệm - Rèm'
			ELSE       'Nấu ăn gia đình'
		END AS service_name,
		CASE ((i - 1) % 6)
			WHEN 0 THEN 4
			WHEN 1 THEN 2
			WHEN 2 THEN 5
			WHEN 3 THEN 8
			WHEN 4 THEN 1
			ELSE       3
		END AS quantity,
		CASE ((i - 1) % 6)
			WHEN 0 THEN 50000.00
			WHEN 1 THEN 150000.00
			WHEN 2 THEN 100000.00
			WHEN 3 THEN 30000.00
			WHEN 4 THEN 300000.00
			ELSE       60000.00
		END AS price_per_unit,
		(
			CASE ((i - 1) % 6)
				WHEN 0 THEN 4 * 50000.00
				WHEN 1 THEN 2 * 150000.00
				WHEN 2 THEN 5 * 100000.00
				WHEN 3 THEN 8 * 30000.00
				WHEN 4 THEN 1 * 300000.00
				ELSE       3 * 60000.00
			END
		) AS sub_total,

		-- Alternate employee between 2 accounts
		CASE (i % 2)
			WHEN 1 THEN 'e1000001-0000-0000-0000-000000000033'
			ELSE       'e1000001-0000-0000-0000-000000000017'
		END AS employee_id
	FROM slots
)
INSERT INTO bookings (booking_id, customer_id, address_id, booking_code, booking_time, note, total_amount, status, promotion_id, is_verified)
SELECT
	g.booking_id,
	g.customer_id,
	g.address_id,
	g.booking_code,
	g.booking_time,
	(
		'Check-in ' || to_char(g.booking_time AT TIME ZONE 'Asia/Ho_Chi_Minh', 'HH24"h"MI') || ' - ' || g.service_name
	) AS note,
	g.sub_total AS total_amount,
	'CONFIRMED' AS status,
	NULL AS promotion_id,
	true AS is_verified
FROM generated g;

-- =================================================================================
-- CHI TIẾT DỊCH VỤ CHO CÁC BOOKINGS
-- =================================================================================

WITH slots AS (
	SELECT
		i,
		ts
	FROM a01_slots
),
generated AS (
	SELECT
		i,
		(
			'b0000001-0000-0000-0000-' || lpad((100000 + i)::text, 12, '0')
		) AS booking_id,
		(
			'bd000001-0000-0000-0000-' || lpad((100000 + i)::text, 12, '0')
		) AS booking_detail_id,
		CASE ((i - 1) % 6)
			WHEN 0 THEN 'Dọn dẹp theo giờ'
			WHEN 1 THEN 'Vệ sinh máy lạnh'
			WHEN 2 THEN 'Tổng vệ sinh'
			WHEN 3 THEN 'Giặt sấy theo kg'
			WHEN 4 THEN 'Vệ sinh Sofa - Nệm - Rèm'
			ELSE       'Nấu ăn gia đình'
		END AS service_name,
		CASE ((i - 1) % 6)
			WHEN 0 THEN 4
			WHEN 1 THEN 2
			WHEN 2 THEN 5
			WHEN 3 THEN 8
			WHEN 4 THEN 1
			ELSE       3
		END AS quantity,
		CASE ((i - 1) % 6)
			WHEN 0 THEN 50000.00
			WHEN 1 THEN 150000.00
			WHEN 2 THEN 100000.00
			WHEN 3 THEN 30000.00
			WHEN 4 THEN 300000.00
			ELSE       60000.00
		END AS price_per_unit,
		(
			CASE ((i - 1) % 6)
				WHEN 0 THEN 4 * 50000.00
				WHEN 1 THEN 2 * 150000.00
				WHEN 2 THEN 5 * 100000.00
				WHEN 3 THEN 8 * 30000.00
				WHEN 4 THEN 1 * 300000.00
				ELSE       3 * 60000.00
			END
		) AS sub_total
	FROM slots
)
INSERT INTO booking_details (booking_detail_id, booking_id, service_id, quantity, price_per_unit, sub_total)
SELECT
	g.booking_detail_id,
	g.booking_id,
	(SELECT service_id FROM service WHERE name = g.service_name),
	g.quantity,
	g.price_per_unit,
	g.sub_total
FROM generated g;

-- =================================================================================
-- ASSIGNMENTS CHO CÁC BOOKINGS - TRẠNG THÁI ASSIGNED (sẵn sàng check-in)
-- =================================================================================

-- Employee: dangthir1 -> e1000001-0000-0000-0000-000000000033
-- Employee: jane_smith -> e1000001-0000-0000-0000-000000000001

WITH slots AS (
	SELECT
		i,
		ts
	FROM a01_slots
),
generated AS (
	SELECT
		i,
		(
			'bd000001-0000-0000-0000-' || lpad((100000 + i)::text, 12, '0')
		) AS booking_detail_id,
		(
			'as000001-0000-0000-0000-' || lpad((100000 + i)::text, 12, '0')
		) AS assignment_id,
		CASE (i % 2)
			WHEN 1 THEN 'e1000001-0000-0000-0000-000000000033'
			ELSE       'e1000001-0000-0000-0000-000000000017'
		END AS employee_id
	FROM slots
)
INSERT INTO assignments (assignment_id, booking_detail_id, employee_id, status, check_in_time, check_out_time)
SELECT
	g.assignment_id,
	g.booking_detail_id,
	g.employee_id,
	'ASSIGNED' AS status,
	NULL AS check_in_time,
	NULL AS check_out_time
FROM generated g;

-- =================================================================================
-- SUMMARY
-- =================================================================================
-- Tổng cộng: 68 bookings và 68 assignments (ASSIGNED status)
-- Thời gian: 09:15 ngày 16/12/2025 đến 02:00 ngày 17/12/2025 (mỗi 15 phút)
-- Employee dangthir1 và jane_smith được phân công luân phiên
-- Tất cả đều ở trạng thái ASSIGNED, sẵn sàng để check-in trên FE
