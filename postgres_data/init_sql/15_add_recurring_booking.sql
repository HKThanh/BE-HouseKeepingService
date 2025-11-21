-- Migration: Add Recurring Booking Feature
-- Date: 2025-11-18
-- Description: Add tables and columns for recurring booking functionality

-- 1. Create recurring_bookings table
CREATE TABLE IF NOT EXISTS recurring_bookings (
    recurring_booking_id VARCHAR(36) PRIMARY KEY,
    customer_id VARCHAR(36) NOT NULL,
    address_id VARCHAR(36) NOT NULL,
    recurrence_type VARCHAR(20) NOT NULL CHECK (recurrence_type IN ('WEEKLY', 'MONTHLY')),
    recurrence_days VARCHAR(100) NOT NULL, -- Comma-separated values
    booking_time TIME NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    note TEXT,
    title VARCHAR(255),
    promotion_id INT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'CANCELLED', 'COMPLETED')),
    cancelled_at TIMESTAMP,
    cancellation_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customer(customer_id) ON DELETE CASCADE,
    FOREIGN KEY (address_id) REFERENCES address(address_id) ON DELETE CASCADE,
    FOREIGN KEY (promotion_id) REFERENCES promotions(promotion_id) ON DELETE SET NULL
);

-- 2. Create recurring_booking_details table
CREATE TABLE IF NOT EXISTS recurring_booking_details (
    recurring_booking_detail_id VARCHAR(36) PRIMARY KEY,
    recurring_booking_id VARCHAR(36) NOT NULL,
    service_id INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    price_per_unit DECIMAL(10, 2),
    selected_choice_ids TEXT,
    FOREIGN KEY (recurring_booking_id) REFERENCES recurring_bookings(recurring_booking_id) ON DELETE CASCADE,
    FOREIGN KEY (service_id) REFERENCES service(service_id) ON DELETE CASCADE
);

-- 3. Add recurring_booking_id column to bookings table
ALTER TABLE bookings 
ADD COLUMN recurring_booking_id VARCHAR(36),
ADD FOREIGN KEY (recurring_booking_id) REFERENCES recurring_bookings(recurring_booking_id) ON DELETE SET NULL;

-- 3b. Add FK from conversations to recurring_bookings (column exists from chat schema)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_conversation_recurring_booking'
          AND table_name = 'conversations'
    ) THEN
        ALTER TABLE conversations
        ADD CONSTRAINT fk_conversation_recurring_booking
        FOREIGN KEY (recurring_booking_id)
        REFERENCES recurring_bookings(recurring_booking_id)
        ON DELETE SET NULL;
    END IF;
END$$;

-- 4. Create indexes for better performance
CREATE INDEX idx_recurring_bookings_customer ON recurring_bookings(customer_id);
CREATE INDEX idx_recurring_bookings_status ON recurring_bookings(status);
CREATE INDEX idx_recurring_bookings_dates ON recurring_bookings(start_date, end_date);
CREATE INDEX idx_recurring_bookings_status_dates ON recurring_bookings(status, start_date, end_date);
CREATE INDEX idx_recurring_booking_details_recurring ON recurring_booking_details(recurring_booking_id);
CREATE INDEX idx_bookings_recurring ON bookings(recurring_booking_id);

-- 5. Create comments for documentation
COMMENT ON TABLE recurring_bookings IS 'Stores recurring booking schedules (weekly or monthly)';
COMMENT ON TABLE recurring_booking_details IS 'Stores service details for recurring bookings';
COMMENT ON COLUMN recurring_bookings.recurrence_type IS 'WEEKLY or MONTHLY';
COMMENT ON COLUMN recurring_bookings.recurrence_days IS 'Comma-separated values: For WEEKLY: 1-7 (Mon-Sun), For MONTHLY: 1-31';
COMMENT ON COLUMN recurring_bookings.status IS 'ACTIVE, CANCELLED, or COMPLETED';
COMMENT ON COLUMN bookings.recurring_booking_id IS 'Links to the parent recurring booking if this booking was auto-generated';
COMMENT ON COLUMN conversations.recurring_booking_id IS 'Conversation tied to the recurring booking (single thread per recurring series)';
