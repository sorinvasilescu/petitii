package ro.petitii.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;

import java.util.LinkedList;
import java.util.List;

public class ValidationStatus {
    private static final Logger classLogger = LoggerFactory.getLogger(ValidationStatus.class);

    private boolean loggedMessages = false;
    private List<ValidationMessage> messageList;

    public ValidationStatus() {
    }

    public ValidationStatus(ValidationLevel level, String msg) {
        this(new ValidationMessage(level, msg));
    }

    public ValidationStatus(String msg) {
        this(new ValidationMessage(msg));
    }

    public ValidationStatus(ValidationMessage msg) {
        this.messageList = new LinkedList<>();
        this.messageList.add(msg);
    }

    public boolean isValid() {
        return messageList == null || messageList.isEmpty();
    }

    public void addMessage(ValidationLevel level, String msg) {
        addMessage(new ValidationMessage(level, msg));
    }

    public void addMessage(String msg) {
        addMessage(new ValidationMessage(msg));
    }

    public void addMessage(ValidationMessage msg) {
        if (messageList == null) {
            messageList = new LinkedList<>();
        }
        messageList.add(msg);
    }

    public List<ValidationMessage> getMessages() {
        return messageList;
    }

    public ValidationStatus logMessages(Logger logger, String successMessage, String errorMessage, Exception e) {
        if (isValid()) {
            if (successMessage != null) {
                logger.info(successMessage);
            }
        } else {
            logMessageList(logger);
            if (errorMessage != null) {
                if (e != null) {
                    logger.error(errorMessage, e);
                } else {
                    logger.error(errorMessage);
                }
            }
        }
        loggedMessages = true;
        return this;
    }

    public ValidationStatus logMessages(Logger logger, String successMessage, String errorMessage) {
        return logMessages(logger, successMessage, errorMessage, null);
    }

    public ValidationStatus logMessages(Logger logger, String errorMessage) {
        return logMessages(logger, null, errorMessage, null);
    }

    public ValidationStatus logMessages(Logger logger, String errorMessage, Exception e) {
        return logMessages(logger, null, errorMessage, e);
    }

    public ValidationStatus logMessages(Logger logger) {
        return logMessages(logger, null, null, null);
    }

    public void failIfInvalid(ModelAndView view) {
        if (!isValid()) {
            if (!loggedMessages) {
                logMessageList(classLogger);
            }
            throw new ValidationException(this, view);
        }
    }

    private void logMessageList(Logger logger) {
        for (ValidationMessage message : messageList) {
            logMessage(logger, message);
        }
    }

    private void logMessage(Logger logger, ValidationMessage message) {
        if (message.getLevel() == ValidationLevel.error) {
            logger.error(message.getMessage());
        } else if (message.getLevel() == ValidationLevel.warning) {
            logger.warn(message.getMessage());
        }
    }

    //todo; remove this
    public String getMsg() {
        return messageList.get(0).getMessage();
    }
}
