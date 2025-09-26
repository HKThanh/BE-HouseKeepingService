package iuh.house_keeping_service_be.controllers;

import iuh.house_keeping_service_be.services.MediaService.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
@Slf4j
public class MediaController {

    private final CloudinaryService cloudinaryService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadMedia(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", required = false) String folder
    ) {
        try {
            Map<String, Object> uploadResult = cloudinaryService.upload(file, folder);
            return ResponseEntity.ok(uploadResult);
        } catch (IllegalArgumentException ex) {
            log.warn("Invalid upload request: {}", ex.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", ex.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (RuntimeException ex) {
            log.error("Failed to upload media", ex);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to upload media");
            return ResponseEntity.internalServerError().body(response);
        }
    }
}