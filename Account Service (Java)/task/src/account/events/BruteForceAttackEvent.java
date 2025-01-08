package account.events;

import account.entity.AuditLog;

import java.time.LocalDateTime;

public class BruteForceAttackEvent extends AuditEvent{
    public BruteForceAttackEvent(Object source, String subject, String newUserEmail, String path) {
        super(source, AuditLog.builder()
                .date(LocalDateTime.now())
                .action("BRUTE_FORCE")
                .subject(subject)
                .object(newUserEmail)
                .path(path)
                .build());
    }
}
