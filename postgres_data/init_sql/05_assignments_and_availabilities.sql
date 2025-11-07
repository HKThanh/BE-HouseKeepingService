-- Assignment tracking tables
CREATE TABLE assignments (
    assignment_id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
    booking_detail_id VARCHAR(36) NOT NULL REFERENCES booking_details(booking_detail_id) ON DELETE CASCADE,
    employee_id VARCHAR(36) NOT NULL REFERENCES employee(employee_id),
    status VARCHAR(20) DEFAULT 'ASSIGNED' CHECK (status IN ('PENDING', 'ASSIGNED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'NO_SHOW')),
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
    reason TEXT,
    is_approved BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);