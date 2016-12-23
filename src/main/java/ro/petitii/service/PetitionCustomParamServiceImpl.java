package ro.petitii.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.petitii.model.PetitionCustomParam;
import ro.petitii.model.PetitionCustomParamType;
import ro.petitii.repository.PetitionCustomParamRepository;

@Service
public class PetitionCustomParamServiceImpl implements PetitionCustomParamService {
    private PetitionCustomParamRepository repository;

    @Autowired
    public PetitionCustomParamServiceImpl(PetitionCustomParamRepository repository) {
        this.repository = repository;
    }

    @Override
    public PetitionCustomParam findByType(PetitionCustomParamType type) {
        return repository.findByParam(type.getDbName());
    }
}
