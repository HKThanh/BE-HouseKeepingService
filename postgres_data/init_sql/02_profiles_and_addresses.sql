-- User profile and address tables
CREATE TABLE customer (
    customer_id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
    account_id VARCHAR(36) NOT NULL UNIQUE REFERENCES account(account_id) ON DELETE CASCADE,
    avatar VARCHAR(255),
    full_name VARCHAR(100) NOT NULL,
    is_male BOOLEAN,
    email VARCHAR(100) UNIQUE,
    birthdate DATE,
    rating VARCHAR(10) CHECK (rating IN ('LOWEST', 'LOW', 'MEDIUM', 'HIGH', 'HIGHEST')),
    vip_level INT CHECK (vip_level BETWEEN 1 AND 5),
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
    rating VARCHAR(10) CHECK (rating IN ('LOWEST', 'LOW', 'MEDIUM', 'HIGH', 'HIGHEST')),
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