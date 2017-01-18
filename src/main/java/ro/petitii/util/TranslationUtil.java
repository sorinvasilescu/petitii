package ro.petitii.util;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;

import ro.petitii.model.EmailTemplate;
import ro.petitii.model.PetitionStatus;

@Component
public class TranslationUtil {
    public static final Locale ro = new Locale("ro");

    private static MessageSource messageSource;
    
    private static LocaleResolver localeResolver;
	
    @Autowired
    public void setMessageSource(MessageSource messageSource) {
        TranslationUtil.messageSource = messageSource;
    }

    @Autowired
    public void setLocaleResolver(LocaleResolver localeResolver) {
        TranslationUtil.localeResolver = localeResolver;
    }
    
    public String i18n(String templateId, HttpServletRequest request) {
        return messageSource.getMessage(templateId, null, localeResolver.resolveLocale(request));
    }

    public String i18n(String key, String args[], HttpServletRequest request) {
    	 return messageSource.getMessage(key, null, localeResolver.resolveLocale(request));
    }
    
    public String i18n(String key, String args[]) {
   	 return messageSource.getMessage(key, args, ro);
    }
    
    public String i18n(String key) {
      	 return messageSource.getMessage(key, (String[])null, ro);
    }
    
    public static String categoryMsg(EmailTemplate.Category category) {
    	return messageSource.getMessage("emailTemplate.category." + category.name().toLowerCase(), null, ro);
    }

    public static String resolutionMsg(PetitionStatus.Resolution resolution) {
    	return messageSource.getMessage("petition.resolution." + resolution.name().toLowerCase(), null, ro);
    }
}
