-- Seed data: 93 Seed Media

-- Khối III: Thêm dữ liệu cho Media
-- =================================================================================

-- Thêm media (ảnh Check-in & Check-out) cho assignments đã hoàn thành
INSERT INTO booking_media (media_id, assignment_id, media_url, public_id, media_type, description) VALUES
('media001-0000-0000-0000-000000000001', 'as000001-0000-0000-0000-000000000001', 'https://res.cloudinary.com/demo/image/upload/booking_images/checkin_job1.jpg', 'booking_images/checkin_job1', 'CHECK_IN_IMAGE', 'Ảnh trước khi bắt đầu công việc'),
('media002-0000-0000-0000-000000000001', 'as000001-0000-0000-0000-000000000001', 'https://res.cloudinary.com/demo/image/upload/booking_images/checkout_job1.jpg', 'booking_images/checkout_job1', 'CHECK_OUT_IMAGE', 'Ảnh sau khi hoàn thành công việc');

-- =================================================================================
