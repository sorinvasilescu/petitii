package ro.petitii.controller;

import java.util.HashMap;
import java.util.Map;

public abstract class ControllerBase {
    enum ToastType {
        success, info, warning, danger
    }

    Map<String, String> createToast(String message, ToastType type) {
        Map<String, String> toast = new HashMap<>();
        toast.put("message", message);
        toast.put("type", type.name());
        return toast;
    }
}