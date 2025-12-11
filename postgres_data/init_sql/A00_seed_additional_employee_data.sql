-- Seed data: 100 Additional Employee Data
-- File này chứa thêm dữ liệu seed cho employee để test các chức năng
-- Bao gồm: ratings, employee_status, working_zones, unavailability, assignments

-- =================================================================================
-- CẬP NHẬT RATING VÀ EMPLOYEE STATUS ĐA DẠNG CHO EMPLOYEES
-- =================================================================================

-- Cập nhật ratings đa dạng cho các nhân viên
UPDATE employee SET rating = 'HIGHEST', employee_status = 'AVAILABLE' WHERE employee_id = 'e1000001-0000-0000-0000-000000000001';
UPDATE employee SET rating = 'HIGH', employee_status = 'AVAILABLE' WHERE employee_id = 'e1000001-0000-0000-0000-000000000002';
UPDATE employee SET rating = 'MEDIUM', employee_status = 'BUSY' WHERE employee_id = 'e1000001-0000-0000-0000-000000000003';
UPDATE employee SET rating = 'HIGH', employee_status = 'AVAILABLE' WHERE employee_id = 'e1000001-0000-0000-0000-000000000004';
UPDATE employee SET rating = 'HIGHEST', employee_status = 'BUSY' WHERE employee_id = 'e1000001-0000-0000-0000-000000000005';
UPDATE employee SET rating = 'MEDIUM', employee_status = 'ON_LEAVE' WHERE employee_id = 'e1000001-0000-0000-0000-000000000006';
UPDATE employee SET rating = 'LOW', employee_status = 'AVAILABLE' WHERE employee_id = 'e1000001-0000-0000-0000-000000000007';
UPDATE employee SET rating = 'HIGH', employee_status = 'AVAILABLE' WHERE employee_id = 'e1000001-0000-0000-0000-000000000008';
UPDATE employee SET rating = 'MEDIUM', employee_status = 'BUSY' WHERE employee_id = 'e1000001-0000-0000-0000-000000000009';
UPDATE employee SET rating = 'HIGHEST', employee_status = 'AVAILABLE' WHERE employee_id = 'e1000001-0000-0000-0000-000000000010';
UPDATE employee SET rating = 'HIGH', employee_status = 'AVAILABLE' WHERE employee_id = 'e1000001-0000-0000-0000-000000000011';
UPDATE employee SET rating = 'LOWEST', employee_status = 'ON_LEAVE' WHERE employee_id = 'e1000001-0000-0000-0000-000000000012';
UPDATE employee SET rating = 'MEDIUM', employee_status = 'AVAILABLE' WHERE employee_id = 'e1000001-0000-0000-0000-000000000013';
UPDATE employee SET rating = 'HIGH', employee_status = 'BUSY' WHERE employee_id = 'e1000001-0000-0000-0000-000000000014';
UPDATE employee SET rating = 'HIGHEST', employee_status = 'AVAILABLE' WHERE employee_id = 'e1000001-0000-0000-0000-000000000015';
UPDATE employee SET rating = 'MEDIUM', employee_status = 'AVAILABLE' WHERE employee_id = 'e1000001-0000-0000-0000-000000000016';

