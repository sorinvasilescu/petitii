package ro.petitii.repository;

import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.stereotype.Repository;
import ro.petitii.model.User;

import java.util.List;

@Repository
public interface UserRepository extends DataTablesRepository<User, Long> {
    List<User> findByEmail(String email);
}
