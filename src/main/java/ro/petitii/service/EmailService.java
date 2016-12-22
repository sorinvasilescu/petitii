package ro.petitii.service;

import org.springframework.data.domain.Sort;
import ro.petitii.model.Email;
import ro.petitii.model.dt.DTEmailResponse;

import java.util.List;

public interface EmailService {
    Email save(Email e);
    Email saveAlone(Email e);
    long count();
    long count(Email.EmailType type);
    long lastUid();
    Email searchById(long emailId);
    List<Email> findAll(int startIndex, int size, Sort.Direction sortDirection, String sortcolumn);
    List<Email> findAllByType(Email.EmailType type, int startIndex, int size, Sort.Direction sortDirection, String sortcolumn);
    DTEmailResponse getTableContent(Email.EmailType type, int startIndex, int size, Sort.Direction sortDirection, String sortColumn);
}
