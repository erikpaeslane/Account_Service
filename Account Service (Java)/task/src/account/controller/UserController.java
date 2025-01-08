package account.controller;

import account.model.ChangePasswordRequest;
import account.model.ChangeUserRoleRequest;
import account.model.UnlockUserRequest;
import account.model.UserCredentials;
import account.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/api/auth/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody UserCredentials user) {
        System.out.println(user.toString());
        return userService.registerUser(user);
    }

    @PostMapping("/api/auth/changepass")
    public ResponseEntity<?> changePassword(@AuthenticationPrincipal UserDetails user,
                                            @RequestBody @Valid ChangePasswordRequest request) {
        return userService.changePassword(user, request.newPassword());
    }

    @PutMapping("/api/admin/user/role")
    public ResponseEntity<?> updateUserRole(@AuthenticationPrincipal UserDetails userDetails,
                                            @RequestBody ChangeUserRoleRequest request) {
        return userService.updateRoleOfUser(userDetails, request);
    }

    @GetMapping("/api/admin/user/")
    public ResponseEntity<?> getAllUsers() {
        return userService.getAllUsers();
    }

    @DeleteMapping("/api/admin/user/{user}")
    public ResponseEntity<?> deleteUser(@PathVariable("user") @Email String email,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        return userService.deleteUser(userDetails, email);
    }

    @PutMapping("/api/admin/user/access")
    public ResponseEntity<?> lockUser(@AuthenticationPrincipal UserDetails userDetails,
                                      @RequestBody @Valid UnlockUserRequest request) {
        return userService.updateUserLockState(userDetails, request);
    }

    @GetMapping("/api/security/events/")
    public ResponseEntity<?> getAllEvents() {
        return userService.getAllEvents();
    }

}
