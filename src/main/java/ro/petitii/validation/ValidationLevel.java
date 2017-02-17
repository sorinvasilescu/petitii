package ro.petitii.validation;

import ro.petitii.util.ToastMaster;

public enum ValidationLevel {
    warning(ToastMaster.ToastType.warning),
    error(ToastMaster.ToastType.danger);

    private ToastMaster.ToastType toastType;

    ValidationLevel(ToastMaster.ToastType toastType) {
        this.toastType = toastType;
    }

    public ToastMaster.ToastType getToastType() {
        return toastType;
    }
}
