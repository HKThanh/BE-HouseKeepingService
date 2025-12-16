-- Migration: Add coordinates for check-in and check-out locations in assignments table
-- Date: 2024-12-16
-- Description: Adds latitude and longitude fields to track employee check-in and check-out locations

-- Add check-in coordinate columns
ALTER TABLE assignments ADD COLUMN IF NOT EXISTS check_in_latitude DOUBLE PRECISION;
ALTER TABLE assignments ADD COLUMN IF NOT EXISTS check_in_longitude DOUBLE PRECISION;

-- Add check-out coordinate columns
ALTER TABLE assignments ADD COLUMN IF NOT EXISTS check_out_latitude DOUBLE PRECISION;
ALTER TABLE assignments ADD COLUMN IF NOT EXISTS check_out_longitude DOUBLE PRECISION;

-- Add comments for documentation
COMMENT ON COLUMN assignments.check_in_latitude IS 'Latitude coordinate when employee checks in';
COMMENT ON COLUMN assignments.check_in_longitude IS 'Longitude coordinate when employee checks in';
COMMENT ON COLUMN assignments.check_out_latitude IS 'Latitude coordinate when employee checks out';
COMMENT ON COLUMN assignments.check_out_longitude IS 'Longitude coordinate when employee checks out';
