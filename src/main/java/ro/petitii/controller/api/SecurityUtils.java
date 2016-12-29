package ro.petitii.controller.api;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import ro.petitii.model.User;

public class SecurityUtils {
    public static boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        for (GrantedAuthority authority : auth.getAuthorities()) {
            if (User.UserRole.ADMIN.name().equalsIgnoreCase(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }
}
