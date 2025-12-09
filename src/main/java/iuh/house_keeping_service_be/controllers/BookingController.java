package iuh.house_keeping_service_be.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import iuh.house_keeping_service_be.config.JwtUtil;
import iuh.house_keeping_service_be.dtos.Booking.internal.BookingValidationResult;
import iuh.house_keeping_service_be.dtos.Booking.request.BookingCreateRequest;
import iuh.house_keeping_service_be.dtos.Booking.request.BookingPreviewRequest;
import iuh.house_keeping_service_be.dtos.Booking.request.MultipleBookingCreateRequest;
import iuh.house_keeping_service_be.dtos.Booking.request.MultipleBookingPreviewRequest;
import iuh.house_keeping_service_be.dtos.Booking.request.RecurringBookingPreviewRequest;
import iuh.house_keeping_service_be.dtos.Booking.request.BookingCancelRequest;
import iuh.house_keeping_service_be.dtos.Booking.request.ConvertBookingToPostRequest;
import iuh.house_keeping_service_be.dtos.Booking.response.BookingHistoryResponse;
import iuh.house_keeping_service_be.dtos.Booking.response.BookingPreviewResponse;
import iuh.house_keeping_service_be.dtos.Booking.response.MultipleBookingPreviewResponse;
import iuh.house_keeping_service_be.dtos.Booking.response.RecurringBookingPreviewResponse;
import iuh.house_keeping_service_be.dtos.Booking.response.BookingResponse;
import iuh.house_keeping_service_be.dtos.Booking.summary.BookingCreationSummary;
import iuh.house_keeping_service_be.dtos.Booking.summary.MultipleBookingCreationSummary;
import iuh.house_keeping_service_be.dtos.Cloudinary.CloudinaryUploadResult;
import iuh.house_keeping_service_be.dtos.Service.ServiceDetailResponse;
import iuh.house_keeping_service_be.models.Address;
import iuh.house_keeping_service_be.models.Customer;
import iuh.house_keeping_service_be.repositories.CustomerRepository;
import iuh.house_keeping_service_be.services.AddressService.AddressService;
import iuh.house_keeping_service_be.services.BookingService.BookingService;
import iuh.house_keeping_service_be.services.CloudinaryService.CloudinaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/customer/bookings")
@Slf4j
@RequiredArgsConstructor
public class BookingController {
    private final AddressService addressService;
    private final BookingService bookingService;
    private final CloudinaryService cloudinaryService;
    private final JwtUtil jwtUtil;
    private final CustomerRepository customerRepository;
    private final ObjectMapper objectMapper;

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

    @PostMapping(value = "", consumes = {"multipart/form-data"})
    @PreAuthorize("hasAnyRole('ROLE_CUSTOMER', 'ROLE_ADMIN')")
    public ResponseEntity<?> createBooking(
            @RequestPart(value = "booking", required = true) String bookingJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        
        log.info("Received booking creation request");
        
        try {
            // Parse JSON string to BookingCreateRequest object
            BookingCreateRequest request;
            try {
                request = objectMapper.readValue(bookingJson, BookingCreateRequest.class);
                log.info("Creating new booking with {} services", request.bookingDetails().size());
            } catch (Exception e) {
                log.error("Failed to parse booking JSON: {}", e.getMessage());
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Invalid booking data format: " + e.getMessage()
                ));
            }
            
            List<String> imageUrls = new ArrayList<>();
            
