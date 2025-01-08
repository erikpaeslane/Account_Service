package account.events;

import account.entity.AuditLog;

import java.time.LocalDateTime;

public class RemoveRoleEvent extends AuditEvent {
    public RemoveRoleEvent(Object source, String subject, String object) {
        super(source, AuditLog.builder()
                .date(LocalDateTime.now())
                .action("REMOVE_ROLE")
                .subject(subject)
                .object(object)
                .path("api/admin/user/role")
                .build());
    }
}
