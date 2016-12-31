package ro.petitii.util;

public class ValidationStatus {
    private boolean valid;
    private String msg;

    public ValidationStatus(boolean valid, String msg) {
        this.valid = valid;
        this.msg = msg;
    }

    public boolean isValid() {
        return valid;
    }

    public String getMsg() {
        return msg;
    }
}
