-- Kích hoạt extension cho UUID
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =================================================================================
-- KHỐI I: QUẢN LÝ TÀI KHOẢN VÀ HỒ SƠ (ACCOUNT & PROFILES)
-- =================================================================================

-- Bảng ACCOUNT giờ đây đại diện cho một người dùng duy nhất trong hệ thống.
CREATE TABLE account (
    account_id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(50) UNIQUE NOT NULL, -- Username là duy nhất trên toàn hệ thống
    password VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) UNIQUE, -- Số điện thoại cũng là duy nhất
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
    is_phone_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP WITH TIME ZONE
);

-- Bảng định nghĩa các vai trò có trong hệ thống
CREATE TABLE roles (
    role_id INT PRIMARY KEY,
    role_name VARCHAR(20) UNIQUE NOT NULL CHECK (role_name IN ('ADMIN', 'EMPLOYEE', 'CUSTOMER'))
);

-- Thêm các vai trò mặc định
INSERT INTO roles (role_id, role_name) VALUES (1, 'CUSTOMER'), (2, 'EMPLOYEE'), (3, 'ADMIN');

-- Bảng trung gian để gán vai trò cho tài khoản. Một tài khoản có thể có nhiều vai trò.
CREATE TABLE account_roles (
    account_id VARCHAR(36) NOT NULL REFERENCES account(account_id) ON DELETE CASCADE,
    role_id INT NOT NULL REFERENCES roles(role_id),
    PRIMARY KEY (account_id, role_id)
);

