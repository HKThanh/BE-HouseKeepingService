-- DEPRECATED: This table definition has been moved to 12_booking_media.sql
-- DO NOT USE THIS - kept for reference only
/*
CREATE TABLE booking_media (
    media_id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
    booking_id VARCHAR(36) NOT NULL REFERENCES bookings(booking_id) ON DELETE CASCADE,
    assignment_id VARCHAR(36) REFERENCES assignments(assignment_id),
    media_url VARCHAR(255) NOT NULL,
    media_type VARCHAR(20) NOT NULL CHECK (media_type IN ('BEFORE', 'AFTER')),
    uploaded_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
*/