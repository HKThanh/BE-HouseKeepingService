-- =====================================================
-- Voice Booking confirmation step support
-- Adds columns for draft booking request & preview payload
-- =====================================================

ALTER TABLE IF EXISTS voice_booking_request
    ADD COLUMN IF NOT EXISTS draft_booking_request JSONB,
    ADD COLUMN IF NOT EXISTS preview_payload JSONB;

COMMENT ON COLUMN voice_booking_request.draft_booking_request IS 'Serialized BookingCreateRequest waiting for customer confirmation';
COMMENT ON COLUMN voice_booking_request.preview_payload IS 'Voice booking preview information shown to customer before saving';
