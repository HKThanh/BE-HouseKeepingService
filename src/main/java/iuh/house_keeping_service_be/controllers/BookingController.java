package iuh.house_keeping_service_be.controllers;

import iuh.house_keeping_service_be.config.JwtUtil;
import iuh.house_keeping_service_be.dtos.Booking.internal.BookingValidationResult;
import iuh.house_keeping_service_be.dtos.Booking.request.BookingCreateRequest;
import iuh.house_keeping_service_be.dtos.Booking.request.BookingCancelRequest;
import iuh.house_keeping_service_be.dtos.Booking.request.ConvertBookingToPostRequest;
import iuh.house_keeping_service_be.dtos.Booking.request.BookingVerificationRequest;
import iuh.house_keeping_service_be.dtos.Booking.response.BookingHistoryResponse;
import iuh.house_keeping_service_be.dtos.Booking.response.BookingResponse;
import iuh.house_keeping_service_be.dtos.Booking.summary.BookingCreationSummary;
import iuh.house_keeping_service_be.dtos.Cloudinary.CloudinaryUploadResult;
import iuh.house_keeping_service_be.dtos.Service.ServiceDetailResponse;
import iuh.house_keeping_service_be.models.Address;
import iuh.house_keeping_service_be.models.Customer;
import iuh.house_keeping_service_be.repositories.CustomerRepository;
import iuh.house_keeping_service_be.services.AddressService.AddressService;
import iuh.house_keeping_service_be.services.AdminService.PermissionService;
import iuh.house_keeping_service_be.services.BookingService.BookingService;
import iuh.house_keeping_service_be.services.CloudinaryService.CloudinaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/customer/bookings")
@Slf4j
@RequiredArgsConstructor
public class BookingController {
    private final AddressService addressService;
    private final PermissionService permissionService;
    private final BookingService bookingService;
    private final CloudinaryService cloudinaryService;
    private final JwtUtil jwtUtil;
    private final CustomerRepository customerRepository;