-- Cập nhật ratings cho 30 nhân viên bổ sung (e17-e46)
UPDATE employee SET rating = 'HIGH', employee_status = 'AVAILABLE' WHERE employee_id = 'e1000001-0000-0000-0000-000000000017';
UPDATE employee SET rating = 'MEDIUM', employee_status = 'AVAILABLE' WHERE employee_id = 'e1000001-0000-0000-0000-000000000018';
UPDATE employee SET rating = 'HIGHEST', employee_status = 'BUSY' WHERE employee_id = 'e1000001-0000-0000-0000-000000000019';
UPDATE employee SET rating = 'LOW', employee_status = 'AVAILABLE' WHERE employee_id = 'e1000001-0000-0000-0000-000000000020';
UPDATE employee SET rating = 'MEDIUM', employee_status = 'AVAILABLE' WHERE employee_id = 'e1000001-0000-0000-0000-000000000021';
UPDATE employee SET rating = 'HIGH', employee_status = 'ON_LEAVE' WHERE employee_id = 'e1000001-0000-0000-0000-000000000022';
UPDATE employee SET rating = 'HIGHEST', employee_status = 'AVAILABLE' WHERE employee_id = 'e1000001-0000-0000-0000-000000000023';
UPDATE employee SET rating = 'MEDIUM', employee_status = 'BUSY' WHERE employee_id = 'e1000001-0000-0000-0000-000000000024';
UPDATE employee SET rating = 'HIGH', employee_status = 'AVAILABLE' WHERE employee_id = 'e1000001-0000-0000-0000-000000000025';
UPDATE employee SET rating = 'LOW', employee_status = 'AVAILABLE' WHERE employee_id = 'e1000001-0000-0000-0000-000000000026';
UPDATE employee SET rating = 'MEDIUM', employee_status = 'AVAILABLE' WHERE employee_id = 'e1000001-0000-0000-0000-000000000027';
UPDATE employee SET rating = 'HIGH', employee_status = 'BUSY' WHERE employee_id = 'e1000001-0000-0000-0000-000000000028';
UPDATE employee SET rating = 'HIGHEST', employee_status = 'AVAILABLE' WHERE employee_id = 'e1000001-0000-0000-0000-000000000029';
UPDATE employee SET rating = 'MEDIUM', employee_status = 'AVAILABLE' WHERE employee_id = 'e1000001-0000-0000-0000-000000000030';
UPDATE employee SET rating = 'HIGH', employee_status = 'ON_LEAVE' WHERE employee_id = 'e1000001-0000-0000-0000-000000000031';
UPDATE employee SET rating = 'LOW', employee_status = 'AVAILABLE' WHERE employee_id = 'e1000001-0000-0000-0000-000000000032';
UPDATE employee SET rating = 'MEDIUM', employee_status = 'AVAILABLE' WHERE employee_id = 'e1000001-0000-0000-0000-000000000033';
UPDATE employee SET rating = 'HIGH', employee_status = 'BUSY' WHERE employee_id = 'e1000001-0000-0000-0000-000000000034';
UPDATE employee SET rating = 'HIGHEST', employee_status = 'AVAILABLE' WHERE employee_id = 'e1000001-0000-0000-0000-000000000035';
UPDATE employee SET rating = 'LOWEST', employee_status = 'AVAILABLE' WHERE employee_id = 'e1000001-0000-0000-0000-000000000036';
UPDATE employee SET rating = 'MEDIUM', employee_status = 'AVAILABLE' WHERE employee_id = 'e1000001-0000-0000-0000-000000000037';
UPDATE employee SET rating = 'HIGH', employee_status = 'AVAILABLE' WHERE employee_id = 'e1000001-0000-0000-0000-000000000038';
UPDATE employee SET rating = 'HIGHEST', employee_status = 'BUSY' WHERE employee_id = 'e1000001-0000-0000-0000-000000000039';
UPDATE employee SET rating = 'MEDIUM', employee_status = 'AVAILABLE' WHERE employee_id = 'e1000001-0000-0000-0000-000000000040';
UPDATE employee SET rating = 'LOW', employee_status = 'AVAILABLE' WHERE employee_id = 'e1000001-0000-0000-0000-000000000041';
UPDATE employee SET rating = 'HIGH', employee_status = 'AVAILABLE' WHERE employee_id = 'e1000001-0000-0000-0000-000000000042';
UPDATE employee SET rating = 'MEDIUM', employee_status = 'ON_LEAVE' WHERE employee_id = 'e1000001-0000-0000-0000-000000000043';
UPDATE employee SET rating = 'HIGHEST', employee_status = 'AVAILABLE' WHERE employee_id = 'e1000001-0000-0000-0000-000000000044';
UPDATE employee SET rating = 'HIGH', employee_status = 'AVAILABLE' WHERE employee_id = 'e1000001-0000-0000-0000-000000000045';
UPDATE employee SET rating = 'MEDIUM', employee_status = 'BUSY' WHERE employee_id = 'e1000001-0000-0000-0000-000000000046';

