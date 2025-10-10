-- Chat and messaging tables
CREATE TABLE conversations (
    conversation_id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
    booking_id VARCHAR(36) NOT NULL UNIQUE REFERENCES bookings(booking_id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE messages (
    message_id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
    conversation_id VARCHAR(36) NOT NULL REFERENCES conversations(conversation_id) ON DELETE CASCADE,
    sender_account_id VARCHAR(36) NOT NULL REFERENCES account(account_id),
    message_type VARCHAR(20) NOT NULL CHECK (message_type IN ('TEXT', 'IMAGE', 'VIDEO')),
    content TEXT,
    reply_to_message_id VARCHAR(36) REFERENCES messages(message_id) ON DELETE SET NULL,
    is_revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_messages_conversation ON messages(conversation_id);
CREATE INDEX idx_messages_sender ON messages(sender_account_id);
CREATE INDEX idx_messages_reply_to ON messages(reply_to_message_id);

CREATE TABLE message_attachments (
    attachment_id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
    message_id VARCHAR(36) NOT NULL REFERENCES messages(message_id) ON DELETE CASCADE,
    file_url TEXT NOT NULL,
    public_id TEXT NOT NULL,
    content_type VARCHAR(50) NOT NULL,
    file_size BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_message_attachments_message ON message_attachments(message_id);