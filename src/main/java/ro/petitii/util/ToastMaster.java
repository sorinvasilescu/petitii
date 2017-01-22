package ro.petitii.util;

import java.util.HashMap;
import java.util.Map;

public class ToastMaster {
    public static final String TOASTS_FIELD = "toasts";

    public enum ToastType {
        success, info, warning, danger
    }

    public static Map<String, String> createToast(String message, ToastType type) {
        Map<String, String> toast = new HashMap<>();
        toast.put("message", message);
        toast.put("type", type.name());
        return toast;
    }
}
