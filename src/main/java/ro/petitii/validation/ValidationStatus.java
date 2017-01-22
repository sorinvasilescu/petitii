package ro.petitii.validation;

import java.util.LinkedList;
import java.util.List;

public class ValidationStatus {
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

    //todo; remove this
    public String getMsg() {
        return messageList.get(0).getMessage();
    }
}
