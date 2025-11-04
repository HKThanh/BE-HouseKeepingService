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
('adrs0001-0000-0000-0000-000000000001', 'c1000001-0000-0000-0000-000000000001', '123 Lê Trọng Tấn, Phường Tây Thạnh, Thành phố Hồ Chí Minh', 'Phường Tây Thạnh', 'Thành phố Hồ Chí Minh', 10.7943, 106.6256, true),
('adrs0001-0000-0000-0000-000000000002', 'c1000001-0000-0000-0000-000000000002', '456 Lê Lợi, Phường Bến Thành, Thành phố Hồ Chí Minh', 'Phường Bến Thành', 'Thành phố Hồ Chí Minh', 10.7769, 106.7009, true),
('adrs0001-0000-0000-0000-000000000003', 'c1000001-0000-0000-0000-000000000003', '104 Lê Lợi, Phường Bến Nghé, Thành phố Hồ Chí Minh', 'Phường Bến Nghé', 'Thành phố Hồ Chí Minh', 10.8142, 106.6938, true),
-- 10 khách hàng Việt Nam với địa chỉ mặc định
('adrs0001-0000-0000-0000-000000000009', 'c1000001-0000-0000-0000-000000000004', '45 Nguyễn Huệ, Phường Bến Nghé, Thành phố Hồ Chí Minh', 'Phường Bến Nghé', 'Thành phố Hồ Chí Minh', 10.7743, 106.7043, true),
('adrs0001-0000-0000-0000-000000000010', 'c1000001-0000-0000-0000-000000000005', '128 Trần Hưng Đạo, Phường Cầu Kho, Thành phố Hồ Chí Minh', 'Phường Cầu Kho', 'Thành phố Hồ Chí Minh', 10.7657, 106.6921, true),
('adrs0001-0000-0000-0000-000000000011', 'c1000001-0000-0000-0000-000000000006', '234 Võ Văn Tần, Phường Võ Thị Sáu, Thành phố Hồ Chí Minh', 'Phường Võ Thị Sáu', 'Thành phố Hồ Chí Minh', 10.7788, 106.6897, true),
('adrs0001-0000-0000-0000-000000000012', 'c1000001-0000-0000-0000-000000000007', '567 Cách Mạng Tháng 8, Phường Phạm Ngũ Lão, Thành phố Hồ Chí Minh', 'Phường Phạm Ngũ Lão', 'Thành phố Hồ Chí Minh', 10.7843, 106.6801, true),
('adrs0001-0000-0000-0000-000000000013', 'c1000001-0000-0000-0000-000000000008', '89 Lý Thường Kiệt, Phường Nguyễn Cư Trinh, Thành phố Hồ Chí Minh', 'Phường Nguyễn Cư Trinh', 'Thành phố Hồ Chí Minh', 10.7993, 106.6554, true),
('adrs0001-0000-0000-0000-000000000014', 'c1000001-0000-0000-0000-000000000009', '321 Hoàng Văn Thụ, Phường Đakao, Thành phố Hồ Chí Minh', 'Phường Đakao', 'Thành phố Hồ Chí Minh', 10.7978, 106.6801, true),
('adrs0001-0000-0000-0000-000000000015', 'c1000001-0000-0000-0000-000000000010', '156 Xô Viết Nghệ Tĩnh, Phường 22, Thành phố Hồ Chí Minh', 'Phường 22', 'Thành phố Hồ Chí Minh', 10.8011, 106.7067, true),
('adrs0001-0000-0000-0000-000000000016', 'c1000001-0000-0000-0000-000000000011', '78 Phan Văn Trị, Phường 10, Thành phố Hồ Chí Minh', 'Phường 10', 'Thành phố Hồ Chí Minh', 10.8387, 106.6666, true),
('adrs0001-0000-0000-0000-000000000017', 'c1000001-0000-0000-0000-000000000012', '245 Quang Trung, Phường 11, Thành phố Hồ Chí Minh', 'Phường 11', 'Thành phố Hồ Chí Minh', 10.8320, 106.6543, true),
('adrs0001-0000-0000-0000-000000000018', 'c1000001-0000-0000-0000-000000000013', '432 Nguyễn Thái Sơn, Phường 4, Thành phố Hồ Chí Minh', 'Phường 4', 'Thành phố Hồ Chí Minh', 10.8258, 106.6721, true);

