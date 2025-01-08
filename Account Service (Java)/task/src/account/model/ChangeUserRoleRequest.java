package account.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

public record ChangeUserRoleRequest(
        @Email String user,
        String role,
        @Pattern(regexp = "GRANT|REMOVE", message = "Action must be 'GRANT' or 'REMOVE'") String operation) {
}
