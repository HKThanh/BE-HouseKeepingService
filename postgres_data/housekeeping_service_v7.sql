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

-- Bảng mới để quản lý các danh mục dịch vụ
CREATE TABLE service_categories (
    category_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    parent_category_id INT REFERENCES service_categories(category_id), -- Để tạo cấu trúc cha-con
    category_name VARCHAR(100) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

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

ALTER TABLE service
    ADD COLUMN category_id INT REFERENCES service_categories(category_id);

-- Đặt dịch vụ
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

-- Các tuỳ chọn cho dịch vụ
CREATE TABLE service_options (
    option_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    service_id INT NOT NULL REFERENCES service(service_id) ON DELETE CASCADE,
    label TEXT NOT NULL,
    option_type VARCHAR(30) NOT NULL
     CHECK (option_type IN (
                            'SINGLE_CHOICE_RADIO',
                            'SINGLE_CHOICE_DROPDOWN',
                            'MULTIPLE_CHOICE_CHECKBOX',
                            'QUANTITY_INPUT',
                            'TEXT_INPUT'
         )),
    display_order INT,
    is_required BOOLEAN DEFAULT TRUE,
    parent_option_id INT REFERENCES service_options(option_id),
    parent_choice_id INT,
    validation_rules JSONB
);

CREATE TABLE service_option_choices (
    choice_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    option_id INT NOT NULL REFERENCES service_options(option_id) ON DELETE CASCADE,
    label TEXT NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    display_order INT
);

ALTER TABLE service_options
ADD CONSTRAINT fk_parent_choice
FOREIGN KEY (parent_choice_id)
REFERENCES service_option_choices(choice_id);

-- Tính toán giá tổng
CREATE TABLE pricing_rules (
    rule_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    service_id INT NOT NULL REFERENCES service(service_id),
    rule_name VARCHAR(255) UNIQUE NOT NULL, -- Tên quy tắc, ví dụ: "Phụ thu nhà phố diện tích lớn"
    condition_logic VARCHAR(10) DEFAULT 'ALL' CHECK (condition_logic IN ('ALL', 'ANY')), -- ALL=AND, ANY=OR
    priority INT DEFAULT 0, -- Độ ưu tiên áp dụng
    price_adjustment DECIMAL(10, 2) DEFAULT 0,
    staff_adjustment INT DEFAULT 0,
    duration_adjustment_hours DECIMAL(5, 2) DEFAULT 0
);

-- Bảng mới: điều kiện để kích hoạt một quy tắc giá
CREATE TABLE rule_conditions (
    rule_id INT NOT NULL REFERENCES pricing_rules(rule_id) ON DELETE CASCADE,
    choice_id INT NOT NULL REFERENCES service_option_choices(choice_id) ON DELETE CASCADE,
    PRIMARY KEY (rule_id, choice_id)
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
    status VARCHAR(20) DEFAULT 'ASSIGNED' CHECK (status IN ('ASSIGNED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'NO_SHOW')),
    check_in_time TIMESTAMP WITH TIME ZONE,
    check_out_time TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (booking_detail_id, employee_id)
);

CREATE TABLE employee_unavailability (
     unavailability_id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
     employee_id VARCHAR(36) NOT NULL REFERENCES employee(employee_id) ON DELETE CASCADE,
     start_time TIMESTAMP WITH TIME ZONE NOT NULL,
     end_time TIMESTAMP WITH TIME ZONE NOT NULL,
     reason TEXT, -- Lý do: "Lịch cá nhân", "Nghỉ phép", "Khám bệnh"
     is_approved BOOLEAN DEFAULT TRUE, -- Có thể cần Admin duyệt nếu là nghỉ phép dài
     created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
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
    payment_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (payment_status IN ('PENDING', 'PAID', 'FAILED', 'CANCELLED', 'REFUNDED')),
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
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE bookings
ADD COLUMN promotion_id INT REFERENCES promotions(promotion_id);

-- =================================================================================
-- KHỐI V: HỆ THỐNG PHÂN QUYỀN ĐỘNG (DYNAMIC PERMISSIONS)
-- Các bảng mới để hỗ trợ ý tưởng phân quyền linh hoạt cho Admin.
-- =================================================================================

-- Bảng định nghĩa tất cả các chức năng có trong hệ thống
CREATE TABLE features (
    feature_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    feature_name VARCHAR(100) UNIQUE NOT NULL, -- Tên định danh (code) của chức năng, Vd: 'booking.create'

    description TEXT, -- Mô tả thân thiện, Vd: 'Tạo một lịch đặt mới'
    module VARCHAR(50) -- Gom nhóm chức năng theo module, Vd: 'Booking', 'Account', 'Payment'
);

-- Bảng trung gian để gán quyền (chức năng) cho vai trò và quản lý trạng thái
CREATE TABLE role_features (
    role_id INT NOT NULL REFERENCES roles(role_id) ON DELETE CASCADE,
    feature_id INT NOT NULL REFERENCES features(feature_id) ON DELETE CASCADE,
    is_enabled BOOLEAN DEFAULT TRUE, -- TRUE: chức năng được phép, FALSE: chức năng bị vô hiệu hóa
    PRIMARY KEY (role_id, feature_id)
);

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
INSERT INTO service_categories (category_name, description) VALUES
('Dọn dẹp nhà', 'Các dịch vụ liên quan đến vệ sinh, làm sạch nhà cửa'),
('Giặt ủi', 'Dịch vụ giặt sấy, ủi đồ chuyên nghiệp'),
('Việc nhà khác', 'Các dịch vụ tiện ích gia đình khác');

-- Thêm các dịch vụ con vào từng danh mục
-- Dữ liệu cho danh mục 'Dọn dẹp nhà' (category_id = 1)
INSERT INTO service (category_id, name, description, base_price, unit, estimated_duration_hours, is_active) VALUES
(1, 'Dọn dẹp theo giờ', 'Lau dọn, hút bụi, làm sạch các bề mặt cơ bản trong nhà. Phù hợp cho nhu cầu duy trì vệ sinh hàng tuần.', 50000, 'Giờ', 2.0, TRUE),
(1, 'Tổng vệ sinh', 'Làm sạch sâu toàn diện, bao gồm các khu vực khó tiếp cận, trần nhà, lau cửa kính. Thích hợp cho nhà mới hoặc dọn dẹp theo mùa.', 100000, 'Gói', 2.0, TRUE),
(1, 'Vệ sinh Sofa - Nệm - Rèm', 'Giặt sạch và khử khuẩn Sofa, Nệm, Rèm cửa bằng máy móc chuyên dụng.', 300000, 'Gói', 3.0, TRUE),
(1, 'Vệ sinh máy lạnh', 'Bảo trì, làm sạch dàn nóng và dàn lạnh, bơm gas nếu cần.', 150000, 'Máy', 1.0, TRUE);

-- Dữ liệu cho danh mục 'Giặt ủi' (category_id = 2)
INSERT INTO service (category_id, name, description, base_price, unit, estimated_duration_hours, is_active) VALUES
(2, 'Giặt sấy theo kg', 'Giặt và sấy khô quần áo thông thường, giao nhận tận nơi.', 30000, 'Kg', 24.0, TRUE),
(2, 'Giặt hấp cao cấp', 'Giặt khô cho các loại vải cao cấp như vest, áo dài, lụa.', 120000, 'Bộ', 48.0, TRUE);

-- Dữ liệu cho danh mục 'Việc nhà khác' (category_id = 3)
INSERT INTO service (category_id, name, description, base_price, unit, estimated_duration_hours, is_active) VALUES
(3, 'Nấu ăn gia đình', 'Đi chợ (chi phí thực phẩm tính riêng) và chuẩn bị bữa ăn cho gia đình theo thực đơn yêu cầu.', 60000, 'Giờ', 2.5, TRUE),
(3, 'Đi chợ hộ', 'Mua sắm và giao hàng tận nơi theo danh sách của bạn.', 40000, 'Lần', 1.0, TRUE);

-- Thêm các chương trình khuyến mãi
INSERT INTO promotions (promo_code, description, discount_type, discount_value, max_discount_amount, start_date, end_date, is_active) VALUES
('GIAM20K', 'Giảm giá 20,000đ cho mọi đơn hàng', 'FIXED_AMOUNT', 20000, NULL, '2025-08-01 00:00:00+07', '2025-09-30 23:59:59+07', TRUE),
('KHAITRUONG10', 'Giảm 10% mừng khai trương', 'PERCENTAGE', 10, 50000, '2025-08-01 00:00:00+07', '2025-08-31 23:59:59+07', TRUE);

-- Thêm 2 lịch đặt (bookings) mẫu
-- Một lịch đã HOÀN THÀNH của khách hàng 'John Doe'
-- Một lịch đã XÁC NHẬN của khách hàng 'Jane Smith Customer'
INSERT INTO bookings (booking_id, customer_id, address_id, booking_code, booking_time, note, total_amount, status, promotion_id) VALUES
('b0000001-0000-0000-0000-000000000001', 'c1000001-0000-0000-0000-000000000001', 'adrs0001-0000-0000-0000-000000000001', 'BK000001', '2025-08-20 09:00:00+07', 'Nhà có trẻ nhỏ, vui lòng lau dọn kỹ khu vực phòng khách.', 380000.00, 'COMPLETED', (SELECT promotion_id FROM promotions WHERE promo_code = 'GIAM20K')),
('b0000001-0000-0000-0000-000000000002', 'c1000001-0000-0000-0000-000000000003', 'adrs0001-0000-0000-0000-000000000003', 'BK000002', '2025-08-28 14:00:00+07', 'Vui lòng đến đúng giờ.', 90000.00, 'CONFIRMED', (SELECT promotion_id FROM promotions WHERE promo_code = 'KHAITRUONG10'));

-- Thêm chi tiết dịch vụ cho các lịch đặt
INSERT INTO booking_details (booking_detail_id, booking_id, service_id, quantity, price_per_unit, sub_total) VALUES
('bd000001-0000-0000-0000-000000000001', 'b0000001-0000-0000-0000-000000000001', (SELECT service_id FROM service WHERE name = 'Tổng vệ sinh'), 1, 400000.00, 400000.00),
('bd000001-0000-0000-0000-000000000002', 'b0000001-0000-0000-0000-000000000002', (SELECT service_id FROM service WHERE name = 'Dọn dẹp theo giờ'), 2, 50000.00, 100000.00);

-- Phân công nhân viên cho các lịch đặt
INSERT INTO assignments (assignment_id, booking_detail_id, employee_id, status, check_in_time, check_out_time) VALUES
('as000001-0000-0000-0000-000000000001', 'bd000001-0000-0000-0000-000000000001', 'e1000001-0000-0000-0000-000000000002', 'COMPLETED', '2025-08-20 09:00:00+07', '2025-08-20 13:00:00+07'),
('as000001-0000-0000-0000-000000000002', 'bd000001-0000-0000-0000-000000000002', 'e1000001-0000-0000-0000-000000000001', 'ASSIGNED', NULL, NULL);

-- Khối IV: Thêm dữ liệu cho Thanh toán và Đánh giá
-- =================================================================================

-- Thêm dữ liệu thanh toán
INSERT INTO payments (payment_id, booking_id, amount, payment_method, payment_status, transaction_code, paid_at) VALUES
('pay00001-0000-0000-0000-000000000001', 'b0000001-0000-0000-0000-000000000001', 380000.00, 'VNPAY', 'PAID', 'VNP123456789', '2025-08-20 13:05:00+07'),
('pay00001-0000-0000-0000-000000000002', 'b0000001-0000-0000-0000-000000000002', 90000.00, 'MOMO', 'PENDING', NULL, NULL);

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


-- Khối III: Thêm dữ liệu cho Checklist và Media
-- =================================================================================

-- Thêm một mẫu checklist cho dịch vụ 'Tổng vệ sinh'
INSERT INTO checklist_templates (service_id, name, description) VALUES
((SELECT service_id FROM service WHERE name = 'Tổng vệ sinh'), 'Checklist Tổng Vệ Sinh Cơ Bản', 'Các đầu việc cần làm cho gói tổng vệ sinh');

-- Thêm các mục con cho mẫu checklist
INSERT INTO checklist_template_items (template_id, item_description, item_order) VALUES
(1, 'Quét và lau sàn tất cả các phòng', 1),
(1, 'Hút bụi thảm, sofa', 2),
(1, 'Lau bụi bề mặt tủ, bàn, ghế', 3),
(1, 'Vệ sinh toàn bộ khu vực nhà vệ sinh', 4),
(1, 'Lau cửa kính mặt trong', 5),
(1, 'Thu gom và đổ rác', 6);

-- Thêm các mục checklist vào lịch đặt đã hoàn thành (giả sử được copy từ template)
INSERT INTO booking_checklist_items (booking_id, item_description, is_completed, completed_at, employee_id) VALUES
('b0000001-0000-0000-0000-000000000001', 'Quét và lau sàn tất cả các phòng', true, '2025-08-20 10:00:00+07', 'e1000001-0000-0000-0000-000000000002'),
('b0000001-0000-0000-0000-000000000001', 'Hút bụi thảm, sofa', true, '2025-08-20 10:30:00+07', 'e1000001-0000-0000-0000-000000000002'),
('b0000001-0000-0000-0000-000000000001', 'Lau bụi bề mặt tủ, bàn, ghế', true, '2025-08-20 11:00:00+07', 'e1000001-0000-0000-0000-000000000002');


-- Thêm media (ảnh Trước & Sau) cho lịch đặt đã hoàn thành
INSERT INTO booking_media (booking_id, assignment_id, media_url, media_type) VALUES
('b0000001-0000-0000-0000-000000000001', 'as000001-0000-0000-0000-000000000001', 'https://example.com/images/before_job1.jpg', 'BEFORE'),
('b0000001-0000-0000-0000-000000000001', 'as000001-0000-0000-0000-000000000001', 'https://example.com/images/after_job1.jpg', 'AFTER');

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
INSERT INTO service_options (service_id, label, option_type, display_order) VALUES (2, 'Loại hình nhà ở?', 'SINGLE_CHOICE_RADIO', 1);
INSERT INTO service_option_choices (option_id, label) VALUES (1, 'Căn hộ'), (1, 'Nhà phố');

-- Câu hỏi 2 (PHỤ THUỘC): Số tầng (chỉ hiện khi chọn 'Nhà phố' - choice_id=2)
INSERT INTO service_options (service_id, label, option_type, display_order, parent_choice_id) VALUES (2, 'Nhà bạn có mấy tầng (bao gồm trệt)?', 'QUANTITY_INPUT', 2, 2);

-- Câu hỏi 3: Diện tích
INSERT INTO service_options (service_id, label, option_type, display_order) VALUES (2, 'Diện tích dọn dẹp?', 'SINGLE_CHOICE_DROPDOWN', 3);
INSERT INTO service_option_choices (option_id, label) VALUES (3, 'Dưới 80m²'), (3, 'Trên 80m²');

INSERT INTO pricing_rules (service_id, rule_name, condition_logic, price_adjustment, staff_adjustment) VALUES
    (2, 'Phụ thu nhà phố lớn', 'ALL', 250000, 1);

-- Gán 2 điều kiện cho quy tắc trên (rule_id=1)
INSERT INTO rule_conditions (rule_id, choice_id) VALUES (1, 2), (1, 4);