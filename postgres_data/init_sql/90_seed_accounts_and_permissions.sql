-- Seed data: 90 Seed Accounts And Permissions

-- Thêm các vai trò mặc định
INSERT INTO roles (role_id, role_name) VALUES (1, 'CUSTOMER'), (2, 'EMPLOYEE'), (3, 'ADMIN');

INSERT INTO account (account_id, username, password, phone_number, status, is_phone_verified) VALUES
('a1000001-0000-0000-0000-000000000001', 'john_doe', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0901234567', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000002', 'jane_smith', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0912345678', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000003', 'admin_1', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0900000001', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000004', 'mary_jones', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0909876543', 'INACTIVE', false),
('a1000001-0000-0000-0000-000000000005', 'bob_wilson', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0923456789', 'ACTIVE', true),
-- 10 tài khoản khách hàng mới
('a1000001-0000-0000-0000-000000000006', 'nguyenvana', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0987654321', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000007', 'tranthib', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0976543210', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000008', 'levanc', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0965432109', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000009', 'phamthid', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0954321098', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000010', 'hoangvane', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0943210987', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000011', 'vothif', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0932109876', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000012', 'dangvang', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0921098765', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000013', 'ngothih', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0910987654', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000014', 'buivani', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0919876543', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000015', 'dothik', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0898765432', 'ACTIVE', true),
-- 10 tài khoản nhân viên mới
('a1000001-0000-0000-0000-000000000016', 'tranvanl', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0887224321', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000017', 'nguyenthim', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0876223210', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000018', 'levann', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0865222109', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000019', 'phamvano', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0854221098', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000020', 'hoangthip', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0843220987', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000021', 'vovanq', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0832229876', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000022', 'dangthir', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0821228765', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000023', 'ngovans', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0810227654', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000024', 'buithit', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0809226543', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000025', 'dovanu', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0798225432', 'ACTIVE', true),
-- 4 tài khoản Việt Nam có 2 vai trò (EMPLOYEE và CUSTOMER)
('a1000001-0000-0000-0000-000000000026', 'nguyenthanhviet', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0988777666', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000027', 'lethihuong', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0977888999', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000028', 'phamvantuan', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0966555444', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000029', 'tranthilan', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0955222333', 'ACTIVE', true);

-- Gán vai trò cho các tài khoản
INSERT INTO account_roles (account_id, role_id) VALUES
('a1000001-0000-0000-0000-000000000001', 1), -- john_doe là CUSTOMER
('a1000001-0000-0000-0000-000000000002', 2), -- jane_smith là EMPLOYEE
('a1000001-0000-0000-0000-000000000002', 1), -- jane_smith cũng là CUSTOMER
('a1000001-0000-0000-0000-000000000003', 3), -- admin_1 là ADMIN
('a1000001-0000-0000-0000-000000000004', 1), -- mary_jones là CUSTOMER
('a1000001-0000-0000-0000-000000000005', 2), -- bob_wilson là EMPLOYEE
-- 10 khách hàng mới
('a1000001-0000-0000-0000-000000000006', 1),
('a1000001-0000-0000-0000-000000000007', 1),
('a1000001-0000-0000-0000-000000000008', 1),
('a1000001-0000-0000-0000-000000000009', 1),
('a1000001-0000-0000-0000-000000000010', 1),
('a1000001-0000-0000-0000-000000000011', 1),
('a1000001-0000-0000-0000-000000000012', 1),
('a1000001-0000-0000-0000-000000000013', 1),
('a1000001-0000-0000-0000-000000000014', 1),
('a1000001-0000-0000-0000-000000000015', 1),
-- 10 nhân viên mới
('a1000001-0000-0000-0000-000000000016', 2),
('a1000001-0000-0000-0000-000000000017', 2),
('a1000001-0000-0000-0000-000000000018', 2),
('a1000001-0000-0000-0000-000000000019', 2),
('a1000001-0000-0000-0000-000000000020', 2),
('a1000001-0000-0000-0000-000000000021', 2),
('a1000001-0000-0000-0000-000000000022', 2),
('a1000001-0000-0000-0000-000000000023', 2),
('a1000001-0000-0000-0000-000000000024', 2),
('a1000001-0000-0000-0000-000000000025', 2),
-- 4 tài khoản có 2 vai trò (CUSTOMER và EMPLOYEE)
('a1000001-0000-0000-0000-000000000026', 1), -- nguyenthanhviet CUSTOMER
('a1000001-0000-0000-0000-000000000026', 2), -- nguyenthanhviet EMPLOYEE
('a1000001-0000-0000-0000-000000000027', 1), -- lethihuong CUSTOMER
('a1000001-0000-0000-0000-000000000027', 2), -- lethihuong EMPLOYEE
('a1000001-0000-0000-0000-000000000028', 1), -- phamvantuan CUSTOMER
('a1000001-0000-0000-0000-000000000028', 2), -- phamvantuan EMPLOYEE
('a1000001-0000-0000-0000-000000000029', 1), -- tranthilan CUSTOMER
('a1000001-0000-0000-0000-000000000029', 2); -- tranthilan EMPLOYEE

-- Thêm hồ sơ khách hàng (customer)
INSERT INTO customer (customer_id, account_id, avatar, full_name, is_male, email, birthdate) VALUES
('c1000001-0000-0000-0000-000000000001', 'a1000001-0000-0000-0000-000000000001', 'https://picsum.photos/200', 'John Doe', TRUE, 'john.doe@example.com', '2003-09-10'),
('c1000001-0000-0000-0000-000000000002', 'a1000001-0000-0000-0000-000000000004', 'https://picsum.photos/200', 'Mary Jones', FALSE, 'mary.jones@example.com', '2003-01-19'),
('c1000001-0000-0000-0000-000000000003', 'a1000001-0000-0000-0000-000000000002', 'https://picsum.photos/200', 'Jane Smith Customer', FALSE, 'jane.smith.customer@example.com', '2003-04-14'),
-- 10 khách hàng Việt Nam
('c1000001-0000-0000-0000-000000000004', 'a1000001-0000-0000-0000-000000000006', 'https://i.pravatar.cc/150?img=11', 'Nguyễn Văn An', TRUE, 'nguyenvanan@gmail.com', '1995-03-15'),
('c1000001-0000-0000-0000-000000000005', 'a1000001-0000-0000-0000-000000000007', 'https://i.pravatar.cc/150?img=5', 'Trần Thị Bích', FALSE, 'tranthibich@gmail.com', '1998-07-22'),
('c1000001-0000-0000-0000-000000000006', 'a1000001-0000-0000-0000-000000000008', 'https://i.pravatar.cc/150?img=12', 'Lê Văn Cường', TRUE, 'levancuong@gmail.com', '1992-11-08'),
('c1000001-0000-0000-0000-000000000007', 'a1000001-0000-0000-0000-000000000009', 'https://i.pravatar.cc/150?img=9', 'Phạm Thị Dung', FALSE, 'phamthidung@gmail.com', '1996-05-30'),
('c1000001-0000-0000-0000-000000000008', 'a1000001-0000-0000-0000-000000000010', 'https://i.pravatar.cc/150?img=13', 'Hoàng Văn Em', TRUE, 'hoangvanem@gmail.com', '1994-09-12'),
('c1000001-0000-0000-0000-000000000009', 'a1000001-0000-0000-0000-000000000011', 'https://i.pravatar.cc/150?img=20', 'Võ Thị Phương', FALSE, 'vothiphuong@gmail.com', '1997-02-18'),
('c1000001-0000-0000-0000-000000000010', 'a1000001-0000-0000-0000-000000000012', 'https://i.pravatar.cc/150?img=14', 'Đặng Văn Giang', TRUE, 'dangvangiang@gmail.com', '1993-06-25'),
('c1000001-0000-0000-0000-000000000011', 'a1000001-0000-0000-0000-000000000013', 'https://i.pravatar.cc/150?img=23', 'Ngô Thị Hà', FALSE, 'ngothiha@gmail.com', '1999-10-14'),
('c1000001-0000-0000-0000-000000000012', 'a1000001-0000-0000-0000-000000000014', 'https://i.pravatar.cc/150?img=15', 'Bùi Văn Ích', TRUE, 'buivanich@gmail.com', '1991-04-07'),
('c1000001-0000-0000-0000-000000000013', 'a1000001-0000-0000-0000-000000000015', 'https://i.pravatar.cc/150?img=24', 'Đỗ Thị Kim', FALSE, 'dothikim@gmail.com', '2000-12-20'),
-- Customer profile cho 4 tài khoản có 2 vai trò
('c1000001-0000-0000-0000-000000000014', 'a1000001-0000-0000-0000-000000000026', 'https://i.pravatar.cc/150?img=16', 'Nguyễn Thành Việt', TRUE, 'nguyenthanhviet@gmail.com', '1995-08-20'),
('c1000001-0000-0000-0000-000000000015', 'a1000001-0000-0000-0000-000000000027', 'https://i.pravatar.cc/150?img=47', 'Lê Thị Hương', FALSE, 'lethihuong@gmail.com', '1996-12-05'),
('c1000001-0000-0000-0000-000000000016', 'a1000001-0000-0000-0000-000000000028', 'https://i.pravatar.cc/150?img=17', 'Phạm Văn Tuấn', TRUE, 'phamvantuan@gmail.com', '1994-06-18'),
('c1000001-0000-0000-0000-000000000017', 'a1000001-0000-0000-0000-000000000029', 'https://i.pravatar.cc/150?img=48', 'Trần Thị Lan', FALSE, 'tranthilan@gmail.com', '1997-09-25');

-- Thêm hồ sơ nhân viên (employee)
INSERT INTO employee (employee_id, account_id, avatar, full_name, is_male, email, birthdate, hired_date, skills, bio) VALUES
('e1000001-0000-0000-0000-000000000001', 'a1000001-0000-0000-0000-000000000002', 'https://picsum.photos/200', 'Jane Smith', FALSE, 'jane.smith@example.com', '2003-04-14', '2024-01-15', ARRAY['Cleaning', 'Organizing'], 'Có kinh nghiệm dọn dẹp nhà cửa và sắp xếp đồ đạc.'),
('e1000001-0000-0000-0000-000000000002', 'a1000001-0000-0000-0000-000000000005', 'https://picsum.photos/200', 'Bob Wilson', TRUE, 'bob.wilson@examplefieldset.com', '2003-08-10', '2023-06-20', ARRAY['Deep Cleaning', 'Laundry'], 'Chuyên gia giặt ủi và làm sạch sâu.'),
-- 10 nhân viên Việt Nam
('e1000001-0000-0000-0000-000000000003', 'a1000001-0000-0000-0000-000000000016', 'https://i.pravatar.cc/150?img=33', 'Trần Văn Long', TRUE, 'tranvanlong@gmail.com', '1994-08-12', '2023-03-10', ARRAY['Vệ sinh tổng quát', 'Lau dọn'], 'Nhiều năm kinh nghiệm vệ sinh nhà cửa, tỉ mỉ và cẩn thận.'),
('e1000001-0000-0000-0000-000000000004', 'a1000001-0000-0000-0000-000000000017', 'https://i.pravatar.cc/150?img=27', 'Nguyễn Thị Mai', FALSE, 'nguyenthimai@gmail.com', '1996-04-25', '2023-05-15', ARRAY['Giặt ủi', 'Nấu ăn'], 'Chuyên về công việc gia đình, giặt ủi và nấu ăn ngon.'),
('e1000001-0000-0000-0000-000000000005', 'a1000001-0000-0000-0000-000000000018', 'https://i.pravatar.cc/150?img=34', 'Lê Văn Nam', TRUE, 'levannam@gmail.com', '1992-11-18', '2022-08-20', ARRAY['Vệ sinh máy lạnh', 'Sửa chữa nhỏ'], 'Có kỹ năng kỹ thuật, chuyên vệ sinh và bảo trì máy lạnh.'),
('e1000001-0000-0000-0000-000000000006', 'a1000001-0000-0000-0000-000000000019', 'https://i.pravatar.cc/150?img=35', 'Phạm Văn Ơn', TRUE, 'phamvanon@gmail.com', '1995-06-30', '2023-07-12', ARRAY['Dọn dẹp', 'Sắp xếp'], 'Nhiệt tình, trách nhiệm, làm việc nhanh nhẹn.'),
('e1000001-0000-0000-0000-000000000007', 'a1000001-0000-0000-0000-000000000020', 'https://i.pravatar.cc/150?img=28', 'Hoàng Thị Phương', FALSE, 'hoangthiphuong@gmail.com', '1997-02-14', '2023-09-08', ARRAY['Vệ sinh sofa', 'Giặt thảm'], 'Chuyên vệ sinh sofa, thảm, rèm cửa bằng máy móc chuyên dụng.'),
('e1000001-0000-0000-0000-000000000008', 'a1000001-0000-0000-0000-000000000021', 'https://i.pravatar.cc/150?img=36', 'Võ Văn Quang', TRUE, 'vovanquang@gmail.com', '1993-09-22', '2022-11-25', ARRAY['Tổng vệ sinh', 'Làm vườn'], 'Kinh nghiệm dọn dẹp nhà phố, biệt thự và chăm sóc vườn.'),
('e1000001-0000-0000-0000-000000000009', 'a1000001-0000-0000-0000-000000000022', 'https://i.pravatar.cc/150?img=29', 'Đặng Thị Rượu', FALSE, 'dangthiruou@gmail.com', '1998-05-16', '2024-01-05', ARRAY['Nấu ăn', 'Đi chợ'], 'Nấu ăn ngon, đa dạng món Việt và món Á, mua sắm khéo léo.'),
('e1000001-0000-0000-0000-000000000010', 'a1000001-0000-0000-0000-000000000023', 'https://i.pravatar.cc/150?img=37', 'Ngô Văn Sơn', TRUE, 'ngovanson@gmail.com', '1991-12-03', '2022-04-18', ARRAY['Vệ sinh công nghiệp', 'Làm sạch kính'], 'Chuyên vệ sinh các tòa nhà cao tầng, lau kính chuyên nghiệp.'),
('e1000001-0000-0000-0000-000000000011', 'a1000001-0000-0000-0000-000000000024', 'https://i.pravatar.cc/150?img=30', 'Bùi Thị Tâm', FALSE, 'buithitam@gmail.com', '1999-07-28', '2024-02-20', ARRAY['Giặt ủi', 'Chăm sóc quần áo'], 'Giặt ủi cẩn thận, chuyên chăm sóc quần áo cao cấp.'),
('e1000001-0000-0000-0000-000000000012', 'a1000001-0000-0000-0000-000000000025', 'https://i.pravatar.cc/150?img=38', 'Đỗ Văn Út', TRUE, 'dovanut@gmail.com', '1990-10-10', '2021-12-15', ARRAY['Tổng vệ sinh', 'Khử khuẩn'], 'Lâu năm trong nghề, chuyên khử khuẩn và vệ sinh sâu.'),
-- Employee profile cho 4 tài khoản có 2 vai trò
('e1000001-0000-0000-0000-000000000013', 'a1000001-0000-0000-0000-000000000026', 'https://i.pravatar.cc/150?img=16', 'Nguyễn Thành Việt', TRUE, 'nguyenthanhviet.employee@gmail.com', '1995-08-20', '2023-10-15', ARRAY['Vệ sinh tổng hợp', 'Bảo trì nhà cửa'], 'Vừa là khách hàng vừa là nhân viên, có kinh nghiệm đa dạng trong lĩnh vực dịch vụ nhà.'),
('e1000001-0000-0000-0000-000000000014', 'a1000001-0000-0000-0000-000000000027', 'https://i.pravatar.cc/150?img=47', 'Lê Thị Hương', FALSE, 'lethihuong.employee@gmail.com', '1996-12-05', '2023-08-20', ARRAY['Chăm sóc trẻ em', 'Nấu ăn gia đình'], 'Kinh nghiệm chăm sóc trẻ nhỏ và nấu ăn dinh dưỡng cho gia đình.'),
('e1000001-0000-0000-0000-000000000015', 'a1000001-0000-0000-0000-000000000028', 'https://i.pravatar.cc/150?img=17', 'Phạm Văn Tuấn', TRUE, 'phamvantuan.employee@gmail.com', '1994-06-18', '2023-05-10', ARRAY['Sửa chữa điện nước', 'Vệ sinh máy lạnh'], 'Thợ điện nước lành nghề, chuyên sửa chữa và bảo trì thiết bị gia đình.'),
('e1000001-0000-0000-0000-000000000016', 'a1000001-0000-0000-0000-000000000029', 'https://i.pravatar.cc/150?img=48', 'Trần Thị Lan', FALSE, 'tranthilan.employee@gmail.com', '1997-09-25', '2024-03-01', ARRAY['Trang trí nhà cửa', 'Sắp xếp đồ đạc'], 'Có khiếu thẩm mỹ, chuyên tư vấn trang trí và sắp xếp nội thất.');

-- Thêm hồ sơ quản trị viên (admin_profile)
INSERT INTO admin_profile (admin_profile_id, account_id, full_name, is_male, department, contact_info, birthdate, hire_date) VALUES
('ad100001-0000-0000-0000-000000000001', 'a1000001-0000-0000-0000-000000000003', 'Admin One', TRUE, 'Management', 'admin1@example.com', '1988-09-10', '2023-03-01');

-- Thêm địa chỉ cho khách hàng (default = true)
INSERT INTO address (address_id, customer_id, full_address, ward, city, latitude, longitude, is_default) VALUES
('adrs0001-0000-0000-0000-000000000001', 'c1000001-0000-0000-0000-000000000001', '123 Lê Trọng Tấn, Phường Thủ Dầu Một, Thành phố Hồ Chí Minh', 'Phường Thủ Dầu Một', 'Thành phố Hồ Chí Minh', 10.7943, 106.6256, true),
('adrs0001-0000-0000-0000-000000000002', 'c1000001-0000-0000-0000-000000000002', '456 Lê Lợi, Phường Phú Lợi, Thành phố Hồ Chí Minh', 'Phường Phú Lợi', 'Thành phố Hồ Chí Minh', 10.7769, 106.7009, true),
('adrs0001-0000-0000-0000-000000000003', 'c1000001-0000-0000-0000-000000000003', '104 Lê Lợi, Phường Bình Dương, Thành phố Hồ Chí Minh', 'Phường Bình Dương', 'Thành phố Hồ Chí Minh', 10.8142, 106.6938, true),
-- 10 khách hàng Việt Nam với địa chỉ mặc định
('adrs0001-0000-0000-0000-000000000009', 'c1000001-0000-0000-0000-000000000004', '45 Nguyễn Huệ, Phường Phú An, Thành phố Hồ Chí Minh', 'Phường Phú An', 'Thành phố Hồ Chí Minh', 10.7743, 106.7043, true),
('adrs0001-0000-0000-0000-000000000010', 'c1000001-0000-0000-0000-000000000005', '128 Trần Hưng Đạo, Phường Chánh Hiệp, Thành phố Hồ Chí Minh', 'Phường Chánh Hiệp', 'Thành phố Hồ Chí Minh', 10.7657, 106.6921, true),
('adrs0001-0000-0000-0000-000000000011', 'c1000001-0000-0000-0000-000000000006', '234 Võ Văn Tần, Phường Bến Cát, Thành phố Hồ Chí Minh', 'Phường Bến Cát', 'Thành phố Hồ Chí Minh', 10.7788, 106.6897, true),
('adrs0001-0000-0000-0000-000000000012', 'c1000001-0000-0000-0000-000000000007', '567 Cách Mạng Tháng 8, Phường Chánh Phú Hòa, Thành phố Hồ Chí Minh', 'Phường Chánh Phú Hòa', 'Thành phố Hồ Chí Minh', 10.7843, 106.6801, true),
('adrs0001-0000-0000-0000-000000000013', 'c1000001-0000-0000-0000-000000000008', '89 Lý Thường Kiệt, Phường Long Nguyên, Thành phố Hồ Chí Minh', 'Phường Long Nguyên', 'Thành phố Hồ Chí Minh', 10.7993, 106.6554, true),
('adrs0001-0000-0000-0000-000000000014', 'c1000001-0000-0000-0000-000000000009', '321 Hoàng Văn Thụ, Phường Tây Nam, Thành phố Hồ Chí Minh', 'Phường Tây Nam', 'Thành phố Hồ Chí Minh', 10.7978, 106.6801, true),
('adrs0001-0000-0000-0000-000000000015', 'c1000001-0000-0000-0000-000000000010', '156 Xô Viết Nghệ Tĩnh, Phường Thới Hòa, Thành phố Hồ Chí Minh', 'Phường Thới Hòa', 'Thành phố Hồ Chí Minh', 10.8011, 106.7067, true),
('adrs0001-0000-0000-0000-000000000016', 'c1000001-0000-0000-0000-000000000011', '78 Phan Văn Trị, Phường Hòa Lợi, Thành phố Hồ Chí Minh', 'Phường Hòa Lợi', 'Thành phố Hồ Chí Minh', 10.8387, 106.6666, true),
('adrs0001-0000-0000-0000-000000000017', 'c1000001-0000-0000-0000-000000000012', '245 Quang Trung, Phường Tân Uyên, Thành phố Hồ Chí Minh', 'Phường Tân Uyên', 'Thành phố Hồ Chí Minh', 10.8320, 106.6543, true),
('adrs0001-0000-0000-0000-000000000018', 'c1000001-0000-0000-0000-000000000013', '432 Nguyễn Thái Sơn, Phường Tân Khánh, Thành phố Hồ Chí Minh', 'Phường Tân Khánh', 'Thành phố Hồ Chí Minh', 10.8258, 106.6721, true);

INSERT INTO employee_working_zones (employee_id, ward, city) VALUES
('e1000001-0000-0000-0000-000000000001', 'Phường Thủ Dầu Một', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000002', 'Phường Phú Lợi', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000003', 'Phường Bình Dương', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000004', 'Phường Phú An', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000005', 'Phường Chánh Hiệp', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000006', 'Phường Bến Cát', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000007', 'Phường Chánh Phú Hòa', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000008', 'Phường Long Nguyên', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000009', 'Phường Tây Nam', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000010', 'Phường Thới Hòa', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000011', 'Phường Hòa Lợi', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000012', 'Phường Tân Uyên', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000013', 'Phường Tân Khánh', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000014', 'Phường Vĩnh Tân', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000015', 'Phường Bình Cơ', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000016', 'Phường Tân Hiệp', 'Thành phố Hồ Chí Minh');

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
