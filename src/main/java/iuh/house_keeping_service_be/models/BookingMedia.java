package iuh.house_keeping_service_be.models;

import iuh.house_keeping_service_be.enums.MediaType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name = "booking_media")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingMedia {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "uuid2")
    @Column(name = "media_id")
    private String mediaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @Column(name = "media_url", nullable = false, length = 500)
    private String mediaUrl;

    @Column(name = "public_id", length = 255)
    private String publicId;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false)
    private MediaType mediaType;

    @Column(name = "description", length = 500)
    private String description;

    @CreationTimestamp
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    // Constructor for easy creation
    public BookingMedia(Assignment assignment, String mediaUrl, String publicId, MediaType mediaType, String description) {
        this.assignment = assignment;
        this.mediaUrl = mediaUrl;
        this.publicId = publicId;
        this.mediaType = mediaType;
        this.description = description;
    }
}
