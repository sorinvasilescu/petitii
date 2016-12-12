package ro.petitii.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.stereotype.Service;
import ro.petitii.model.User;
import ro.petitii.repository.UserRepository;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public DataTablesOutput<User> findAll(DataTablesInput input) {
        return userRepository.findAll(input);
    }
}
