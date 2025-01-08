package account.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.LocalDate;
import java.util.Date;

@Entity
@Table(name = "payments", uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "period"}))
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @ManyToOne
    @JoinColumn(name="employee_id", nullable=false)
    private User user;
    @Column(name="period", nullable=false)
    private LocalDate period;
    @Column(name="salary", nullable=false)
    private long salary;

}
