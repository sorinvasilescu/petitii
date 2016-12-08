package ro.petitii.service;

import org.springframework.data.domain.Sort;
import ro.petitii.model.Email;
import ro.petitii.model.rest.RestEmailResponse;

import java.util.List;

public interface EmailService {
    Email save(Email e);
    long count();
    long lastUid();
    List<Email> findAll(int startIndex, int size, Sort.Direction sortDirection, String sortcolumn);
    RestEmailResponse getTableContent(int startIndex, int size, Sort.Direction sortDirection, String sortColumn);
}
