package account.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCredentials(@NotBlank String name,
                              @NotBlank String lastname,
                              @Email String email,
                              @Size (min = 12, message = "Password must be at least 12 characters long")
                              String password) {
}
