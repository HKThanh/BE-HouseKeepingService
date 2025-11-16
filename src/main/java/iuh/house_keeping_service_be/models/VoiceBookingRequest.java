package iuh.house_keeping_service_be.models;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Entity for storing voice booking request data
 */
@Entity
@Table(name = "voice_booking_request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VoiceBookingRequest {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "uuid2")
    @Column(name = "id")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "audio_file_name", length = 255)
    private String audioFileName;

    @Column(name = "audio_duration_seconds", precision = 10, scale = 2)
    private BigDecimal audioDurationSeconds;

    @Column(name = "audio_size_bytes")
    private Long audioSizeBytes;

    @Column(name = "transcript", nullable = false, columnDefinition = "TEXT")
    private String transcript;

    @Column(name = "confidence_score", precision = 5, scale = 4)
    private BigDecimal confidenceScore;

    @Column(name = "processing_time_ms", nullable = false)
    private Integer processingTimeMs = 0; // Default value, will be updated after processing

    @Type(JsonBinaryType.class)
    @Column(name = "hints", columnDefinition = "jsonb")
    private Map<String, Object> hints;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @Column(name = "status", nullable = false, length = 50)
    private String status = "PENDING"; // PENDING, PROCESSING, COMPLETED, FAILED, PARTIAL

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Type(JsonBinaryType.class)
    @Column(name = "missing_fields", columnDefinition = "jsonb")
    private List<String> missingFields;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper methods
    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }

    public boolean isFailed() {
        return "FAILED".equals(status);
    }

    public boolean requiresClarification() {
        return "PARTIAL".equals(status) && missingFields != null && !missingFields.isEmpty();
    }

    public void markAsProcessing() {
        this.status = "PROCESSING";
    }

    public void markAsCompleted(Booking booking) {
        this.status = "COMPLETED";
        this.booking = booking;
        this.errorMessage = null;
        this.missingFields = null;
    }

    public void markAsPartial(List<String> missingFields) {
        this.status = "PARTIAL";
        this.missingFields = missingFields;
    }

    public void markAsFailed(String errorMessage) {
        this.status = "FAILED";
        this.errorMessage = errorMessage;
    }
}
