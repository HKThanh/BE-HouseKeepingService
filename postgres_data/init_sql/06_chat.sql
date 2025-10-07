-- =================================================================================
-- KHỐI VII: CHAT REAL-TIME GIỮA CUSTOMER VÀ EMPLOYEE
-- =================================================================================

-- Bảng lưu trữ thông tin cuộc hội thoại giữa một nhân viên và một khách hàng.
CREATE TABLE conversations (
                               conversation_id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
                               employee_id VARCHAR(36) NOT NULL REFERENCES employee(employee_id) ON DELETE CASCADE,
                               customer_id VARCHAR(36) NOT NULL REFERENCES customer(customer_id) ON DELETE CASCADE,
                               created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                               last_message_at TIMESTAMP WITH TIME ZONE,
                               UNIQUE (employee_id, customer_id)
);

CREATE INDEX idx_conversations_employee ON conversations (employee_id);
CREATE INDEX idx_conversations_customer ON conversations (customer_id);
CREATE INDEX idx_conversations_last_message ON conversations (last_message_at DESC NULLS LAST);

-- Bảng lưu trữ chi tiết từng tin nhắn thuộc một cuộc hội thoại.
CREATE TABLE chat_messages (
                               message_id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
                               conversation_id VARCHAR(36) NOT NULL REFERENCES conversations(conversation_id) ON DELETE CASCADE,
                               sender_account_id VARCHAR(36) NOT NULL REFERENCES account(account_id) ON DELETE CASCADE,
                               message_type VARCHAR(20) NOT NULL CHECK (message_type IN ('TEXT', 'FILE')),
                               content TEXT,
                               file_url VARCHAR(500),
                               file_name VARCHAR(255),
                               file_size BIGINT,
                               is_revoked BOOLEAN NOT NULL DEFAULT FALSE,
                               revoked_at TIMESTAMP WITH TIME ZONE,
                               reply_to_message_id VARCHAR(36) REFERENCES chat_messages(message_id) ON DELETE SET NULL,
                               sent_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_chat_messages_conversation ON chat_messages (conversation_id);
CREATE INDEX idx_chat_messages_sender ON chat_messages (sender_account_id);
CREATE INDEX idx_chat_messages_sent_at ON chat_messages (sent_at DESC);
CREATE INDEX idx_chat_messages_reply_to ON chat_messages (reply_to_message_id);