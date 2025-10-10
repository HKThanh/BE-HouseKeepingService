-- Checklist templates and booking media
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