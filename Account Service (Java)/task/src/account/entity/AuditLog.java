package account.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column
    private LocalDateTime date;
    @Column
    private String action;
    @Column
    private String subject;
    @Column
    private String object;
    @Column
    private String path;

    @Override
    public String toString() {
        return "AuditLog{" +
                "id=" + id +
                ", date=" + date +
                ", action='" + action + '\'' +
                ", subject='" + subject + '\'' +
                ", object='" + object + '\'' +
                ", path='" + path + '\'' +
                '}';
    }
}
