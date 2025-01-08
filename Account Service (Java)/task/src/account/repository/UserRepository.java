package account.repository;

import account.entity.Payment;
import account.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findUserByName(String name);
    Optional<User> findUserByEmail(String email);
    boolean existsUserByEmail(String email);
    List<User> findByEmailIn(List<String> emails);
    @Query("UPDATE User u SET u.attemptedLoginCount = ?1 WHERE u.email = ?2")
    @Transactional
    @Modifying
    void updateFailedAttempts(int failAttempts, String email);
}
