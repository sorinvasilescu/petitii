package ro.petitii.repository;

import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.stereotype.Repository;
import ro.petitii.model.EmailTemplate;

import java.util.List;

@Repository
public interface EmailTemplateRepository extends DataTablesRepository<EmailTemplate, Long> {
    List<EmailTemplate> findByCategory(EmailTemplate.Category category);
}
