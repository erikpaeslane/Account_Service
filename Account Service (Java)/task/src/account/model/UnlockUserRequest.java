package account.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

public record UnlockUserRequest(@Email String user,
                                @Pattern(regexp = "(LOCK)|(UNLOCK)") String operation){
}
