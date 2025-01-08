package account.events;

import account.entity.AuditLog;

import java.time.LocalDateTime;

public class UnlockUserEvent extends AuditEvent {
    public UnlockUserEvent(Object source, String subject, String object) {
        super(source, AuditLog.builder()
                .date(LocalDateTime.now())
                .action("UNLOCK_USER")
                .subject(subject)
                .object(object)
                .path("api/admin/user/access")
                .build());
    }
}
