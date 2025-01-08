package account.events;

import account.entity.AuditLog;
import account.entity.Group;
import account.entity.Role;
import account.entity.User;
import account.repository.AuditLogRepository;
import account.repository.UserRepository;
import account.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authorization.event.AuthorizationDeniedEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class AuditEventListener {

    private final AuditLogRepository logRepository;
    private final UserRepository userRepository;
    private final UserService userService;


    @Autowired
    public AuditEventListener(AuditLogRepository logRepository, UserRepository userRepository, UserService userService) {
        this.logRepository = logRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @EventListener
    public void handleCreateUserEvent(CreateUserEvent event) {
        AuditLog auditLog = event.getAuditLog();
        log.info("User created!");
        log.info(auditLog.toString());
        logRepository.save(auditLog);
        logSavedLogs();
    }

    @EventListener
    public void handleChangePasswordEvent(ChangePasswordEvent event) {
        AuditLog auditLog = event.getAuditLog();
        log.info("Password changed!");
        log.info(event.getAuditLog().toString());
        logRepository.save(auditLog);
        logSavedLogs();
    }

    @EventListener
    public void handleGrantRoleEvent(GrantRoleEvent event) {
        AuditLog auditLog = event.getAuditLog();
        log.info("Role granted!");
        log.info(event.getAuditLog().toString());
        logRepository.save(auditLog);
        logSavedLogs();
    }

    @EventListener
    public void handleRemoveRoleEvent(RemoveRoleEvent event) {
        AuditLog auditLog = event.getAuditLog();
        log.info("Role removed!");
        log.info(event.getAuditLog().toString());
        logRepository.save(auditLog);
        logSavedLogs();
    }

    @EventListener
    public void handleLockUserEvent(LockUserEvent event) {
        AuditLog auditLog = event.getAuditLog();
        log.info("User locked!");
        log.info(event.getAuditLog().toString());
        logRepository.save(auditLog);
        logSavedLogs();
    }

    @EventListener
    public void handleUnlockUserEvent(UnlockUserEvent event) {
        AuditLog auditLog = event.getAuditLog();
        log.info("User unlocked!");
        log.info(event.getAuditLog().toString());
        logRepository.save(auditLog);
        logSavedLogs();
    }

    @EventListener
    public void handleDeleteUserEvent(DeleteUserEvent event) {
        AuditLog auditLog = event.getAuditLog();
        log.info("User deleted!");
        log.info(event.getAuditLog().toString());
        logRepository.save(auditLog);
        logSavedLogs();
    }

    @EventListener
    public void handleBruteForceAttackEvent(BruteForceAttackEvent event) {
        AuditLog auditLog = event.getAuditLog();
        log.info("Brute force attack detected!");
        log.info(event.getAuditLog().toString());
        logRepository.save(auditLog);
        logSavedLogs();
    }

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();
        User user = userRepository.findUserByEmail(username).orElse(null);
        if (user == null) {
            return;
        }
        System.out.println("-----------------------");
        System.out.println("Authentication success!");
        System.out.println("User: " + user);
        List<Role> roles = user.getUserGroups().stream().map(Group::getRole).toList();
        System.out.println(roles);
        System.out.println("Attempted logins: " + user.getAttemptedLoginCount());
        System.out.println("Updating...");
        user.setAttemptedLoginCount(0);
        user.setLocked(false);
        user = userRepository.save(user);
        System.out.println("User updated!");
        System.out.println("Attempted logins: " + user.getAttemptedLoginCount());
        System.out.println("-----------------------");
    }

    @EventListener
    public void onAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) {

        String username = event.getAuthentication().getName();
        System.out.println("----------------------");
        System.out.println("Authentication failed!");
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String path = attributes != null ? attributes.getRequest().getRequestURI() : "Unknown Path";

        log.info("Bad credentials for user: {}", username);
        logEvent("LOGIN_FAILED", username, path, path);
        User user = userRepository.findUserByEmail(username.toLowerCase()).orElse(null);
        if (user == null)
            return;
        List<Role> roles = user.getUserGroups().stream().map(Group::getRole).toList();
        System.out.println("User login attempts before: " + user.getAttemptedLoginCount());
        userService.increaseFailedAttempts(user);
        user = userRepository.findUserByEmail(user.getEmail()).orElse(null);
        if(user == null) {return;}
        System.out.println("Updated: " + user.getAttemptedLoginCount());
        System.out.println("User roles: " + roles);
        if (roles.contains(Role.ADMINISTRATOR))
            userService.resetFailedAttempts(user.getEmail());
        boolean isBruteForce = isBruteForceAttack(username);
        System.out.println("Is brute force attack: " + isBruteForce);
        if (isBruteForce){
            System.out.println("----------------------");
            System.out.println("BRUTE FORCE ATTACK!!!!!");
            System.out.println("Email: " + user.getEmail());
            System.out.println("Attempts: " + user.getAttemptedLoginCount());
            logEvent("BRUTE_FORCE", username, path, path);
            //if (!roles.contains(Role.ADMINISTRATOR))
            user.setLocked(true);
            user = userRepository.save(user);
            System.out.println("Is user locked now: " + user.isLocked());
            System.out.println("Attempts: " + user.getAttemptedLoginCount());
            logEvent("LOCK_USER", username, "Lock user " + username, path);
            System.out.println("----------------------");
        }
        logSavedLogs();
        System.out.println("----------------------");
    }

    @EventListener
    public void onAuthorizationFailure(AuthorizationDeniedEvent<?> event) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() &&
                !"anonymousUser".equals(authentication.getName())) {
            String username = authentication.getName();
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            String path = attributes != null ? attributes.getRequest().getRequestURI() : "Unknown Path";
            log.info("Access denied for user: {}", username);
            logEvent("ACCESS_DENIED", username, path, path);
        }
    }

    private boolean isBruteForceAttack(String email) {
        User user = userRepository.findUserByEmail(email.toLowerCase()).orElse(null);
        if (user == null)
            return false;
        System.out.println("---------------");
        System.out.println(user.getEmail());
        System.out.println(user.getAttemptedLoginCount());
        System.out.println("---------------");
        return user.getAttemptedLoginCount() >= 5;
    }

    private void logEvent(String action, String subject, String object, String path) {
        AuditLog auditLog = AuditLog.builder()
                .date(LocalDateTime.now())
                .action(action)
                .subject(subject)
                .object(object)
                .path(path)
                .build();
        logRepository.save(auditLog);
        log.info("Logged event: {}", auditLog);
    }

    private void logSavedLogs() {
        log.info(logRepository.findAllByOrderByIdAsc().toString());
        log.info("Total events processed: {}", logRepository.count());
    }




}
