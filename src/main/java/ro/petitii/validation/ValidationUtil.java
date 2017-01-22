package ro.petitii.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import ro.petitii.util.ToastMaster;

import java.util.*;

import static ro.petitii.util.ToastMaster.TOASTS_FIELD;
import static ro.petitii.util.ToastMaster.createToast;

public class ValidationUtil {
    private static final Logger vlogger = LoggerFactory.getLogger(ValidationUtil.class);

    public interface GenericMethod {
        void doSomething();
    }

    private final Logger classLogger;

    public ValidationUtil(Logger classLogger) {
        this.classLogger = classLogger;
    }

    public void failIfNull(Object object, String message, ModelAndView view) {
        if (Objects.isNull(object)) {
            throw new ValidationException(new ValidationStatus(message), view);
        }
    }

    public void failIfNull(Object object, String message, ModelAndView view, String logMessage) {
        if (Objects.isNull(object)) {

            throw new ValidationException(new ValidationStatus(message), view);
        }
    }

    public void failIfNotNull(Object object, String message, ModelAndView view) {
        if (!Objects.isNull(object)) {
            throw new ValidationException(new ValidationStatus(message), view);
        }
    }

    public void failIfEquals(Object object, Object value, String message, ModelAndView view) {
        if (Objects.equals(object, value)) {
            throw new ValidationException(new ValidationStatus(message), view);
        }
    }

    public void failIfNotEquals(Object object, Object value, String message, ModelAndView view) {
        if (!Objects.equals(object, value)) {
            throw new ValidationException(new ValidationStatus(message), view);
        }
    }

    public void failIfTrue(boolean flag, String message, ModelAndView view) {
        if (flag) {
            throw new ValidationException(new ValidationStatus(message), view);
        }
    }

    public void failIfFalse(boolean flag, String message, ModelAndView view) {
        if (!flag) {
            throw new ValidationException(new ValidationStatus(message), view);
        }
    }

    public void fail(String message, ModelAndView view) {
        fail(message, view, null,  null);
    }

    public void fail(String message, ModelAndView view, String logMessage, Exception e) {
        if (logMessage != null) {
            classLogger.error(logMessage, e);
        }
        throw new ValidationException(new ValidationStatus(message), view);
    }

    public void fail(String message, ModelAndView view, String logMessage) {
        fail(message, view, logMessage, null);
    }

    public void failOnException(GenericMethod method, String message, ModelAndView view) {
        try {
            method.doSomething();
        } catch (Exception e) {
            vlogger.error("Method execution failed with exception: " + e.getMessage(), e);
            throw new ValidationException(new ValidationStatus(message), view);
        }
    }

    public ModelAndView success(String message, ModelAndView view) {
        view.addObject(TOASTS_FIELD, Collections.singletonList(createToast(message, ToastMaster.ToastType.success)));
        return view;
    }

    public static List<Map<String, String>> convert(ValidationStatus status) {
        List<Map<String, String>> result = new LinkedList<>();
        for (ValidationMessage msg : status.getMessages()) {
            result.add(createToast(msg.getMessage(), msg.getLevel().getToastType()));
        }

        return result;
    }

    public static ModelAndView redirect(String view) {
        return new ModelAndView("redirect:/" + view);
    }
}
