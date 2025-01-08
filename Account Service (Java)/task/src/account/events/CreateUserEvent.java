package account.events;

import account.entity.AuditLog;

import java.time.LocalDateTime;

public class CreateUserEvent extends AuditEvent {
    public CreateUserEvent(Object source, String newUserEmail) {
        super(source, AuditLog.builder()
                .date(LocalDateTime.now())
                .action("CREATE_USER")
                .subject("Anonymous")
                .object(newUserEmail)
                .path("/api/auth/signup")
                .build());
    }
}