            // Upload images to Cloudinary if provided
            if (images != null && !images.isEmpty()) {
                log.info("Uploading {} booking images to Cloudinary", images.size());
                
                // Validate number of images (max 10)
                if (images.size() > 10) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Số lượng ảnh không được vượt quá 10"
                    ));
                }
                
                for (MultipartFile image : images) {
                    if (image == null || image.isEmpty()) {
                        continue;
                    }
                    
                    // Validate file type
                    String contentType = image.getContentType();
                    if (contentType == null || !contentType.startsWith("image/")) {
                        return ResponseEntity.badRequest().body(Map.of(
                            "success", false,
                            "message", "Tất cả file phải là định dạng ảnh"
                        ));
                    }
                    
                    // Validate file size (max 10MB)
                    if (image.getSize() > 10 * 1024 * 1024) {
                        return ResponseEntity.badRequest().body(Map.of(
                            "success", false,
                            "message", "Kích thước mỗi file không được vượt quá 10MB"
                        ));
                    }
                    
                    try {
                        CloudinaryUploadResult uploadResult = cloudinaryService.uploadBookingImage(image);
                        imageUrls.add(uploadResult.secureUrl());
                        log.info("Image uploaded successfully: {}", uploadResult.secureUrl());
                    } catch (Exception e) {
                        log.error("Failed to upload image: {}", e.getMessage());
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                            "success", false,
                            "message", "Lỗi khi tải ảnh lên: " + e.getMessage()
                        ));
                    }
                }
            }
            
            // Create new request with image URLs if uploaded
            BookingCreateRequest bookingRequest = request;
            if (!imageUrls.isEmpty()) {
                bookingRequest = new BookingCreateRequest(
                    request.addressId(),
                    request.newAddress(),
                    request.bookingTime(),
                    request.note(),
                    request.title(),
                    imageUrls,
                    request.promoCode(),
                    request.bookingDetails(),
                    request.assignments(),
                    request.paymentMethodId(),
                    request.additionalFeeIds()
                );
            }
            
            BookingCreationSummary summary = bookingService.createBooking(bookingRequest);
            
            log.info("Booking created successfully: {}", summary.getBookingId());
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "data", summary
            ));
        } catch (IllegalArgumentException e) {
            log.error("Validation error creating booking: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error creating booking: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Đã xảy ra lỗi khi tạo booking"
            ));
        }
    }

    @PostMapping(value = "/multiple", consumes = {"multipart/form-data"})
    @PreAuthorize("hasAnyRole('ROLE_CUSTOMER', 'ROLE_ADMIN')")
    public ResponseEntity<?> createMultipleBookings(
            @RequestPart(value = "booking", required = true) String bookingJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        
        log.info("Received multiple booking creation request");
        
        try {
            // Parse JSON string to MultipleBookingCreateRequest object
            MultipleBookingCreateRequest request;
            try {
                request = objectMapper.readValue(bookingJson, MultipleBookingCreateRequest.class);
                log.info("Creating {} bookings with {} services each", 
                    request.bookingTimes().size(), 
                    request.bookingDetails().size());
            } catch (Exception e) {
                log.error("Failed to parse booking JSON: {}", e.getMessage());
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Invalid booking data format: " + e.getMessage()
                ));
            }
            
            List<String> imageUrls = new ArrayList<>();
            
            // Upload images to Cloudinary if provided
            if (images != null && !images.isEmpty()) {
                log.info("Uploading {} booking images to Cloudinary", images.size());
                
                // Validate number of images (max 10)
                if (images.size() > 10) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Số lượng ảnh không được vượt quá 10"
                    ));
                }
                
                for (MultipartFile image : images) {
                    if (image == null || image.isEmpty()) {
                        continue;
                    }
                    
                    // Validate file type
                    String contentType = image.getContentType();
                    if (contentType == null || !contentType.startsWith("image/")) {
                        return ResponseEntity.badRequest().body(Map.of(
                            "success", false,
                            "message", "Tất cả file phải là định dạng ảnh"
                        ));
                    }
                    
                    // Validate file size (max 10MB)
                    if (image.getSize() > 10 * 1024 * 1024) {
                        return ResponseEntity.badRequest().body(Map.of(
                            "success", false,
                            "message", "Kích thước mỗi file không được vượt quá 10MB"
                        ));
                    }
                    
                    try {
                        CloudinaryUploadResult uploadResult = cloudinaryService.uploadBookingImage(image);
                        imageUrls.add(uploadResult.secureUrl());
                        log.info("Image uploaded successfully: {}", uploadResult.secureUrl());
                    } catch (Exception e) {
                        log.error("Failed to upload image: {}", e.getMessage());
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                            "success", false,
                            "message", "Lỗi khi tải ảnh lên: " + e.getMessage()
                        ));
                    }
                }
            }
            
            // Create new request with image URLs if uploaded
            MultipleBookingCreateRequest bookingRequest = request;
            if (!imageUrls.isEmpty()) {
                bookingRequest = new MultipleBookingCreateRequest(
                    request.addressId(),
                    request.newAddress(),
                    request.bookingTimes(),
                    request.note(),
                    request.title(),
                    imageUrls,
                    request.promoCode(),
                    request.bookingDetails(),
                    request.assignments(),
                    request.paymentMethodId(),
                    request.additionalFeeIds()
                );
            }
            
            MultipleBookingCreationSummary summary = bookingService.createMultipleBookings(bookingRequest);
            
            log.info("Multiple bookings created: {}/{} successful", 
                summary.getSuccessfulBookings(), 
                summary.getTotalBookingsCreated());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "data", summary
            ));
        } catch (IllegalArgumentException e) {
            log.error("Validation error creating multiple bookings: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error creating multiple bookings: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Đã xảy ra lỗi khi tạo các booking"
            ));
        }
    }

    @GetMapping("/{bookingId}")
    @PreAuthorize("hasAnyRole('ROLE_CUSTOMER', 'ROLE_ADMIN')")
    public ResponseEntity<BookingResponse> getBooking(@PathVariable String bookingId) {
        log.info("Getting booking details: {}", bookingId);
        
        BookingResponse response = bookingService.getBookingDetails(bookingId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{bookingId}/charges")
    @PreAuthorize("hasAnyRole('ROLE_CUSTOMER', 'ROLE_ADMIN', 'ROLE_EMPLOYEE')")
    public ResponseEntity<BookingResponse> getBookingCharges(@PathVariable String bookingId) {
        log.info("Getting booking charge breakdown: {}", bookingId);
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

    /**
     * Preview/Quote endpoint - Returns detailed pricing breakdown for a booking without creating it.
     * This endpoint allows customers and admins to see a complete invoice-like preview of the booking
     * including services, pricing, promotions, fees, and grand total.
     * 
     * Admin users can specify a customerId to preview on behalf of a customer.
     * 
     * Always returns HTTP 200 with validation status in the response body.
     */
    @PostMapping("/preview")
    @PreAuthorize("hasAnyRole('ROLE_CUSTOMER', 'ROLE_ADMIN')")
    public ResponseEntity<BookingPreviewResponse> previewBooking(@Valid @RequestBody BookingPreviewRequest request) {
        log.info("Generating booking preview");
        
        try {
            // Get current user info from security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUserId = authentication.getName();
            
            // Check if current user is admin
            boolean isAdmin = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(auth -> auth.equals("ROLE_ADMIN"));
            
            // For customers, get their customerId from the repository
            if (!isAdmin) {
                Customer customer = customerRepository.findByAccount_Username(currentUserId).orElse(null);
                if (customer != null) {
                    currentUserId = customer.getCustomerId();
                }
            }
            
            log.info("Preview requested by user: {}, isAdmin: {}", currentUserId, isAdmin);
            
            BookingPreviewResponse preview = bookingService.previewBooking(request, currentUserId, isAdmin);
            
            // Always return HTTP 200 (validation errors are in the response body)
            return ResponseEntity.ok(preview);
            
        } catch (Exception e) {
            log.error("Error generating booking preview: {}", e.getMessage(), e);
            return ResponseEntity.ok(BookingPreviewResponse.error(
                    List.of("Error generating preview: " + e.getMessage())
            ));
        }
    }

    /**
     * Preview multiple bookings with different time slots but same services.
     * Promo code applies to ALL bookings.
     * Returns individual preview for each time slot plus aggregated totals.
     * 
     * Admin users can specify a customerId to preview on behalf of a customer.
     * 
     * Always returns HTTP 200 with validation status in the response body.
     */
    @PostMapping("/preview/multiple")
    @PreAuthorize("hasAnyRole('ROLE_CUSTOMER', 'ROLE_ADMIN')")
    public ResponseEntity<MultipleBookingPreviewResponse> previewMultipleBookings(
            @Valid @RequestBody MultipleBookingPreviewRequest request) {
        log.info("Generating multiple booking preview for {} time slots", request.bookingTimes().size());
        
        try {
            // Get current user info from security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUserId = authentication.getName();
            
            // Check if current user is admin
            boolean isAdmin = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(auth -> auth.equals("ROLE_ADMIN"));
            
            // For customers, get their customerId from the repository
            if (!isAdmin) {
                Customer customer = customerRepository.findByAccount_Username(currentUserId).orElse(null);
                if (customer != null) {
                    currentUserId = customer.getCustomerId();
                }
            }
            
            log.info("Multiple preview requested by user: {}, isAdmin: {}", currentUserId, isAdmin);
            
            MultipleBookingPreviewResponse preview = bookingService.previewMultipleBookings(request, currentUserId, isAdmin);
            
            return ResponseEntity.ok(preview);
            
        } catch (Exception e) {
            log.error("Error generating multiple booking preview: {}", e.getMessage(), e);
            return ResponseEntity.ok(MultipleBookingPreviewResponse.error(
                    List.of("Error generating preview: " + e.getMessage())
            ));
        }
    }

    /**
     * Preview recurring booking with pricing for all planned occurrences.
     * Shows pricing breakdown per occurrence and total across all occurrences.
     * Limited to maxPreviewOccurrences (default 30, max 30).
     * 
     * Admin users can specify a customerId to preview on behalf of a customer.
     * 
     * Always returns HTTP 200 with validation status in the response body.
     */
    @PostMapping("/preview/recurring")
    @PreAuthorize("hasAnyRole('ROLE_CUSTOMER', 'ROLE_ADMIN')")
    public ResponseEntity<RecurringBookingPreviewResponse> previewRecurringBooking(
            @Valid @RequestBody RecurringBookingPreviewRequest request) {
        log.info("Generating recurring booking preview, recurrenceType: {}", request.recurrenceType());
        
        try {
            // Get current user info from security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUserId = authentication.getName();
            
            // Check if current user is admin
            boolean isAdmin = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(auth -> auth.equals("ROLE_ADMIN"));
            
            // For customers, get their customerId from the repository
            if (!isAdmin) {
                Customer customer = customerRepository.findByAccount_Username(currentUserId).orElse(null);
                if (customer != null) {
                    currentUserId = customer.getCustomerId();
                }
            }
            
            log.info("Recurring preview requested by user: {}, isAdmin: {}", currentUserId, isAdmin);
            
            RecurringBookingPreviewResponse preview = bookingService.previewRecurringBooking(request, currentUserId, isAdmin);
            
            return ResponseEntity.ok(preview);
            
        } catch (Exception e) {
            log.error("Error generating recurring booking preview: {}", e.getMessage(), e);
            return ResponseEntity.ok(RecurringBookingPreviewResponse.error(
                    List.of("Error generating preview: " + e.getMessage())
            ));
        }
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ROLE_CUSTOMER', 'ROLE_ADMIN')")
    public ResponseEntity<Page<BookingHistoryResponse>> getBookingsByCustomerId(
            @PathVariable String customerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort) {

        log.info("Getting bookings for customer: {} from date: {} (page: {}, size: {})", customerId, fromDate, page, size);

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

            Page<BookingHistoryResponse> bookingsPage;
            if (fromDate != null) {
                bookingsPage = bookingService.getBookingsByCustomerId(customerId, fromDate, pageable);
            } else {
                bookingsPage = bookingService.getBookingsByCustomerId(customerId, pageable);
            }

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

            // Validate file size (max 10MB)
            if (file.getSize() > 10 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Kích thước file không được vượt quá 10MB"
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
