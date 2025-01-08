package account.events;


import account.entity.AuditLog;

import java.time.LocalDateTime;

public class ChangePasswordEvent extends AuditEvent {
    public ChangePasswordEvent(Object source, String subject, String newUserEmail) {
        super(source, AuditLog.builder()
                .date(LocalDateTime.now())
                .action("CHANGE_PASSWORD")
                .subject(subject)
                .object(newUserEmail)
                .path("api/auth/changepass")
                .build());
    }
}
