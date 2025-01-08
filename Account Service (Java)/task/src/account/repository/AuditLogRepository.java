package account.repository;

import account.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findAllByOrderByIdAsc();
    boolean existsByActionAndSubjectAndPathAndDateAfter(String action, String subject, String path, LocalDateTime after);
    List<AuditLog> findTop5ByOrderByIdDesc();
}
