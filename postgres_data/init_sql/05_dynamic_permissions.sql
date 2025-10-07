-- KHỐI V: HỆ THỐNG PHÂN QUYỀN ĐỘNG (DYNAMIC PERMISSIONS)
-- Các bảng mới để hỗ trợ ý tưởng phân quyền linh hoạt cho Admin.
-- =================================================================================

-- Bảng định nghĩa tất cả các chức năng có trong hệ thống
CREATE TABLE features (
                          feature_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
                          feature_name VARCHAR(100) UNIQUE NOT NULL, -- Tên định danh (code) của chức năng, Vd: 'booking.create'

                          description TEXT, -- Mô tả thân thiện, Vd: 'Tạo một lịch đặt mới'
                          module VARCHAR(50) -- Gom nhóm chức năng theo module, Vd: 'Booking', 'Account', 'Payment'
);

-- Bảng trung gian để gán quyền (chức năng) cho vai trò và quản lý trạng thái
CREATE TABLE role_features (
                               role_id INT NOT NULL REFERENCES roles(role_id) ON DELETE CASCADE,
                               feature_id INT NOT NULL REFERENCES features(feature_id) ON DELETE CASCADE,
                               is_enabled BOOLEAN DEFAULT TRUE, -- TRUE: chức năng được phép, FALSE: chức năng bị vô hiệu hóa
                               PRIMARY KEY (role_id, feature_id)
);

-- =================================================================================