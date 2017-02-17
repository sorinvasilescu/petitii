package ro.petitii.validation;

import org.springframework.web.servlet.ModelAndView;

public class ValidationException extends RuntimeException {
    private ValidationStatus validationStatus;
    private ModelAndView modelAndView;

    public ValidationException(ValidationStatus validationStatus, ModelAndView modelAndView) {
        this.validationStatus = validationStatus;
        this.modelAndView = modelAndView;
    }

    public ValidationStatus getValidationStatus() {
        return validationStatus;
    }

    public ModelAndView getModelAndView() {
        return modelAndView;
    }
}
