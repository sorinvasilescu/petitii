package ro.petitii.util;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.List;

public class ToastMaster {
    public static final String TOASTS_FIELD = "toasts";

    public enum ToastType {
        success(false), info(false), warning(true), danger(true);
        private boolean error;

        ToastType(boolean error) {
            this.error = error;
        }

        public boolean isError() {
            return error;
        }
    }

    public static class Toast {
        private String message;

        @Enumerated(EnumType.STRING)
        private ToastType type;

        Toast(String message, ToastType type) {
            this.message = message;
            this.type = type;
        }

        public String getMessage() {
            return message;
        }

        public ToastType getType() {
            return type;
        }
    }

    public static Toast createToast(String message, ToastType type) {
        return new Toast(message, type);
    }

    public static boolean anyError(List<Toast> toasts) {
        if (toasts == null || toasts.isEmpty()) {
            return false;
        } else {
            for (Toast toast : toasts) {
                if (toast.getType().isError()) {
                    return true;
                }
            }
            return false;
        }
    }
}
