package account.service;

import account.entity.AuditLog;
import account.entity.Group;
import account.entity.Role;
import account.entity.User;
import account.events.*;
import account.exception.*;
import account.model.*;
import account.repository.AuditLogRepository;
import account.repository.GroupRepository;
import account.repository.UserRepository;
import account.utils.PasswordBreachHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final AuditLogRepository logRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher applicationEventPublisher;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private static final String EMAIL_REGEX = ".*@acme\\.com$";
    public static final int MAX_FAILED_ATTEMPTS = 5;

    @Autowired
    public UserService(UserRepository userRepository, GroupRepository groupRepository, AuditLogRepository logRepository,
                       PasswordEncoder passwordEncoder, ApplicationEventPublisher applicationEventPublisher) {
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.logRepository = logRepository;
        this.passwordEncoder = passwordEncoder;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public ResponseEntity<?> registerUser(UserCredentials userCredentials) {

        String email = validateEmail(userCredentials.email());
        boolean isUserExists = userRepository.existsUserByEmail(email);
        if (isUserExists) {
            throw new InvalidEmailFormatException("User already exists!");
        }
        String userName = validateUserName(userCredentials.name());
        String lastName = validateUserName(userCredentials.lastname());
        String encodedPassword = validatePassword(userCredentials.password());

        User user = User.builder()
                .name(userName)
                .lastname(lastName)
                .email(email)
                .password(encodedPassword)
                .build();
        if (userRepository.count() == 0)
            user.getUserGroups().add(groupRepository.findByRole(Role.ADMINISTRATOR).orElse(null));
        else
            user.getUserGroups().add(groupRepository.findByRole(Role.USER).orElse(null));

        userRepository.save(user);
        applicationEventPublisher.publishEvent(new CreateUserEvent(this, email));
        return ResponseEntity.status(HttpStatus.OK).body(UserResponse.fromUserToResponse(user));
    }

    public ResponseEntity<?> changePassword(UserDetails userDetails, String newPassword) {
        User user = userRepository.findUserByEmail(userDetails.getUsername()).orElse(null);
        if (user == null)
            throw new UsernameNotFoundException("User not found!");
        if (comparePasswords(user.getPassword(), newPassword))
            throw new InvalidPasswordException("The passwords must be different!");
        String validatedNewPassword = validatePassword(newPassword);
        user.setPassword(validatedNewPassword);
        userRepository.save(user);
        applicationEventPublisher.publishEvent(
                new ChangePasswordEvent(this, userDetails.getUsername(), userDetails.getUsername())
        );
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of(
                        "email", user.getEmail(),
                        "status", "The password has been updated successfully"
                ));
    }

    public ResponseEntity<?> updateUserLockState(UserDetails userDetails, UnlockUserRequest request) {
        String email = validateEmail(request.user());
        User user = userRepository.findUserByEmail(email).orElse(null);
        System.out.println("----------------------------");
        System.out.println(request.operation() + "ing user...");
        logger.info("User: {}", user);
        if (user == null)
            throw new UsernameNotFoundException("User not found!");
        if (user.getUserGroups().stream().map(Group::getRole).collect(Collectors.toSet()).contains(Role.ADMINISTRATOR))
            throw new BlockAdministratorException("Can't lock the ADMINISTRATOR!");
        switch (request.operation()) {
            case "LOCK":
                if (user.isLocked())
                    throw new UpdateLockStateException("User is already locked!");
                user.setLocked(true);
                applicationEventPublisher.publishEvent(
                        new LockUserEvent(this, userDetails.getUsername(), "Lock user " + email)
                );
                break;
            case "UNLOCK":
                if (!user.isLocked())
                    throw new UpdateLockStateException("User is not locked!");
                user.setLocked(false);
                userRepository.updateFailedAttempts(0, email);
                applicationEventPublisher.publishEvent(
                        new UnlockUserEvent(this, userDetails.getUsername(), "Unlock user " + email)
                );
                break;
        }
        user = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                "status", ("User " + user.getEmail() + " " + (request.operation().equals("LOCK") ? "locked" : "unlocked") + "!")
        ));

    }

    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userRepository.findAll()
                        .stream()
                        .map(UserResponse::fromUserToResponse)
                        .collect(Collectors.toList()));
    }

    public ResponseEntity<?> deleteUser(UserDetails authenticatedUser, String email) {
        User user = userRepository.findUserByEmail(email).orElse(null);
        if (user == null)
            throw new UsernameNotFoundException("User not found!");
        if (authenticatedUser.getUsername().equals(user.getEmail()))
            throw new CannotRemoveItselfException("Can't remove ADMINISTRATOR role!");

        userRepository.delete(user);
        applicationEventPublisher.publishEvent(
                new DeleteUserEvent(this, authenticatedUser.getUsername(), email)
        );
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of(
                        "user", email,
                        "status", "Deleted successfully!"
                ));
    }

    public ResponseEntity<?> updateRoleOfUser(UserDetails userDetails, ChangeUserRoleRequest changeUserRoleRequest) {
        User user = findUserByEmail(changeUserRoleRequest.user());
        Role role;
        try {
            role = Role.valueOf(changeUserRoleRequest.role());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "timestamp", LocalDateTime.now(),
                    "status", 404,
                    "error", "Not Found",
                    "message", "Role not found!",
                    "path", "/api/admin/user/role" // Adjust dynamically if needed
            ));
        }
        switch (changeUserRoleRequest.operation()) {
            case "GRANT" -> {
                User changedUser = addRoleToUser(userDetails.getUsername(), user, role);
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(UserResponse.fromUserToResponse(changedUser));
            }
            case "REMOVE" -> {
                User changedUser = removeRoleFromUser(userDetails.getUsername(), user, role);
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(UserResponse.fromUserToResponse(changedUser));
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid operation!");
    }

    private User addRoleToUser(String authenticatedUserEmail, User user, Role role) {
        Set<Role> roles = user.getUserGroups().stream().map(Group::getRole).collect(Collectors.toSet());
        if (role == Role.ADMINISTRATOR) {
            for (String groupName : user.getUserGroups().stream().map(Group::getName).toList()) {
                if (groupName.equals("Business"))
                    throw new InvalidRoleGrantingException("The user cannot combine administrative and business roles!");
            }
        } else {
            if (roles.contains(role))
                throw new InvalidRoleGrantingException("The user already has this role!");
            if (roles.contains(Role.ADMINISTRATOR))
                throw new InvalidRoleGrantingException("The user cannot combine administrative and business roles!");
            user.getUserGroups().add(groupRepository.findByRole(role).orElse(null));
            userRepository.save(user);
            applicationEventPublisher.publishEvent(
                    new GrantRoleEvent(this, authenticatedUserEmail,"Grant role " + role + " to " + user.getEmail())
            );
        }
        return user;

    }

    private User removeRoleFromUser(String authenticatedUserEmail, User user, Role role) {
        Set<Role> roles = user.getUserGroups().stream().map(Group::getRole).collect(Collectors.toSet());
        if (roles.contains(Role.ADMINISTRATOR))
            throw new InvalidRoleGrantingException("Can't remove ADMINISTRATOR role!");
        if (!roles.contains(role))
            throw new InvalidRoleGrantingException("The user does not have a role!");
        if (roles.size() == 1)
            throw new InvalidRoleGrantingException("The user must have at least one role!");
        user.getUserGroups().remove(groupRepository.findByRole(role).orElse(null));
        userRepository.save(user);
        applicationEventPublisher.publishEvent(
                new RemoveRoleEvent(this, authenticatedUserEmail, "Remove role " + role + " from " + user.getEmail())
        );
        return user;
    }

    public ResponseEntity<?> getAllEvents() {
        List<AuditLog> events = logRepository.findAllByOrderByIdAsc();
        logger.info(events.toString());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(events.stream().map(AuditLogDTO::fromAuditLogToDTO).collect(Collectors.toList()));
    }

    public ResponseEntity<?> getAuthenticatedUser(String email) {
        logger.info("Getting authenticated user: {}", email);
        logger.info("Validating email...");
        String validatedEmail = validateEmail(email);
        Optional<User> user = userRepository.findUserByEmail(validatedEmail);
        if (user.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        logger.info("User found: {}", user.get());
        return ResponseEntity.status(HttpStatus.OK).body(UserResponse.fromUserToResponse(user.get()));
    }

    public void increaseFailedAttempts(User user) {
        int newFailAttempts = user.getAttemptedLoginCount() + 1;
        userRepository.updateFailedAttempts(newFailAttempts, user.getEmail());
    }

    public void resetFailedAttempts(String email) {
        userRepository.updateFailedAttempts(0, email);
    }

    public User findUserByEmail(String email) {
        return userRepository
                .findUserByEmail(email.toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("User not found!"));
    }

    public List<User> findAllUsersByEmails(List<String> emails) {
        List<User> users = userRepository.findByEmailIn(emails);
        if (users.size() == emails.size())
            throw new UsernameNotFoundException("User not found!");
        return users;
    }

    private String validateUserName(String userName) {
        if (userName == null || userName.isEmpty()) {
            throw new InvalidUserNameException("Name cannot be empty");
        }
        return userName;
    }

    private String validatePassword(String password) {
        logger.info("Validating password...");
        if (password == null || password.length() < 12) {
            throw new InvalidPasswordException("Password length must be 12 chars minimum!");
        }
        if (PasswordBreachHandler.isPasswordBreached(password))
            throw new PasswordBreachedException("The password is in the hacker's database!");
        return passwordEncoder.encode(password);
    }

    private boolean comparePasswords(String passwordOldHash, String passwordNew) {
        return passwordEncoder.matches(passwordNew, passwordOldHash);
    }

    private String validateEmail(String email) {
        if (email == null || email.isEmpty())
            throw new InvalidEmailFormatException("Email cannot be null or empty");
        if (!email.matches(EMAIL_REGEX))
            throw new InvalidEmailFormatException("Email has to end with @acme.com");
        return email.toLowerCase();
    }
}
