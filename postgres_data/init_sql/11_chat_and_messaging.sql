-- =====================================================
-- 11. Chat and Messaging Tables
-- =====================================================

-- Create conversations table
CREATE TABLE IF NOT EXISTS conversations (
    conversation_id VARCHAR(36) PRIMARY KEY,
    customer_id VARCHAR(36) NOT NULL,
    employee_id VARCHAR(36),
    booking_id VARCHAR(36),
    last_message TEXT,
    last_message_time TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_conversation_customer FOREIGN KEY (customer_id) REFERENCES customer(customer_id),
    CONSTRAINT fk_conversation_employee FOREIGN KEY (employee_id) REFERENCES employee(employee_id),
    CONSTRAINT fk_conversation_booking FOREIGN KEY (booking_id) REFERENCES bookings(booking_id)
);

-- Create chat_messages table
CREATE TABLE IF NOT EXISTS chat_messages (
    message_id VARCHAR(36) PRIMARY KEY,
    conversation_id VARCHAR(36) NOT NULL,
    sender_id VARCHAR(36) NOT NULL,
    message_type VARCHAR(20) NOT NULL DEFAULT 'TEXT',
    content TEXT,
    image_url VARCHAR(500),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_message_conversation FOREIGN KEY (conversation_id) REFERENCES conversations(conversation_id) ON DELETE CASCADE,
    CONSTRAINT fk_message_sender FOREIGN KEY (sender_id) REFERENCES account(account_id),
    CONSTRAINT chk_message_type CHECK (message_type IN ('TEXT', 'IMAGE'))
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_conversations_customer ON conversations(customer_id);
CREATE INDEX IF NOT EXISTS idx_conversations_employee ON conversations(employee_id);
CREATE INDEX IF NOT EXISTS idx_conversations_booking ON conversations(booking_id);
CREATE INDEX IF NOT EXISTS idx_conversations_last_message_time ON conversations(last_message_time DESC);
CREATE INDEX IF NOT EXISTS idx_conversations_is_active ON conversations(is_active);

CREATE INDEX IF NOT EXISTS idx_chat_messages_conversation ON chat_messages(conversation_id);
CREATE INDEX IF NOT EXISTS idx_chat_messages_sender ON chat_messages(sender_id);
CREATE INDEX IF NOT EXISTS idx_chat_messages_created_at ON chat_messages(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_chat_messages_is_read ON chat_messages(is_read);
CREATE INDEX IF NOT EXISTS idx_chat_messages_conversation_created ON chat_messages(conversation_id, created_at DESC);

-- Add comments
COMMENT ON TABLE conversations IS 'Stores conversation threads between customers and employees';
COMMENT ON TABLE chat_messages IS 'Stores individual chat messages within conversations';
COMMENT ON COLUMN conversations.is_active IS 'Indicates if the conversation is active or archived';
COMMENT ON COLUMN chat_messages.message_type IS 'Type of message: TEXT or IMAGE';
COMMENT ON COLUMN chat_messages.is_read IS 'Indicates if the message has been read by the recipient';
