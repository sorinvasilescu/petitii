package ro.petitii.model;

import org.springframework.security.core.GrantedAuthority;

public class UserAuthority implements GrantedAuthority {

    private User user;

    public UserAuthority(User u) {
        this.user = u;
    }

    @Override
    public String getAuthority() {
        return user.getRole().toString();
    }
}