-- =================================================================================
-- THÊM WORKING ZONES BỔ SUNG (Mỗi employee có nhiều khu vực làm việc)
-- =================================================================================

-- Thêm working zones đa dạng cho employees (mỗi employee làm việc ở nhiều phường)
-- Sử dụng ON CONFLICT DO NOTHING để tránh lỗi duplicate
-- Employee 1-5: Làm việc ở khu vực trung tâm HCM
INSERT INTO employee_working_zones (employee_id, ward, city) VALUES
-- Employee 1: Jane Smith - Quận 1, 3
('e1000001-0000-0000-0000-000000000001', 'Phường Bến Nghé', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000001', 'Phường Bến Thành', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000001', 'Phường Cầu Kho', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000001', 'Phường Võ Thị Sáu', 'Thành phố Hồ Chí Minh'),
-- Employee 2: Bob Wilson - Quận 7, Phú Mỹ Hưng
('e1000001-0000-0000-0000-000000000002', 'Phường Tân Phú', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000002', 'Phường Tân Phong', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000002', 'Phường Tân Quy', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000002', 'Phường Phú Mỹ', 'Thành phố Hồ Chí Minh'),
-- Employee 3: Trần Văn Long - Quận Bình Thạnh
('e1000001-0000-0000-0000-000000000003', 'Phường 1', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000003', 'Phường 2', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000003', 'Phường 3', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000003', 'Phường 25', 'Thành phố Hồ Chí Minh'),
-- Employee 4: Nguyễn Thị Mai - Quận Gò Vấp
('e1000001-0000-0000-0000-000000000004', 'Phường 1', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000004', 'Phường 3', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000004', 'Phường 4', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000004', 'Phường 5', 'Thành phố Hồ Chí Minh'),
-- Employee 5: Lê Văn Nam - Quận Tân Bình
('e1000001-0000-0000-0000-000000000005', 'Phường 1', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000005', 'Phường 2', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000005', 'Phường 4', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000005', 'Phường 15', 'Thành phố Hồ Chí Minh'),
-- Employee 6-10: Các quận ngoại thành
('e1000001-0000-0000-0000-000000000006', 'Phường Thạnh Mỹ Lợi', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000006', 'Phường An Phú', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000006', 'Phường Thảo Điền', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000007', 'Phường Bình Trưng Đông', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000007', 'Phường Bình Trưng Tây', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000007', 'Phường Cát Lái', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000008', 'Phường Hiệp Bình Chánh', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000008', 'Phường Hiệp Bình Phước', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000008', 'Phường Linh Chiểu', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000009', 'Phường Linh Đông', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000009', 'Phường Linh Tây', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000009', 'Phường Linh Trung', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000010', 'Phường Tam Bình', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000010', 'Phường Tam Phú', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000010', 'Phường Trường Thọ', 'Thành phố Hồ Chí Minh'),
-- Employee 11-16: Quận 9, Thủ Đức
('e1000001-0000-0000-0000-000000000011', 'Phường Phước Long A', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000011', 'Phường Phước Long B', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000011', 'Phường Tăng Nhơn Phú A', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000012', 'Phường Tăng Nhơn Phú B', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000012', 'Phường Long Bình', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000012', 'Phường Long Phước', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000013', 'Phường Long Thạnh Mỹ', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000013', 'Phường Long Trường', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000013', 'Phường Phú Hữu', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000014', 'Phường Phước Bình', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000014', 'Phường Trường Thạnh', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000014', 'Phường Tân Phú', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000015', 'Phường Bình Chiểu', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000015', 'Phường Bình Thọ', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000015', 'Phường Trường Thọ', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000016', 'Phường An Bình', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000016', 'Phường An Khánh', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000016', 'Phường An Lợi Đông', 'Thành phố Hồ Chí Minh')
ON CONFLICT (employee_id, ward, city) DO NOTHING;

-- Working zones cho 30 employees bổ sung
INSERT INTO employee_working_zones (employee_id, ward, city) VALUES
-- Employees 17-26: Khu vực Quận 1, 3, 5, 10
('e1000001-0000-0000-0000-000000000017', 'Phường Bến Nghé', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000017', 'Phường Bến Thành', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000018', 'Phường Cầu Kho', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000018', 'Phường Cầu Ông Lãnh', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000019', 'Phường Đa Kao', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000019', 'Phường Nguyễn Cư Trinh', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000020', 'Phường Nguyễn Thái Bình', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000020', 'Phường Phạm Ngũ Lão', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000021', 'Phường Tân Định', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000021', 'Phường Võ Thị Sáu', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000022', 'Phường 1', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000022', 'Phường 2', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000023', 'Phường 3', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000023', 'Phường 4', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000024', 'Phường 5', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000024', 'Phường 6', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000025', 'Phường 7', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000025', 'Phường 8', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000026', 'Phường 9', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000026', 'Phường 10', 'Thành phố Hồ Chí Minh'),
-- Employees 27-36: Khu vực Tân Bình, Tân Phú
('e1000001-0000-0000-0000-000000000027', 'Phường 1', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000027', 'Phường 2', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000028', 'Phường 3', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000028', 'Phường 4', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000029', 'Phường 5', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000029', 'Phường 6', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000030', 'Phường 7', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000030', 'Phường 8', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000031', 'Phường 9', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000031', 'Phường 10', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000032', 'Phường 11', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000032', 'Phường 12', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000033', 'Phường 13', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000033', 'Phường 14', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000034', 'Phường 15', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000034', 'Phường Sơn Kỳ', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000035', 'Phường Tân Quý', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000035', 'Phường Tân Sơn Nhì', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000036', 'Phường Tân Thành', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000036', 'Phường Tân Thới Hòa', 'Thành phố Hồ Chí Minh'),
-- Employees 37-46: Khu vực Bình Tân, Quận 8
('e1000001-0000-0000-0000-000000000037', 'Phường An Lạc', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000037', 'Phường An Lạc A', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000038', 'Phường Bình Hưng Hòa', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000038', 'Phường Bình Hưng Hòa A', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000039', 'Phường Bình Hưng Hòa B', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000039', 'Phường Bình Trị Đông', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000040', 'Phường Bình Trị Đông A', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000040', 'Phường Bình Trị Đông B', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000041', 'Phường Tân Tạo', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000041', 'Phường Tân Tạo A', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000042', 'Phường 1', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000042', 'Phường 2', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000043', 'Phường 3', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000043', 'Phường 4', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000044', 'Phường 5', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000044', 'Phường 6', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000045', 'Phường 7', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000045', 'Phường 14', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000046', 'Phường 15', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000046', 'Phường 16', 'Thành phố Hồ Chí Minh')
ON CONFLICT (employee_id, ward, city) DO NOTHING;

-- =================================================================================
-- THÊM EMPLOYEE UNAVAILABILITY (Lịch nghỉ, bận của nhân viên)
-- =================================================================================

-- Thêm lịch bận đa dạng cho các nhân viên
INSERT INTO employee_unavailability (unavailability_id, employee_id, start_time, end_time, reason, is_approved) VALUES
-- Employee 1: Jane Smith - Bận cá nhân
('unav0001-0000-0000-0000-000000000001', 'e1000001-0000-0000-0000-000000000001', '2025-12-15 09:00:00+07', '2025-12-15 12:00:00+07', 'Khám bệnh định kỳ', true),
('unav0001-0000-0000-0000-000000000002', 'e1000001-0000-0000-0000-000000000001', '2025-12-20 00:00:00+07', '2025-12-22 23:59:59+07', 'Nghỉ phép năm', true),

-- Employee 2: Bob Wilson - Nghỉ phép dài
('unav0001-0000-0000-0000-000000000003', 'e1000001-0000-0000-0000-000000000002', '2025-12-24 00:00:00+07', '2025-12-26 23:59:59+07', 'Nghỉ Giáng sinh', true),

-- Employee 3: Trần Văn Long - Bận đi học
('unav0001-0000-0000-0000-000000000004', 'e1000001-0000-0000-0000-000000000003', '2025-12-12 18:00:00+07', '2025-12-12 21:00:00+07', 'Đi học thêm buổi tối', true),
('unav0001-0000-0000-0000-000000000005', 'e1000001-0000-0000-0000-000000000003', '2025-12-14 18:00:00+07', '2025-12-14 21:00:00+07', 'Đi học thêm buổi tối', true),

-- Employee 4: Nguyễn Thị Mai - Bận gia đình
('unav0001-0000-0000-0000-000000000006', 'e1000001-0000-0000-0000-000000000004', '2025-12-18 08:00:00+07', '2025-12-18 12:00:00+07', 'Họp phụ huynh con', true),

-- Employee 5: Lê Văn Nam - Bận sức khỏe
('unav0001-0000-0000-0000-000000000007', 'e1000001-0000-0000-0000-000000000005', '2025-12-16 00:00:00+07', '2025-12-17 23:59:59+07', 'Nghỉ ốm', true),

-- Employee 6: ON_LEAVE - Nghỉ phép dài hạn
('unav0001-0000-0000-0000-000000000008', 'e1000001-0000-0000-0000-000000000006', '2025-12-01 00:00:00+07', '2025-12-31 23:59:59+07', 'Nghỉ phép dài hạn', true),

-- Employee 7-10: Bận rải rác
('unav0001-0000-0000-0000-000000000009', 'e1000001-0000-0000-0000-000000000007', '2025-12-13 14:00:00+07', '2025-12-13 17:00:00+07', 'Việc gia đình', true),
('unav0001-0000-0000-0000-000000000010', 'e1000001-0000-0000-0000-000000000008', '2025-12-19 09:00:00+07', '2025-12-19 11:00:00+07', 'Đi ngân hàng', true),
('unav0001-0000-0000-0000-000000000011', 'e1000001-0000-0000-0000-000000000009', '2025-12-21 00:00:00+07', '2025-12-21 23:59:59+07', 'Ngày nghỉ cá nhân', true),
('unav0001-0000-0000-0000-000000000012', 'e1000001-0000-0000-0000-000000000010', '2025-12-25 00:00:00+07', '2025-12-25 23:59:59+07', 'Nghỉ lễ Giáng sinh', true),

-- Employee 11-16: Bận công việc khác
('unav0001-0000-0000-0000-000000000013', 'e1000001-0000-0000-0000-000000000011', '2025-12-14 08:00:00+07', '2025-12-14 10:00:00+07', 'Tham gia tập huấn nội bộ', true),
('unav0001-0000-0000-0000-000000000014', 'e1000001-0000-0000-0000-000000000012', '2025-12-01 00:00:00+07', '2025-12-31 23:59:59+07', 'Nghỉ thai sản', true),
('unav0001-0000-0000-0000-000000000015', 'e1000001-0000-0000-0000-000000000013', '2025-12-17 15:00:00+07', '2025-12-17 18:00:00+07', 'Đi nha khoa', true),
('unav0001-0000-0000-0000-000000000016', 'e1000001-0000-0000-0000-000000000014', '2025-12-22 09:00:00+07', '2025-12-22 12:00:00+07', 'Việc riêng', true),
('unav0001-0000-0000-0000-000000000017', 'e1000001-0000-0000-0000-000000000015', '2025-12-23 14:00:00+07', '2025-12-23 17:00:00+07', 'Đi công chứng giấy tờ', true),
('unav0001-0000-0000-0000-000000000018', 'e1000001-0000-0000-0000-000000000016', '2025-12-27 00:00:00+07', '2025-12-28 23:59:59+07', 'Du lịch gia đình', true),

-- Unavailability cho nhân viên bổ sung (17-30)
('unav0001-0000-0000-0000-000000000019', 'e1000001-0000-0000-0000-000000000017', '2025-12-15 10:00:00+07', '2025-12-15 13:00:00+07', 'Việc cá nhân', true),
('unav0001-0000-0000-0000-000000000020', 'e1000001-0000-0000-0000-000000000018', '2025-12-16 14:00:00+07', '2025-12-16 17:00:00+07', 'Đi khám sức khỏe', true),
('unav0001-0000-0000-0000-000000000021', 'e1000001-0000-0000-0000-000000000019', '2025-12-18 00:00:00+07', '2025-12-19 23:59:59+07', 'Nghỉ phép', true),
('unav0001-0000-0000-0000-000000000022', 'e1000001-0000-0000-0000-000000000020', '2025-12-20 08:00:00+07', '2025-12-20 11:00:00+07', 'Họp gia đình', true),
('unav0001-0000-0000-0000-000000000023', 'e1000001-0000-0000-0000-000000000022', '2025-12-01 00:00:00+07', '2025-12-31 23:59:59+07', 'Nghỉ phép dài hạn', true),
('unav0001-0000-0000-0000-000000000024', 'e1000001-0000-0000-0000-000000000024', '2025-12-21 09:00:00+07', '2025-12-21 12:00:00+07', 'Đưa con đi học', true),
('unav0001-0000-0000-0000-000000000025', 'e1000001-0000-0000-0000-000000000026', '2025-12-22 15:00:00+07', '2025-12-22 18:00:00+07', 'Việc gia đình', true),
('unav0001-0000-0000-0000-000000000026', 'e1000001-0000-0000-0000-000000000028', '2025-12-23 00:00:00+07', '2025-12-24 23:59:59+07', 'Du lịch', true),
('unav0001-0000-0000-0000-000000000027', 'e1000001-0000-0000-0000-000000000030', '2025-12-25 00:00:00+07', '2025-12-26 23:59:59+07', 'Nghỉ lễ', true),
('unav0001-0000-0000-0000-000000000028', 'e1000001-0000-0000-0000-000000000031', '2025-12-01 00:00:00+07', '2025-12-31 23:59:59+07', 'Nghỉ không lương', true),
('unav0001-0000-0000-0000-000000000029', 'e1000001-0000-0000-0000-000000000034', '2025-12-27 10:00:00+07', '2025-12-27 14:00:00+07', 'Đi ngân hàng', true),
('unav0001-0000-0000-0000-000000000030', 'e1000001-0000-0000-0000-000000000039', '2025-12-28 08:00:00+07', '2025-12-28 12:00:00+07', 'Khám bệnh', true),
('unav0001-0000-0000-0000-000000000031', 'e1000001-0000-0000-0000-000000000043', '2025-12-01 00:00:00+07', '2025-12-31 23:59:59+07', 'Nghỉ phép không lương', true)
ON CONFLICT (unavailability_id) DO NOTHING;

-- =================================================================================
-- CẬP NHẬT SKILLS ĐA DẠNG CHO EMPLOYEES
-- =================================================================================

-- Cập nhật skills đa dạng cho các nhân viên
UPDATE employee SET skills = ARRAY['Dọn dẹp nhà cửa', 'Lau kính', 'Vệ sinh sofa', 'Giặt ủi'] WHERE employee_id = 'e1000001-0000-0000-0000-000000000001';
UPDATE employee SET skills = ARRAY['Giặt khô', 'Giặt ủi cao cấp', 'Chăm sóc quần áo'] WHERE employee_id = 'e1000001-0000-0000-0000-000000000002';
UPDATE employee SET skills = ARRAY['Vệ sinh tổng quát', 'Lau dọn', 'Khử khuẩn'] WHERE employee_id = 'e1000001-0000-0000-0000-000000000003';
UPDATE employee SET skills = ARRAY['Giặt ủi', 'Nấu ăn gia đình', 'Đi chợ'] WHERE employee_id = 'e1000001-0000-0000-0000-000000000004';
UPDATE employee SET skills = ARRAY['Vệ sinh máy lạnh', 'Bảo trì điện', 'Sửa chữa nhỏ'] WHERE employee_id = 'e1000001-0000-0000-0000-000000000005';
UPDATE employee SET skills = ARRAY['Dọn dẹp', 'Sắp xếp đồ đạc', 'Trang trí nhà'] WHERE employee_id = 'e1000001-0000-0000-0000-000000000006';
UPDATE employee SET skills = ARRAY['Vệ sinh sofa', 'Giặt thảm', 'Giặt rèm cửa'] WHERE employee_id = 'e1000001-0000-0000-0000-000000000007';
UPDATE employee SET skills = ARRAY['Tổng vệ sinh', 'Làm vườn', 'Cắt tỉa cây'] WHERE employee_id = 'e1000001-0000-0000-0000-000000000008';
UPDATE employee SET skills = ARRAY['Nấu ăn', 'Đi chợ hộ', 'Chuẩn bị bữa ăn'] WHERE employee_id = 'e1000001-0000-0000-0000-000000000009';
UPDATE employee SET skills = ARRAY['Vệ sinh công nghiệp', 'Làm sạch kính cao tầng', 'Vệ sinh văn phòng'] WHERE employee_id = 'e1000001-0000-0000-0000-000000000010';
UPDATE employee SET skills = ARRAY['Giặt ủi', 'Chăm sóc quần áo cao cấp', 'Là hơi'] WHERE employee_id = 'e1000001-0000-0000-0000-000000000011';
UPDATE employee SET skills = ARRAY['Tổng vệ sinh', 'Khử khuẩn', 'Diệt khuẩn chuyên sâu'] WHERE employee_id = 'e1000001-0000-0000-0000-000000000012';
UPDATE employee SET skills = ARRAY['Vệ sinh tổng hợp', 'Bảo trì nhà cửa', 'Sửa chữa cơ bản'] WHERE employee_id = 'e1000001-0000-0000-0000-000000000013';
UPDATE employee SET skills = ARRAY['Chăm sóc trẻ em', 'Nấu ăn dinh dưỡng', 'Dọn dẹp nhà cửa'] WHERE employee_id = 'e1000001-0000-0000-0000-000000000014';
UPDATE employee SET skills = ARRAY['Sửa chữa điện nước', 'Vệ sinh máy lạnh', 'Bảo trì thiết bị'] WHERE employee_id = 'e1000001-0000-0000-0000-000000000015';
UPDATE employee SET skills = ARRAY['Trang trí nhà cửa', 'Sắp xếp nội thất', 'Dọn dẹp gọn gàng'] WHERE employee_id = 'e1000001-0000-0000-0000-000000000016';

-- =================================================================================
-- THỐNG KÊ TÓM TẮT SEED DATA
-- =================================================================================
-- EMPLOYEES:
-- - Tổng: 46 employees
-- - Rating HIGHEST: 10 employees
-- - Rating HIGH: 15 employees  
-- - Rating MEDIUM: 15 employees
-- - Rating LOW: 4 employees
-- - Rating LOWEST: 2 employees
-- 
-- - Status AVAILABLE: 30 employees
-- - Status BUSY: 10 employees
-- - Status ON_LEAVE: 6 employees
--
-- WORKING ZONES:
-- - Mỗi employee có 2-4 working zones
-- - Phân bố khắp các quận HCM
--
-- UNAVAILABILITY:
-- - 31 lịch nghỉ/bận cho các employees
-- - Bao gồm: nghỉ phép, khám bệnh, việc gia đình, học tập, v.v.
-- =================================================================================
