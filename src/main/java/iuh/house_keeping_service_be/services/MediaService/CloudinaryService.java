package iuh.house_keeping_service_be.services.MediaService;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public Map<String, Object> upload(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must not be null or empty");
        }

        // Validate file size (max 10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("Kích thước file không được vượt quá 10MB");
        }

        Map<String, Object> options = new HashMap<>();
        String resourceType = determineResourceType(file.getContentType());
        options.put("resource_type", resourceType);

        if (folder != null && !folder.isBlank()) {
            options.put("folder", folder);
        }

        try {
            Uploader uploader = cloudinary.uploader();
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = (Map<String, Object>) uploader.upload(file.getBytes(), options);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("secureUrl", uploadResult.get("secure_url"));
            response.put("publicId", uploadResult.get("public_id"));
            return response;
        } catch (IOException ex) {
            log.error("Error uploading file to Cloudinary", ex);
            throw new RuntimeException("Failed to upload file to Cloudinary", ex);
        }
    }

    private String determineResourceType(String contentType) {
        if (contentType == null) {
            return "auto";
        }

        if (contentType.startsWith("image/")) {
            return "image";
        }

        if (contentType.startsWith("video/")) {
            return "video";
        }

        return "auto";
    }
}