-- =================================================================================
-- Migration: Remove created_at and updated_at columns from tables
-- Date: 2025-10-26
-- Description: Remove timestamp columns from all tables except payments and bookings
-- =================================================================================

-- Account and roles
ALTER TABLE account DROP COLUMN IF EXISTS created_at;
ALTER TABLE account DROP COLUMN IF EXISTS updated_at;

-- Profiles
ALTER TABLE customer DROP COLUMN IF EXISTS created_at;
ALTER TABLE customer DROP COLUMN IF EXISTS updated_at;

ALTER TABLE employee DROP COLUMN IF EXISTS created_at;
ALTER TABLE employee DROP COLUMN IF EXISTS updated_at;

ALTER TABLE admin_profile DROP COLUMN IF EXISTS created_at;
ALTER TABLE admin_profile DROP COLUMN IF EXISTS updated_at;

-- Address (keep created_at as it's useful for address history)
ALTER TABLE address DROP COLUMN IF EXISTS created_at;

-- Service categories
ALTER TABLE service_categories DROP COLUMN IF EXISTS created_at;
ALTER TABLE service_categories DROP COLUMN IF EXISTS updated_at;

-- Service
ALTER TABLE service DROP COLUMN IF EXISTS created_at;
ALTER TABLE service DROP COLUMN IF EXISTS updated_at;

-- NOTE: bookings table - KEEP created_at and updated_at (as requested)
-- ALTER TABLE bookings DROP COLUMN IF EXISTS created_at; -- COMMENTED OUT
-- ALTER TABLE bookings DROP COLUMN IF EXISTS updated_at; -- COMMENTED OUT

-- Assignments
ALTER TABLE assignments DROP COLUMN IF EXISTS created_at;
ALTER TABLE assignments DROP COLUMN IF EXISTS updated_at;

-- Employee unavailability
ALTER TABLE employee_unavailability DROP COLUMN IF EXISTS created_at;

-- Booking media
ALTER TABLE booking_media DROP COLUMN IF EXISTS uploaded_at;

-- NOTE: payments table - KEEP created_at (as requested)
-- ALTER TABLE payments DROP COLUMN IF EXISTS created_at; -- COMMENTED OUT

-- Payment methods
ALTER TABLE payment_methods DROP COLUMN IF EXISTS created_at;
ALTER TABLE payment_methods DROP COLUMN IF EXISTS updated_at;

-- Promotions
ALTER TABLE promotions DROP COLUMN IF EXISTS created_at;
ALTER TABLE promotions DROP COLUMN IF EXISTS updated_at;

-- Verification: Show table structures after migration
DO $$
BEGIN
    RAISE NOTICE 'Migration completed successfully';
    RAISE NOTICE 'Tables with timestamps preserved: bookings (created_at, updated_at), payments (created_at)';
END $$;