    @GetMapping("/{customerId}/default-address")
    public ResponseEntity<?> getDefaultAddress(@PathVariable String customerId
                                                , @RequestHeader("Authorization") String authHeader) {

        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(
                        new ServiceDetailResponse(false, "Token không hợp lệ", null)
                );
            }

            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);

            if (username == null || !jwtUtil.validateToken(token, username)) {
                return ResponseEntity.status(401).body(
                        Map.of(
                                "success", false,
                                "message", "Token không hợp lệ"
                        )
                );
            }

            Address address = addressService.findByCustomerId(customerId);
            if (address != null) {
                return ResponseEntity.ok(
                        Map.of(
                                "success", true,
                                "data", address
                        )
                );
            }
            return ResponseEntity.status(404).body(
                    Map.of(
                            "success", false,
                            "message", "Khách hàng chưa có địa chỉ mặc định: " + customerId
                    )
            );

        } catch (Exception e) {
            return ResponseEntity.status(401).body(
                    Map.of(
                            "success", false,
                            "message", "Token không hợp lệ"
                    )
            );
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_CUSTOMER', 'ROLE_ADMIN')")
    public ResponseEntity<BookingCreationSummary> createBooking(@Valid @RequestBody BookingCreateRequest request) {
        log.info("Creating new booking with {} services", request.bookingDetails().size());
        
        BookingCreationSummary summary = bookingService.createBooking(request);

        log.info("Booking created successfully: {}", summary.getBookingId());
        return ResponseEntity.status(HttpStatus.CREATED).body(summary);
    }

    @GetMapping("/{bookingId}")
    @PreAuthorize("hasAnyRole('ROLE_CUSTOMER', 'ROLE_ADMIN')")
    public ResponseEntity<BookingResponse> getBooking(@PathVariable String bookingId) {
        log.info("Getting booking details: {}", bookingId);
        
        BookingResponse response = bookingService.getBookingDetails(bookingId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate")
    @PreAuthorize("hasAnyRole('ROLE_CUSTOMER', 'ROLE_ADMIN')")
    public ResponseEntity<BookingValidationResult> validateBooking(@Valid @RequestBody BookingCreateRequest request) {
        log.info("Validating booking request");
        
        BookingValidationResult result = bookingService.validateBooking(request);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ROLE_CUSTOMER', 'ROLE_ADMIN')")
    public ResponseEntity<Page<BookingHistoryResponse>> getBookingsByCustomerId(
            @PathVariable String customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort) {

        log.info("Getting bookings for customer: {} (page: {}, size: {})", customerId, page, size);

        try {
            // Validate pagination parameters
            if (page < 0) page = 0;
            if (size <= 0 || size > 100) size = 10; // Limit max size to prevent performance issues

            // Safe sort parameter parsing
            String sortProperty = sort.length > 0 ? sort[0] : "createdAt";
            Sort.Direction direction = Sort.Direction.DESC; // Default to DESC

            if (sort.length > 1) {
                direction = sort[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
            }

            Pageable pageable = PageRequest.of(page, size, Sort.by(new Sort.Order(direction, sortProperty)));

            Page<BookingHistoryResponse> bookingsPage = bookingService.getBookingsByCustomerId(customerId, pageable);

            log.info("Retrieved {} bookings for customer: {}", bookingsPage.getNumberOfElements(), customerId);
            return ResponseEntity.ok(bookingsPage);

        } catch (Exception e) {
            log.error("Error retrieving bookings for customer {}: {}", customerId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Page.empty());
        }
    }

    @PutMapping("/{bookingId}/convert-to-post")
    @PreAuthorize("hasAnyRole('ROLE_CUSTOMER')")
    public ResponseEntity<?> convertBookingToPost(
            @PathVariable String bookingId,
            @Valid @RequestBody ConvertBookingToPostRequest request) {
        
        log.info("Converting booking {} to post", bookingId);
        
        try {
            BookingResponse response = bookingService.convertBookingToPost(bookingId, request);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Chuyển booking thành bài post thành công",
                "data", response
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Error converting booking to post: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Unexpected error converting booking to post: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Đã xảy ra lỗi khi chuyển booking thành bài post"
            ));
        }
    }

    @PutMapping("/{bookingId}/cancel")
    @PreAuthorize("hasAnyRole('ROLE_CUSTOMER')")
    public ResponseEntity<?> cancelBooking(
            @PathVariable String bookingId,
            @Valid @RequestBody BookingCancelRequest request,
            @RequestHeader("Authorization") String authHeader) {
        
        log.info("Customer cancelling booking {}", bookingId);
        
        // Extract customer ID from JWT token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Token không hợp lệ"
            ));
        }

        String token = authHeader.substring(7);
        String username = jwtUtil.extractUsername(token);

        if (username == null || !jwtUtil.validateToken(token, username)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "success", false,
                "message", "Token không hợp lệ"
            ));
        }

        // Get customer from username
        Customer customer = customerRepository.findByAccount_Username(username)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin khách hàng"));
        
        BookingResponse response = bookingService.cancelBooking(bookingId, customer.getCustomerId(), request.reason());
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Huỷ booking thành công",
            "data", response
        ));
    }

    @GetMapping("/admin/unverified")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getUnverifiedBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("Admin fetching unverified bookings (page: {}, size: {})", page, size);
        
        try {
            if (page < 0) page = 0;
            if (size <= 0 || size > 100) size = 10;
            
            Pageable pageable = PageRequest.of(page, size);
            Page<BookingResponse> unverifiedBookings = bookingService.getUnverifiedBookings(pageable);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", unverifiedBookings.getContent(),
                "currentPage", unverifiedBookings.getNumber(),
                "totalItems", unverifiedBookings.getTotalElements(),
                "totalPages", unverifiedBookings.getTotalPages()
            ));
        } catch (Exception e) {
            log.error("Error fetching unverified bookings: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Đã xảy ra lỗi khi lấy danh sách booking chưa xác minh"
            ));
        }
    }

    @PutMapping("/admin/{bookingId}/verify")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> verifyBooking(
            @PathVariable String bookingId,
            @Valid @RequestBody BookingVerificationRequest request) {
        
        log.info("Admin verifying booking {}: approve={}", bookingId, request.approve());
        
        try {
            BookingResponse response = bookingService.verifyBooking(bookingId, request);
            
            String message = request.approve() 
                ? "Chấp nhận bài post thành công" 
                : "Từ chối bài post thành công";
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", message,
                "data", response
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Error verifying booking: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Unexpected error verifying booking: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Đã xảy ra lỗi khi xác minh booking"
            ));
        }
    }

    @PostMapping("/{bookingId}/upload-image")
    @PreAuthorize("hasAnyRole('ROLE_CUSTOMER', 'ROLE_ADMIN')")
    public ResponseEntity<?> uploadBookingImage(
            @PathVariable String bookingId,
            @RequestParam("file") MultipartFile file) {
        
        log.info("Uploading image for booking {}", bookingId);
        
        try {
            // Validate file
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "File không được để trống"
                ));
            }

            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "File phải là định dạng ảnh"
                ));
            }

            // Validate file size (max 5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Kích thước file không được vượt quá 5MB"
                ));
            }

            // Upload to Cloudinary
            CloudinaryUploadResult result = cloudinaryService.uploadBookingImage(file);
            
            log.info("Successfully uploaded image for booking {}: {}", bookingId, result.secureUrl());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Tải ảnh lên thành công",
                "data", Map.of(
                    "bookingId", bookingId,
                    "imageUrl", result.secureUrl(),
                    "publicId", result.publicId()
                )
            ));
        } catch (IllegalArgumentException e) {
            log.error("Invalid file for booking {}: {}", bookingId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error uploading image for booking {}: {}", bookingId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Đã xảy ra lỗi khi tải ảnh lên"
            ));
        }
    }
}
