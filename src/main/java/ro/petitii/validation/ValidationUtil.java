package ro.petitii.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ro.petitii.util.ToastMaster;

import java.util.*;

import static java.util.Collections.singletonList;
import static ro.petitii.util.ToastMaster.*;

public class ValidationUtil {
    private static final ValidationStatus emptyValidStatus = new ValidationStatus();

    private static final Logger vlogger = LoggerFactory.getLogger(ValidationUtil.class);

    public interface GenericMethod {
        void doSomething();
    }

    public static void check(ValidationStatus status, Logger logger, ModelAndView view) {
        status.logMessages(logger).failIfInvalid(view);
    }

    public static void check(BindingResult result, String message, Logger logger, ModelAndView view) {
        ValidationStatus status = assertFalse(result.hasErrors(), message);
        if (!status.isValid()) {
            String serializedErrors = Arrays.toString(result.getAllErrors().toArray());
            status.logMessages(logger, "Serialization errors: " + serializedErrors);
            status.failIfInvalid(view);
        }
    }

    public static void check(List<ToastMaster.Toast> toasts, Logger logger, ModelAndView view) {
        if (anyError(toasts)) {
            ValidationStatus status = new ValidationStatus();
            toasts.stream().filter(t -> t.getType().isError())
                  .forEach(t -> status.addMessage(t.getMessage()));
            status.logMessages(logger).failIfInvalid(view);
        }
    }

    public static ValidationStatus assertNotNull(Object object, String message) {
        return assertFalse(Objects.isNull(object), message);
    }

    public static ValidationStatus assertNull(Object object, String message) {
        return assertFalse(Objects.nonNull(object), message);
    }

    public static ValidationStatus assertNotEquals(Object object, Object value, String message) {
        return assertFalse(Objects.equals(object, value), message);
    }

    public static ValidationStatus assertEquals(Object object, Object value, String message) {
        return assertTrue(Objects.equals(object, value), message);
    }

    public static ValidationStatus assertFalse(boolean flag, String message) {
        if (flag) {
            return new ValidationStatus(message);
        } else {
            return emptyValidStatus;
        }
    }

    public static ValidationStatus assertTrue(boolean flag, String message) {
        return assertFalse(!flag, message);
    }

    public static ValidationStatus fail(String message) {
        return new ValidationStatus(message);
    }

    public static ValidationStatus failOnException(GenericMethod method, String message) {
        try {
            method.doSomething();
            return emptyValidStatus;
        } catch (Exception e) {
            vlogger.error("Method execution failed with exception: " + e.getMessage(), e);
            return new ValidationStatus(message);
        }
    }

    public static List<ToastMaster.Toast> convert(ValidationStatus status) {
        List<ToastMaster.Toast> result = new LinkedList<>();
        for (ValidationMessage msg : status.getMessages()) {
            result.add(createToast(msg.getMessage(), msg.getLevel().getToastType()));
        }

        return result;
    }

    public static ModelAndView success(String message, ModelAndView view) {
        return success(singletonList(createToast(message, ToastMaster.ToastType.success)), view);
    }

    public static ModelAndView success(List<Toast> toasts, ModelAndView view) {
        view.addObject(TOASTS_FIELD, toasts);
        return view;
    }

    public static ModelAndView successAndRedirect(String message, String view, RedirectAttributes attributes) {
        return successAndRedirect(singletonList(createToast(message, ToastMaster.ToastType.success)), view, attributes);
    }

    public static ModelAndView successAndRedirect(List<Toast> toasts, String view, RedirectAttributes attributes) {
        attributes.addFlashAttribute(TOASTS_FIELD, toasts);
        return redirect(view);
    }

    public static ModelAndView redirect(String view) {
        return new ModelAndView("redirect:/" + view);
    }
}
