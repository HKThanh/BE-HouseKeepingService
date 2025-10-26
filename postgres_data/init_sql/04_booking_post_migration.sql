-- Add new columns for booking post feature
ALTER TABLE bookings 
ADD COLUMN IF NOT EXISTS title VARCHAR(255),
ADD COLUMN IF NOT EXISTS image_url VARCHAR(500),
ADD COLUMN IF NOT EXISTS is_verified BOOLEAN NOT NULL DEFAULT TRUE,
ADD COLUMN IF NOT EXISTS admin_comment TEXT;

-- Create index for faster queries on unverified bookings
CREATE INDEX IF NOT EXISTS idx_bookings_is_verified ON bookings(is_verified);
CREATE INDEX IF NOT EXISTS idx_bookings_is_verified_created_at ON bookings(is_verified, created_at DESC);
