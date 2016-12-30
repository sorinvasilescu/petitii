package ro.petitii.repository;

import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.stereotype.Repository;
import ro.petitii.model.EmailTemplate;

/**
 * Created by mpostelnicu on 12/28/2016.
 */
@Repository
public interface EmailTemplateRepository extends DataTablesRepository<EmailTemplate, Long> {


}
