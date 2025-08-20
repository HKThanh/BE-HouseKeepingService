CREATE EXTENSION "uuid-ossp";

CREATE TABLE account (
    account_id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) CHECK (role IN ('ADMIN', 'EMPLOYEE', 'CUSTOMER')),
    status VARCHAR(20) CHECK (status IN ('ACTIVE', 'INACTIVE')),
    is_admin BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP,
    CONSTRAINT unique_username_password_role UNIQUE (username, password, role)
);

INSERT INTO account (account_id, username, password, role, status, is_admin, created_at, updated_at, last_login) VALUES
(uuid_generate_v4(), 'john_doe', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', 'CUSTOMER', 'ACTIVE', FALSE, '2025-08-14 19:57:00', '2025-08-14 19:57:00', '2025-08-14 18:00:00'),
(uuid_generate_v4(), 'jane_smith', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', 'EMPLOYEE', 'ACTIVE', FALSE, '2025-08-14 19:57:00', '2025-08-14 19:57:00', '2025-08-14 17:30:00'),
(uuid_generate_v4(), 'admin_1', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', 'ADMIN', 'ACTIVE', TRUE, '2025-08-14 19:57:00', '2025-08-14 19:57:00', '2025-08-14 16:45:00'),
(uuid_generate_v4(), 'mary_jones', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', 'CUSTOMER', 'INACTIVE', FALSE, '2025-08-14 19:57:00', '2025-08-14 19:57:00', NULL),
(uuid_generate_v4(), 'jane_smith', '$2a$12$dRX/zeerYun4LF16PRZuzuaaQDv673McBavp3xEciXKezLjSzyyiK', 'CUSTOMER', 'ACTIVE', FALSE, '2025-08-14 19:57:00', '2025-08-14 19:57:00', '2025-08-14 17:30:00');



CREATE TABLE customer (
    customer_id VARCHAR(36) PRIMARY KEY,
    account_id VARCHAR(36) REFERENCES account(account_id) ON DELETE SET NULL,
    avatar VARCHAR(255),
    full_name VARCHAR(100) NOT NULL,
    is_male BOOLEAN,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone_number VARCHAR(20) UNIQUE,
	birthdate DATE,
	address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO customer (customer_id, account_id, avatar, full_name, is_male, email, phone_number, birthdate, address, created_at, updated_at) VALUES
(uuid_generate_v4(), (SELECT account_id FROM account WHERE username = 'john_doe'), 'https://picsum.photos/200', 'John Doe', TRUE, 'john.doe@example.com', '0901234567', '2003-09-10', '123 Nguyen Van Cu, Hanoi', '2025-08-14 19:57:00', '2025-08-14 19:57:00'),
(uuid_generate_v4(), (SELECT account_id FROM account WHERE username = 'mary_jones'), 'https://picsum.photos/200', 'Mary Jones', FALSE, 'mary.jones@example.com', '0909876543', '2003-01-19', '456 Le Loi, Ho Chi Minh City', '2025-08-14 19:57:00', '2025-08-14 19:57:00');


CREATE TABLE employee (
    employee_id VARCHAR(36) PRIMARY KEY,
    account_id VARCHAR(36) REFERENCES account(account_id) ON DELETE CASCADE,
    avatar VARCHAR(255),
    full_name VARCHAR(100) NOT NULL,
    is_male BOOLEAN,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone_number VARCHAR(20) UNIQUE,
	birthdate DATE,
    hired_date DATE,
    skills TEXT,
	address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_employee_account UNIQUE (account_id)
);

INSERT INTO employee (employee_id, account_id, avatar, full_name, is_male, email, phone_number, birthdate, hired_date, skills, address, created_at, updated_at) VALUES
(uuid_generate_v4(), (SELECT account_id FROM account WHERE username = 'jane_smith'), 'https://picsum.photos/200', 'Jane Smith', FALSE, 'jane.smith@example.com', '0912345678', '2003-04-14', '2024-01-15', 'Cleaning, Organizing', '789 Tran Hung Dao, Hanoi', '2025-08-14 19:57:00', '2025-08-14 19:57:00'),
(uuid_generate_v4(), NULL, 'https://picsum.photos/200', 'Bob Wilson', TRUE, 'bob.wilson@examplefieldset.com', '0923456789', '2003-08-10', '2023-06-20', 'Deep Cleaning, Laundry', '101 Pham Van Dong, Da Nang', '2025-08-14 19:57:00', '2025-08-14 19:57:00');


CREATE TABLE admin_profile (
    admin_profile_id VARCHAR(36) PRIMARY KEY,
    account_id VARCHAR(36) REFERENCES account(account_id) ON DELETE CASCADE,
	full_name VARCHAR(100) NOT NULL,
	is_male BOOLEAN,
	address TEXT,
    department VARCHAR(50),
    contact_info VARCHAR(255),
	birthdate DATE,
    hire_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_admin_account UNIQUE (account_id)
);

INSERT INTO admin_profile (admin_profile_id, account_id, full_name, is_male, address, department, contact_info, birthdate, hire_date, created_at, updated_at) VALUES
(uuid_generate_v4(), (SELECT account_id FROM account WHERE username = 'admin_1'), 'Admin One', TRUE, 'Ho Chi Minh City', 'Management', 'admin1@example.com', '1988-09-10', '2023-03-01', '2025-08-14 19:57:00', '2025-08-14 19:57:00'),
(uuid_generate_v4(), NULL, 'Admin Two', FALSE, 'Ho Chi Minh City', 'HR', 'admin2@example.com', '1990-06-23', '2022-09-10', '2025-08-14 19:57:00', '2025-08-14 19:57:00');

-- CREATE TABLE address (
--     address_id BIGINT PRIMARY KEY,
--     customer_id VARCHAR(36) REFERENCES customer(customer_id),
--     street_text TEXT NOT NULL,
--     city TEXT NOT NULL,
--     postal_code VARCHAR(20),
--     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
-- );

-- CREATE TABLE addresses_for_order (
--     address_id BIGINT PRIMARY KEY,
--     order_id VARCHAR(36) REFERENCES order(order_id),
--     street_text TEXT NOT NULL,
--     city TEXT NOT NULL,
--     postal_code VARCHAR(20),
--     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
-- );

CREATE TABLE service (
    service_id INT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2),
    duration DECIMAL(5, 2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE orders (
    order_id VARCHAR(36) PRIMARY KEY,
    customer_id VARCHAR(36) REFERENCES customer(customer_id),
    employee_id VARCHAR(36) REFERENCES employee(employee_id),
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    address TEXT NOT NULL,
    status VARCHAR(20) CHECK (status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    payment_method VARCHAR(20) CHECK (payment_method IN ('CASH', 'TRANSFER', 'MOMO')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE employees_in_order (
    employee_id VARCHAR(36) REFERENCES employee(employee_id),
    order_id VARCHAR(36) REFERENCES orders(order_id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (employee_id, order_id)
);

CREATE TABLE order_service_detail (
    order_id VARCHAR(36) REFERENCES orders(order_id),
    service_id INT REFERENCES service(service_id),
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    required_staff_count INT,
    total_price DECIMAL(10, 2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (order_id, service_id)
);

CREATE TABLE invoice (
    invoice_id INT PRIMARY KEY,
    order_id VARCHAR(36) REFERENCES orders(order_id),
    issue_date DATE,
    total_amount DECIMAL(10, 2),
    payment_method VARCHAR(20) CHECK (payment_method IN ('CASH', 'TRANSFER', 'MOMO')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE schedules (
    schedule_id INT PRIMARY KEY,
    employee_id VARCHAR(36) REFERENCES employee(employee_id),
    day_of_week INT CHECK (day_of_week BETWEEN 0 AND 6),
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    is_every_week BOOLEAN,
    is_every_month BOOLEAN,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE review (
    review_id INT PRIMARY KEY,
    customer_id VARCHAR(36) REFERENCES customer(customer_id),
    order_id VARCHAR(36) REFERENCES orders(order_id),
    employee_id VARCHAR(36) REFERENCES employee(employee_id),
    rating DECIMAL(3, 1) CHECK (rating BETWEEN 0 AND 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE support_ticket (
    ticket_id VARCHAR(36) PRIMARY KEY,
    customer_id VARCHAR(36) REFERENCES customer(customer_id),
    subject VARCHAR(100),
    description TEXT,
    address VARCHAR(255),
    status VARCHAR(20) CHECK (status IN ('OPEN', 'IN_PROGRESS', 'RESOLVED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

