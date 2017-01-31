package ro.petitii.model.datatables;

import org.springframework.core.convert.converter.Converter;
import ro.petitii.model.Email;

import java.text.SimpleDateFormat;

public class EmailConverter implements Converter<Email,EmailResponse> {

    private static final SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm");

    @Override
    public EmailResponse convert(Email email) {
        EmailResponse result = new EmailResponse();
        result.setId(email.getId());
        result.setSender(email.getSender());
        result.setRecipients(email.getRecipients());
        result.setSubject(email.getSubject());
        result.setDate(df.format(email.getDate()));
        if (email.getPetition() != null) result.setPetition_id(email.getPetition().getId());
        return result;
    }
}
