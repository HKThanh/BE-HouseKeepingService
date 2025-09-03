package iuh.house_keeping_service_be.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "service_option_choices")
public class ServiceOptionChoice {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "service_option_choices_id_gen")
    @SequenceGenerator(name = "service_option_choices_id_gen", sequenceName = "service_option_choices_choice_id_seq", allocationSize = 1)
    @Column(name = "choice_id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "option_id", nullable = false)
    private ServiceOption option;

    @NotNull
    @Column(name = "label", nullable = false, length = Integer.MAX_VALUE)
    private String label;

    @ColumnDefault("false")
    @Column(name = "is_default")
    private Boolean isDefault;

    @Column(name = "display_order")
    private Integer displayOrder;

}