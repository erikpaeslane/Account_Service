package account.entity;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

public class UserAdapter implements UserDetails {

    private final User user;

    public UserAdapter(User user) {
        System.out.println("Authentication step:");
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        System.out.println(user.getUserGroups().stream().map(Group::getRole).collect(Collectors.toList()));
        return user.getUserGroups()
                .stream()
                .map(group -> new SimpleGrantedAuthority("ROLE_" + group.getRole()))
                .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        System.out.println("User: " + user.getEmail());
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        System.out.println("User login attempt: " + user.getAttemptedLoginCount());
        System.out.println("Locked: " + user.isLocked());
        return !user.isLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}


