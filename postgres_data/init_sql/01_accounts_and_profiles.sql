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
    ward VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL,
    PRIMARY KEY (employee_id, ward, city)
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
    city VARCHAR(100),
    latitude DECIMAL(9, 6),
    longitude DECIMAL(9, 6),
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
