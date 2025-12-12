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
('a1000001-0000-0000-0000-000000000029', 'tranthilan', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0955222333', 'ACTIVE', true),
-- 30 tài khoản nhân viên bổ sung để test findSuitableEmployees
('a1000001-0000-0000-0000-000000000030', 'nguyenvana1', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0912000001', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000031', 'tranthib1', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0912000002', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000032', 'levanc1', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0912000003', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000033', 'phamthid1', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0912000004', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000034', 'hoangvane1', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0912000005', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000035', 'vothif1', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0912000006', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000036', 'dangvang1', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0912000007', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000037', 'ngothih1', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0912000008', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000038', 'buivani1', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0912000009', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000039', 'dothik1', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0912000010', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000040', 'tranvanl1', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0912000011', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000041', 'nguyenthim1', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0912000012', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000042', 'levann1', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0912000013', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000043', 'phamvano1', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0912000014', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000044', 'hoangthip1', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0912000015', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000045', 'vovanq1', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0912000016', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000046', 'dangthir1', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0912000017', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000047', 'ngovans1', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0912000018', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000048', 'buithit1', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0912000019', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000049', 'dovanu1', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0912000020', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000050', 'nguyenvanb2', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0912000021', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000051', 'tranthic2', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0912000022', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000052', 'levand2', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0912000023', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000053', 'phamthie2', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0912000024', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000054', 'hoangvanf2', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0912000025', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000055', 'vothig2', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0912000026', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000056', 'dangvanh2', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0912000027', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000057', 'ngothii2', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0912000028', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000058', 'buivank2', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0912000029', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000059', 'dothil2', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0912000030', 'ACTIVE', true);

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
('a1000001-0000-0000-0000-000000000029', 2), -- tranthilan EMPLOYEE
-- 30 nhân viên bổ sung
('a1000001-0000-0000-0000-000000000030', 2),
('a1000001-0000-0000-0000-000000000031', 2),
('a1000001-0000-0000-0000-000000000032', 2),
('a1000001-0000-0000-0000-000000000033', 2),
('a1000001-0000-0000-0000-000000000034', 2),
('a1000001-0000-0000-0000-000000000035', 2),
('a1000001-0000-0000-0000-000000000036', 2),
('a1000001-0000-0000-0000-000000000037', 2),
('a1000001-0000-0000-0000-000000000038', 2),
('a1000001-0000-0000-0000-000000000039', 2),
('a1000001-0000-0000-0000-000000000040', 2),
('a1000001-0000-0000-0000-000000000041', 2),
('a1000001-0000-0000-0000-000000000042', 2),
('a1000001-0000-0000-0000-000000000043', 2),
('a1000001-0000-0000-0000-000000000044', 2),
('a1000001-0000-0000-0000-000000000045', 2),
('a1000001-0000-0000-0000-000000000046', 2),
('a1000001-0000-0000-0000-000000000047', 2),
('a1000001-0000-0000-0000-000000000048', 2),
('a1000001-0000-0000-0000-000000000049', 2),
('a1000001-0000-0000-0000-000000000050', 2),
('a1000001-0000-0000-0000-000000000051', 2),
('a1000001-0000-0000-0000-000000000052', 2),
('a1000001-0000-0000-0000-000000000053', 2),
('a1000001-0000-0000-0000-000000000054', 2),
('a1000001-0000-0000-0000-000000000055', 2),
('a1000001-0000-0000-0000-000000000056', 2),
('a1000001-0000-0000-0000-000000000057', 2),
('a1000001-0000-0000-0000-000000000058', 2),
('a1000001-0000-0000-0000-000000000059', 2);

-- Thêm hồ sơ khách hàng (customer)
INSERT INTO customer (customer_id, account_id, avatar, full_name, is_male, email, birthdate, is_email_verified) VALUES
('c1000001-0000-0000-0000-000000000001', 'a1000001-0000-0000-0000-000000000001', 'https://picsum.photos/200', 'John Doe', TRUE, 'john.doe@example.com', '2003-09-10', true),
('c1000001-0000-0000-0000-000000000002', 'a1000001-0000-0000-0000-000000000004', 'https://picsum.photos/200', 'Mary Jones', FALSE, 'mary.jones@example.com', '2003-01-19', true),
('c1000001-0000-0000-0000-000000000003', 'a1000001-0000-0000-0000-000000000002', 'https://picsum.photos/200', 'Jane Smith Customer', FALSE, 'jane.smith.customer@example.com', '2003-04-14', true),
-- 10 khách hàng Việt Nam
('c1000001-0000-0000-0000-000000000004', 'a1000001-0000-0000-0000-000000000006', 'https://i.pravatar.cc/150?img=11', 'Nguyễn Văn An', TRUE, 'nguyenvanan@gmail.com', '1995-03-15', true),
('c1000001-0000-0000-0000-000000000005', 'a1000001-0000-0000-0000-000000000007', 'https://i.pravatar.cc/150?img=5', 'Trần Thị Bích', FALSE, 'tranthibich@gmail.com', '1998-07-22', true),
('c1000001-0000-0000-0000-000000000006', 'a1000001-0000-0000-0000-000000000008', 'https://i.pravatar.cc/150?img=12', 'Lê Văn Cường', TRUE, 'levancuong@gmail.com', '1992-11-08', true),
('c1000001-0000-0000-0000-000000000007', 'a1000001-0000-0000-0000-000000000009', 'https://i.pravatar.cc/150?img=9', 'Phạm Thị Dung', FALSE, 'phamthidung@gmail.com', '1996-05-30', true),
('c1000001-0000-0000-0000-000000000008', 'a1000001-0000-0000-0000-000000000010', 'https://i.pravatar.cc/150?img=13', 'Hoàng Văn Em', TRUE, 'hoangvanem@gmail.com', '1994-09-12', true),
('c1000001-0000-0000-0000-000000000009', 'a1000001-0000-0000-0000-000000000011', 'https://i.pravatar.cc/150?img=20', 'Võ Thị Phương', FALSE, 'vothiphuong@gmail.com', '1997-02-18', true),
('c1000001-0000-0000-0000-000000000010', 'a1000001-0000-0000-0000-000000000012', 'https://i.pravatar.cc/150?img=14', 'Đặng Văn Giang', TRUE, 'dangvangiang@gmail.com', '1993-06-25', true),
('c1000001-0000-0000-0000-000000000011', 'a1000001-0000-0000-0000-000000000013', 'https://i.pravatar.cc/150?img=23', 'Ngô Thị Hà', FALSE, 'ngothiha@gmail.com', '1999-10-14', true),
('c1000001-0000-0000-0000-000000000012', 'a1000001-0000-0000-0000-000000000014', 'https://i.pravatar.cc/150?img=15', 'Bùi Văn Ích', TRUE, 'buivanich@gmail.com', '1991-04-07', true),
('c1000001-0000-0000-0000-000000000013', 'a1000001-0000-0000-0000-000000000015', 'https://i.pravatar.cc/150?img=24', 'Đỗ Thị Kim', FALSE, 'dothikim@gmail.com', '2000-12-20', true),
-- Customer profile cho 4 tài khoản có 2 vai trò
('c1000001-0000-0000-0000-000000000014', 'a1000001-0000-0000-0000-000000000026', 'https://i.pravatar.cc/150?img=16', 'Nguyễn Thành Việt', TRUE, 'nguyenthanhviet@gmail.com', '1995-08-20', true),
('c1000001-0000-0000-0000-000000000015', 'a1000001-0000-0000-0000-000000000027', 'https://i.pravatar.cc/150?img=47', 'Lê Thị Hương', FALSE, 'lethihuong@gmail.com', '1996-12-05', true),
('c1000001-0000-0000-0000-000000000016', 'a1000001-0000-0000-0000-000000000028', 'https://i.pravatar.cc/150?img=17', 'Phạm Văn Tuấn', TRUE, 'phamvantuan@gmail.com', '1994-06-18', true),
('c1000001-0000-0000-0000-000000000017', 'a1000001-0000-0000-0000-000000000029', 'https://i.pravatar.cc/150?img=48', 'Trần Thị Lan', FALSE, 'tranthilan@gmail.com', '1997-09-25', true);

-- Thiết lập rating/VIP mẫu cho khách hàng để dễ dàng kiểm thử
UPDATE customer
SET rating = 'HIGH', vip_level = 4
WHERE customer_id = 'c1000001-0000-0000-0000-000000000001';

UPDATE customer
SET rating = 'MEDIUM', vip_level = 3
WHERE customer_id = 'c1000001-0000-0000-0000-000000000003';

UPDATE customer
SET rating = 'LOW', vip_level = 2
WHERE customer_id = 'c1000001-0000-0000-0000-000000000005';

-- Thêm hồ sơ nhân viên (employee)
INSERT INTO employee (employee_id, account_id, avatar, full_name, is_male, email, birthdate, hired_date, skills, bio, is_email_verified) VALUES
('e1000001-0000-0000-0000-000000000001', 'a1000001-0000-0000-0000-000000000002', 'https://picsum.photos/200', 'Jane Smith', FALSE, 'jane.smith@example.com', '2003-04-14', '2024-01-15', ARRAY['Cleaning', 'Organizing'], 'Có kinh nghiệm dọn dẹp nhà cửa và sắp xếp đồ đạc.', true),
('e1000001-0000-0000-0000-000000000002', 'a1000001-0000-0000-0000-000000000005', 'https://picsum.photos/200', 'Bob Wilson', TRUE, 'bob.wilson@examplefieldset.com', '2003-08-10', '2023-06-20', ARRAY['Deep Cleaning', 'Laundry'], 'Chuyên gia giặt ủi và làm sạch sâu.', true),
-- 10 nhân viên Việt Nam
('e1000001-0000-0000-0000-000000000003', 'a1000001-0000-0000-0000-000000000016', 'https://i.pravatar.cc/150?img=33', 'Trần Văn Long', TRUE, 'tranvanlong@gmail.com', '1994-08-12', '2023-03-10', ARRAY['Vệ sinh tổng quát', 'Lau dọn'], 'Nhiều năm kinh nghiệm vệ sinh nhà cửa, tỉ mỉ và cẩn thận.', true),
('e1000001-0000-0000-0000-000000000004', 'a1000001-0000-0000-0000-000000000017', 'https://i.pravatar.cc/150?img=27', 'Nguyễn Thị Mai', FALSE, 'nguyenthimai@gmail.com', '1996-04-25', '2023-05-15', ARRAY['Giặt ủi', 'Nấu ăn'], 'Chuyên về công việc gia đình, giặt ủi và nấu ăn ngon.', true),
('e1000001-0000-0000-0000-000000000005', 'a1000001-0000-0000-0000-000000000018', 'https://i.pravatar.cc/150?img=34', 'Lê Văn Nam', TRUE, 'levannam@gmail.com', '1992-11-18', '2022-08-20', ARRAY['Vệ sinh máy lạnh', 'Sửa chữa nhỏ'], 'Có kỹ năng kỹ thuật, chuyên vệ sinh và bảo trì máy lạnh.', true),
('e1000001-0000-0000-0000-000000000006', 'a1000001-0000-0000-0000-000000000019', 'https://i.pravatar.cc/150?img=35', 'Phạm Văn Ơn', TRUE, 'phamvanon@gmail.com', '1995-06-30', '2023-07-12', ARRAY['Dọn dẹp', 'Sắp xếp'], 'Nhiệt tình, trách nhiệm, làm việc nhanh nhẹn.', true),
('e1000001-0000-0000-0000-000000000007', 'a1000001-0000-0000-0000-000000000020', 'https://i.pravatar.cc/150?img=28', 'Hoàng Thị Phương', FALSE, 'hoangthiphuong@gmail.com', '1997-02-14', '2023-09-08', ARRAY['Vệ sinh sofa', 'Giặt thảm'], 'Chuyên vệ sinh sofa, thảm, rèm cửa bằng máy móc chuyên dụng.', true),
('e1000001-0000-0000-0000-000000000008', 'a1000001-0000-0000-0000-000000000021', 'https://i.pravatar.cc/150?img=36', 'Võ Văn Quang', TRUE, 'vovanquang@gmail.com', '1993-09-22', '2022-11-25', ARRAY['Tổng vệ sinh', 'Làm vườn'], 'Kinh nghiệm dọn dẹp nhà phố, biệt thự và chăm sóc vườn.', true),
('e1000001-0000-0000-0000-000000000009', 'a1000001-0000-0000-0000-000000000022', 'https://i.pravatar.cc/150?img=29', 'Đặng Thị Rượu', FALSE, 'dangthiruou@gmail.com', '1998-05-16', '2024-01-05', ARRAY['Nấu ăn', 'Đi chợ'], 'Nấu ăn ngon, đa dạng món Việt và món Á, mua sắm khéo léo.', true),
('e1000001-0000-0000-0000-000000000010', 'a1000001-0000-0000-0000-000000000023', 'https://i.pravatar.cc/150?img=37', 'Ngô Văn Sơn', TRUE, 'ngovanson@gmail.com', '1991-12-03', '2022-04-18', ARRAY['Vệ sinh công nghiệp', 'Làm sạch kính'], 'Chuyên vệ sinh các tòa nhà cao tầng, lau kính chuyên nghiệp.', true),
('e1000001-0000-0000-0000-000000000011', 'a1000001-0000-0000-0000-000000000024', 'https://i.pravatar.cc/150?img=30', 'Bùi Thị Tâm', FALSE, 'buithitam@gmail.com', '1999-07-28', '2024-02-20', ARRAY['Giặt ủi', 'Chăm sóc quần áo'], 'Giặt ủi cẩn thận, chuyên chăm sóc quần áo cao cấp.', true),
('e1000001-0000-0000-0000-000000000012', 'a1000001-0000-0000-0000-000000000025', 'https://i.pravatar.cc/150?img=38', 'Đỗ Văn Út', TRUE, 'dovanut@gmail.com', '1990-10-10', '2021-12-15', ARRAY['Tổng vệ sinh', 'Khử khuẩn'], 'Lâu năm trong nghề, chuyên khử khuẩn và vệ sinh sâu.', true),
-- Employee profile cho 4 tài khoản có 2 vai trò
('e1000001-0000-0000-0000-000000000013', 'a1000001-0000-0000-0000-000000000026', 'https://i.pravatar.cc/150?img=16', 'Nguyễn Thành Việt', TRUE, 'nguyenthanhviet.employee@gmail.com', '1995-08-20', '2023-10-15', ARRAY['Vệ sinh tổng hợp', 'Bảo trì nhà cửa'], 'Vừa là khách hàng vừa là nhân viên, có kinh nghiệm đa dạng trong lĩnh vực dịch vụ nhà.', true),
('e1000001-0000-0000-0000-000000000014', 'a1000001-0000-0000-0000-000000000027', 'https://i.pravatar.cc/150?img=47', 'Lê Thị Hương', FALSE, 'lethihuong.employee@gmail.com', '1996-12-05', '2023-08-20', ARRAY['Chăm sóc trẻ em', 'Nấu ăn gia đình'], 'Kinh nghiệm chăm sóc trẻ nhỏ và nấu ăn dinh dưỡng cho gia đình.', true),
('e1000001-0000-0000-0000-000000000015', 'a1000001-0000-0000-0000-000000000028', 'https://i.pravatar.cc/150?img=17', 'Phạm Văn Tuấn', TRUE, 'phamvantuan.employee@gmail.com', '1994-06-18', '2023-05-10', ARRAY['Sửa chữa điện nước', 'Vệ sinh máy lạnh'], 'Thợ điện nước lành nghề, chuyên sửa chữa và bảo trì thiết bị gia đình.', true),
('e1000001-0000-0000-0000-000000000016', 'a1000001-0000-0000-0000-000000000029', 'https://i.pravatar.cc/150?img=48', 'Trần Thị Lan', FALSE, 'tranthilan.employee@gmail.com', '1997-09-25', '2024-03-01', ARRAY['Trang trí nhà cửa', 'Sắp xếp đồ đạc'], 'Có khiếu thẩm mỹ, chuyên tư vấn trang trí và sắp xếp nội thất.', true),

-- 30 employee profiles bổ sung
('e1000001-0000-0000-0000-000000000017', 'a1000001-0000-0000-0000-000000000030', 'https://i.pravatar.cc/150?img=51', 'Nguyễn Văn Ba', TRUE, 'nguyenvana1@gmail.com', '1990-01-15', '2023-01-10', ARRAY['Vệ sinh nhà cửa', 'Lau dọn'], 'Có 5 năm kinh nghiệm vệ sinh nhà cửa.', true),
('e1000001-0000-0000-0000-000000000018', 'a1000001-0000-0000-0000-000000000031', 'https://i.pravatar.cc/150?img=44', 'Trần Thị Hai', FALSE, 'tranthib1@gmail.com', '1992-03-20', '2023-02-15', ARRAY['Giặt ủi', 'Nấu ăn'], 'Chuyên về công việc gia đình.', true),
('e1000001-0000-0000-0000-000000000019', 'a1000001-0000-0000-0000-000000000032', 'https://i.pravatar.cc/150?img=52', 'Lê Văn An', TRUE, 'levanc1@gmail.com', '1991-05-10', '2023-03-20', ARRAY['Vệ sinh công nghiệp', 'Làm sạch kính'], 'Chuyên nghiệp vệ sinh tòa nhà cao tầng.', true),
('e1000001-0000-0000-0000-000000000020', 'a1000001-0000-0000-0000-000000000033', 'https://i.pravatar.cc/150?img=45', 'Phạm Thị Dung Em', FALSE, 'phamthid1@gmail.com', '1993-07-25', '2023-04-05', ARRAY['Vệ sinh sofa', 'Giặt thảm'], 'Chuyên vệ sinh nội thất cao cấp.', true),
('e1000001-0000-0000-0000-000000000021', 'a1000001-0000-0000-0000-000000000034', 'https://i.pravatar.cc/150?img=53', 'Hoàng Văn Em Tới', TRUE, 'hoangvane1@gmail.com', '1989-09-12', '2023-05-18', ARRAY['Tổng vệ sinh', 'Làm vườn'], 'Kinh nghiệm dọn dẹp biệt thự và chăm sóc vườn.', true),
('e1000001-0000-0000-0000-000000000022', 'a1000001-0000-0000-0000-000000000035', 'https://i.pravatar.cc/150?img=46', 'Võ Thị Phương Anh', FALSE, 'vothif1@gmail.com', '1994-11-08', '2023-06-22', ARRAY['Nấu ăn', 'Đi chợ'], 'Nấu ăn ngon, đa dạng món Á Âu.', true),
('e1000001-0000-0000-0000-000000000023', 'a1000001-0000-0000-0000-000000000036', 'https://i.pravatar.cc/150?img=54', 'Đặng Văn Giang An', TRUE, 'dangvang1@gmail.com', '1988-02-14', '2023-07-30', ARRAY['Vệ sinh máy lạnh', 'Sửa chữa nhỏ'], 'Có kỹ năng kỹ thuật, bảo trì thiết bị.', true),
('e1000001-0000-0000-0000-000000000024', 'a1000001-0000-0000-0000-000000000037', 'https://i.pravatar.cc/150?img=49', 'Ngô Thị Hà Tâm', FALSE, 'ngothih1@gmail.com', '1995-04-18', '2023-08-12', ARRAY['Giặt ủi', 'Chăm sóc quần áo'], 'Giặt ủi cẩn thận, chuyên nghiệp.', true),
('e1000001-0000-0000-0000-000000000025', 'a1000001-0000-0000-0000-000000000038', 'https://i.pravatar.cc/150?img=55', 'Bùi Văn Tùng', TRUE, 'buivani1@gmail.com', '1990-06-22', '2023-09-05', ARRAY['Tổng vệ sinh', 'Khử khuẩn'], 'Chuyên khử khuẩn và vệ sinh sâu.', true),
('e1000001-0000-0000-0000-000000000026', 'a1000001-0000-0000-0000-000000000039', 'https://i.pravatar.cc/150?img=50', 'Đỗ Thị Kim Mai', FALSE, 'dothik1@gmail.com', '1992-08-30', '2023-10-20', ARRAY['Dọn dẹp', 'Sắp xếp'], 'Nhiệt tình, trách nhiệm cao.', true),
('e1000001-0000-0000-0000-000000000027', 'a1000001-0000-0000-0000-000000000040', 'https://i.pravatar.cc/150?img=56', 'Trần Văn Long Tới', TRUE, 'tranvanl1@gmail.com', '1991-10-05', '2023-11-15', ARRAY['Vệ sinh nhà cửa', 'Lau dọn'], 'Tỉ mỉ và cẩn thận trong công việc.', true),
('e1000001-0000-0000-0000-000000000028', 'a1000001-0000-0000-0000-000000000041', 'https://i.pravatar.cc/150?img=31', 'Nguyễn Thị Mai Dung', FALSE, 'nguyenthim1@gmail.com', '1993-12-12', '2024-01-08', ARRAY['Giặt ủi', 'Nấu ăn'], 'Chuyên về công việc gia đình, nấu ăn ngon.', true),
('e1000001-0000-0000-0000-000000000029', 'a1000001-0000-0000-0000-000000000042', 'https://i.pravatar.cc/150?img=57', 'Lê Tùng Nam', TRUE, 'levann1@gmail.com', '1989-01-20', '2024-02-10', ARRAY['Vệ sinh công nghiệp', 'Làm sạch kính'], 'Chuyên nghiệp, an toàn cao.', true),
('e1000001-0000-0000-0000-000000000030', 'a1000001-0000-0000-0000-000000000043', 'https://i.pravatar.cc/150?img=58', 'Phạm Tùng Ơn', TRUE, 'phamvano1@gmail.com', '1994-03-15', '2024-03-12', ARRAY['Dọn dẹp', 'Sắp xếp'], 'Nhanh nhẹn, làm việc hiệu quả.', true),
('e1000001-0000-0000-0000-000000000031', 'a1000001-0000-0000-0000-000000000044', 'https://i.pravatar.cc/150?img=32', 'Hoàng Thị Phương An', FALSE, 'hoangthip1@gmail.com', '1990-05-28', '2024-04-18', ARRAY['Vệ sinh sofa', 'Giặt thảm'], 'Chuyên vệ sinh nội thất bằng máy.', true),
('e1000001-0000-0000-0000-000000000032', 'a1000001-0000-0000-0000-000000000045', 'https://i.pravatar.cc/150?img=59', 'Võ Minh Quang', TRUE, 'vovanq1@gmail.com', '1992-07-10', '2024-05-20', ARRAY['Tổng vệ sinh', 'Làm vườn'], 'Có kinh nghiệm dọn dẹp nhà phố.', true),
('e1000001-0000-0000-0000-000000000033', 'a1000001-0000-0000-0000-000000000046', 'https://i.pravatar.cc/150?img=25', 'Đặng Thị Bé', FALSE, 'dangthir1@gmail.com', '1991-09-22', '2024-06-15', ARRAY['Nấu ăn', 'Đi chợ'], 'Nấu ăn đa dạng món Việt.', true),
('e1000001-0000-0000-0000-000000000034', 'a1000001-0000-0000-0000-000000000047', 'https://i.pravatar.cc/150?img=60', 'Ngô Văn Tài', TRUE, 'ngovans1@gmail.com', '1988-11-05', '2024-07-10', ARRAY['Vệ sinh công nghiệp', 'Làm sạch kính'], 'Chuyên vệ sinh cao tầng.', true),
('e1000001-0000-0000-0000-000000000035', 'a1000001-0000-0000-0000-000000000048', 'https://i.pravatar.cc/150?img=26', 'Bùi Thị Tâm Nhi', FALSE, 'buithit1@gmail.com', '1993-01-18', '2024-08-05', ARRAY['Giặt ủi', 'Chăm sóc quần áo'], 'Chăm sóc quần áo cao cấp.', true),
('e1000001-0000-0000-0000-000000000036', 'a1000001-0000-0000-0000-000000000049', 'https://i.pravatar.cc/150?img=61', 'Đỗ Văn Út Tùng', TRUE, 'dovanu1@gmail.com', '1990-03-25', '2024-09-12', ARRAY['Tổng vệ sinh', 'Khử khuẩn'], 'Lâu năm trong nghề vệ sinh.', true),
('e1000001-0000-0000-0000-000000000037', 'a1000001-0000-0000-0000-000000000050', 'https://i.pravatar.cc/150?img=62', 'Nguyễn Văn Bình An', TRUE, 'nguyenvanb2@gmail.com', '1989-05-14', '2024-10-08', ARRAY['Vệ sinh nhà cửa', 'Lau dọn'], 'Chuyên nghiệp, uy tín.', true),
('e1000001-0000-0000-0000-000000000038', 'a1000001-0000-0000-0000-000000000051', 'https://i.pravatar.cc/150?img=41', 'Trần Thị Chi Giang', FALSE, 'tranthic2@gmail.com', '1991-07-20', '2024-11-01', ARRAY['Giặt ủi', 'Nấu ăn'], 'Nấu ăn ngon, giặt ủi sạch.', true),
('e1000001-0000-0000-0000-000000000039', 'a1000001-0000-0000-0000-000000000052', 'https://i.pravatar.cc/150?img=63', 'Lê Văn Dũng Hai', TRUE, 'levand2@gmail.com', '1990-09-08', '2022-01-15', ARRAY['Vệ sinh công nghiệp', 'Làm sạch kính'], 'Kinh nghiệm lâu năm.', true),
('e1000001-0000-0000-0000-000000000040', 'a1000001-0000-0000-0000-000000000053', 'https://i.pravatar.cc/150?img=42', 'Phạm Thị Em Mai', FALSE, 'phamthie2@gmail.com', '1992-11-16', '2022-02-20', ARRAY['Vệ sinh sofa', 'Giặt thảm'], 'Chuyên vệ sinh nội thất.', true),
('e1000001-0000-0000-0000-000000000041', 'a1000001-0000-0000-0000-000000000054', 'https://i.pravatar.cc/150?img=64', 'Hoàng Văn Phúc Tài', TRUE, 'hoangvanf2@gmail.com', '1988-12-22', '2022-03-25', ARRAY['Tổng vệ sinh', 'Làm vườn'], 'Dọn dẹp biệt thự chuyên nghiệp.', true),
('e1000001-0000-0000-0000-000000000042', 'a1000001-0000-0000-0000-000000000055', 'https://i.pravatar.cc/150?img=43', 'Võ Thị Giang Nhi', FALSE, 'vothig2@gmail.com', '1993-02-10', '2022-04-30', ARRAY['Nấu ăn', 'Đi chợ'], 'Nấu ăn đa dạng món ăn.', true),
('e1000001-0000-0000-0000-000000000043', 'a1000001-0000-0000-0000-000000000056', 'https://i.pravatar.cc/150?img=65', 'Đặng Văn Minh Tới', TRUE, 'dangvanh2@gmail.com', '1991-04-18', '2022-05-15', ARRAY['Vệ sinh máy lạnh', 'Sửa chữa nhỏ'], 'Thợ kỹ thuật lành nghề.', true),
('e1000001-0000-0000-0000-000000000044', 'a1000001-0000-0000-0000-000000000057', 'https://i.pravatar.cc/150?img=40', 'Ngô Thị Ích Nhi', FALSE, 'ngothii2@gmail.com', '1994-06-24', '2022-06-20', ARRAY['Giặt ủi', 'Chăm sóc quần áo'], 'Giặt ủi chuyên nghiệp.', true),
('e1000001-0000-0000-0000-000000000045', 'a1000001-0000-0000-0000-000000000058', 'https://i.pravatar.cc/150?img=66', 'Bùi Phạm Tấn Khoa', TRUE, 'buivank2@gmail.com', '1989-08-30', '2022-07-25', ARRAY['Tổng vệ sinh', 'Khử khuẩn'], 'Chuyên khử khuẩn an toàn.', true),
('e1000001-0000-0000-0000-000000000046', 'a1000001-0000-0000-0000-000000000059', 'https://i.pravatar.cc/150?img=39', 'Đỗ Thị Liên Nhi', FALSE, 'dothil2@gmail.com', '1990-10-12', '2022-08-30', ARRAY['Dọn dẹp', 'Sắp xếp'], 'Sắp xếp gọn gàng, khoa học.', true);


-- Rating mẫu cho nhân viên chủ lực
UPDATE employee
SET rating = 'HIGHEST'
WHERE employee_id = 'e1000001-0000-0000-0000-000000000001';

UPDATE employee
SET rating = 'HIGH'
WHERE employee_id = 'e1000001-0000-0000-0000-000000000002';

UPDATE employee
SET rating = 'MEDIUM'
WHERE employee_id = 'e1000001-0000-0000-0000-000000000003';

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
('e1000001-0000-0000-0000-000000000016', 'Phường Tân Hiệp', 'Thành phố Hồ Chí Minh'),
-- Working zones cho 30 nhân viên bổ sung
('e1000001-0000-0000-0000-000000000017', 'Phường Thủ Dầu Một', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000018', 'Phường Phú Lợi', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000019', 'Phường Bình Dương', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000020', 'Phường Phú An', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000021', 'Phường Chánh Hiệp', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000022', 'Phường Bến Cát', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000023', 'Phường Chánh Phú Hòa', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000024', 'Phường Long Nguyên', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000025', 'Phường Tây Nam', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000026', 'Phường Thới Hòa', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000027', 'Phường Hòa Lợi', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000028', 'Phường Tân Uyên', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000029', 'Phường Tân Khánh', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000030', 'Phường Vĩnh Tân', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000031', 'Phường Bình Cơ', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000032', 'Phường Tân Hiệp', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000033', 'Phường Thủ Dầu Một', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000034', 'Phường Phú Lợi', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000035', 'Phường Bình Dương', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000036', 'Phường Phú An', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000037', 'Phường Chánh Hiệp', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000038', 'Phường Bến Cát', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000039', 'Phường Chánh Phú Hòa', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000040', 'Phường Long Nguyên', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000041', 'Phường Tây Nam', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000042', 'Phường Thới Hòa', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000043', 'Phường Hòa Lợi', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000044', 'Phường Tân Uyên', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000045', 'Phường Tân Khánh', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000046', 'Phường Vĩnh Tân', 'Thành phố Hồ Chí Minh');

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
-- 4 NHÂN VIÊN LÀM VIỆC TẠI PHƯỜNG GÒ VẤP, TP.HCM
-- =================================================================================

-- Thêm 4 tài khoản nhân viên Gò Vấp
INSERT INTO account (account_id, username, password, phone_number, status, is_phone_verified) VALUES
('a1000001-0000-0000-0000-000000000060', 'nmtuan01', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0912100001', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000061', 'tthnhung', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0912100002', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000062', 'lvhung', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0912100003', 'ACTIVE', true),
('a1000001-0000-0000-0000-000000000063', 'ptlanh', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', '0912100004', 'ACTIVE', true);

-- Gán vai trò EMPLOYEE cho 4 tài khoản Gò Vấp
INSERT INTO account_roles (account_id, role_id) VALUES
('a1000001-0000-0000-0000-000000000060', 2),
('a1000001-0000-0000-0000-000000000061', 2),
('a1000001-0000-0000-0000-000000000062', 2),
('a1000001-0000-0000-0000-000000000063', 2);

-- Thêm hồ sơ nhân viên Gò Vấp
INSERT INTO employee (employee_id, account_id, avatar, full_name, is_male, email, birthdate, hired_date, skills, bio, is_email_verified) VALUES
('e1000001-0000-0000-0000-000000000047', 'a1000001-0000-0000-0000-000000000060', 'https://i.pravatar.cc/150?img=67', 'Nguyễn Minh Tuấn', TRUE, 'nguyenminhtuan.govap@gmail.com', '1993-05-15', '2024-01-10', ARRAY['Vệ sinh nhà cửa', 'Lau dọn', 'Dọn dẹp'], 'Nhân viên vệ sinh chuyên nghiệp tại Gò Vấp, có 3 năm kinh nghiệm.', true),
('e1000001-0000-0000-0000-000000000048', 'a1000001-0000-0000-0000-000000000061', 'https://i.pravatar.cc/150?img=68', 'Trần Thị Hồng Nhung', FALSE, 'tranhongnhung.govap@gmail.com', '1995-08-22', '2024-02-15', ARRAY['Giặt ủi', 'Nấu ăn', 'Dọn dẹp'], 'Chuyên về công việc gia đình, giặt ủi và nấu ăn tại khu vực Gò Vấp.', true),
('e1000001-0000-0000-0000-000000000049', 'a1000001-0000-0000-0000-000000000062', 'https://i.pravatar.cc/150?img=69', 'Lê Văn Hùng', TRUE, 'levanhung.govap@gmail.com', '1991-11-08', '2023-11-20', ARRAY['Vệ sinh máy lạnh', 'Sửa chữa nhỏ', 'Tổng vệ sinh'], 'Thợ kỹ thuật lành nghề, chuyên vệ sinh máy lạnh và sửa chữa tại Gò Vấp.', true),
('e1000001-0000-0000-0000-000000000050', 'a1000001-0000-0000-0000-000000000063', 'https://i.pravatar.cc/150?img=70', 'Phạm Thị Lan Anh', FALSE, 'phamthilananh.govap@gmail.com', '1997-03-30', '2024-03-05', ARRAY['Vệ sinh sofa', 'Giặt thảm', 'Khử khuẩn'], 'Chuyên vệ sinh nội thất, sofa, thảm bằng máy móc chuyên dụng tại Gò Vấp.', true);

-- Thiết lập vùng làm việc Gò Vấp cho 4 nhân viên
INSERT INTO employee_working_zones (employee_id, ward, city) VALUES
('e1000001-0000-0000-0000-000000000047', 'Phường Gò Vấp', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000048', 'Phường Gò Vấp', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000049', 'Phường Gò Vấp', 'Thành phố Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000050', 'Phường Gò Vấp', 'Thành phố Hồ Chí Minh');

-- Thiết lập rating cho nhân viên Gò Vấp
UPDATE employee SET rating = 'HIGH' WHERE employee_id = 'e1000001-0000-0000-0000-000000000047';
UPDATE employee SET rating = 'MEDIUM' WHERE employee_id = 'e1000001-0000-0000-0000-000000000048';
UPDATE employee SET rating = 'HIGH' WHERE employee_id = 'e1000001-0000-0000-0000-000000000049';
UPDATE employee SET rating = 'MEDIUM' WHERE employee_id = 'e1000001-0000-0000-0000-000000000050';

-- =================================================================================
-- THÊM DỮ LIỆU MẪU (TIẾNG VIỆT) VÀO CÁC KHỐI CÒN LẠI
-- =================================================================================
