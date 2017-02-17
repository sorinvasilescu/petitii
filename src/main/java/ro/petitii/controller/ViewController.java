package ro.petitii.controller;

import ro.petitii.model.PetitionStatus;
import ro.petitii.util.ToastMaster;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static ro.petitii.util.ToastMaster.createToast;

public abstract class ViewController extends BaseController {
    protected String i18n(PetitionStatus.Resolution resolution) {
        return i18n("petition.resolution." + resolution.name().toLowerCase());
    }

    ToastMaster.Toast i18nToast(String key, ToastMaster.ToastType type, String... args) {
        return createToast(i18n.i18n(key, args), type);
    }

    ToastMaster.Toast i18nToast(String key, ToastMaster.ToastType type) {
        return createToast(i18n.i18n(key), type);
    }
}