-- Các bảng hồ sơ vẫn liên kết với bảng ACCOUNT.
-- Sự tồn tại của một record trong bảng này ngầm định tài khoản đó có vai trò tương ứng.
CREATE TABLE customer (
    customer_id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
    account_id VARCHAR(36) NOT NULL UNIQUE REFERENCES account(account_id) ON DELETE CASCADE,
    avatar VARCHAR(255),
    full_name VARCHAR(100) NOT NULL,
    is_male BOOLEAN,
    email VARCHAR(100) UNIQUE,
    birthdate DATE,
	rating varchar(10) CHECK (rating IN ('LOWEST', 'LOW', 'MEDIUM', 'HIGH', 'HIGHEST')),
	vip_level int CHECK (vip_level BETWEEN 1 AND 5),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE employee (
    employee_id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
    account_id VARCHAR(36) NOT NULL UNIQUE REFERENCES account(account_id) ON DELETE CASCADE,
    avatar VARCHAR(255),
    full_name VARCHAR(100) NOT NULL,
    is_male BOOLEAN,
    email VARCHAR(100) UNIQUE,
    birthdate DATE,
    hired_date DATE,
    skills TEXT[],
    bio TEXT,
	rating varchar(10) CHECK (rating IN ('LOWEST', 'LOW', 'MEDIUM', 'HIGH', 'HIGHEST')),
    employee_status VARCHAR(20) DEFAULT 'AVAILABLE' CHECK (employee_status IN ('AVAILABLE', 'BUSY', 'ON_LEAVE')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE employee_working_zones (
    employee_id VARCHAR(36) NOT NULL REFERENCES employee(employee_id) ON DELETE CASCADE,
    district VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL,
    PRIMARY KEY (employee_id, district, city)
);

CREATE TABLE admin_profile (
    admin_profile_id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
    account_id VARCHAR(36) NOT NULL UNIQUE REFERENCES account(account_id) ON DELETE CASCADE,
    full_name VARCHAR(100) NOT NULL,
    is_male BOOLEAN,
    department VARCHAR(50),
    contact_info VARCHAR(255),
    birthdate DATE,
    hire_date DATE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE address (
    address_id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
    customer_id VARCHAR(36) NOT NULL REFERENCES customer(customer_id) ON DELETE CASCADE,
    full_address TEXT NOT NULL,
    ward VARCHAR(100),
    district VARCHAR(100),
    city VARCHAR(100),
    latitude DECIMAL(9, 6),
    longitude DECIMAL(9, 6),
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- =================================================================================
-- CÁC KHỐI CÒN LẠI (BOOKINGS, SERVICES, PAYMENTS...)
-- =================================================================================

CREATE TABLE service (
    service_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    base_price DECIMAL(10, 2) NOT NULL,
    unit VARCHAR(20) NOT NULL, -- Ví dụ: 'hour', 'm2', 'package'
    estimated_duration_hours DECIMAL(5, 2), -- Thời gian dự kiến (giờ)
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE bookings (
    booking_id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
    customer_id VARCHAR(36) NOT NULL REFERENCES customer(customer_id),
    address_id VARCHAR(36) NOT NULL REFERENCES address(address_id),
    booking_code VARCHAR(10) UNIQUE, -- Mã đặt lịch dễ nhớ cho người dùng
    booking_time TIMESTAMP WITH TIME ZONE NOT NULL, -- Thời gian khách hàng muốn thực hiện
    note TEXT,
    total_amount DECIMAL(10, 2), 
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'CONFIRMED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE booking_details (
    booking_detail_id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
    booking_id VARCHAR(36) NOT NULL REFERENCES bookings(booking_id) ON DELETE CASCADE,
    service_id INT NOT NULL REFERENCES service(service_id),
    quantity INT DEFAULT 1,
    price_per_unit DECIMAL(10, 2),
    sub_total DECIMAL(10, 2), -- (price_per_unit * quantity)
   	CONSTRAINT unique_booking_service UNIQUE (booking_id, service_id)
);

CREATE TABLE recurring_bookings (
    recurring_id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
    customer_id VARCHAR(36) NOT NULL REFERENCES customer(customer_id),
    address_id VARCHAR(36) NOT NULL REFERENCES address(address_id),
    service_id INT NOT NULL REFERENCES service(service_id),
    -- Quy tắc lặp lại
    frequency_type VARCHAR(20) NOT NULL CHECK (frequency_type IN ('WEEKLY', 'MONTHLY')),
    interval INT DEFAULT 1, -- Ví dụ: Lặp lại mỗi 1 tuần, hoặc mỗi 2 tuần
    day_of_week INT, -- 1=Thứ Hai, ..., 7=Chủ Nhật (nếu là WEEKLY)
    day_of_month INT, -- Ngày trong tháng (nếu là MONTHLY)
    start_time TIME NOT NULL, -- Thời gian bắt đầu công việc
    -- Thời gian áp dụng
    start_date DATE NOT NULL,
    end_date DATE, -- Có thể NULL nếu là vô thời hạn
    is_active BOOLEAN DEFAULT TRUE
);

--Bảng phân công nhân viên cho một dịch vụ cụ thể trong một lần đặt lịch
CREATE TABLE assignments (
    assignment_id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
    booking_detail_id VARCHAR(36) NOT NULL REFERENCES booking_details(booking_detail_id) ON DELETE CASCADE,
    employee_id VARCHAR(36) NOT NULL REFERENCES employee(employee_id),
    status VARCHAR(20) DEFAULT 'ASSIGNED' CHECK (status IN ('ASSIGNED', 'CHECKED_IN', 'CHECKED_OUT')),
    check_in_time TIMESTAMP WITH TIME ZONE,
    check_out_time TIMESTAMP WITH TIME ZONE,
    UNIQUE (booking_detail_id, employee_id)
);

-- =================================================================================
-- KHỐI III: CÁC TÍNH NĂNG NÂNG CAO (ADVANCED FEATURES)
-- Các bảng này được giữ nguyên.
-- =================================================================================

CREATE TABLE checklist_templates (
    template_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    service_id INT REFERENCES service(service_id),
    name VARCHAR(255) NOT NULL,
    description TEXT
);

CREATE TABLE checklist_template_items (
    item_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    template_id INT NOT NULL REFERENCES checklist_templates(template_id) ON DELETE CASCADE,
    item_description TEXT NOT NULL,
    item_order INT
);

CREATE TABLE booking_checklist_items (
    booking_checklist_item_id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
    booking_id VARCHAR(36) NOT NULL REFERENCES bookings(booking_id) ON DELETE CASCADE,
    item_description TEXT NOT NULL,
    is_completed BOOLEAN DEFAULT FALSE,
    completed_at TIMESTAMP WITH TIME ZONE,
    employee_id VARCHAR(36) REFERENCES employee(employee_id)
);

CREATE TABLE booking_media (
    media_id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
    booking_id VARCHAR(36) NOT NULL REFERENCES bookings(booking_id) ON DELETE CASCADE,
    assignment_id VARCHAR(36) REFERENCES assignments(assignment_id),
    media_url VARCHAR(255) NOT NULL,
    media_type VARCHAR(20) NOT NULL CHECK (media_type IN ('BEFORE', 'AFTER')),
    uploaded_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);


-- =================================================================================
-- KHỐI IV: THANH TOÁN, ĐÁNH GIÁ VÀ HỖ TRỢ
-- Các bảng này được giữ nguyên.
-- =================================================================================

CREATE TABLE payments (
    payment_id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
    booking_id VARCHAR(36) NOT NULL REFERENCES bookings(booking_id),
    amount DECIMAL(10, 2) NOT NULL,
    payment_method VARCHAR(20) CHECK (payment_method IN ('CASH', 'TRANSFER', 'MOMO', 'VNPAY')),
    payment_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (payment_status IN ('PENDING', 'PAID', 'FAILED')),
    transaction_code VARCHAR(100),
    paid_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE review (
    review_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    booking_id VARCHAR(36) NOT NULL REFERENCES bookings(booking_id),
    customer_id VARCHAR(36) NOT NULL REFERENCES customer(customer_id),
    employee_id VARCHAR(36) NOT NULL REFERENCES employee(employee_id),
    comment TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE review_criteria (
    criteria_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    criteria_name VARCHAR(100) UNIQUE NOT NULL -- Ví dụ: "Thái độ", "Đúng giờ", "Chất lượng công việc"
);

CREATE TABLE review_details (
    review_id INT NOT NULL REFERENCES review(review_id) ON DELETE CASCADE,
    criteria_id INT NOT NULL REFERENCES review_criteria(criteria_id),
    rating DECIMAL(2, 1) NOT NULL CHECK (rating BETWEEN 1 AND 5),
    PRIMARY KEY (review_id, criteria_id)
);

CREATE TABLE support_ticket (
    ticket_id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
    customer_id VARCHAR(36) REFERENCES customer(customer_id),
    booking_id VARCHAR(36) REFERENCES bookings(booking_id),
    subject VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE promotions (
    promotion_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    promo_code VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    discount_type VARCHAR(20) NOT NULL CHECK (discount_type IN ('PERCENTAGE', 'FIXED_AMOUNT')),
    discount_value DECIMAL(10, 2) NOT NULL,
    max_discount_amount DECIMAL(10, 2), -- Mức giảm giá tối đa (cho loại PERCENTAGE)
    start_date TIMESTAMP WITH TIME ZONE,
    end_date TIMESTAMP WITH TIME ZONE,
    usage_limit INT,
    is_active BOOLEAN DEFAULT TRUE
);

ALTER TABLE bookings
ADD COLUMN promotion_id INT REFERENCES promotions(promotion_id);

-- LƯU Ý: Mật khẩu '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK' tương ứng với 'password123'.
-- TRONG THỰC TẾ, LUÔN MÃ HÓA MẬT KHẨU TRƯỚC KHI LƯU VÀO DATABASE.

-- =================================================================================
-- THÊM DỮ LIỆU MẪU TỪ V4 VÀO KHỐI I CỦA V5
-- =================================================================================

-- Thêm dữ liệu vào bảng `account` từ dữ liệu gốc của v4
-- Account của jane_smith được gộp lại thành một vì SĐT là duy nhất.
-- Account của mary_jones và bob_wilson (nhân viên không có account) được tạo mới.
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
INSERT INTO address (address_id, customer_id, full_address, ward, district, city, is_default) VALUES
('adrs0001-0000-0000-0000-000000000001', 'c1000001-0000-0000-0000-000000000001', '123 Lê Trọng Tấn, Tây Thạnh, Tân Phú, TP. Hồ Chí Minh', 'Phường Tây Thạnh', 'Quận Tân Phú', 'TP. Hồ Chí Minh', true),
('adrs0001-0000-0000-0000-000000000002', 'c1000001-0000-0000-0000-000000000002', '456 Lê Lợi, Bến Nghé, Quận 1,TP. Hồ Chí Minh', 'Phường Bến Nghé', 'Quận 1', 'TP. Hồ Chí Minh', true),
('adrs0001-0000-0000-0000-000000000003', 'c1000001-0000-0000-0000-000000000003', '104 Lê Lợi, Phường 1, Gò Vấp, TP. Hồ Chí Minh', 'Phường 1', 'Quận Gò Vấp', 'TP. Hồ Chí Minh', true);

-- Thêm khu vực làm việc cho nhân viên
INSERT INTO employee_working_zones (employee_id, district, city) VALUES
('e1000001-0000-0000-0000-000000000001', 'Quận Tân Phú', 'TP. Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000001', 'Quận Tân Bình', 'TP. Hồ Chí Minh'),
('e1000001-0000-0000-0000-000000000002', 'Quận Gò Vấp', 'TP. Hồ Chí Minh');