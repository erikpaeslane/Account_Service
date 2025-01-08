package account.events;

import account.entity.AuditLog;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class AuditEvent extends ApplicationEvent {

    private final AuditLog auditLog;

    public AuditEvent(Object source, AuditLog auditLog) {
        super(source);
        this.auditLog = auditLog;
    }
}
