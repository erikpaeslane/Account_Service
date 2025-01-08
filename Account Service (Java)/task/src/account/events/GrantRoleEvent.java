package account.events;

import account.entity.AuditLog;

import java.time.LocalDateTime;

public class GrantRoleEvent extends AuditEvent{
    public GrantRoleEvent(Object source, String subject, String object) {
        super(source, AuditLog.builder()
                .date(LocalDateTime.now())
                .action("GRANT_ROLE")
                .subject(subject)
                .object(object)
                .path("api/admin/user/role")
                .build());
    }
}
