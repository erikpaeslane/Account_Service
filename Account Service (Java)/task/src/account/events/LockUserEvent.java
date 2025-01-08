package account.events;

import account.entity.AuditLog;

import java.time.LocalDateTime;

public class LockUserEvent extends AuditEvent {
    public LockUserEvent(Object source, String subject, String object) {
        super(source, AuditLog.builder()
                .date(LocalDateTime.now())
                .action("LOCK_USER")
                .subject(subject)
                .object(object)
                .path("api/admin/user/access")
                .build());
    }
}
