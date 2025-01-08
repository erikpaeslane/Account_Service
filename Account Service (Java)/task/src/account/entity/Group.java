package account.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "principle_groups")
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@ToString
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(unique = true, nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;
    @Column(nullable = false)
    private String name;

    @ManyToMany(mappedBy = "userGroups")
    Set<User> users;

    public Group(Role role) {
        this.role = role;
        this.name = role == Role.ADMINISTRATOR ? "Administrative" : "Business";
    }
}
