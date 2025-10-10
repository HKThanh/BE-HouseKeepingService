-- Payments, reviews, support, and promotions
CREATE TABLE payments (
    payment_id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
    booking_id VARCHAR(36) NOT NULL REFERENCES bookings(booking_id),
    amount DECIMAL(10, 2) NOT NULL,
    payment_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (payment_status IN ('PENDING', 'PAID', 'FAILED', 'CANCELLED', 'REFUNDED')),
    transaction_code VARCHAR(100),
    paid_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE payment_methods (
     method_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
     method_code VARCHAR(20) UNIQUE NOT NULL,
     method_name VARCHAR(100) NOT NULL,
     is_active BOOLEAN DEFAULT TRUE,
     description TEXT,
     created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
     updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE payments
ADD COLUMN method_id INT REFERENCES payment_methods(method_id);

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
     criteria_name VARCHAR(100) UNIQUE NOT NULL
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
    max_discount_amount DECIMAL(10, 2),
    start_date TIMESTAMP WITH TIME ZONE,
    end_date TIMESTAMP WITH TIME ZONE,
    usage_limit INT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE bookings
ADD COLUMN promotion_id INT REFERENCES promotions(promotion_id);