package ro.petitii.repository;

import org.springframework.data.repository.CrudRepository;
import ro.petitii.model.PetitionStatus;

public interface PetitionStatusRepository extends CrudRepository<PetitionStatus,Long> {
}