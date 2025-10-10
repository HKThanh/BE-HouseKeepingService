-- Service catalog and booking core tables
CREATE TABLE service_categories (
    category_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    parent_category_id INT REFERENCES service_categories(category_id),
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
    unit VARCHAR(20) NOT NULL,
    estimated_duration_hours DECIMAL(5, 2),
    recommended_staff INT NOT NULL DEFAULT 1,
    icon_url VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE service
ADD COLUMN category_id INT REFERENCES service_categories(category_id);

CREATE TABLE bookings (
    booking_id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
    customer_id VARCHAR(36) NOT NULL REFERENCES customer(customer_id),
    address_id VARCHAR(36) NOT NULL REFERENCES address(address_id),
    booking_code VARCHAR(10) UNIQUE,
    booking_time TIMESTAMP WITH TIME ZONE NOT NULL,
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
    sub_total DECIMAL(10, 2),
    CONSTRAINT unique_booking_service UNIQUE (booking_id, service_id)
);

ALTER TABLE booking_details
ADD COLUMN selected_choice_ids TEXT;

CREATE TABLE recurring_bookings (
    recurring_id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
    customer_id VARCHAR(36) NOT NULL REFERENCES customer(customer_id),
    address_id VARCHAR(36) NOT NULL REFERENCES address(address_id),
    service_id INT NOT NULL REFERENCES service(service_id),
    frequency_type VARCHAR(20) NOT NULL CHECK (frequency_type IN ('WEEKLY', 'MONTHLY')),
    interval INT DEFAULT 1,
    day_of_week INT,
    day_of_month INT,
    start_time TIME NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    is_active BOOLEAN DEFAULT TRUE
);