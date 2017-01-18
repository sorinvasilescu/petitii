package ro.petitii.controller;

import org.springframework.beans.factory.annotation.Autowired;
import ro.petitii.model.PetitionStatus;
import ro.petitii.util.TranslationUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public abstract class ControllerBase {
    enum ToastType {
        success, info, warning, danger
    }

    @Autowired
    private TranslationUtil i18n;

    String i18n(String key) {
        return i18n.i18n(key);
    }

    String i18n(PetitionStatus.Resolution key) {
        return i18n.i18n(key);
    }

    Map<String, String> i18nToast(String key, ToastType type, String... args) {
        return createToast(i18n.i18n(key, args), type);
    }

    Map<String, String> i18nToast(String key, String[] args, ToastType type) {
        return createToast(i18n.i18n(key, args), type);
    }

    Map<String, String> i18nToast(String key, HttpServletRequest request, ToastType type) {
        return createToast(i18n.i18n(key, request), type);
    }

    Map<String, String> i18nToast(String key, ToastType type) {
        return createToast(i18n.i18n(key), type);
    }

    private Map<String, String> createToast(String message, ToastType type) {
        Map<String, String> toast = new HashMap<>();
        toast.put("message", message);
        toast.put("type", type.name());
        return toast;
    }
}