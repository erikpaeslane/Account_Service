package account.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @JsonProperty("new_password")
        @Size(min = 12, message = "Password length must be 12 chars minimum!")
        String newPassword
) {
}
