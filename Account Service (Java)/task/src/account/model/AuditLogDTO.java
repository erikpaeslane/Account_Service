package account.model;

import account.entity.AuditLog;

import java.time.LocalDateTime;

public record AuditLogDTO (
        Long id,
        LocalDateTime date,
        String action,
        String subject,
        String object,
        String path
) {
    public static AuditLogDTO fromAuditLogToDTO(AuditLog auditLog){
        return new AuditLogDTO(
                auditLog.getId(),
                auditLog.getDate(),
                auditLog.getAction(),
                auditLog.getSubject(),
                auditLog.getObject(),
                auditLog.getPath()
        );
    }
}
