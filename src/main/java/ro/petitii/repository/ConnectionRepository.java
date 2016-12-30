package ro.petitii.repository;

import org.springframework.data.repository.CrudRepository;
import ro.petitii.model.Connection;
import ro.petitii.model.Petition;

public interface ConnectionRepository extends CrudRepository<Connection, Long>{
    Connection findByOldPetitionAndNewPetition(Petition oldPetition, Petition newPetition);
}
