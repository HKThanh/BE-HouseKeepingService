CREATE TABLE IF NOT EXISTS chat_rooms (
    chat_room_id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
    assignment_id VARCHAR(36) NOT NULL REFERENCES assignments(assignment_id) ON DELETE CASCADE,
    customer_account_id VARCHAR(36) NOT NULL REFERENCES account(account_id) ON DELETE CASCADE,
    employee_account_id VARCHAR(36) NOT NULL REFERENCES account(account_id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_message_at TIMESTAMP WITHOUT TIME ZONE
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_chat_rooms_assignment_id ON chat_rooms (assignment_id);
CREATE INDEX IF NOT EXISTS idx_chat_rooms_customer ON chat_rooms (customer_account_id);
CREATE INDEX IF NOT EXISTS idx_chat_rooms_employee ON chat_rooms (employee_account_id);

CREATE TABLE IF NOT EXISTS chat_messages (
    chat_message_id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
    chat_room_id VARCHAR(36) NOT NULL REFERENCES chat_rooms(chat_room_id) ON DELETE CASCADE,
    sender_account_id VARCHAR(36) NOT NULL REFERENCES account(account_id) ON DELETE CASCADE,
    message TEXT NOT NULL,
    sent_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP WITH TIME ZONE,
    read_by_account_id VARCHAR(36),
);

CREATE INDEX IF NOT EXISTS idx_chat_messages_room_id ON chat_messages (chat_room_id);
CREATE INDEX IF NOT EXISTS idx_chat_messages_sender ON chat_messages (sender_account_id);