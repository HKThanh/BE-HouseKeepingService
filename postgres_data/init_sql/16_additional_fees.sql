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
    id SERIAL PRIMARY KEY,
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

-- Seed: mặc định phí hệ thống 20%, và một số phí mẫu
INSERT INTO additional_fee (id, name, description, fee_type, value, is_system_surcharge, active, priority)
VALUES
    ('fee-system-20', 'Phí hệ thống', 'Phụ phí mặc định 20% tính trên dịch vụ', 'PERCENT', 0.20, TRUE, TRUE, 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO additional_fee (id, name, description, fee_type, value, is_system_surcharge, active, priority)
VALUES
    ('fee-peak-10', 'Phụ phí cao điểm', 'Áp dụng cho khung giờ cao điểm', 'PERCENT', 0.10, FALSE, TRUE, 1),
    ('fee-transport-50k', 'Phí di chuyển', 'Phí cố định cho khu vực xa', 'FLAT', 50000, FALSE, TRUE, 2)
ON CONFLICT (id) DO NOTHING;

-- Demo áp dụng phí cho một số booking mẫu nếu tồn tại (dùng booking_id đã seed)
-- Booking 1: BK000001 subtotal 80,000 -> phí hệ thống 16,000; phụ phí cao điểm 8,000
INSERT INTO booking_additional_fee (booking_id, fee_name, fee_type, fee_value, fee_amount, is_system_surcharge)
VALUES
('b0000001-0000-0000-0000-000000000001', 'Phí hệ thống', 'PERCENT', 0.20, 16000, TRUE),
('b0000001-0000-0000-0000-000000000001', 'Phụ phí cao điểm', 'PERCENT', 0.10, 8000, FALSE)
ON CONFLICT DO NOTHING;

-- Booking 2: BK000002 subtotal 90,000 -> phí hệ thống 18,000; phí di chuyển 50,000
INSERT INTO booking_additional_fee (booking_id, fee_name, fee_type, fee_value, fee_amount, is_system_surcharge)
VALUES
('b0000001-0000-0000-0000-000000000002', 'Phí hệ thống', 'PERCENT', 0.20, 18000, TRUE),
('b0000001-0000-0000-0000-000000000002', 'Phí di chuyển', 'FLAT', 50000, 50000, FALSE)
ON CONFLICT DO NOTHING;

-- Booking 8: BK000008 subtotal 200,000 -> phí hệ thống 40,000
INSERT INTO booking_additional_fee (booking_id, fee_name, fee_type, fee_value, fee_amount, is_system_surcharge)
VALUES
('b0000001-0000-0000-0000-000000000008', 'Phí hệ thống', 'PERCENT', 0.20, 40000, TRUE)
ON CONFLICT DO NOTHING;
