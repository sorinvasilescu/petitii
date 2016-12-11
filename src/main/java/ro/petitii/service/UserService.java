package ro.petitii.service;

import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import ro.petitii.model.User;

import java.util.List;

public interface UserService {
    List<User> findUserByEmail(String email);

    DataTablesOutput<User> findAll(DataTablesInput input);
}
