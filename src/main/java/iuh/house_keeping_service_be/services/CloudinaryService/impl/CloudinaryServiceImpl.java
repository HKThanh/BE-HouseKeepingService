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

    @Override
    public CloudinaryUploadResult uploadCustomerAvatar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File tải lên không hợp lệ");
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
}