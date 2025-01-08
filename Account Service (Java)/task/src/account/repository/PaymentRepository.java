package account.repository;

import account.entity.Payment;
import account.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    boolean existsByUserAndPeriod(User user, LocalDate period);
    List<Payment> findAllByUserOrderByPeriodDesc(User user);
    Optional<Payment> findByUserAndPeriod(User user, LocalDate period);
}