-- Thêm khu vực làm việc cho nhân viên
INSERT INTO employee_working_zones (employee_id, ward, city) VALUES
-- 2 nhân viên cũ
('e1000001-0000-0000-0000-000000000001', 'Phường Tây Thạnh', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000001', 'Phường 11', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000002', 'Phường Bến Nghé', 'Thành phố Hồ Chí Minh'),
-- 10 nhân viên Việt Nam - mỗi nhân viên 3 phường
-- Trần Văn Long
('e1000001-0000-0000-0000-000000000003', 'Phường Bến Nghé', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000003', 'Phường Bến Thành', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000003', 'Phường Nguyễn Thái Bình', 'Thành phố Hồ Chí Minh'),
-- Nguyễn Thị Mai
('e1000001-0000-0000-0000-000000000004', 'Phường Cầu Kho', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000004', 'Phường Phạm Ngũ Lão', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000004', 'Phường Nguyễn Cư Trinh', 'Thành phố Hồ Chí Minh'),
-- Lê Văn Nam
('e1000001-0000-0000-0000-000000000005', 'Phường Võ Thị Sáu', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000005', 'Phường Đakao', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000005', 'Phường 1', 'Thành phố Hồ Chí Minh'),
-- Phạm Văn Ơn
('e1000001-0000-0000-0000-000000000006', 'Phường 11', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000006', 'Phường 12', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000006', 'Phường 13', 'Thành phố Hồ Chí Minh'),
-- Hoàng Thị Phương
('e1000001-0000-0000-0000-000000000007', 'Phường 17', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000007', 'Phường 4', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000007', 'Phường 5', 'Thành phố Hồ Chí Minh'),
-- Võ Văn Quang
('e1000001-0000-0000-0000-000000000008', 'Phường 8', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000008', 'Phường 9', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000008', 'Phường 15', 'Thành phố Hồ Chí Minh'),
-- Đặng Thị Rượu
('e1000001-0000-0000-0000-000000000009', 'Phường 21', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000009', 'Phường 22', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000009', 'Phường 24', 'Thành phố Hồ Chí Minh'),
-- Ngô Văn Sơn
('e1000001-0000-0000-0000-000000000010', 'Phường 10', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000010', 'Phường 14', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000010', 'Phường 18', 'Thành phố Hồ Chí Minh'),
-- Bùi Thị Tâm
('e1000001-0000-0000-0000-000000000011', 'Phường 11', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000011', 'Phường 16', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000011', 'Phường 17', 'Thành phố Hồ Chí Minh'),
-- Đỗ Văn Út
('e1000001-0000-0000-0000-000000000012', 'Phường 4', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000012', 'Phường 5', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000012', 'Phường 6', 'Thành phố Hồ Chí Minh');

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


-- Khối III: Thêm dữ liệu cho Media
-- =================================================================================

-- Thêm media (ảnh Check-in & Check-out) cho assignments đã hoàn thành
INSERT INTO booking_media (media_id, assignment_id, media_url, public_id, media_type, description) VALUES
('media001-0000-0000-0000-000000000001', 'as000001-0000-0000-0000-000000000001', 'https://res.cloudinary.com/demo/image/upload/booking_images/checkin_job1.jpg', 'booking_images/checkin_job1', 'CHECK_IN_IMAGE', 'Ảnh trước khi bắt đầu công việc'),
('media002-0000-0000-0000-000000000001', 'as000001-0000-0000-0000-000000000001', 'https://res.cloudinary.com/demo/image/upload/booking_images/checkout_job1.jpg', 'booking_images/checkout_job1', 'CHECK_OUT_IMAGE', 'Ảnh sau khi hoàn thành công việc');

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

INSERT INTO pricing_rules (service_id, rule_name, condition_logic, priority, price_adjustment, staff_adjustment, duration_adjustment_hours) VALUES
(2, 'Phụ thu nhà phố lớn', 'ALL', 10, 250000, 1, 2.0),
(1, 'Giặt chăn ga', 'ALL', 5, 30000, 0, 0.5),
(1, 'Rửa chén', 'ALL', 5, 15000, 0, 0.5),
(1, 'Lau cửa kính', 'ALL', 5, 40000, 0, 1.0),
(3, 'Vệ sinh nệm', 'ALL', 5, 150000, 0, 1.0),
(3, 'Vệ sinh rèm', 'ALL', 5, 100000, 0, 1.0),
(4, 'Máy lạnh âm trần', 'ALL', 5, 50000, 0, 0.5),
(5, 'Gấp quần áo', 'ALL', 5, 10000, 0, 1.0);

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

