package ro.petitii.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import ro.petitii.model.EmailTemplate;
import ro.petitii.model.PetitionStatus;

import java.util.Locale;

@Component
public class TranslationUtil {
    private static final Locale ro = new Locale("ro");

    private static MessageSource messageSource;

    @Autowired
    public void setMessageSource(MessageSource messageSource) {
        TranslationUtil.messageSource = messageSource;
    }

    public static String i18n(String templateId) {
        return messageSource.getMessage(templateId, null, ro);
    }

    public static String categoryMsg(EmailTemplate.Category category) {
        return i18n("emailTemplate.category." + category.name().toLowerCase());
    }

    public static String resolutionMsg(PetitionStatus.Resolution resolution) {
        return i18n("petition.resolution." + resolution.name().toLowerCase());
    }
}
