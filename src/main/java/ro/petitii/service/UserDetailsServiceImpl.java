package ro.petitii.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ro.petitii.model.User;
import ro.petitii.model.UserDetail;

import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    UserService userService;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        List<User> userList = userService.findUserByEmail(s);
        if (userList.isEmpty() || userList.size()>1) throw new UsernameNotFoundException("Username "+s+" was not found");
        return new UserDetail(userList.get(0));
    }
}