INSERT INTO payment_methods (method_code, method_name, is_active) VALUES
('CASH', 'Thanh toán tiền mặt', TRUE),
('MOMO', 'Ví điện tử Momo', TRUE),
('VNPAY', 'Cổng thanh toán VNPAY', TRUE),
('BANK_TRANSFER', 'Chuyển khoản ngân hàng', TRUE);

-- Add corresponding payments for the bookings
INSERT INTO payments (payment_id, booking_id, amount, method_id, payment_status, transaction_code, paid_at) VALUES
('pay00001-0000-0000-0000-000000000001', 'b0000001-0000-0000-0000-000000000001', 80000.00, (SELECT method_id FROM payment_methods WHERE method_code = 'VNPAY'), 'PAID', 'VNP123456789', '2025-08-20 13:05:00+07'),
('pay00001-0000-0000-0000-000000000002', 'b0000001-0000-0000-0000-000000000002', 90000.00, (SELECT method_id FROM payment_methods WHERE method_code = 'MOMO'), 'PENDING', NULL, NULL);

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

-- =============================================
-- Sample Notifications
-- =============================================

-- Notifications for john_doe (Customer)
INSERT INTO notifications (notification_id, account_id, type, title, message, related_id, related_type, is_read, priority, action_url, created_at) VALUES
('ntf00001-0000-0000-0000-000000000001', 'a1000001-0000-0000-0000-000000000001', 'BOOKING_CREATED', 'Đặt lịch thành công', 'Booking HKS000001 của bạn đã được tạo thành công và đang chờ xác minh.', 'b0000001-0000-0000-0000-000000000001', 'BOOKING', true, 'NORMAL', '/bookings/b0000001-0000-0000-0000-000000000001', '2025-10-25 09:00:00+07'),
('ntf00002-0000-0000-0000-000000000001', 'a1000001-0000-0000-0000-000000000001', 'BOOKING_CONFIRMED', 'Booking đã được xác nhận', 'Booking HKS000001 của bạn đã được xác nhận. Nhân viên sẽ đến đúng giờ đã hẹn.', 'b0000001-0000-0000-0000-000000000001', 'BOOKING', false, 'HIGH', '/bookings/b0000001-0000-0000-0000-000000000001', '2025-10-26 10:30:00+07'),
('ntf00003-0000-0000-0000-000000000001', 'a1000001-0000-0000-0000-000000000001', 'PAYMENT_SUCCESS', 'Thanh toán thành công', 'Thanh toán của bạn đã được xử lý thành công. Số tiền: 300,000 VND', 'pay00001-0000-0000-0000-000000000001', 'PAYMENT', true, 'NORMAL', '/payments/pay00001-0000-0000-0000-000000000001', '2025-10-26 11:00:00+07');

-- Notifications for jane_smith (Employee)
INSERT INTO notifications (notification_id, account_id, type, title, message, related_id, related_type, is_read, priority, action_url, created_at) VALUES
('ntf00004-0000-0000-0000-000000000001', 'a1000001-0000-0000-0000-000000000002', 'ASSIGNMENT_CREATED', 'Bạn có công việc mới', 'Bạn đã được phân công làm việc cho booking HKS000002. Vui lòng xem chi tiết.', 'as000001-0000-0000-0000-000000000002', 'ASSIGNMENT', false, 'HIGH', '/assignments/as000001-0000-0000-0000-000000000002', '2025-10-27 08:00:00+07'),
('ntf00005-0000-0000-0000-000000000001', 'a1000001-0000-0000-0000-000000000002', 'REVIEW_RECEIVED', 'Bạn nhận được đánh giá mới', 'Bạn đã nhận được đánh giá ⭐⭐⭐⭐⭐. Cảm ơn bạn đã sử dụng dịch vụ!', 'rev00001-0000-0000-0000-000000000001', 'REVIEW', true, 'NORMAL', '/reviews/rev00001-0000-0000-0000-000000000001', '2025-10-28 14:30:00+07');

