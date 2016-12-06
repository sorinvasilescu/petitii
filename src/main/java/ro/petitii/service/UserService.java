package ro.petitii.service;

import ro.petitii.model.User;
import java.util.List;

public interface UserService {
    List<User> findUserByEmail(String email);
}
