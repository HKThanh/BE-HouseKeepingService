-- Migration: Add BookingMedia table for employee check-in/check-out images
-- Date: 2025-11-04

-- Drop existing objects if they exist
DROP TABLE IF EXISTS booking_media CASCADE;

-- Create booking_media table
CREATE TABLE booking_media (
    media_id VARCHAR(255) PRIMARY KEY,
    assignment_id VARCHAR(255) NOT NULL,
    media_url VARCHAR(500) NOT NULL,
    public_id VARCHAR(255),
    media_type VARCHAR(50) NOT NULL CHECK (media_type IN ('CHECK_IN_IMAGE', 'CHECK_OUT_IMAGE', 'PROGRESS_IMAGE', 'OTHER')),
    description VARCHAR(500),
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_booking_media_assignment 
        FOREIGN KEY (assignment_id) 
        REFERENCES assignments(assignment_id) 
        ON DELETE CASCADE
);

-- Create indexes for better query performance
CREATE INDEX idx_booking_media_assignment ON booking_media(assignment_id);
CREATE INDEX idx_booking_media_type ON booking_media(media_type);
CREATE INDEX idx_booking_media_uploaded_at ON booking_media(uploaded_at DESC);