-- Notifications for mary_jones (Customer)
INSERT INTO notifications (notification_id, account_id, type, title, message, related_id, related_type, is_read, priority, action_url, created_at) VALUES
('ntf00006-0000-0000-0000-000000000001', 'a1000001-0000-0000-0000-000000000004', 'BOOKING_CREATED', 'Đặt lịch thành công', 'Booking HKS000003 của bạn đã được tạo thành công và đang chờ xác minh.', 'book0003-0000-0000-0000-000000000001', 'BOOKING', false, 'NORMAL', '/bookings/book0003-0000-0000-0000-000000000001', '2025-10-28 10:00:00+07'),
('ntf00007-0000-0000-0000-000000000001', 'a1000001-0000-0000-0000-000000000004', 'BOOKING_VERIFIED', 'Bài post được chấp nhận', 'Bài post HKS000003 của bạn đã được Admin chấp nhận. Nhân viên có thể nhận việc.', 'book0003-0000-0000-0000-000000000001', 'BOOKING', false, 'HIGH', '/bookings/book0003-0000-0000-0000-000000000001', '2025-10-28 11:00:00+07');

-- Notifications for bob_wilson (Employee)
INSERT INTO notifications (notification_id, account_id, type, title, message, related_id, related_type, is_read, priority, action_url, created_at) VALUES
('ntf00008-0000-0000-0000-000000000001', 'a1000001-0000-0000-0000-000000000005', 'ASSIGNMENT_CREATED', 'Bạn có công việc mới', 'Bạn đã được phân công làm việc cho booking HKS000001. Vui lòng xem chi tiết.', 'as000001-0000-0000-0000-000000000001', 'ASSIGNMENT', true, 'HIGH', '/assignments/as000001-0000-0000-0000-000000000001', '2025-10-26 09:00:00+07'),
('ntf00009-0000-0000-0000-000000000001', 'a1000001-0000-0000-0000-000000000005', 'SYSTEM_ANNOUNCEMENT', 'Cập nhật hệ thống', 'Hệ thống sẽ bảo trì vào 2:00 AM ngày 01/11/2025. Vui lòng hoàn thành công việc trước thời gian này.', NULL, 'SYSTEM', false, 'NORMAL', NULL, '2025-10-29 18:00:00+07');

-- Crisis notification example
INSERT INTO notifications (notification_id, account_id, type, title, message, related_id, related_type, is_read, priority, action_url, created_at) VALUES
('ntf00010-0000-0000-0000-000000000001', 'a1000001-0000-0000-0000-000000000001', 'ASSIGNMENT_CRISIS', 'KHẨN CẤP: Nhân viên hủy công việc', 'Nhân viên đã hủy công việc cho booking HKS000004. Lý do: Bị ốm đột xuất. Vui lòng liên hệ ngay để được hỗ trợ.', 'book0004-0000-0000-0000-000000000001', 'BOOKING', false, 'URGENT', '/bookings/book0004-0000-0000-0000-000000000001', '2025-10-30 08:30:00+07');

-- Promotion notification
INSERT INTO notifications (notification_id, account_id, type, title, message, related_id, related_type, is_read, priority, action_url, created_at) VALUES
('ntf00011-0000-0000-0000-000000000001', 'a1000001-0000-0000-0000-000000000006', 'PROMOTION_AVAILABLE', 'Ưu đãi đặc biệt dành cho bạn', 'Giảm giá 20% cho dịch vụ tổng vệ sinh. Áp dụng đến hết ngày 15/11/2025. Đặt lịch ngay!', 'promo001-0000-0000-0000-000000000001', 'PROMOTION', false, 'NORMAL', '/promotions', '2025-10-29 09:00:00+07');

-- =================================================================================
-- THÊM DỮ LIỆU MẪU CHO TÍNH NĂNG CHAT REAL-TIME
-- =================================================================================

