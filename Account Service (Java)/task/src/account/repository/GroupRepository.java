package account.repository;

import account.entity.Group;
import account.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Integer> {
    Optional<Group> findByRole(Role role);
    boolean existsByRole(Role role);
}
