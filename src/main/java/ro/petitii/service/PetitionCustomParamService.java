package ro.petitii.service;

import ro.petitii.model.PetitionCustomParam;
import ro.petitii.model.PetitionCustomParamType;

public interface PetitionCustomParamService {
    PetitionCustomParam findByType(PetitionCustomParamType type);
}
