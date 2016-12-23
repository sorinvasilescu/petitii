package ro.petitii.service;

import org.springframework.validation.Errors;
import ro.petitii.model.Petition;
import ro.petitii.model.PetitionCustomParam;

public interface PetitionCustomParamService {
    void initDefaults(Petition petition);

    void validate(Petition petition, Errors errors);

    PetitionCustomParam findByType(PetitionCustomParam.Type type);
}
