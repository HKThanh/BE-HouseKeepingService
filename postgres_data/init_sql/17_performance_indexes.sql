-- Migration: Performance Optimization Indexes
-- Date: 2025-11-26
-- Description: Add indexes for better query performance in booking operations

-- ============================================================
-- BOOKING TABLE INDEXES
-- ============================================================

-- Index for booking time range queries (common in conflict detection)
CREATE INDEX IF NOT EXISTS idx_bookings_booking_time 
    ON bookings(booking_time);

-- Index for customer booking lookups and history
CREATE INDEX IF NOT EXISTS idx_bookings_customer_id 
    ON bookings(customer_id);

-- Composite index for customer booking queries with time filter
CREATE INDEX IF NOT EXISTS idx_bookings_customer_time 
    ON bookings(customer_id, booking_time DESC);

-- Index for status-based queries (pending, confirmed, etc.)
CREATE INDEX IF NOT EXISTS idx_bookings_status 
    ON bookings(status);

-- Composite index for customer + status queries
CREATE INDEX IF NOT EXISTS idx_bookings_customer_status 
    ON bookings(customer_id, status);

-- Index for recurring booking lookups
-- (idx_bookings_recurring already exists in 15_add_recurring_booking.sql)
-- CREATE INDEX IF NOT EXISTS idx_bookings_recurring ON bookings(recurring_booking_id);

-- Index for unverified bookings (admin review queue)
CREATE INDEX IF NOT EXISTS idx_bookings_unverified 
    ON bookings(is_verified) WHERE is_verified = FALSE;

-- ============================================================
-- BOOKING DETAILS TABLE INDEXES
-- ============================================================

-- Index for booking details lookup by booking_id (for batch operations)
CREATE INDEX IF NOT EXISTS idx_booking_details_booking_id 
    ON booking_details(booking_id);

-- Composite index for service-based queries
CREATE INDEX IF NOT EXISTS idx_booking_details_service 
    ON booking_details(service_id);

-- ============================================================
-- ASSIGNMENTS TABLE INDEXES
-- ============================================================

-- Index for employee assignment lookups (conflict detection)
CREATE INDEX IF NOT EXISTS idx_assignments_employee_id 
    ON assignments(employee_id);

-- Index for booking detail assignments
CREATE INDEX IF NOT EXISTS idx_assignments_booking_detail 
    ON assignments(booking_detail_id);

-- Composite index for employee assignments with status
CREATE INDEX IF NOT EXISTS idx_assignments_employee_status 
    ON assignments(employee_id, status);

-- ============================================================
-- EMPLOYEE UNAVAILABILITY TABLE INDEXES
-- ============================================================

-- Index for time-based unavailability queries
CREATE INDEX IF NOT EXISTS idx_employee_unavailability_time_range 
    ON employee_unavailability(employee_id, start_time, end_time);

-- ============================================================
-- SERVICE TABLE INDEXES
-- ============================================================

-- Index for active services lookup
CREATE INDEX IF NOT EXISTS idx_service_active 
    ON service(is_active) WHERE is_active = TRUE;

-- ============================================================
-- ADDRESS TABLE INDEXES
-- ============================================================

-- Index for customer address lookups
CREATE INDEX IF NOT EXISTS idx_address_customer_id 
    ON address(customer_id);

-- ============================================================
-- PAYMENTS TABLE INDEXES
-- ============================================================

-- Index for booking payment lookups
CREATE INDEX IF NOT EXISTS idx_payments_booking_id 
    ON payments(booking_id);

-- Index for payment status queries
CREATE INDEX IF NOT EXISTS idx_payments_status 
    ON payments(payment_status);

-- ============================================================
-- COMMENTS
-- ============================================================

COMMENT ON INDEX idx_bookings_booking_time IS 'Speeds up booking time range queries for conflict detection';
COMMENT ON INDEX idx_bookings_customer_id IS 'Speeds up customer booking history lookups';
COMMENT ON INDEX idx_bookings_customer_time IS 'Composite index for paginated customer booking queries';
COMMENT ON INDEX idx_bookings_status IS 'Speeds up status-based booking queries';
COMMENT ON INDEX idx_assignments_employee_id IS 'Critical for employee conflict detection during booking creation';
COMMENT ON INDEX idx_assignments_employee_status IS 'Speeds up employee assignment status queries';
COMMENT ON INDEX idx_employee_unavailability_time_range IS 'Speeds up employee availability checks';
