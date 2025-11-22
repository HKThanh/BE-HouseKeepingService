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
