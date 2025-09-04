package iuh.house_keeping_service_be.models;

    import iuh.house_keeping_service_be.enums.ConditionLogic;
    import jakarta.persistence.*;
    import jakarta.validation.constraints.NotNull;
    import jakarta.validation.constraints.Size;
    import lombok.Getter;
    import lombok.Setter;
    import org.hibernate.annotations.ColumnDefault;

    import java.math.BigDecimal;

    @Getter
    @Setter
    @Entity
    @Table(name = "pricing_rules")
    public class PricingRule {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "rule_id", nullable = false)
        private Integer id;

        @NotNull
        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "service_id", nullable = false)
        private Service service;

        @Size(max = 255)
        @NotNull
        @Column(name = "rule_name", nullable = false)
        private String ruleName;

        @Enumerated(EnumType.STRING)
        @Column(name = "condition_logic", length = 10)
        private ConditionLogic conditionLogic;

        @ColumnDefault("0")
        @Column(name = "priority")
        private Integer priority;

        @ColumnDefault("0")
        @Column(name = "price_adjustment", precision = 10, scale = 2)
        private BigDecimal priceAdjustment;

        @ColumnDefault("0")
        @Column(name = "staff_adjustment")
        private Integer staffAdjustment;

        @ColumnDefault("0")
        @Column(name = "duration_adjustment_hours", precision = 5, scale = 2)
        private BigDecimal durationAdjustmentHours;
    }