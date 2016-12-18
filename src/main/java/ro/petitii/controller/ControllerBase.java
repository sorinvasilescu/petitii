package ro.petitii.controller;

import java.util.HashMap;
import java.util.Map;

public abstract class ControllerBase {
    enum ToastType {
        success, info, warning, danger
    }

    public Map<String, String> createToast(String message, ToastType type) {
        Map<String, String> toast = new HashMap<>();
        toast.put("message", message);
        toast.put("type", "alert-" + type.name());
        return toast;
    }
}