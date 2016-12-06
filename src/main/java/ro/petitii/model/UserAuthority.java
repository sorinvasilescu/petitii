package ro.petitii.model;

import org.springframework.security.core.GrantedAuthority;

/**
 * Created by Sorin on 12/6/2016.
 */
public class UserAuthority implements GrantedAuthority {

    private User user;

    public UserAuthority(User u) {
        this.user = u;
    }

    @Override
    public String getAuthority() {
        return user.getRole();
    }
}
