-- =====================================================
-- Additional fees & booking_applied_fees
-- =====================================================

CREATE TABLE IF NOT EXISTS additional_fee (
    id VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid()::TEXT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    fee_type VARCHAR(20) NOT NULL, -- PERCENT | FLAT
    value NUMERIC(10,4) NOT NULL,
    is_system_surcharge BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    priority INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE additional_fee IS 'Phụ phí cấu hình bởi admin, bao gồm phí hệ thống (duy nhất) và các phí khác';
COMMENT ON COLUMN additional_fee.fee_type IS 'PERCENT hoặc FLAT';
COMMENT ON COLUMN additional_fee.value IS 'Giá trị phần trăm (0.2 = 20%) hoặc số tiền cố định';
COMMENT ON COLUMN additional_fee.is_system_surcharge IS 'Đánh dấu phí hệ thống; chỉ được phép 1 phí hệ thống active';

-- Partial unique index: chỉ 1 phụ phí hệ thống được active
CREATE UNIQUE INDEX IF NOT EXISTS ux_additional_fee_system_active
    ON additional_fee (is_system_surcharge)
    WHERE is_system_surcharge = TRUE AND active = TRUE;

-- Trigger update timestamp
CREATE OR REPLACE FUNCTION trg_update_additional_fee_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tg_update_additional_fee_timestamp
    BEFORE UPDATE ON additional_fee
    FOR EACH ROW EXECUTE FUNCTION trg_update_additional_fee_timestamp();

-- Booking applied fees (snapshot)
CREATE TABLE IF NOT EXISTS booking_additional_fee (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    booking_id VARCHAR(36) NOT NULL REFERENCES bookings(booking_id) ON DELETE CASCADE,
    fee_name VARCHAR(255) NOT NULL,
    fee_type VARCHAR(20) NOT NULL,
    fee_value NUMERIC(10,4) NOT NULL,
    fee_amount NUMERIC(12,2) NOT NULL,
    is_system_surcharge BOOLEAN NOT NULL DEFAULT FALSE
);

COMMENT ON TABLE booking_additional_fee IS 'Lưu snapshot phụ phí áp dụng cho từng booking';
COMMENT ON COLUMN booking_additional_fee.fee_type IS 'PERCENT hoặc FLAT';
COMMENT ON COLUMN booking_additional_fee.fee_value IS 'Giá trị gốc (phần trăm hoặc số tiền) tại thời điểm áp dụng';
COMMENT ON COLUMN booking_additional_fee.fee_amount IS 'Số tiền tính ra áp dụng vào booking';