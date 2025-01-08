package account.events;

import account.entity.AuditLog;

import java.time.LocalDateTime;

public class DeleteUserEvent extends AuditEvent {
    public DeleteUserEvent(Object source, String subject, String object) {
        super(source, AuditLog.builder()
                .date(LocalDateTime.now())
                .action("DELETE_USER")
                .subject(subject)
                .object(object)
                .path("api/admin/user")
                .build());
    }
}
