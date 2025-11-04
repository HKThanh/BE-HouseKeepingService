-- Migration: Add BookingMedia table for employee check-in/check-out images
-- Date: 2025-11-04

-- Drop existing objects if they exist
DROP TABLE IF EXISTS booking_media CASCADE;
DROP TYPE IF EXISTS media_type CASCADE;

-- Create enum for media types
CREATE TYPE media_type AS ENUM ('CHECK_IN_IMAGE', 'CHECK_OUT_IMAGE', 'PROGRESS_IMAGE', 'OTHER');

-- Create booking_media table
CREATE TABLE booking_media (
    media_id VARCHAR(255) PRIMARY KEY,
    assignment_id VARCHAR(255) NOT NULL,
    media_url VARCHAR(500) NOT NULL,
    public_id VARCHAR(255),
    media_type media_type NOT NULL,
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

-- Add comments
COMMENT ON TABLE booking_media IS 'Stores images uploaded by employees during check-in and check-out';
COMMENT ON COLUMN booking_media.media_id IS 'Primary key';
COMMENT ON COLUMN booking_media.assignment_id IS 'Foreign key to assignments table';
COMMENT ON COLUMN booking_media.media_url IS 'Cloudinary secure URL of the uploaded image';
COMMENT ON COLUMN booking_media.public_id IS 'Cloudinary public ID for deletion';
COMMENT ON COLUMN booking_media.media_type IS 'Type of media: CHECK_IN_IMAGE, CHECK_OUT_IMAGE, PROGRESS_IMAGE, OTHER';
COMMENT ON COLUMN booking_media.description IS 'Optional description provided by employee';
COMMENT ON COLUMN booking_media.uploaded_at IS 'Timestamp when the image was uploaded';
