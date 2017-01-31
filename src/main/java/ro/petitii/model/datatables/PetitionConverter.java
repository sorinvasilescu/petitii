package ro.petitii.model.datatables;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ro.petitii.config.DeadlineConfig;
import ro.petitii.model.Petition;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static ro.petitii.util.DateUtil.alertStatus;
import static ro.petitii.util.StringUtil.prepareForView;

@Component
public class PetitionConverter implements Converter<Petition,PetitionResponse> {

    private static final DateFormat df = new SimpleDateFormat("dd.MM.yyyy");

    @Autowired
    private DeadlineConfig deadlineConfig;

    @Autowired
    private MessageSource messageSource;

    @Override
    public PetitionResponse convert(Petition petition) {
        PetitionResponse element = new PetitionResponse();
        element.setId(petition.getId());
        element.setSubject(prepareForView(petition.getSubject(), 100));
        element.setPetitionerEmail(petition.getPetitioner().getEmail());
        element.setPetitionerName(prepareForView(petition.getPetitioner().getFullName(), 30));
        element.setUser(petition.getResponsible().getFullName());
        element.setReceivedDate(df.format(petition.getReceivedDate()));
        element.setLastUpdateDate(df.format(petition.getLastUpdateDate()));
        element.setRegNo(petition.getRegNo().getNumber());
        element.setStatus(messageSource.getMessage(petition.statusString(), null, new Locale("ro")));
        DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
        element.setDeadline(df.format(petition.getDeadline()));
        element.setAlertStatus(alertStatus(new Date(), petition.getDeadline(), deadlineConfig));
        return element;
    }
}
