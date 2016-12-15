package ro.petitii.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ro.petitii.model.Petition;

@Repository
public interface PetitionRepository extends CrudRepository<Petition,Long> {
}
