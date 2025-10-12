-- ============================================================
-- Bảng phục vụ chức năng chat realtime giữa khách hàng và nhân viên
-- ============================================================

CREATE TABLE IF NOT EXISTS chat_rooms (
    chat_room_id VARCHAR(36) PRIMARY KEY,
    booking_id VARCHAR(36) NOT NULL UNIQUE REFERENCES bookings(booking_id) ON DELETE CASCADE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS chat_participants (
    chat_participant_id VARCHAR(36) PRIMARY KEY,
    chat_room_id VARCHAR(36) NOT NULL REFERENCES chat_rooms(chat_room_id) ON DELETE CASCADE,
    account_id VARCHAR(36) NOT NULL REFERENCES account(account_id) ON DELETE CASCADE,
    participant_role VARCHAR(20) NOT NULL,
    joined_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE (chat_room_id, account_id)
);

CREATE TABLE IF NOT EXISTS chat_messages (
    chat_message_id VARCHAR(36) PRIMARY KEY,
    chat_room_id VARCHAR(36) NOT NULL REFERENCES chat_rooms(chat_room_id) ON DELETE CASCADE,
    sender_id VARCHAR(36) NOT NULL REFERENCES account(account_id) ON DELETE CASCADE,
    message_type VARCHAR(20) NOT NULL,
    content TEXT,
    attachment_url TEXT,
    reply_to_id VARCHAR(36) REFERENCES chat_messages(chat_message_id) ON DELETE SET NULL,
    is_revoke BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_chat_messages_room_created_at ON chat_messages (chat_room_id, created_at DESC);