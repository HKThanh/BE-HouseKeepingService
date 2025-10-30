-- =============================================
-- Notifications Table Schema
-- =============================================

CREATE TABLE IF NOT EXISTS notifications (
    notification_id VARCHAR(36) PRIMARY KEY,
    account_id VARCHAR(36) NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    related_id VARCHAR(36),
    related_type VARCHAR(50),
    is_read BOOLEAN DEFAULT FALSE NOT NULL,
    read_at TIMESTAMP,
    priority VARCHAR(20) DEFAULT 'NORMAL' NOT NULL,
    action_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_notification_account 
        FOREIGN KEY (account_id) 
        REFERENCES account(account_id) 
        ON DELETE CASCADE,
    
    CONSTRAINT chk_notification_type 
        CHECK (type IN (
            'BOOKING_CREATED',
            'BOOKING_CONFIRMED',
            'BOOKING_CANCELLED',
            'BOOKING_COMPLETED',
            'BOOKING_VERIFIED',
            'BOOKING_REJECTED',
            'ASSIGNMENT_CREATED',
            'ASSIGNMENT_CANCELLED',
            'ASSIGNMENT_CRISIS',
            'PAYMENT_SUCCESS',
            'PAYMENT_FAILED',
            'REVIEW_RECEIVED',
            'SYSTEM_ANNOUNCEMENT',
            'PROMOTION_AVAILABLE'
        )),
    
    CONSTRAINT chk_related_type 
        CHECK (related_type IN (
            'BOOKING',
            'ASSIGNMENT',
            'PAYMENT',
            'REVIEW',
            'PROMOTION',
            'SYSTEM'
        ) OR related_type IS NULL),
    
    CONSTRAINT chk_priority 
        CHECK (priority IN ('LOW', 'NORMAL', 'HIGH', 'URGENT'))
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_notifications_account_id ON notifications(account_id);
CREATE INDEX IF NOT EXISTS idx_notifications_is_read ON notifications(is_read);
CREATE INDEX IF NOT EXISTS idx_notifications_created_at ON notifications(created_at);
CREATE INDEX IF NOT EXISTS idx_notifications_account_unread ON notifications(account_id, is_read) WHERE is_read = FALSE;
CREATE INDEX IF NOT EXISTS idx_notifications_type ON notifications(type);
CREATE INDEX IF NOT EXISTS idx_notifications_priority ON notifications(priority);
CREATE INDEX IF NOT EXISTS idx_notifications_related ON notifications(related_id, related_type);

-- Add comment to table
COMMENT ON TABLE notifications IS 'Stores system notifications for users';
COMMENT ON COLUMN notifications.notification_id IS 'Unique identifier for the notification';
COMMENT ON COLUMN notifications.account_id IS 'Reference to the account receiving the notification';
COMMENT ON COLUMN notifications.type IS 'Type of notification (BOOKING_CREATED, PAYMENT_SUCCESS, etc.)';
COMMENT ON COLUMN notifications.title IS 'Short title/subject of the notification';
COMMENT ON COLUMN notifications.message IS 'Detailed message content';
COMMENT ON COLUMN notifications.related_id IS 'ID of related entity (booking ID, payment ID, etc.)';
COMMENT ON COLUMN notifications.related_type IS 'Type of related entity (BOOKING, PAYMENT, etc.)';
COMMENT ON COLUMN notifications.is_read IS 'Flag indicating if notification has been read';
COMMENT ON COLUMN notifications.read_at IS 'Timestamp when notification was marked as read';
COMMENT ON COLUMN notifications.priority IS 'Priority level (LOW, NORMAL, HIGH, URGENT)';
COMMENT ON COLUMN notifications.action_url IS 'URL to navigate when notification is clicked';
COMMENT ON COLUMN notifications.created_at IS 'Timestamp when notification was created';
COMMENT ON COLUMN notifications.updated_at IS 'Timestamp when notification was last updated';
