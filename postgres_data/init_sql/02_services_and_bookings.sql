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
    icon_url VARCHAR(255),
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
    recommended_staff INT NOT NULL DEFAULT 1,
    icon_url VARCHAR(255),
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
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'AWAITING_EMPLOYEE', 'CONFIRMED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
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

ALTER TABLE booking_details
ADD COLUMN selected_choice_ids TEXT;

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