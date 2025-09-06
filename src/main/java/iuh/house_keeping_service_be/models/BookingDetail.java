package iuh.house_keeping_service_be.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "booking_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingDetail {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "booking_detail_id")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;

    @Column(name = "price_per_unit", precision = 10, scale = 2)
    private BigDecimal pricePerUnit;

    @Column(name = "sub_total", precision = 10, scale = 2)
    private BigDecimal subTotal;

    @Column(name = "selected_choice_ids", columnDefinition = "TEXT")
    private String selectedChoiceIds;

    @OneToMany(mappedBy = "bookingDetail", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Assignment> assignments = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.id = UUID.randomUUID().toString();
    }

    // Helper methods
    public void addAssignment(Assignment assignment) {
        assignments.add(assignment);
        assignment.setBookingDetail(this);
    }

    public List<Integer> getSelectedChoiceIdsList() {
        if (selectedChoiceIds == null || selectedChoiceIds.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return Arrays.stream(selectedChoiceIds.split(","))
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
        } catch (NumberFormatException e) {
            return new ArrayList<>();
        }
    }

    public void setSelectedChoiceIdsList(List<Integer> choiceIds) {
        if (choiceIds == null || choiceIds.isEmpty()) {
            this.selectedChoiceIds = "";
        } else {
            this.selectedChoiceIds = choiceIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
        }
    }
}