package iuh.house_keeping_service_be.services.CloudinaryService.impl;

import com.cloudinary.Cloudinary;
import iuh.house_keeping_service_be.dtos.Cloudinary.CloudinaryUploadResult;
import iuh.house_keeping_service_be.services.CloudinaryService.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    @Value("${cloudinary.folders.customer:customer_avatars}")
    private String customerFolder;

    @Value("${cloudinary.folders.employee:employee_avatars}")
    private String employeeFolder;

    @Value("${cloudinary.folders.booking:booking_images}")
    private String bookingFolder;

    @Override
    public CloudinaryUploadResult uploadCustomerAvatar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File tải lên không hợp lệ");
        }
        
        // Validate file size (max 10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("Kích thước file không được vượt quá 10MB");
        }
        
        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File phải là định dạng ảnh");
        }
        
        try {
            Map<String, Object> options = new HashMap<>();
            if (customerFolder != null && !customerFolder.isBlank()) {
                options.put("folder", customerFolder);
            }
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), options);
            String secureUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");
            if (secureUrl == null || publicId == null) {
                throw new IllegalStateException("Kết quả tải lên Cloudinary không hợp lệ");
            }
            return new CloudinaryUploadResult(secureUrl, publicId);
        } catch (IOException e) {
            throw new RuntimeException("Không thể tải ảnh lên Cloudinary", e);
        }
    }

    @Override
    public CloudinaryUploadResult uploadEmployeeAvatar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File tải lên không hợp lệ");
        }
        
        // Validate file size (max 10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("Kích thước file không được vượt quá 10MB");
        }
        
        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File phải là định dạng ảnh");
        }
        
        try {
            Map<String, Object> options = new HashMap<>();
            if (employeeFolder != null && !employeeFolder.isBlank()) {
                options.put("folder", employeeFolder);
            }
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), options);
            String secureUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");
            if (secureUrl == null || publicId == null) {
                throw new IllegalStateException("Kết quả tải lên Cloudinary không hợp lệ");
            }
            return new CloudinaryUploadResult(secureUrl, publicId);
        } catch (IOException e) {
            throw new RuntimeException("Không thể tải ảnh lên Cloudinary", e);
        }
    }

    @Override
    public CloudinaryUploadResult uploadBookingImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File tải lên không hợp lệ");
        }
        
        // Validate file size (max 10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("Kích thước file không được vượt quá 10MB");
        }
        
        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File phải là định dạng ảnh");
        }
        
        try {
            Map<String, Object> options = new HashMap<>();
            if (bookingFolder != null && !bookingFolder.isBlank()) {
                options.put("folder", bookingFolder);
            }
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), options);
            String secureUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");
            if (secureUrl == null || publicId == null) {
                throw new IllegalStateException("Kết quả tải lên Cloudinary không hợp lệ");
            }
            return new CloudinaryUploadResult(secureUrl, publicId);
        } catch (IOException e) {
            throw new RuntimeException("Không thể tải ảnh lên Cloudinary", e);
        }
    }

    @Override
    public void deleteImage(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            throw new IllegalArgumentException("Public ID không hợp lệ");
        }
        try {
            cloudinary.uploader().destroy(publicId, Map.of());
        } catch (IOException e) {
            throw new RuntimeException("Không thể xóa ảnh từ Cloudinary", e);
        }
    }
}