-- Thêm Conversations (Cuộc trò chuyện)
-- Conversation 1: john_doe (Customer) <-> jane_smith (Employee)
INSERT INTO conversations (conversation_id, customer_id, employee_id, booking_id, last_message, last_message_time, is_active, created_at, updated_at) VALUES
('conv0001-0000-0000-0000-000000000001', 'c1000001-0000-0000-0000-000000000001', 'e1000001-0000-0000-0000-000000000001', 'b0000001-0000-0000-0000-000000000001', 'Cảm ơn bạn!', '2025-11-03 11:00:00+07', true, '2025-11-03 10:00:00+07', '2025-11-03 11:00:00+07');

-- Conversation 2: john_doe (Customer) <-> bob_wilson (Employee)
INSERT INTO conversations (conversation_id, customer_id, employee_id, booking_id, last_message, last_message_time, is_active, created_at, updated_at) VALUES
('conv0002-0000-0000-0000-000000000002', 'c1000001-0000-0000-0000-000000000001', 'e1000001-0000-0000-0000-000000000002', NULL, 'Tôi sẽ đến lúc 9h sáng', '2025-11-02 15:30:00+07', true, '2025-11-02 14:00:00+07', '2025-11-02 15:30:00+07');

-- Conversation 3: nguyenvana (Customer) <-> tranvanl (Employee)
INSERT INTO conversations (conversation_id, customer_id, employee_id, booking_id, last_message, last_message_time, is_active, created_at, updated_at) VALUES
('conv0003-0000-0000-0000-000000000003', 'c1000001-0000-0000-0000-000000000004', 'e1000001-0000-0000-0000-000000000003', 'b0000001-0000-0000-0000-000000000003', 'Tôi đã xem ảnh rồi, cảm ơn bạn!', '2025-11-01 10:30:00+07', true, '2025-11-01 09:00:00+07', '2025-11-01 10:30:00+07');

-- Conversation 4: tranthib (Customer) <-> nguyenthim (Employee)
INSERT INTO conversations (conversation_id, customer_id, employee_id, booking_id, last_message, last_message_time, is_active, created_at, updated_at) VALUES
('conv0004-0000-0000-0000-000000000004', 'c1000001-0000-0000-0000-000000000005', 'e1000001-0000-0000-0000-000000000004', NULL, 'Có thể đổi lịch được không?', '2025-11-02 16:00:00+07', true, '2025-11-02 15:00:00+07', '2025-11-02 16:00:00+07');

-- Conversation 5: levanc (Customer) <-> levann (Employee)
INSERT INTO conversations (conversation_id, customer_id, employee_id, booking_id, last_message, last_message_time, is_active, created_at, updated_at) VALUES
('conv0005-0000-0000-0000-000000000005', 'c1000001-0000-0000-0000-000000000006', 'e1000001-0000-0000-0000-000000000005', 'b0000001-0000-0000-0000-000000000005', 'Vâng, tôi sẽ mang theo máy móc chuyên dụng', '2025-11-03 14:45:00+07', true, '2025-11-03 14:00:00+07', '2025-11-03 14:45:00+07');

-- Thêm Chat Messages (Tin nhắn)
-- Messages cho Conversation 1 (john_doe <-> jane_smith)
INSERT INTO chat_messages (message_id, conversation_id, sender_id, message_type, content, image_url, is_read, created_at) VALUES
('msg00001-0000-0000-0000-000000000001', 'conv0001-0000-0000-0000-000000000001', 'a1000001-0000-0000-0000-000000000001', 'TEXT', 'Xin chào, tôi cần hỗ trợ về dịch vụ dọn dẹp', NULL, true, '2025-11-03 10:15:00+07'),
('msg00002-0000-0000-0000-000000000002', 'conv0001-0000-0000-0000-000000000001', 'a1000001-0000-0000-0000-000000000002', 'TEXT', 'Chào anh! Tôi là Jane, tôi sẽ hỗ trợ anh. Anh cần dọn dẹp khu vực nào ạ?', NULL, true, '2025-11-03 10:16:00+07'),
('msg00003-0000-0000-0000-000000000003', 'conv0001-0000-0000-0000-000000000001', 'a1000001-0000-0000-0000-000000000001', 'IMAGE', 'Đây là khu vực cần dọn dẹp', 'https://res.cloudinary.com/dhhntolb5/image/upload/v1730620800/chat_images/living_room_messy.jpg', true, '2025-11-03 10:20:00+07'),
('msg00004-0000-0000-0000-000000000004', 'conv0001-0000-0000-0000-000000000001', 'a1000001-0000-0000-0000-000000000002', 'TEXT', 'Tôi đã xem ảnh rồi. Khu vực này tôi ước tính mất khoảng 3-4 giờ để dọn dẹp hoàn toàn.', NULL, true, '2025-11-03 10:25:00+07'),
('msg00005-0000-0000-0000-000000000005', 'conv0001-0000-0000-0000-000000000001', 'a1000001-0000-0000-0000-000000000001', 'TEXT', 'Được rồi, vậy tôi đặt lịch cho chiều nay nhé', NULL, true, '2025-11-03 10:30:00+07'),
('msg00006-0000-0000-0000-000000000006', 'conv0001-0000-0000-0000-000000000001', 'a1000001-0000-0000-0000-000000000002', 'TEXT', 'Cảm ơn bạn!', NULL, true, '2025-11-03 11:00:00+07');

