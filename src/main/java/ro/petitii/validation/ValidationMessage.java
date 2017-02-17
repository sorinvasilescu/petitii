package ro.petitii.validation;

public class ValidationMessage {
    private ValidationLevel level;
    private String message;

    public ValidationMessage(String message) {
        this(ValidationLevel.error, message);
    }

    public ValidationMessage(ValidationLevel level, String message) {
        this.level = level;
        this.message = message;
    }

    public ValidationLevel getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "[" + level + "] " + message;
    }
}
