package ro.petitii.controller.api;

import org.springframework.web.bind.annotation.RestController;
import ro.petitii.controller.BaseController;

import java.util.Objects;

@RestController
class ApiController extends BaseController {
    public class ApiResult {
        private boolean success;
        private String message;
        private String warning;

        private ApiResult(boolean success, String message, String warning) {
            this.success = success;
            this.message = message;
            this.warning = warning;
        }

        private ApiResult(boolean success, String message) {
            this(success, message, null);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public String getWarning() {
            return warning;
        }
    }

    public ApiResult success(String i18nKey) {
        return new ApiResult(true, i18n(i18nKey));
    }

    public ApiResult success() {
        return new ApiResult(true, "");
    }

    public ApiResult fail(String i18nKey, String extraMessage, String warningMessage) {
        if (extraMessage == null || Objects.equals(extraMessage, "")) {
            extraMessage = "";
        } else {
            extraMessage = ": " + extraMessage;
        }
        return new ApiResult(false, i18n(i18nKey) + extraMessage, warningMessage);
    }

    public ApiResult fail(String i18nKey, String extraMessage) {
        return fail(i18nKey, extraMessage, null);
    }

    public ApiResult fail(String i18nKey) {
        return new ApiResult(false, i18n(i18nKey));
    }
}