-- Messages cho Conversation 2 (john_doe <-> bob_wilson)
INSERT INTO chat_messages (message_id, conversation_id, sender_id, message_type, content, image_url, is_read, created_at) VALUES
('msg00007-0000-0000-0000-000000000007', 'conv0002-0000-0000-0000-000000000002', 'a1000001-0000-0000-0000-000000000001', 'TEXT', 'Chào bạn, bạn có thể đến lúc 9h sáng ngày mai được không?', NULL, true, '2025-11-02 14:30:00+07'),
('msg00008-0000-0000-0000-000000000008', 'conv0002-0000-0000-0000-000000000002', 'a1000001-0000-0000-0000-000000000005', 'TEXT', 'Chào anh! Vâng, tôi sẽ đến lúc 9h sáng', NULL, true, '2025-11-02 15:30:00+07'),
('msg00009-0000-0000-0000-000000000009', 'conv0002-0000-0000-0000-000000000002', 'a1000001-0000-0000-0000-000000000001', 'TEXT', 'Cảm ơn bạn nhiều!', NULL, false, '2025-11-02 15:32:00+07');

-- Messages cho Conversation 3 (nguyenvana <-> tranvanl)
INSERT INTO chat_messages (message_id, conversation_id, sender_id, message_type, content, image_url, is_read, created_at) VALUES
('msg00010-0000-0000-0000-000000000010', 'conv0003-0000-0000-0000-000000000003', 'a1000001-0000-0000-0000-000000000006', 'TEXT', 'Anh ơi, nhà em cần vệ sinh tổng quát, có thể gửi em hình ảnh dụng cụ được không?', NULL, true, '2025-11-01 09:15:00+07'),
('msg00011-0000-0000-0000-000000000011', 'conv0003-0000-0000-0000-000000000003', 'a1000001-0000-0000-0000-000000000016', 'TEXT', 'Chào chị, vâng ạ, anh gửi hình ngay đây', NULL, true, '2025-11-01 09:20:00+07'),
('msg00012-0000-0000-0000-000000000012', 'conv0003-0000-0000-0000-000000000003', 'a1000001-0000-0000-0000-000000000016', 'IMAGE', 'Đây là dụng cụ vệ sinh chuyên nghiệp của anh', 'https://res.cloudinary.com/dhhntolb5/image/upload/v1730620900/chat_images/cleaning_tools.jpg', true, '2025-11-01 09:22:00+07'),
('msg00013-0000-0000-0000-000000000013', 'conv0003-0000-0000-0000-000000000003', 'a1000001-0000-0000-0000-000000000006', 'TEXT', 'Tôi đã xem ảnh rồi, cảm ơn bạn!', NULL, true, '2025-11-01 10:30:00+07');

