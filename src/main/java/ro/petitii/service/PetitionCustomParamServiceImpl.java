package ro.petitii.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import ro.petitii.model.Petition;
import ro.petitii.model.PetitionCustomParam;
import ro.petitii.repository.PetitionCustomParamRepository;

import static org.springframework.validation.ValidationUtils.rejectIfEmptyOrWhitespace;

@Service
public class PetitionCustomParamServiceImpl implements PetitionCustomParamService {
    private PetitionCustomParamRepository repository;

    @Autowired
    public PetitionCustomParamServiceImpl(PetitionCustomParamRepository repository) {
        this.repository = repository;
    }

    @Override
    public void initDefaults(Petition petition) {
        for (PetitionCustomParam.Type type : PetitionCustomParam.Type.values()) {
            PetitionCustomParam param = this.findByType(type);
            if (param.hasDefault()) {
                switch (type) {
                    case domain:
                        petition.setField(param.getDefaultValue());
                        break;
                    case entity:
                        petition.getPetitioner().setEntity_type(param.getDefaultValue());
                        break;
                    case information:
                        petition.setRelation(param.getDefaultValue());
                        break;
                    case problem:
                        petition.setProblemType(param.getDefaultValue());
                        break;
                    case title:
                        petition.getPetitioner().setTitle(param.getDefaultValue());
                        break;
                }
            }
        }
    }

    @Override
    public void validate(Petition petition, Errors errors) {
        for (PetitionCustomParam.Type type : PetitionCustomParam.Type.values()) {
            PetitionCustomParam param = this.findByType(type);
            if (param.isRequired()) {
                switch (type) {
                    case domain:
                        rejectIfEmptyOrWhitespace(errors, "field", null);
                        break;
                    case entity:
                        rejectIfEmptyOrWhitespace(errors, "petitioner.entity_type", null);
                        break;
                    case information:
                        rejectIfEmptyOrWhitespace(errors, "relation", null);
                        break;
                    case problem:
                        rejectIfEmptyOrWhitespace(errors, "problemType", null);
                        break;
                    case title:
                        rejectIfEmptyOrWhitespace(errors, "petitioner.title", null);
                        break;
                }
            }
        }
    }

    @Override
    public PetitionCustomParam findByType(PetitionCustomParam.Type type) {
        return repository.findByParam(type);
    }
}
