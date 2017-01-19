package ro.petitii.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ro.petitii.model.Petitioner;

import java.util.List;

@Repository
public interface PetitionerRepository extends CrudRepository<Petitioner,Long> {
    List<Petitioner> findByEmail(String email);
}
