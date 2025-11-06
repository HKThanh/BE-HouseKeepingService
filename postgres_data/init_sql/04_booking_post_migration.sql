-- Add new columns for booking post feature
ALTER TABLE bookings 
ADD COLUMN IF NOT EXISTS title VARCHAR(255),
ADD COLUMN IF NOT EXISTS is_verified BOOLEAN NOT NULL DEFAULT TRUE,
ADD COLUMN IF NOT EXISTS admin_comment TEXT;

-- Remove old single image_url column if exists (migrated to booking_image_urls table)
ALTER TABLE bookings DROP COLUMN IF EXISTS image_url;

-- Create table for multiple image URLs per booking
CREATE TABLE IF NOT EXISTS booking_image_urls (
    booking_id VARCHAR(255) NOT NULL,
    image_url VARCHAR(500),
    CONSTRAINT fk_booking_image_urls_booking 
        FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE CASCADE
);

-- Create index for faster queries
CREATE INDEX IF NOT EXISTS idx_booking_image_urls_booking_id ON booking_image_urls(booking_id);
CREATE INDEX IF NOT EXISTS idx_bookings_is_verified ON bookings(is_verified);
CREATE INDEX IF NOT EXISTS idx_bookings_is_verified_created_at ON bookings(is_verified, created_at DESC);
