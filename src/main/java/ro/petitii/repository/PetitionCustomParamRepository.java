package ro.petitii.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ro.petitii.model.PetitionCustomParam;

@Repository
public interface PetitionCustomParamRepository extends CrudRepository<PetitionCustomParam, Long> {
    PetitionCustomParam findByParam(PetitionCustomParam.Type param);
}