-- Messages cho Conversation 4 (tranthib <-> nguyenthim)
INSERT INTO chat_messages (message_id, conversation_id, sender_id, message_type, content, image_url, is_read, created_at) VALUES
('msg00014-0000-0000-0000-000000000014', 'conv0004-0000-0000-0000-000000000004', 'a1000001-0000-0000-0000-000000000007', 'TEXT', 'Xin chào, tôi muốn hỏi về dịch vụ giặt ủi', NULL, true, '2025-11-02 15:10:00+07'),
('msg00015-0000-0000-0000-000000000015', 'conv0004-0000-0000-0000-000000000004', 'a1000001-0000-0000-0000-000000000017', 'TEXT', 'Dạ vâng, chị cần giặt bao nhiêu kg quần áo ạ?', NULL, true, '2025-11-02 15:15:00+07'),
('msg00016-0000-0000-0000-000000000016', 'conv0004-0000-0000-0000-000000000004', 'a1000001-0000-0000-0000-000000000007', 'TEXT', 'Khoảng 10kg, nhưng tôi có việc đột xuất. Có thể đổi lịch được không?', NULL, true, '2025-11-02 16:00:00+07'),
('msg00017-0000-0000-0000-000000000017', 'conv0004-0000-0000-0000-000000000004', 'a1000001-0000-0000-0000-000000000017', 'TEXT', 'Vâng được ạ, chị muốn đổi sang ngày nào?', NULL, false, '2025-11-02 16:05:00+07');

-- Messages cho Conversation 5 (levanc <-> levann)
INSERT INTO chat_messages (message_id, conversation_id, sender_id, message_type, content, image_url, is_read, created_at) VALUES
('msg00018-0000-0000-0000-000000000018', 'conv0005-0000-0000-0000-000000000005', 'a1000001-0000-0000-0000-000000000008', 'TEXT', 'Anh ơi, nhà em có sofa da, anh có máy chuyên dụng để vệ sinh không?', NULL, true, '2025-11-03 14:10:00+07'),
('msg00019-0000-0000-0000-000000000019', 'conv0005-0000-0000-0000-000000000005', 'a1000001-0000-0000-0000-000000000018', 'TEXT', 'Vâng, tôi sẽ mang theo máy móc chuyên dụng', NULL, true, '2025-11-03 14:45:00+07'),
('msg00020-0000-0000-0000-000000000020', 'conv0005-0000-0000-0000-000000000005', 'a1000001-0000-0000-0000-000000000008', 'TEXT', 'Tuyệt vời, cảm ơn anh!', NULL, false, '2025-11-03 14:50:00+07');

-- Thêm một số tin nhắn chưa đọc để test tính năng đếm số tin nhắn chưa đọc
INSERT INTO chat_messages (message_id, conversation_id, sender_id, message_type, content, image_url, is_read, created_at) VALUES
('msg00021-0000-0000-0000-000000000021', 'conv0001-0000-0000-0000-000000000001', 'a1000001-0000-0000-0000-000000000002', 'TEXT', 'Anh nhớ chuẩn bị chỗ để đồ nhé, tôi sẽ dọn dẹp kỹ lưỡng', NULL, false, '2025-11-03 16:00:00+07'),
('msg00022-0000-0000-0000-000000000022', 'conv0001-0000-0000-0000-000000000001', 'a1000001-0000-0000-0000-000000000002', 'TEXT', 'Và nếu có vật dụng quý giá, anh nên cất đi để tránh va chạm', NULL, false, '2025-11-03 16:02:00+07');

-- Thêm conversation với booking được liên kết
INSERT INTO conversations (conversation_id, customer_id, employee_id, booking_id, last_message, last_message_time, is_active, created_at, updated_at) VALUES
('conv0006-0000-0000-0000-000000000006', 'c1000001-0000-0000-0000-000000000001', 'e1000001-0000-0000-0000-000000000001', 'b0000001-0000-0000-0000-000000000002', 'Tôi sẽ hoàn thành công việc đúng hạn', '2025-08-28 16:00:00+07', true, '2025-08-28 14:30:00+07', '2025-08-28 16:00:00+07');

-- Messages cho conversation liên kết với booking
INSERT INTO chat_messages (message_id, conversation_id, sender_id, message_type, content, image_url, is_read, created_at) VALUES
('msg00023-0000-0000-0000-000000000023', 'conv0006-0000-0000-0000-000000000006', 'a1000001-0000-0000-0000-000000000001', 'TEXT', 'Xin chào, tôi đã đặt lịch dọn dẹp cho ngày mai', NULL, true, '2025-08-28 14:35:00+07'),
('msg00024-0000-0000-0000-000000000024', 'conv0006-0000-0000-0000-000000000006', 'a1000001-0000-0000-0000-000000000002', 'TEXT', 'Vâng, tôi đã nhận được booking BK000002. Tôi sẽ hoàn thành công việc đúng hạn', NULL, true, '2025-08-28 16:00:00+07');
