-- =====================================================
-- Voice Booking Request Table
-- Stores voice-to-text conversion results and metadata
-- for booking creation via voice input
-- =====================================================

CREATE TABLE IF NOT EXISTS voice_booking_request (
    id VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid()::TEXT,
    customer_id VARCHAR(36) NOT NULL,
    audio_file_name VARCHAR(255),
    audio_duration_seconds DECIMAL(10, 2),
    audio_size_bytes BIGINT,
    transcript TEXT NOT NULL,
    confidence_score DECIMAL(5, 4), -- 0.0000 to 1.0000
    processing_time_ms INTEGER NOT NULL,
    hints JSONB, -- JSON hints provided by user for context
    booking_id VARCHAR(36), -- Reference to created booking if successful
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING', -- PENDING, PROCESSING, COMPLETED, FAILED, PARTIAL
    error_message TEXT,
    missing_fields JSONB, -- JSON array of missing required fields
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_voice_booking_customer FOREIGN KEY (customer_id) 
        REFERENCES customer(customer_id) ON DELETE CASCADE,
    CONSTRAINT fk_voice_booking_booking FOREIGN KEY (booking_id) 
        REFERENCES booking(booking_id) ON DELETE SET NULL,
    CONSTRAINT chk_confidence_range CHECK (confidence_score >= 0 AND confidence_score <= 1),
    CONSTRAINT chk_audio_duration CHECK (audio_duration_seconds > 0 AND audio_duration_seconds <= 120),
    CONSTRAINT chk_audio_size CHECK (audio_size_bytes > 0 AND audio_size_bytes <= 5242880) -- 5MB max
);

-- Index for customer queries
CREATE INDEX IF NOT EXISTS idx_voice_booking_customer ON voice_booking_request(customer_id);

-- Index for status queries
CREATE INDEX IF NOT EXISTS idx_voice_booking_status ON voice_booking_request(status);

-- Index for booking reference
CREATE INDEX IF NOT EXISTS idx_voice_booking_booking ON voice_booking_request(booking_id);

-- Index for timestamp queries
CREATE INDEX IF NOT EXISTS idx_voice_booking_created ON voice_booking_request(created_at DESC);

-- Auto-update timestamp trigger
CREATE OR REPLACE FUNCTION update_voice_booking_request_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_voice_booking_request_timestamp
    BEFORE UPDATE ON voice_booking_request
    FOR EACH ROW
    EXECUTE FUNCTION update_voice_booking_request_timestamp();

-- Comments for documentation
COMMENT ON TABLE voice_booking_request IS 'Stores voice booking requests with transcript and processing metadata';
COMMENT ON COLUMN voice_booking_request.transcript IS 'Raw text output from Whisper voice-to-text service';
COMMENT ON COLUMN voice_booking_request.confidence_score IS 'Whisper confidence score (0-1), if available';
COMMENT ON COLUMN voice_booking_request.processing_time_ms IS 'Total time taken for voice-to-text processing in milliseconds';
COMMENT ON COLUMN voice_booking_request.hints IS 'Optional JSON hints provided by user (e.g., preferred service, time range)';
COMMENT ON COLUMN voice_booking_request.missing_fields IS 'JSON array of fields that could not be extracted from transcript';
COMMENT ON COLUMN voice_booking_request.status IS 'Processing status: PENDING, PROCESSING, COMPLETED, FAILED, PARTIAL';
