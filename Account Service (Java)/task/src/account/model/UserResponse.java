package account.model;

import account.entity.User;

import java.util.ArrayList;
import java.util.List;

public record UserResponse (long id, String name, String lastname, String email, List<String> roles) {

    public static UserResponse fromUserToResponse(User user) {
        List<String> roles = new ArrayList<>(
                user.getUserGroups())
                .stream()
                .map((group) -> "ROLE_" + group.getRole()).sorted().toList();
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getLastname(),
                user.getEmail(),
                roles
        );
    }
}
