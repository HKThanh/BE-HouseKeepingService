package iuh.house_keeping_service_be.models;

import iuh.house_keeping_service_be.enums.OptionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "service_options")
public class ServiceOption {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "service_options_id_gen")
    @SequenceGenerator(name = "service_options_id_gen", sequenceName = "service_options_option_id_seq", allocationSize = 1)
    @Column(name = "option_id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @NotNull
    @Column(name = "label", nullable = false, length = Integer.MAX_VALUE)
    private String label;

    @Enumerated(EnumType.STRING)
    @Column(name = "option_type", nullable = false, length = 30)
    private OptionType optionType;

    @Column(name = "display_order")
    private Integer displayOrder;

    @ColumnDefault("true")
    @Column(name = "is_required")
    private Boolean isRequired;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_option_id")
    private ServiceOption parentOption;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_choice_id")
    private ServiceOptionChoice parentChoice;

    @Column(name = "validation_rules")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> validationRules;

}