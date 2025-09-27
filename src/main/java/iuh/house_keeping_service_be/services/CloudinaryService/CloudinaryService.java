package iuh.house_keeping_service_be.services.CloudinaryService;

import iuh.house_keeping_service_be.dtos.Cloudinary.CloudinaryUploadResult;
import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {
    CloudinaryUploadResult uploadCustomerAvatar(MultipartFile file);

    CloudinaryUploadResult uploadEmployeeAvatar(MultipartFile file);
}