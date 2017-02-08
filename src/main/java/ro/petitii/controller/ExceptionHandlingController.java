package ro.petitii.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ro.petitii.util.ToastMaster;
import ro.petitii.validation.ValidationException;

import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static ro.petitii.validation.ValidationUtil.convert;

@ControllerAdvice
public class ExceptionHandlingController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandlingController.class);

    @Autowired
    private Environment env;

    @ExceptionHandler(ValidationException.class)
    public ModelAndView handleFailedValidation(HttpServletRequest req, ValidationException ex, RedirectAttributes att) {
        logger.debug("Building validation failure result for request: " + req.getRequestURL());

        if (ex.getModelAndView() == null || !ex.getModelAndView().hasView()) {
            // it seems that the validation exception was thrown, but not by the validation api
            return handleError(req, ex);
        } else if (ex.getValidationStatus().isValid()) {
            logger.error("Something went wrong! Received a validation exception, but the data is valid", ex);
            return handleError(req, ex);
        } else {
            List<ToastMaster.Toast> toasts = convert(ex.getValidationStatus());
            ModelAndView view = ex.getModelAndView();
            if (isRedirect(view)) {
                att.addFlashAttribute(ToastMaster.TOASTS_FIELD, toasts);
            } else {
                view.addObject(ToastMaster.TOASTS_FIELD, toasts);
            }
            return view;
        }
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleError(HttpServletRequest req, Exception ex) {
        logger.error("Request: " + req.getRequestURL() + " raised " + ex);

        ModelAndView view = new ModelAndView("error");
        view.addObject("url", req.getRequestURL());
        view.addObject("exception", ex);
        view.addObject("isLogged", isLoggedIn());
        if (isLoggedIn() && isDevelopment()) {
            view.addObject("stack", stackTrace(ex));
        }
        return view;
    }

    private String stackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    private boolean isDevelopment() {
        for (String profile : env.getActiveProfiles()) {
            if (Objects.equals(profile, "development")) {
                return true;
            }
        }
        return false;
    }

    private boolean isLoggedIn() {
        return SecurityContextHolder.getContext().getAuthentication() != null &&
                SecurityContextHolder.getContext().getAuthentication().isAuthenticated()
                && !(SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken);
    }

    private boolean isRedirect(ModelAndView view) {
        return view.getViewName().startsWith("redirect:/");
    }
}
